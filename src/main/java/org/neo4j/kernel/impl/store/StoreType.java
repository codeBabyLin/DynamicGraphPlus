//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

//import cn.DynamicGraph.store.DynamicNeoStores;
import org.neo4j.io.layout.DatabaseFile;
import org.neo4j.kernel.impl.store.counts.CountsTracker;

public enum StoreType {
    //DynamicGraph
    //*******************************

  /*  NODE_VERSION_LABEL(DatabaseFile.Node_VERSION_LABEL_STORE,true,false){
        public CommonAbstractStore open(NeoStores neoStores) {
            DynamicNeoStores dns = (DynamicNeoStores)neoStores;
            return dns.createNodeVersionLabelStore();
        }
    },*/
    DB_VERSION_STORE(DatabaseFile.DB_VERSION_STORE,true,true){
        public CommonAbstractStore open(NeoStores neoStores) {
            //DynamicNeoStores dns = (DynamicNeoStores)neoStores;
            return neoStores.createdbVersionStore();
        }
    },

    //DynamicGraph
    //*******************************
    NODE_LABEL(DatabaseFile.NODE_LABEL_STORE, true, false) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createNodeLabelStore();
        }
    },
    NODE(DatabaseFile.NODE_STORE, true, false) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createNodeStore();
        }
    },
    PROPERTY_KEY_TOKEN_NAME(DatabaseFile.PROPERTY_KEY_TOKEN_NAMES_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createPropertyKeyTokenNamesStore();
        }
    },
    PROPERTY_KEY_TOKEN(DatabaseFile.PROPERTY_KEY_TOKEN_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createPropertyKeyTokenStore();
        }
    },
    PROPERTY_STRING(DatabaseFile.PROPERTY_STRING_STORE, true, false) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createPropertyStringStore();
        }
    },
    PROPERTY_ARRAY(DatabaseFile.PROPERTY_ARRAY_STORE, true, false) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createPropertyArrayStore();
        }
    },
    PROPERTY(DatabaseFile.PROPERTY_STORE, true, false) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createPropertyStore();
        }
    },
    RELATIONSHIP(DatabaseFile.RELATIONSHIP_STORE, true, false) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createRelationshipStore();
        }
    },
    RELATIONSHIP_TYPE_TOKEN_NAME(DatabaseFile.RELATIONSHIP_TYPE_TOKEN_NAMES_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createRelationshipTypeTokenNamesStore();
        }
    },
    RELATIONSHIP_TYPE_TOKEN(DatabaseFile.RELATIONSHIP_TYPE_TOKEN_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createRelationshipTypeTokenStore();
        }
    },
    LABEL_TOKEN_NAME(DatabaseFile.LABEL_TOKEN_NAMES_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createLabelTokenNamesStore();
        }
    },
    LABEL_TOKEN(DatabaseFile.LABEL_TOKEN_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createLabelTokenStore();
        }
    },
    SCHEMA(DatabaseFile.SCHEMA_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createSchemaStore();
        }
    },
    RELATIONSHIP_GROUP(DatabaseFile.RELATIONSHIP_GROUP_STORE, true, false) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createRelationshipGroupStore();
        }
    },
    COUNTS(DatabaseFile.COUNTS_STORES, false, false) {
        public CountsTracker open(NeoStores neoStores) {
            return neoStores.createCountStore();
        }

        void close(Object object) {
            try {
                ((CountsTracker)object).shutdown();
            } catch (IOException var3) {
                throw new UnderlyingStorageException(var3);
            }
        }
    },
    META_DATA(DatabaseFile.METADATA_STORE, true, true) {
        public CommonAbstractStore open(NeoStores neoStores) {
            return neoStores.createMetadataStore();
        }
    };

    private final boolean recordStore;
    private final boolean limitedIdStore;
    private final DatabaseFile databaseFile;

    private StoreType(DatabaseFile databaseFile, boolean recordStore, boolean limitedIdStore) {
        this.databaseFile = databaseFile;
        this.recordStore = recordStore;
        this.limitedIdStore = limitedIdStore;
    }

    abstract Object open(NeoStores var1);

    public boolean isRecordStore() {
        return this.recordStore;
    }

    public boolean isLimitedIdStore() {
        return this.limitedIdStore;
    }

    public DatabaseFile getDatabaseFile() {
        return this.databaseFile;
    }

    void close(Object object) {
        ((CommonAbstractStore)object).close();
    }

    public static Optional<StoreType> typeOf(DatabaseFile databaseFile) {
        Objects.requireNonNull(databaseFile);
        StoreType[] values = values();
        StoreType[] var2 = values;
        int var3 = values.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            StoreType value = var2[var4];
            if (value.getDatabaseFile().equals(databaseFile)) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }
}
