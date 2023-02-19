//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

//import cn.DynamicGraph.store.DynamicstoreFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.index.internal.gbptree.RecoveryCleanupWorkCollector;
import org.neo4j.internal.diagnostics.DiagnosticsManager;
import org.neo4j.internal.kernel.api.TokenNameLookup;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.IOLimiter;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.tracing.cursor.context.VersionContextSupplier;
import org.neo4j.kernel.api.exceptions.TransactionApplyKernelException;
import org.neo4j.kernel.api.labelscan.LabelScanStore;
import org.neo4j.kernel.api.labelscan.LabelScanWriter;
import org.neo4j.kernel.api.labelscan.LoggingMonitor;
import org.neo4j.kernel.api.txstate.TransactionCountingStateVisitor;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.api.BatchTransactionApplier;
import org.neo4j.kernel.impl.api.BatchTransactionApplierFacade;
import org.neo4j.kernel.impl.api.CountsRecordState;
import org.neo4j.kernel.impl.api.CountsStoreBatchTransactionApplier;
import org.neo4j.kernel.impl.api.ExplicitBatchIndexApplier;
import org.neo4j.kernel.impl.api.ExplicitIndexApplierLookup;
import org.neo4j.kernel.impl.api.ExplicitIndexProvider;
import org.neo4j.kernel.impl.api.IndexReaderFactory;
import org.neo4j.kernel.impl.api.SchemaState;
import org.neo4j.kernel.impl.api.TransactionApplier;
import org.neo4j.kernel.impl.api.ExplicitIndexApplierLookup.Direct;
import org.neo4j.kernel.impl.api.IndexReaderFactory.Caching;
import org.neo4j.kernel.impl.api.index.IndexProviderMap;
import org.neo4j.kernel.impl.api.index.IndexStoreView;
import org.neo4j.kernel.impl.api.index.IndexingService;
import org.neo4j.kernel.impl.api.index.IndexingServiceFactory;
import org.neo4j.kernel.impl.api.index.IndexingUpdateService;
import org.neo4j.kernel.impl.api.index.IndexingService.Monitor;
import org.neo4j.kernel.impl.api.scan.FullLabelStream;
import org.neo4j.kernel.impl.api.store.SchemaCache;
import org.neo4j.kernel.impl.cache.BridgingCacheAccess;
import org.neo4j.kernel.impl.constraints.ConstraintSemantics;
import org.neo4j.kernel.impl.core.CacheAccessBackDoor;
import org.neo4j.kernel.impl.core.TokenHolders;
import org.neo4j.kernel.impl.factory.OperationalMode;
import org.neo4j.kernel.impl.index.IndexConfigStore;
import org.neo4j.kernel.impl.index.labelscan.NativeLabelScanStore;
import org.neo4j.kernel.impl.locking.LockGroup;
import org.neo4j.kernel.impl.locking.LockService;
import org.neo4j.kernel.impl.storageengine.impl.recordstorage.id.IdController;
import org.neo4j.kernel.impl.store.*;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.kernel.impl.transaction.command.*;
import org.neo4j.kernel.impl.transaction.state.IntegrityValidator;
import org.neo4j.kernel.impl.transaction.state.storeview.DynamicIndexStoreView;
import org.neo4j.kernel.impl.transaction.state.storeview.NeoStoreIndexStoreView;
import org.neo4j.kernel.impl.util.DependencySatisfier;
import org.neo4j.kernel.impl.util.IdOrderingQueue;
import org.neo4j.kernel.internal.DatabaseHealth;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.kernel.spi.explicitindex.IndexImplementation;
import org.neo4j.logging.LogProvider;
import org.neo4j.scheduler.JobScheduler;
import org.neo4j.storageengine.api.CommandReaderFactory;
import org.neo4j.storageengine.api.CommandsToApply;
import org.neo4j.storageengine.api.StorageCommand;
import org.neo4j.storageengine.api.StorageEngine;
import org.neo4j.storageengine.api.StorageReader;
import org.neo4j.storageengine.api.StoreFileMetadata;
import org.neo4j.storageengine.api.StoreId;
import org.neo4j.storageengine.api.TransactionApplicationMode;
import org.neo4j.storageengine.api.lock.ResourceLocker;
import org.neo4j.storageengine.api.schema.SchemaRule;
import org.neo4j.storageengine.api.txstate.ReadableTransactionState;
import org.neo4j.storageengine.api.txstate.TxStateVisitor;
import org.neo4j.storageengine.api.txstate.TxStateVisitor.Decorator;
import org.neo4j.util.VisibleForTesting;
import org.neo4j.util.concurrent.WorkSync;

public class RecordStorageEngine implements StorageEngine, Lifecycle {
    private final IndexingService indexingService;
    private final NeoStores neoStores;
    private final TokenHolders tokenHolders;
    private final DatabaseHealth databaseHealth;
    private final IndexConfigStore indexConfigStore;
    private final SchemaCache schemaCache;
    private final IntegrityValidator integrityValidator;
    private final CacheAccessBackDoor cacheAccess;
    private final LabelScanStore labelScanStore;
    private final IndexProviderMap indexProviderMap;
    private final ExplicitIndexApplierLookup explicitIndexApplierLookup;
    private final SchemaState schemaState;
    private final SchemaStorage schemaStorage;
    private final ConstraintSemantics constraintSemantics;
    private final IdOrderingQueue explicitIndexTransactionOrdering;
    private final LockService lockService;
    private final WorkSync<Supplier<LabelScanWriter>, LabelUpdateWork> labelScanStoreSync;
    private final CommandReaderFactory commandReaderFactory;
    private final WorkSync<IndexingUpdateService, IndexUpdatesWork> indexUpdatesSync;
    private final IndexStoreView indexStoreView;
    private final ExplicitIndexProvider explicitIndexProviderLookup;
    private final IdController idController;
    private final int denseNodeThreshold;
    private final int recordIdBatchSize;

    public RecordStorageEngine(DatabaseLayout databaseLayout, Config config, PageCache pageCache, FileSystemAbstraction fs, LogProvider logProvider, LogProvider userLogProvider, TokenHolders tokenHolders, SchemaState schemaState, ConstraintSemantics constraintSemantics, JobScheduler scheduler, TokenNameLookup tokenNameLookup, LockService lockService, IndexProviderMap indexProviderMap, Monitor indexingServiceMonitor, DatabaseHealth databaseHealth, ExplicitIndexProvider explicitIndexProvider, IndexConfigStore indexConfigStore, IdOrderingQueue explicitIndexTransactionOrdering, IdGeneratorFactory idGeneratorFactory, IdController idController, Monitors monitors, RecoveryCleanupWorkCollector recoveryCleanupWorkCollector, OperationalMode operationalMode, VersionContextSupplier versionContextSupplier) throws NoSuchMethodException {
        this.tokenHolders = tokenHolders;
        this.schemaState = schemaState;
        this.lockService = lockService;
        this.databaseHealth = databaseHealth;
        this.explicitIndexProviderLookup = explicitIndexProvider;
        this.indexConfigStore = indexConfigStore;
        this.constraintSemantics = constraintSemantics;
        this.explicitIndexTransactionOrdering = explicitIndexTransactionOrdering;
        this.idController = idController;
        StoreFactory factory = new StoreFactory(databaseLayout, config, idGeneratorFactory, pageCache, fs, logProvider, versionContextSupplier);
        this.neoStores = factory.openAllNeoStores(true);

        try {
            this.schemaCache = new SchemaCache(constraintSemantics, Collections.emptyList(), indexProviderMap);
            this.schemaStorage = new SchemaStorage(this.neoStores.getSchemaStore());
            NeoStoreIndexStoreView neoStoreIndexStoreView = new NeoStoreIndexStoreView(lockService, this.neoStores);
            boolean readOnly = (Boolean)config.get(GraphDatabaseSettings.read_only) && operationalMode == OperationalMode.single;
            monitors.addMonitorListener(new LoggingMonitor(logProvider.getLog(NativeLabelScanStore.class)), new String[0]);
            this.labelScanStore = new NativeLabelScanStore(pageCache, databaseLayout, fs, new FullLabelStream(neoStoreIndexStoreView), readOnly, monitors, recoveryCleanupWorkCollector);
            this.indexStoreView = new DynamicIndexStoreView(neoStoreIndexStoreView, this.labelScanStore, lockService, this.neoStores, logProvider);
            this.indexProviderMap = indexProviderMap;
            this.indexingService = IndexingServiceFactory.createIndexingService(config, scheduler, indexProviderMap, this.indexStoreView, tokenNameLookup, Iterators.asList(this.schemaStorage.loadAllSchemaRules()), logProvider, userLogProvider, indexingServiceMonitor, schemaState);
            this.integrityValidator = new IntegrityValidator(this.neoStores, this.indexingService);
            this.cacheAccess = new BridgingCacheAccess(this.schemaCache, schemaState, tokenHolders);
            this.explicitIndexApplierLookup = new Direct(explicitIndexProvider);
            LabelScanStore var10003 = this.labelScanStore;
            //Class<?> df = var10003.getClass();
            //this.labelScanStoreSync = new WorkSync(var10003::newWriter);
            Supplier<LabelScanWriter> labelSS = var10003::newWriter;

            this.labelScanStoreSync = new WorkSync(labelSS);
            this.commandReaderFactory = new RecordStorageCommandReaderFactory();
            this.indexUpdatesSync = new WorkSync(this.indexingService);
            this.denseNodeThreshold = (Integer)config.get(GraphDatabaseSettings.dense_node_threshold);
            this.recordIdBatchSize = (Integer)config.get(GraphDatabaseSettings.record_id_batch_size);
        } catch (Throwable var28) {
            this.neoStores.close();
            throw var28;
        }
    }


    public StorageReader newReader() {
        Supplier<IndexReaderFactory> indexReaderFactory = () -> {
            return new Caching(this.indexingService);
        };
        return new RecordStorageReader(this.tokenHolders, this.schemaStorage, this.neoStores, this.indexingService, this.schemaCache, indexReaderFactory, this.labelScanStore::newReader, this.allocateCommandCreationContext());
    }

    public RecordStorageCommandCreationContext allocateCommandCreationContext() {
        return new RecordStorageCommandCreationContext(this.neoStores, this.denseNodeThreshold, this.recordIdBatchSize);
    }

    public CommandReaderFactory commandReaderFactory() {
        return this.commandReaderFactory;
    }

    public void createCommands(Collection<StorageCommand> commands, ReadableTransactionState txState, StorageReader storageReader, ResourceLocker locks, long lastTransactionIdWhenStarted, Decorator additionalTxStateVisitor) throws TransactionFailureException, CreateConstraintFailureException, ConstraintValidationException {
        if (txState != null) {
            RecordStorageCommandCreationContext creationContext = ((RecordStorageReader)storageReader).getCommandCreationContext();
            TransactionRecordState recordState = creationContext.createTransactionRecordState(this.integrityValidator, lastTransactionIdWhenStarted, locks);
            TxStateVisitor txStateVisitor1 = new TransactionToRecordStateVisitor(recordState, this.schemaState, this.schemaStorage, this.constraintSemantics);
            CountsRecordState countsRecordState = new CountsRecordState();
            TxStateVisitor txStateVisitor2 = (TxStateVisitor)additionalTxStateVisitor.apply(txStateVisitor1);
            TxStateVisitor txStateVisitor = new TransactionCountingStateVisitor(txStateVisitor2, storageReader, txState, countsRecordState);
            TxStateVisitor visitor = txStateVisitor;
            Throwable var13 = null;

            try {
                txState.accept(visitor);
            } catch (Throwable var22) {
                var13 = var22;
                throw var22;
            } finally {
                if (txStateVisitor != null) {
                    if (var13 != null) {
                        try {
                            visitor.close();
                        } catch (Throwable var21) {
                            var13.addSuppressed(var21);
                        }
                    } else {
                        txStateVisitor.close();
                    }
                }

            }

            recordState.extractCommands(commands);
            countsRecordState.extractCommands(commands);
        }

    }

    public void apply(CommandsToApply batch, TransactionApplicationMode mode) throws Exception {
        try {
            IndexActivator indexActivator = new IndexActivator(this.indexingService);
            Throwable var95 = null;

            try {
                LockGroup locks = new LockGroup();
                Throwable var6 = null;

                try {
                    BatchTransactionApplier batchApplier = this.applier(mode, indexActivator);
                    Throwable var8 = null;

                    try {
                        for(; batch != null; batch = batch.next()) {
                            TransactionApplier txApplier = batchApplier.startTx(batch, locks);
                            Throwable var10 = null;

                            try {
                                batch.accept(txApplier);
                            } catch (Throwable var86) {
                                var10 = var86;
                                throw var86;
                            } finally {
                                if (txApplier != null) {
                                    if (var10 != null) {
                                        try {
                                            txApplier.close();
                                        } catch (Throwable var85) {
                                            var10.addSuppressed(var85);
                                        }
                                    } else {
                                        txApplier.close();
                                    }
                                }

                            }
                        }
                    } catch (Throwable var88) {
                        var8 = var88;
                        throw var88;
                    } finally {
                        if (batchApplier != null) {
                            if (var8 != null) {
                                try {
                                    batchApplier.close();
                                } catch (Throwable var84) {
                                    var8.addSuppressed(var84);
                                }
                            } else {
                                batchApplier.close();
                            }
                        }

                    }
                } catch (Throwable var90) {
                    var6 = var90;
                    throw var90;
                } finally {
                    if (locks != null) {
                        if (var6 != null) {
                            try {
                                locks.close();
                            } catch (Throwable var83) {
                                var6.addSuppressed(var83);
                            }
                        } else {
                            locks.close();
                        }
                    }

                }
            } catch (Throwable var92) {
                var95 = var92;
                throw var92;
            } finally {
                if (indexActivator != null) {
                    if (var95 != null) {
                        try {
                            indexActivator.close();
                        } catch (Throwable var82) {
                            var95.addSuppressed(var82);
                        }
                    } else {
                        indexActivator.close();
                    }
                }

            }

        } catch (Throwable var94) {
            TransactionApplyKernelException kernelException = new TransactionApplyKernelException(var94, "Failed to apply transaction: %s", new Object[]{batch});
            this.databaseHealth.panic(kernelException);
            throw kernelException;
        }
    }

    protected BatchTransactionApplierFacade applier(TransactionApplicationMode mode, IndexActivator indexActivator) {
        ArrayList<BatchTransactionApplier> appliers = new ArrayList();
        appliers.add(new NeoStoreBatchTransactionApplier(mode.version(), this.neoStores, this.cacheAccess, this.lockService(mode)));
        if (mode.needsHighIdTracking()) {
            appliers.add(new HighIdBatchTransactionApplier(this.neoStores));
        }

        if (mode.needsCacheInvalidationOnUpdates()) {
            appliers.add(new CacheInvalidationBatchTransactionApplier(this.neoStores, this.cacheAccess));
        }

        if (mode.needsAuxiliaryStores()) {
            appliers.add(new CountsStoreBatchTransactionApplier(this.neoStores.getCounts(), mode));
            appliers.add(new IndexBatchTransactionApplier(this.indexingService, this.labelScanStoreSync, this.indexUpdatesSync, this.neoStores.getNodeStore(), this.neoStores.getRelationshipStore(), this.neoStores.getPropertyStore(), indexActivator));
            appliers.add(new ExplicitBatchIndexApplier(this.indexConfigStore, this.explicitIndexApplierLookup, this.explicitIndexTransactionOrdering, mode));
        }

        return new BatchTransactionApplierFacade((BatchTransactionApplier[])appliers.toArray(new BatchTransactionApplier[appliers.size()]));
    }

    private LockService lockService(TransactionApplicationMode mode) {
        return mode != TransactionApplicationMode.RECOVERY && mode != TransactionApplicationMode.REVERSE_RECOVERY ? this.lockService : LockService.NO_LOCK_SERVICE;
    }

    public void satisfyDependencies(DependencySatisfier satisfier) {
        satisfier.satisfyDependency(this.explicitIndexApplierLookup);
        satisfier.satisfyDependency(this.cacheAccess);
        satisfier.satisfyDependency(this.indexProviderMap);
        satisfier.satisfyDependency(this.integrityValidator);
        satisfier.satisfyDependency(this.labelScanStore);
        satisfier.satisfyDependency(this.indexingService);
        satisfier.satisfyDependency(this.neoStores.getMetaDataStore());
        satisfier.satisfyDependency(this.neoStores.getDbVersionStore());
        satisfier.satisfyDependency(this.indexStoreView);
    }

    public void init() throws Throwable {
        this.labelScanStore.init();
    }

    public void start() throws Throwable {
        this.neoStores.makeStoreOk();
        this.neoStores.startCountStore();
        this.indexingService.start();
        this.labelScanStore.start();
        this.idController.start();
    }

    public void loadSchemaCache() {
        List<SchemaRule> schemaRules = Iterators.asList(this.neoStores.getSchemaStore().loadAllSchemaRules());
        this.schemaCache.load(schemaRules);
    }

    public void clearBufferedIds() {
        this.idController.clear();
    }

    public void stop() throws Throwable {
        this.indexingService.stop();
        this.labelScanStore.stop();
        this.idController.stop();
    }

    public void shutdown() throws Throwable {
        this.indexingService.shutdown();
        this.labelScanStore.shutdown();
        this.neoStores.close();
    }

    public void flushAndForce(IOLimiter limiter) {
        this.indexingService.forceAll(limiter);
        this.labelScanStore.force(limiter);
        Iterator var2 = this.explicitIndexProviderLookup.allIndexProviders().iterator();

        while(var2.hasNext()) {
            IndexImplementation index = (IndexImplementation)var2.next();
            index.force();
        }

        this.neoStores.flush(limiter);
    }

    public void registerDiagnostics(DiagnosticsManager diagnosticsManager) {
        this.neoStores.registerDiagnostics(diagnosticsManager);
    }

    public void forceClose() {
        try {
            this.shutdown();
        } catch (Throwable var2) {
            throw new RuntimeException(var2);
        }
    }

    public void prepareForRecoveryRequired() {
        this.neoStores.deleteIdGenerators();
    }

    public Collection<StoreFileMetadata> listStorageFiles() {
        List<StoreFileMetadata> files = new ArrayList();
        StoreType[] var2 = StoreType.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            StoreType type = var2[var4];
            if (type.equals(StoreType.COUNTS)) {
                this.addCountStoreFiles(files);
            } else {
                RecordStore<AbstractBaseRecord> recordStore = this.neoStores.getRecordStore(type);
                StoreFileMetadata metadata = new StoreFileMetadata(recordStore.getStorageFile(), recordStore.getRecordSize());
                files.add(metadata);
            }
        }

        return files;
    }

    private void addCountStoreFiles(List<StoreFileMetadata> files) {
        Iterable<File> countStoreFiles = this.neoStores.getCounts().allFiles();
        Iterator var3 = countStoreFiles.iterator();

        while(var3.hasNext()) {
            File countStoreFile = (File)var3.next();
            StoreFileMetadata countStoreFileMetadata = new StoreFileMetadata(countStoreFile, 1);
            files.add(countStoreFileMetadata);
        }

    }

    @VisibleForTesting
    public NeoStores testAccessNeoStores() {
        return this.neoStores;
    }

    public StoreId getStoreId() {
        return this.neoStores.getMetaDataStore().getStoreId();
    }

    public Lifecycle schemaAndTokensLifecycle() {
        return new LifecycleAdapter() {
            public void init() {
                RecordStorageEngine.this.tokenHolders.propertyKeyTokens().setInitialTokens(RecordStorageEngine.this.neoStores.getPropertyKeyTokenStore().getTokens());
                RecordStorageEngine.this.tokenHolders.relationshipTypeTokens().setInitialTokens(RecordStorageEngine.this.neoStores.getRelationshipTypeTokenStore().getTokens());
                RecordStorageEngine.this.tokenHolders.labelTokens().setInitialTokens(RecordStorageEngine.this.neoStores.getLabelTokenStore().getTokens());
                RecordStorageEngine.this.loadSchemaCache();
                RecordStorageEngine.this.indexingService.init();
            }
        };
    }
}
