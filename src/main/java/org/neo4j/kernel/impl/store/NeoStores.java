//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.util.Iterator;
import java.util.function.Predicate;

import cn.DynamicGraph.kernel.impl.store.DbVersionStore;

import cn.DynamicGraph.kernel.impl.store.format.standard.DbVersionRecordFormat;
import cn.DynamicGraph.kernel.impl.store.record.DbVersionRecord;
//import cn.DynamicGraph.store.versionStore.DynamicVersionArrayStore;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.ArrayUtil;
import org.neo4j.helpers.Exceptions;
import org.neo4j.helpers.collection.FilteringIterator;
import org.neo4j.helpers.collection.IteratorWrapper;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.internal.diagnostics.DiagnosticsManager;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.IOLimiter;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.io.pagecache.tracing.cursor.context.VersionContextSupplier;
import org.neo4j.kernel.NeoStoresDiagnostics;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.api.CountsAccessor.Updater;
import org.neo4j.kernel.impl.store.MetaDataStore.Position;
import org.neo4j.kernel.impl.store.counts.CountsTracker;
import org.neo4j.kernel.impl.store.counts.ReadOnlyCountsTracker;
import org.neo4j.kernel.impl.store.format.*;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.impl.store.kvstore.DataInitializer;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.Logger;

public class NeoStores implements AutoCloseable {
    private static final String STORE_ALREADY_CLOSED_MESSAGE = "Specified store was already closed.";
    private static final String STORE_NOT_INITIALIZED_TEMPLATE = "Specified store was not initialized. Please specify %s as one of the stores types that should be open to be able to use it.";
    private static final StoreType[] STORE_TYPES = StoreType.values();
    private final Predicate<StoreType> INSTANTIATED_RECORD_STORES = new Predicate<StoreType>() {
        public boolean test(StoreType type) {
            return type.isRecordStore() && NeoStores.this.stores[type.ordinal()] != null;
        }
    };
    private final DatabaseLayout layout;
    private final Config config;
    private final IdGeneratorFactory idGeneratorFactory;
    private final PageCache pageCache;
    private final LogProvider logProvider;
    private final VersionContextSupplier versionContextSupplier;
    private final boolean createIfNotExist;
    private final File metadataStore;
    private final StoreType[] initializedStores;
    private final FileSystemAbstraction fileSystemAbstraction;
    private final RecordFormats recordFormats;
    private final Object[] stores;
    private final OpenOption[] openOptions;

    public static boolean isStorePresent(PageCache pageCache, DatabaseLayout databaseLayout) {
        File metaDataStore = databaseLayout.metadataStore();

        try {
            PagedFile ignore = pageCache.map(metaDataStore, MetaDataStore.getPageSize(pageCache), new OpenOption[0]);
            Throwable var4 = null;

            boolean var5;
            try {
                var5 = true;
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (ignore != null) {
                    if (var4 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var5;
        } catch (IOException var17) {
            return false;
        }
    }

    public NeoStores(DatabaseLayout layout, Config config, IdGeneratorFactory idGeneratorFactory, PageCache pageCache, LogProvider logProvider, FileSystemAbstraction fileSystemAbstraction, VersionContextSupplier versionContextSupplier, RecordFormats recordFormats, boolean createIfNotExist, StoreType[] storeTypes, OpenOption[] openOptions) {
        this.layout = layout;
        this.metadataStore = layout.metadataStore();
        this.config = config;
        this.idGeneratorFactory = idGeneratorFactory;
        this.pageCache = pageCache;
        this.logProvider = logProvider;
        this.fileSystemAbstraction = fileSystemAbstraction;
        this.versionContextSupplier = versionContextSupplier;
        this.recordFormats = recordFormats;
        this.createIfNotExist = createIfNotExist;
        this.openOptions = openOptions;
        this.verifyRecordFormat();
        this.stores = new Object[StoreType.values().length];

        try {
            StoreType[] var12 = storeTypes;
            int var13 = storeTypes.length;

            for(int var14 = 0; var14 < var13; ++var14) {
                StoreType type = var12[var14];
                this.getOrCreateStore(type);
            }
        } catch (RuntimeException var17) {
            try {
                this.close();
            } catch (RuntimeException var16) {
                var17.addSuppressed(var16);
            }

            throw var17;
        }

        this.initializedStores = storeTypes;
    }

    public void close() {
        RuntimeException ex = null;
        StoreType[] var2 = STORE_TYPES;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            StoreType type = var2[var4];

            try {
                this.closeStore(type);
            } catch (RuntimeException var7) {
                ex = (RuntimeException)Exceptions.chain(ex, var7);
            }
        }

        if (ex != null) {
            throw ex;
        }
    }

    private void verifyRecordFormat() {
        try {
            String expectedStoreVersion = this.recordFormats.storeVersion();
            long record = MetaDataStore.getRecord(this.pageCache, this.metadataStore, Position.STORE_VERSION);
            if (record != -1L) {
                String actualStoreVersion = MetaDataStore.versionLongToString(record);
                RecordFormats actualStoreFormat = RecordFormatSelector.selectForVersion(actualStoreVersion);
                if (!this.isCompatibleFormats(actualStoreFormat)) {
                    throw new UnexpectedStoreVersionException(actualStoreVersion, expectedStoreVersion);
                }
            }
        } catch (NoSuchFileException var6) {
        } catch (IOException var7) {
            throw new UnderlyingStorageException(var7);
        }

    }

    private boolean isCompatibleFormats(RecordFormats storeFormat) {
        return FormatFamily.isSameFamily(this.recordFormats, storeFormat) && this.recordFormats.hasCompatibleCapabilities(storeFormat, CapabilityType.FORMAT) && this.recordFormats.generation() >= storeFormat.generation();
    }

    private void closeStore(StoreType type) {
        int i = type.ordinal();
        if (this.stores[i] != null) {
            try {
                type.close(this.stores[i]);
            } finally {
                this.stores[i] = null;
            }
        }

    }

    public void flush(IOLimiter limiter) {
        try {
            CountsTracker counts = (CountsTracker)this.stores[StoreType.COUNTS.ordinal()];
            if (counts != null) {
                counts.rotate(this.getMetaDataStore().getLastCommittedTransactionId());
            }

            this.pageCache.flushAndForce(limiter);
        } catch (IOException var3) {
            throw new UnderlyingStorageException("Failed to flush", var3);
        }
    }

    private Object openStore(StoreType type) {
        int storeIndex = type.ordinal();
        Object store = type.open(this);
        this.stores[storeIndex] = store;
        return store;
    }

    private <T extends CommonAbstractStore> T initialize(T store) {
        store.initialise(this.createIfNotExist);
        return store;
    }

    private Object getStore(StoreType storeType) {
        Object store = this.stores[storeType.ordinal()];
        if (store == null) {
            String message = ArrayUtil.contains(this.initializedStores, storeType) ? "Specified store was already closed." : String.format("Specified store was not initialized. Please specify %s as one of the stores types that should be open to be able to use it.", storeType.name());
            throw new IllegalStateException(message);
        } else {
            return store;
        }
    }

    private Object getOrCreateStore(StoreType storeType) {
        Object store = this.stores[storeType.ordinal()];
        if (store == null) {
            store = this.openStore(storeType);
        }

        return store;
    }

    public MetaDataStore getMetaDataStore() {
        return (MetaDataStore)this.getStore(StoreType.META_DATA);
    }

    public NodeStore getNodeStore() {
        return (NodeStore)this.getStore(StoreType.NODE);
    }
   public DbVersionStore getDbVersionStore(){
       return (DbVersionStore)this.getStore(StoreType.DB_VERSION_STORE);
   }

    private DynamicArrayStore getNodeLabelStore() {
        return (DynamicArrayStore)this.getStore(StoreType.NODE_LABEL);
    }

    public RelationshipStore getRelationshipStore() {
        return (RelationshipStore)this.getStore(StoreType.RELATIONSHIP);
    }

    public RelationshipTypeTokenStore getRelationshipTypeTokenStore() {
        return (RelationshipTypeTokenStore)this.getStore(StoreType.RELATIONSHIP_TYPE_TOKEN);
    }

    private DynamicStringStore getRelationshipTypeTokenNamesStore() {
        return (DynamicStringStore)this.getStore(StoreType.RELATIONSHIP_TYPE_TOKEN_NAME);
    }

    public LabelTokenStore getLabelTokenStore() {
        return (LabelTokenStore)this.getStore(StoreType.LABEL_TOKEN);
    }

    private DynamicStringStore getLabelTokenNamesStore() {
        return (DynamicStringStore)this.getStore(StoreType.LABEL_TOKEN_NAME);
    }

    public PropertyStore getPropertyStore() {
        return (PropertyStore)this.getStore(StoreType.PROPERTY);
    }

    private DynamicStringStore getStringPropertyStore() {
        return (DynamicStringStore)this.getStore(StoreType.PROPERTY_STRING);
    }

    private DynamicArrayStore getArrayPropertyStore() {
        return (DynamicArrayStore)this.getStore(StoreType.PROPERTY_ARRAY);
    }

    public PropertyKeyTokenStore getPropertyKeyTokenStore() {
        return (PropertyKeyTokenStore)this.getStore(StoreType.PROPERTY_KEY_TOKEN);
    }

    private DynamicStringStore getPropertyKeyTokenNamesStore() {
        return (DynamicStringStore)this.getStore(StoreType.PROPERTY_KEY_TOKEN_NAME);
    }

    public RelationshipGroupStore getRelationshipGroupStore() {
        return (RelationshipGroupStore)this.getStore(StoreType.RELATIONSHIP_GROUP);
    }

    public SchemaStore getSchemaStore() {
        return (SchemaStore)this.getStore(StoreType.SCHEMA);
    }

    public CountsTracker getCounts() {
        return (CountsTracker)this.getStore(StoreType.COUNTS);
    }

    private CountsTracker createWritableCountsTracker(DatabaseLayout databaseLayout) {
        return new CountsTracker(this.logProvider, this.fileSystemAbstraction, this.pageCache, this.config, databaseLayout, this.versionContextSupplier);
    }

    private ReadOnlyCountsTracker createReadOnlyCountsTracker(DatabaseLayout databaseLayout) {
        return new ReadOnlyCountsTracker(this.logProvider, this.fileSystemAbstraction, this.pageCache, this.config, databaseLayout);
    }

    private Iterable<CommonAbstractStore> instantiatedRecordStores() {
        Iterator<StoreType> storeTypes = new FilteringIterator(Iterators.iterator(STORE_TYPES), this.INSTANTIATED_RECORD_STORES);
        return Iterators.loop(new IteratorWrapper<CommonAbstractStore, StoreType>(storeTypes) {
            protected CommonAbstractStore underlyingObjectToObject(StoreType type) {
                return (CommonAbstractStore)NeoStores.this.stores[type.ordinal()];
            }
        });
    }

    public void makeStoreOk() {
        this.visitStore((store) -> {
            store.makeStoreOk();
            return false;
        });
    }

    public void verifyStoreOk() {
        this.visitStore((store) -> {
            store.checkStoreOk();
            return false;
        });
    }

    public void logVersions(Logger msgLog) {
        this.visitStore((store) -> {
            store.logVersions(msgLog);
            return false;
        });
    }

    public void logIdUsage(Logger msgLog) {
        this.visitStore((store) -> {
            store.logIdUsage(msgLog);
            return false;
        });
    }

    public void visitStore(Visitor<CommonAbstractStore, RuntimeException> visitor) {
        Iterator var2 = this.instantiatedRecordStores().iterator();

        while(var2.hasNext()) {
            CommonAbstractStore store = (CommonAbstractStore)var2.next();
            store.visitStore(visitor);
        }

    }

    public void startCountStore() throws IOException {
        this.getCounts().start();
    }

    public void deleteIdGenerators() {
        this.visitStore((store) -> {
            store.deleteIdGenerator();
            return false;
        });
    }

    public void assertOpen() {
        if (this.stores[StoreType.NODE.ordinal()] == null) {
            throw new IllegalStateException("Database has been shutdown");
        }
    }

    //NodeStore -> DynamicNodeStore
    CommonAbstractStore createdbVersionStore() {
        //return this.initialize(new NodeStore(this.layout.nodeStore(), this.layout.idNodeStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, (DynamicArrayStore)this.getOrCreateStore(StoreType.NODE_LABEL), this.recordFormats, this.openOptions));
        //DbVersionStore(File file, File idFile, Config configuration, IdType idType, IdGeneratorFactory idGeneratorFactory, PageCache pageCache, LogProvider logProvider, String typeDescriptor, RecordFormat< DbVersionRecord > recordFormat, StoreHeaderFormat<NoStoreHeader> storeHeaderFormat, String storeVersion, OpenOption... openOptions)
        return this.initialize(new DbVersionStore(this.layout.dbVersionStore(), this.layout.idDbVersionStore(),this.config,IdType.NODE,this.idGeneratorFactory,this.pageCache,this.logProvider,"dbversionStore", new DbVersionRecordFormat(),NoStoreHeaderFormat.NO_STORE_HEADER_FORMAT,recordFormats.storeVersion(), this.openOptions));

    }
    CommonAbstractStore createNodeStore() {
        return this.initialize(new NodeStore(this.layout.nodeStore(), this.layout.idNodeStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, (DynamicArrayStore)this.getOrCreateStore(StoreType.NODE_LABEL), this.recordFormats, this.openOptions));
        //return this.initialize(new NodeStore(this.layout.nodeStore(), this.layout.idNodeStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, (DynamicArrayStore)this.getOrCreateStore(StoreType.NODE_LABEL),(DynamicVersionArrayStore)this.getOrCreateStore(StoreType.NODE_VERSION_LABEL),this.recordFormats, this.openOptions));

    }

    CommonAbstractStore createNodeLabelStore() {
        return this.createDynamicArrayStore(this.layout.nodeLabelStore(), this.layout.idNodeLabelStore(), IdType.NODE_LABELS, GraphDatabaseSettings.label_block_size);
    }

    CommonAbstractStore createPropertyKeyTokenStore() {
        return this.initialize(new PropertyKeyTokenStore(this.layout.propertyKeyTokenStore(), this.layout.idPropertyKeyTokenStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, (DynamicStringStore)this.getOrCreateStore(StoreType.PROPERTY_KEY_TOKEN_NAME), this.recordFormats, this.openOptions));
    }

    CommonAbstractStore createPropertyKeyTokenNamesStore() {
        return this.createDynamicStringStore(this.layout.propertyKeyTokenNamesStore(), this.layout.idPropertyKeyTokenNamesStore(), IdType.PROPERTY_KEY_TOKEN_NAME, 30);
    }

    CommonAbstractStore createPropertyStore() {
        return this.initialize(new PropertyStore(this.layout.propertyStore(), this.layout.idPropertyStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, (DynamicStringStore)this.getOrCreateStore(StoreType.PROPERTY_STRING), (PropertyKeyTokenStore)this.getOrCreateStore(StoreType.PROPERTY_KEY_TOKEN), (DynamicArrayStore)this.getOrCreateStore(StoreType.PROPERTY_ARRAY), this.recordFormats, this.openOptions));
    }

    CommonAbstractStore createPropertyStringStore() {
        return this.createDynamicStringStore(this.layout.propertyStringStore(), this.layout.idPropertyStringStore(), IdType.STRING_BLOCK, GraphDatabaseSettings.string_block_size);
    }

    CommonAbstractStore createPropertyArrayStore() {
        return this.createDynamicArrayStore(this.layout.propertyArrayStore(), this.layout.idPropertyArrayStore(), IdType.ARRAY_BLOCK, GraphDatabaseSettings.array_block_size);
    }

    CommonAbstractStore createRelationshipStore() {
        return this.initialize(new RelationshipStore(this.layout.relationshipStore(), this.layout.idRelationshipStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, this.recordFormats, this.openOptions));
    }

    CommonAbstractStore createRelationshipTypeTokenStore() {
        return this.initialize(new RelationshipTypeTokenStore(this.layout.relationshipTypeTokenStore(), this.layout.idRelationshipTypeTokenStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, (DynamicStringStore)this.getOrCreateStore(StoreType.RELATIONSHIP_TYPE_TOKEN_NAME), this.recordFormats, this.openOptions));
    }

    CommonAbstractStore createRelationshipTypeTokenNamesStore() {
        return this.createDynamicStringStore(this.layout.relationshipTypeTokenNamesStore(), this.layout.idRelationshipTypeTokenNamesStore(), IdType.RELATIONSHIP_TYPE_TOKEN_NAME, 30);
    }

    CommonAbstractStore createLabelTokenStore() {
        return this.initialize(new LabelTokenStore(this.layout.labelTokenStore(), this.layout.idLabelTokenStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, (DynamicStringStore)this.getOrCreateStore(StoreType.LABEL_TOKEN_NAME), this.recordFormats, this.openOptions));
    }

    CommonAbstractStore createSchemaStore() {
        return this.initialize(new SchemaStore(this.layout.schemaStore(), this.layout.idSchemaStore(), this.config, IdType.SCHEMA, this.idGeneratorFactory, this.pageCache, this.logProvider, this.recordFormats, this.openOptions));
    }

    CommonAbstractStore createRelationshipGroupStore() {
        return this.initialize(new RelationshipGroupStore(this.layout.relationshipGroupStore(), this.layout.idRelationshipGroupStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, this.recordFormats, this.openOptions));
    }

    CommonAbstractStore createLabelTokenNamesStore() {
        return this.createDynamicStringStore(this.layout.labelTokenNamesStore(), this.layout.idLabelTokenNamesStore(), IdType.LABEL_TOKEN_NAME, 30);
    }

    CountsTracker createCountStore() {
        boolean readOnly = (Boolean)this.config.get(GraphDatabaseSettings.read_only);
        CountsTracker counts = readOnly ? this.createReadOnlyCountsTracker(this.layout) : this.createWritableCountsTracker(this.layout);
        ((CountsTracker)counts).setInitializer(new DataInitializer<Updater>() {
            private final Log log;

            {
                this.log = NeoStores.this.logProvider.getLog(MetaDataStore.class);
            }

            public void initialize(Updater updater) {
                this.log.warn("Missing counts store, rebuilding it.");
                (new CountsComputer(NeoStores.this, NeoStores.this.pageCache, NeoStores.this.layout)).initialize(updater);
                this.log.warn("Counts store rebuild completed.");
            }

            public long initialVersion() {
                return ((MetaDataStore)NeoStores.this.getOrCreateStore(StoreType.META_DATA)).getLastCommittedTransactionId();
            }
        });

        try {
            ((CountsTracker)counts).init();
            return (CountsTracker)counts;
        } catch (IOException var5) {
            throw new UnderlyingStorageException("Failed to initialize counts store", var5);
        }
    }

    CommonAbstractStore createMetadataStore() {
        return this.initialize(new MetaDataStore(this.metadataStore, this.layout.idMetadataStore(), this.config, this.idGeneratorFactory, this.pageCache, this.logProvider, this.recordFormats.metaData(), this.recordFormats.storeVersion(), this.openOptions));
    }

    private CommonAbstractStore createDynamicStringStore(File storeFile, File idFile, IdType idType, Setting<Integer> blockSizeProperty) {
        return this.createDynamicStringStore(storeFile, idFile, idType, (Integer)this.config.get(blockSizeProperty));
    }

    private CommonAbstractStore createDynamicStringStore(File storeFile, File idFile, IdType idType, int blockSize) {
        return this.initialize(new DynamicStringStore(storeFile, idFile, this.config, idType, this.idGeneratorFactory, this.pageCache, this.logProvider, blockSize, this.recordFormats.dynamic(), this.recordFormats.storeVersion(), this.openOptions));
    }

    private CommonAbstractStore createDynamicArrayStore(File storeFile, File idFile, IdType idType, Setting<Integer> blockSizeProperty) {
        return this.createDynamicArrayStore(storeFile, idFile, idType, (Integer)this.config.get(blockSizeProperty));
    }

    CommonAbstractStore createDynamicArrayStore(File storeFile, File idFile, IdType idType, int blockSize) {
        if (blockSize <= 0) {
            throw new IllegalArgumentException("Block size of dynamic array store should be positive integer.");
        } else {
            return this.initialize(new DynamicArrayStore(storeFile, idFile, this.config, idType, this.idGeneratorFactory, this.pageCache, this.logProvider, blockSize, this.recordFormats, this.openOptions));
        }
    }

    public void registerDiagnostics(DiagnosticsManager diagnosticsManager) {
        diagnosticsManager.registerAll(NeoStoresDiagnostics.class, this);
    }

    public <RECORD extends AbstractBaseRecord> RecordStore<RECORD> getRecordStore(StoreType type) {
        assert type.isRecordStore();

        return (RecordStore)this.getStore(type);
    }

    public RecordFormats getRecordFormats() {
        return this.recordFormats;
    }
}
