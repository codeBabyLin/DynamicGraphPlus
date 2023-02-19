//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.factory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import cn.DynamicGraph.Common.DGVersion;
import cn.DynamicGraph.TransactionVersionCommitImpl;
import cn.DynamicGraph.kernel.impl.store.DbVersionStore;
import org.neo4j.function.Suppliers;
import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.MultipleFoundException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.StringSearchMode;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.security.URLAccessValidationError;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.helpers.collection.PrefetchingResourceIterator;
import org.neo4j.internal.kernel.api.IndexOrder;
import org.neo4j.internal.kernel.api.IndexQuery;
import org.neo4j.internal.kernel.api.IndexReference;
import org.neo4j.internal.kernel.api.Kernel;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.NodeIndexCursor;
import org.neo4j.internal.kernel.api.NodeLabelIndexCursor;
import org.neo4j.internal.kernel.api.NodeValueIndexCursor;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.Read;
import org.neo4j.internal.kernel.api.RelationshipScanCursor;
import org.neo4j.internal.kernel.api.TokenRead;
import org.neo4j.internal.kernel.api.TokenWrite;
import org.neo4j.internal.kernel.api.Write;
import org.neo4j.internal.kernel.api.IndexQuery.ExactPredicate;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.exceptions.EntityNotFoundException;
import org.neo4j.internal.kernel.api.exceptions.InvalidTransactionTypeKernelException;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.SchemaKernelException;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.io.IOUtils;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.kernel.GraphDatabaseQueryService;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.SilentTokenNameLookup;
import org.neo4j.kernel.api.Statement;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.explicitindex.AutoIndexing;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.api.TokenAccess;
import org.neo4j.kernel.impl.core.EmbeddedProxySPI;
import org.neo4j.kernel.impl.core.GraphPropertiesProxy;
import org.neo4j.kernel.impl.core.NodeProxy;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.impl.core.TokenHolders;
import org.neo4j.kernel.impl.core.TokenNotFoundException;
import org.neo4j.kernel.impl.coreapi.AutoIndexerFacade;
import org.neo4j.kernel.impl.coreapi.IndexManagerImpl;
import org.neo4j.kernel.impl.coreapi.IndexProviderImpl;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.kernel.impl.coreapi.PlaceboTransaction;
import org.neo4j.kernel.impl.coreapi.PropertyContainerLocker;
import org.neo4j.kernel.impl.coreapi.ReadOnlyIndexFacade;
import org.neo4j.kernel.impl.coreapi.ReadOnlyRelationshipIndexFacade;
import org.neo4j.kernel.impl.coreapi.RelationshipAutoIndexerFacade;
import org.neo4j.kernel.impl.coreapi.TopLevelTransaction;
import org.neo4j.kernel.impl.coreapi.schema.SchemaImpl;
import org.neo4j.kernel.impl.query.Neo4jTransactionalContextFactory;
import org.neo4j.kernel.impl.query.TransactionalContext;
import org.neo4j.kernel.impl.query.TransactionalContextFactory;
import org.neo4j.kernel.impl.query.clientconnection.ClientConnectionInfo;
import org.neo4j.kernel.impl.traversal.BidirectionalTraversalDescriptionImpl;
import org.neo4j.kernel.impl.traversal.MonoDirectionalTraversalDescription;
import org.neo4j.kernel.impl.util.ValueUtils;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.storageengine.api.EntityType;
import org.neo4j.storageengine.api.StoreId;
import org.neo4j.values.storable.Values;
import org.neo4j.values.virtual.MapValue;

public class GraphDatabaseFacade implements GraphDatabaseAPI, EmbeddedProxySPI {
    private static final PropertyContainerLocker locker = new PropertyContainerLocker();
    private Schema schema;
    private Supplier<IndexManager> indexManager;
    private ThreadToStatementContextBridge statementContext;
    private SPI spi;
    private TransactionalContextFactory contextFactory;
    private Config config;
    private TokenHolders tokenHolders;
    private DbVersionStore dbVersionStore;

    public GraphDatabaseFacade() {
    }

    public void init(SPI spi, ThreadToStatementContextBridge txBridge, Config config, TokenHolders tokenHolders) {
        this.spi = spi;
        this.config = config;
        this.schema = new SchemaImpl(() -> {
            return txBridge.getKernelTransactionBoundToThisThread(true);
        });
        this.statementContext = txBridge;
        this.tokenHolders = tokenHolders;
        this.indexManager = Suppliers.lazySingleton(() -> {
            IndexProviderImpl idxProvider = new IndexProviderImpl(this, () -> {
                return txBridge.getKernelTransactionBoundToThisThread(true);
            });
            AutoIndexerFacade<Node> nodeAutoIndexer = new AutoIndexerFacade(() -> {
                return new ReadOnlyIndexFacade(idxProvider.getOrCreateNodeIndex("node_auto_index", (Map)null));
            }, spi.autoIndexing().nodes());
            RelationshipAutoIndexerFacade relAutoIndexer = new RelationshipAutoIndexerFacade(() -> {
                return new ReadOnlyRelationshipIndexFacade(idxProvider.getOrCreateRelationshipIndex("relationship_auto_index", (Map)null));
            }, spi.autoIndexing().relationships());
            return new IndexManagerImpl(() -> {
                return txBridge.getKernelTransactionBoundToThisThread(true);
            }, idxProvider, nodeAutoIndexer, relAutoIndexer);
        });
        this.contextFactory = Neo4jTransactionalContextFactory.create(spi, txBridge, locker);
    }

    //DynamicGraph
    //*************************************************************

    private DbVersionStore getTransactionIdStore(){
        if(this.dbVersionStore == null){
            Class clazz1 = DbVersionStore.class;
            Iterable stores = this.getDependencyResolver().resolveTypeDependencies(clazz1);
            this.dbVersionStore = (DbVersionStore) stores.iterator().next();
        }

        //DbVersionStore txIdStore = (DbVersionStore)
        return this.dbVersionStore;
    }

    public Transaction beginTx(long version) {
        return this.beginTransaction(version,Type.explicit, LoginContext.AUTH_DISABLED);
    }

    public Transaction beginTx(long version,long timeout, TimeUnit unit) {
        return this.beginTransaction(version,Type.explicit, LoginContext.AUTH_DISABLED, timeout, unit);
    }

    public InternalTransaction beginTransaction(long version,Type type, LoginContext loginContext) {
        return this.beginTransactionInternal(version,type, loginContext, ((Duration)this.config.get(GraphDatabaseSettings.transaction_timeout)).toMillis());
    }

    public InternalTransaction beginTransaction(long version,Type type, LoginContext loginContext, long timeout, TimeUnit unit) {
        return this.beginTransactionInternal(version,type, loginContext, unit.toMillis(timeout));
    }

    private InternalTransaction beginTransactionInternal(long version,Type type, LoginContext loginContext, long timeoutMillis) {
        return (InternalTransaction)(this.statementContext.hasTransaction() ? new PlaceboTransaction(version,this.statementContext.getKernelTransactionBoundToThisThread(true)) : new TopLevelTransaction(version,this.spi.beginTransaction(type, loginContext, timeoutMillis)));
    }


    @Override
    public long[] listAllVersions() {
        long[] versions = new long[0];
        try {
            versions = this.getTransactionIdStore().listAllVersionsSuccessCommited();
        }
        catch (Exception e){

        }

        return versions;
    }

    @Override
    public long getCurrentVersion() {
       return this.getTransactionIdStore().getCurrentHighestVersion();
    }

    @Override
    public void seekVersion(long version) {

    }

    @Override
    public long getNextVersion() {
        return this.getTransactionIdStore().getNextVersion();
    }

    @Override
    public long[] getNextVersions(long versionCount) {
        return this.getTransactionIdStore().getNextVersions(versionCount);
    }

    @Override
    public boolean commitVersions(long[] versions) {
        return false;
    }

    @Override
    public boolean commitVersion(long version, boolean isInuse) {
        return this.getTransactionIdStore().transactionCommit(version,isInuse);
    }


   /* @Override
    public boolean commitVersion(long version) {
        return false;
    }*/


    //DynamicGraph
    //*************************************************************


    public Node createNode() {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var3 = null;

            NodeProxy var4;
            try {
                var4 = this.newNodeProxy(transaction.dataWrite().nodeCreate());
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (ignore != null) {
                    if (var3 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var4;
        } catch (InvalidTransactionTypeKernelException var16) {
            throw new ConstraintViolationException(var16.getMessage(), var16);
        }
    }

    public Long createNodeId() {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var3 = null;

            Long var4;
            try {
                var4 = transaction.dataWrite().nodeCreate();
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (ignore != null) {
                    if (var3 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var4;
        } catch (InvalidTransactionTypeKernelException var16) {
            throw new ConstraintViolationException(var16.getMessage(), var16);
        }
    }

    public Node createNode(Label... labels) {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var4 = null;

            try {
                TokenWrite tokenWrite = transaction.tokenWrite();
                int[] labelIds = new int[labels.length];
                String[] labelNames = new String[labels.length];

                for(int i = 0; i < labelNames.length; ++i) {
                    labelNames[i] = labels[i].name();
                }

                tokenWrite.labelGetOrCreateForNames(labelNames, labelIds);
                Write write = transaction.dataWrite();
                long nodeId = write.nodeCreateWithLabels(labelIds);
                NodeProxy var11 = this.newNodeProxy(nodeId);
                return var11;
            } catch (Throwable var23) {
                var4 = var23;
                throw var23;
            } finally {
                if (ignore != null) {
                    if (var4 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var22) {
                            var4.addSuppressed(var22);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }
        } catch (ConstraintValidationException var25) {
            throw new ConstraintViolationException("Unable to add label.", var25);
        } catch (SchemaKernelException var26) {
            throw new IllegalArgumentException(var26);
        } catch (KernelException var27) {
            throw new ConstraintViolationException(var27.getMessage(), var27);
        }
    }

    public Node getNodeById(long id) {
        if (id < 0L) {
            throw new NotFoundException(String.format("Node %d not found", id), new EntityNotFoundException(EntityType.NODE, id));
        } else {
            KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
            assertTransactionOpen(ktx);
            Statement ignore = ktx.acquireStatement();
            Throwable var5 = null;

            NodeProxy var6;
            try {
                if (!ktx.dataRead().nodeExists(id)) {
                    throw new NotFoundException(String.format("Node %d not found", id), new EntityNotFoundException(EntityType.NODE, id));
                }

                var6 = this.newNodeProxy(id);
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (ignore != null) {
                    if (var5 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var6;
        }
    }

    public Relationship getRelationshipById(long id) {
        if (id < 0L) {
            throw new NotFoundException(String.format("Relationship %d not found", id), new EntityNotFoundException(EntityType.RELATIONSHIP, id));
        } else {
            KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
            assertTransactionOpen(ktx);
            Statement ignore = this.statementContext.get();
            Throwable var5 = null;

            RelationshipProxy var6;
            try {
                if (!ktx.dataRead().relationshipExists(id)) {
                    throw new NotFoundException(String.format("Relationship %d not found", id), new EntityNotFoundException(EntityType.RELATIONSHIP, id));
                }

                var6 = this.newRelationshipProxy(id);
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (ignore != null) {
                    if (var5 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var6;
        }
    }

    /** @deprecated */
    @Deprecated
    public IndexManager index() {
        return (IndexManager)this.indexManager.get();
    }

    public Schema schema() {
        this.assertTransactionOpen();
        return this.schema;
    }

    public boolean isAvailable(long timeoutMillis) {
        return this.spi.databaseIsAvailable(timeoutMillis);
    }

    public void shutdown() {
        this.spi.shutdown();
    }

    public Transaction beginTx() {

        //DynamicGraph
        InternalTransaction transaction = this.beginTransaction(Type.explicit, LoginContext.AUTH_DISABLED);
        //transaction.setVersion(DGVersion.setStartEndVersion(this.getNextVersion,65535))
        transaction.setVersion(DGVersion.setStartVersion(this.getNextVersion()));
        //val func = (e:java.lang.Long,l:java.lang.Boolean) => this.commitVersion(e,l)
        //transaction.setFunction(func)
        //transaction.setVersionstore(this.getTransactionIdStore());
        transaction.setVersionstore(new TransactionVersionCommitImpl(this.getTransactionIdStore()));

        //DynamicGraph
        return transaction;
    }

    public Transaction beginTx(long timeout, TimeUnit unit) {
        return this.beginTransaction(Type.explicit, LoginContext.AUTH_DISABLED, timeout, unit);
    }

    public InternalTransaction beginTransaction(Type type, LoginContext loginContext) {
        return this.beginTransactionInternal(type, loginContext, ((Duration)this.config.get(GraphDatabaseSettings.transaction_timeout)).toMillis());
    }

    public InternalTransaction beginTransaction(Type type, LoginContext loginContext, long timeout, TimeUnit unit) {
        return this.beginTransactionInternal(type, loginContext, unit.toMillis(timeout));
    }

    @Override
    public InternalTransaction beginTransaction(Type var1, LoginContext var2, long version) {
        //DynamicGraph
        InternalTransaction transaction = this.beginTransaction(var1, var2);

        transaction.setVersion(DGVersion.setStartVersion(version));

        transaction.setVersionstore(new TransactionVersionCommitImpl(this.getTransactionIdStore()));

        //DynamicGraph
        return transaction;
    }

    @Override
    public InternalTransaction beginTransaction(Type var1, LoginContext var2, long var3, TimeUnit var5, long version) {
        InternalTransaction transaction = this.beginTransaction(var1, var2,var3,var5);

        transaction.setVersion(DGVersion.setStartVersion(version));

        transaction.setVersionstore(new TransactionVersionCommitImpl(this.getTransactionIdStore()));
        return transaction;
    }

    public Result execute(String query) throws QueryExecutionException {
        return this.execute(query, Collections.emptyMap());
    }

    public Result execute(String query, long timeout, TimeUnit unit) throws QueryExecutionException {
        return this.execute(query, Collections.emptyMap(), timeout, unit);
    }

    public Result execute(String query, Map<String, Object> parameters) throws QueryExecutionException {
        InternalTransaction transaction = this.beginTransaction(Type.implicit, LoginContext.AUTH_DISABLED);

        //Dy
        transaction.setVersion(DGVersion.setStartVersion(this.getNextVersion()));

        //transaction.setVersionstore(this.getTransactionIdStore());
        transaction.setVersionstore(new TransactionVersionCommitImpl(this.getTransactionIdStore()));
        //Dy

        return this.execute(transaction, query, ValueUtils.asParameterMapValue(parameters));
    }

    public Result execute(String query, Map<String, Object> parameters, long timeout, TimeUnit unit) throws QueryExecutionException {
        InternalTransaction transaction = this.beginTransaction(Type.implicit, LoginContext.AUTH_DISABLED, timeout, unit);
        return this.execute(transaction, query, ValueUtils.asParameterMapValue(parameters));
    }

    public Result execute(InternalTransaction transaction, String query, MapValue parameters) throws QueryExecutionException {
        TransactionalContext context = this.contextFactory.newContext(ClientConnectionInfo.EMBEDDED_CONNECTION, transaction, query, parameters);
        return this.spi.executeQuery(query, parameters, context);
    }

    public ResourceIterable<Node> getAllNodes() {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final NodeCursor cursor = ktx.cursors().allocateNodeCursor();
            ktx.dataRead().allNodesScan(cursor);
            return new PrefetchingResourceIterator<Node>() {
                protected Node fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                        while(DGVersion.hasEndVersion(cursor.nodeVersion())){
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                        }
                        //DynamicGraph
                        return GraphDatabaseFacade.this.newNodeProxy(cursor.nodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
    }

    public ResourceIterable<Relationship> getAllRelationships() {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final RelationshipScanCursor cursor = ktx.cursors().allocateRelationshipScanCursor();
            ktx.dataRead().allRelationshipsScan(cursor);
            return new PrefetchingResourceIterator<Relationship>() {
                protected Relationship fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                        while(DGVersion.hasEndVersion(cursor.relVersion())){
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                        }
                        //DynamicGraph

                        return GraphDatabaseFacade.this.newRelationshipProxy(cursor.relationshipReference(), cursor.sourceNodeReference(), cursor.type(), cursor.targetNodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
    }

    public ResourceIterable<Label> getAllLabelsInUse() {
        return this.allInUse(TokenAccess.LABELS);
    }

    public ResourceIterable<RelationshipType> getAllRelationshipTypesInUse() {
        return this.allInUse(TokenAccess.RELATIONSHIP_TYPES);
    }

    private <T> ResourceIterable<T> allInUse(TokenAccess<T> tokens) {
        this.assertTransactionOpen();
        return () -> {
            return tokens.inUse(this.statementContext.getKernelTransactionBoundToThisThread(true));
        };
    }

    public ResourceIterable<Label> getAllLabels() {
        return this.all(TokenAccess.LABELS);
    }

    public ResourceIterable<RelationshipType> getAllRelationshipTypes() {
        return this.all(TokenAccess.RELATIONSHIP_TYPES);
    }

    public ResourceIterable<String> getAllPropertyKeys() {
        return this.all(TokenAccess.PROPERTY_KEYS);
    }

    private <T> ResourceIterable<T> all(TokenAccess<T> tokens) {
        this.assertTransactionOpen();
        return () -> {
            KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);
            return tokens.all(transaction);
        };
    }

    public KernelEventHandler registerKernelEventHandler(KernelEventHandler handler) {
        this.spi.registerKernelEventHandler(handler);
        return handler;
    }

    public <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> handler) {
        this.spi.registerTransactionEventHandler(handler);
        return handler;
    }

    public KernelEventHandler unregisterKernelEventHandler(KernelEventHandler handler) {
        this.spi.unregisterKernelEventHandler(handler);
        return handler;
    }

    public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> handler) {
        this.spi.unregisterTransactionEventHandler(handler);
        return handler;
    }

    public ResourceIterator<Node> findNodes(Label myLabel, String key, Object value) {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);
        TokenRead tokenRead = transaction.tokenRead();
        int labelId = tokenRead.nodeLabel(myLabel.name());
        int propertyId = tokenRead.propertyKey(key);
        return this.nodesByLabelAndProperty(transaction, labelId, IndexQuery.exact(propertyId, Values.of(value)));
    }

    public ResourceIterator<Node> findNodes(Label label, String key1, Object value1, String key2, Object value2) {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);
        TokenRead tokenRead = transaction.tokenRead();
        int labelId = tokenRead.nodeLabel(label.name());
        return this.nodesByLabelAndProperties(transaction, labelId, IndexQuery.exact(tokenRead.propertyKey(key1), Values.of(value1)), IndexQuery.exact(tokenRead.propertyKey(key2), Values.of(value2)));
    }

    public ResourceIterator<Node> findNodes(Label label, String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);
        TokenRead tokenRead = transaction.tokenRead();
        int labelId = tokenRead.nodeLabel(label.name());
        return this.nodesByLabelAndProperties(transaction, labelId, IndexQuery.exact(tokenRead.propertyKey(key1), Values.of(value1)), IndexQuery.exact(tokenRead.propertyKey(key2), Values.of(value2)), IndexQuery.exact(tokenRead.propertyKey(key3), Values.of(value3)));
    }

    public ResourceIterator<Node> findNodes(Label label, Map<String, Object> propertyValues) {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);
        TokenRead tokenRead = transaction.tokenRead();
        int labelId = tokenRead.nodeLabel(label.name());
        ExactPredicate[] queries = new ExactPredicate[propertyValues.size()];
        int i = 0;

        Entry entry;
        for(Iterator var8 = propertyValues.entrySet().iterator(); var8.hasNext(); queries[i++] = IndexQuery.exact(tokenRead.propertyKey((String)entry.getKey()), Values.of(entry.getValue()))) {
            entry = (Entry)var8.next();
        }

        return this.nodesByLabelAndProperties(transaction, labelId, queries);
    }

    public ResourceIterator<Node> findNodes(Label myLabel, String key, String value, StringSearchMode searchMode) {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);
        TokenRead tokenRead = transaction.tokenRead();
        int labelId = tokenRead.nodeLabel(myLabel.name());
        int propertyId = tokenRead.propertyKey(key);
        Object query;
        switch(searchMode) {
            case EXACT:
                query = IndexQuery.exact(propertyId, Values.utf8Value(value.getBytes(StandardCharsets.UTF_8)));
                break;
            case PREFIX:
                query = IndexQuery.stringPrefix(propertyId, Values.utf8Value(value.getBytes(StandardCharsets.UTF_8)));
                break;
            case SUFFIX:
                query = IndexQuery.stringSuffix(propertyId, Values.utf8Value(value.getBytes(StandardCharsets.UTF_8)));
                break;
            case CONTAINS:
                query = IndexQuery.stringContains(propertyId, Values.utf8Value(value.getBytes(StandardCharsets.UTF_8)));
                break;
            default:
                throw new IllegalStateException("Unknown string search mode: " + searchMode);
        }

        return this.nodesByLabelAndProperty(transaction, labelId, (IndexQuery)query);
    }

    public Node findNode(Label myLabel, String key, Object value) {
        ResourceIterator<Node> iterator = this.findNodes(myLabel, key, value);
        Throwable var5 = null;

        Node var7;
        try {
            Node node;
            if (!iterator.hasNext()) {
                node = null;
                return node;
            }

            node = (Node)iterator.next();
            if (iterator.hasNext()) {
                throw new MultipleFoundException(String.format("Found multiple nodes with label: '%s', property name: '%s' and property value: '%s' while only one was expected.", myLabel, key, value));
            }

            var7 = node;
        } catch (Throwable var17) {
            var5 = var17;
            throw var17;
        } finally {
            if (iterator != null) {
                if (var5 != null) {
                    try {
                        iterator.close();
                    } catch (Throwable var16) {
                        var5.addSuppressed(var16);
                    }
                } else {
                    iterator.close();
                }
            }

        }

        return var7;
    }

    public ResourceIterator<Node> findNodes(Label myLabel) {
        return this.allNodesWithLabel(myLabel);
    }

    private InternalTransaction beginTransactionInternal(Type type, LoginContext loginContext, long timeoutMillis) {

        return (InternalTransaction)(this.statementContext.hasTransaction() ? new PlaceboTransaction(this.statementContext.getKernelTransactionBoundToThisThread(true)) : new TopLevelTransaction(this.spi.beginTransaction(type, loginContext, timeoutMillis)));
    }

/*    private InternalTransaction beginTransactionInternal(Type type, LoginContext loginContext, long timeoutMillis) {

        return (InternalTransaction)(this.statementContext.hasTransaction() ? new PlaceboTransaction(this.statementContext.getKernelTransactionBoundToThisThread(true)) : new TopLevelTransaction(this.spi.beginTransaction(type, loginContext, timeoutMillis)));
    }*/


    private ResourceIterator<Node> nodesByLabelAndProperty(KernelTransaction transaction, int labelId, IndexQuery query) {
        Statement statement = transaction.acquireStatement();
        Read read = transaction.dataRead();
        if (query.propertyKeyId() != -1 && labelId != -1) {
            IndexReference index = transaction.schemaRead().index(labelId, new int[]{query.propertyKeyId()});
            if (index != IndexReference.NO_INDEX) {
                try {
                    NodeValueIndexCursor cursor = transaction.cursors().allocateNodeValueIndexCursor();
                    read.nodeIndexSeek(index, cursor, IndexOrder.NONE, false, new IndexQuery[]{query});
                    return new NodeCursorResourceIterator(cursor, statement, this::newNodeProxy);
                } catch (KernelException var8) {
                }
            }

            return this.getNodesByLabelAndPropertyWithoutIndex(statement, labelId, query);
        } else {
            statement.close();
            return Iterators.emptyResourceIterator();
        }
    }

    private ResourceIterator<Node> nodesByLabelAndProperties(KernelTransaction transaction, int labelId, ExactPredicate... queries) {
        Statement statement = transaction.acquireStatement();
        Read read = transaction.dataRead();
        if (isInvalidQuery(labelId, queries)) {
            statement.close();
            return Iterators.emptyResourceIterator();
        } else {
            int[] propertyIds = getPropertyIds(queries);
            IndexReference index = findMatchingIndex(transaction, labelId, propertyIds);
            if (index != IndexReference.NO_INDEX) {
                try {
                    NodeValueIndexCursor cursor = transaction.cursors().allocateNodeValueIndexCursor();
                    read.nodeIndexSeek(index, cursor, IndexOrder.NONE, false, getReorderedIndexQueries(index.properties(), queries));
                    return new NodeCursorResourceIterator(cursor, statement, this::newNodeProxy);
                } catch (KernelException var9) {
                }
            }

            return this.getNodesByLabelAndPropertyWithoutIndex(statement, labelId, queries);
        }
    }

    private static IndexReference findMatchingIndex(KernelTransaction transaction, int labelId, int[] propertyIds) {
        IndexReference index = transaction.schemaRead().index(labelId, propertyIds);
        if (index != IndexReference.NO_INDEX) {
            return index;
        } else {
            Arrays.sort(propertyIds);
            assertNoDuplicates(propertyIds, transaction.tokenRead());
            int[] workingCopy = new int[propertyIds.length];
            Iterator indexes = transaction.schemaRead().indexesGetForLabel(labelId);

            int[] original;
            do {
                if (!indexes.hasNext()) {
                    return IndexReference.NO_INDEX;
                }

                index = (IndexReference)indexes.next();
                original = index.properties();
            } while(!hasSamePropertyIds(original, workingCopy, propertyIds));

            return index;
        }
    }

    private static IndexQuery[] getReorderedIndexQueries(int[] indexPropertyIds, IndexQuery[] queries) {
        IndexQuery[] orderedQueries = new IndexQuery[queries.length];

        for(int i = 0; i < indexPropertyIds.length; ++i) {
            int propertyKeyId = indexPropertyIds[i];
            IndexQuery[] var5 = queries;
            int var6 = queries.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                IndexQuery query = var5[var7];
                if (query.propertyKeyId() == propertyKeyId) {
                    orderedQueries[i] = query;
                    break;
                }
            }
        }

        return orderedQueries;
    }

    private static boolean hasSamePropertyIds(int[] original, int[] workingCopy, int[] propertyIds) {
        if (original.length == propertyIds.length) {
            System.arraycopy(original, 0, workingCopy, 0, original.length);
            Arrays.sort(workingCopy);
            return Arrays.equals(propertyIds, workingCopy);
        } else {
            return false;
        }
    }

    private static int[] getPropertyIds(IndexQuery[] queries) {
        int[] propertyIds = new int[queries.length];

        for(int i = 0; i < queries.length; ++i) {
            propertyIds[i] = queries[i].propertyKeyId();
        }

        return propertyIds;
    }

    private static boolean isInvalidQuery(int labelId, IndexQuery[] queries) {
        boolean invalidQuery = labelId == -1;
        IndexQuery[] var3 = queries;
        int var4 = queries.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            IndexQuery query = var3[var5];
            int propertyKeyId = query.propertyKeyId();
            invalidQuery = invalidQuery || propertyKeyId == -1;
        }

        return invalidQuery;
    }

    private static void assertNoDuplicates(int[] propertyIds, TokenRead tokenRead) {
        int prev = propertyIds[0];

        for(int i = 1; i < propertyIds.length; ++i) {
            int curr = propertyIds[i];
            if (curr == prev) {
                SilentTokenNameLookup tokenLookup = new SilentTokenNameLookup(tokenRead);
                throw new IllegalArgumentException(String.format("Provided two queries for property %s. Only one query per property key can be performed", tokenLookup.propertyKeyGetName(curr)));
            }

            prev = curr;
        }

    }

    private ResourceIterator<Node> getNodesByLabelAndPropertyWithoutIndex(Statement statement, int labelId, IndexQuery... queries) {
        KernelTransaction transaction = this.statementContext.getKernelTransactionBoundToThisThread(true);
        NodeLabelIndexCursor nodeLabelCursor = transaction.cursors().allocateNodeLabelIndexCursor();
        NodeCursor nodeCursor = transaction.cursors().allocateNodeCursor();
        PropertyCursor propertyCursor = transaction.cursors().allocatePropertyCursor();
        transaction.dataRead().nodeLabelScan(labelId, nodeLabelCursor);
        return new NodeLabelPropertyIterator(transaction.dataRead(), nodeLabelCursor, nodeCursor, propertyCursor, statement, this::newNodeProxy, queries);
    }

    private ResourceIterator<Node> allNodesWithLabel(Label myLabel) {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        Statement statement = ktx.acquireStatement();
        int labelId = ktx.tokenRead().nodeLabel(myLabel.name());
        if (labelId == -1) {
            statement.close();
            return Iterators.emptyResourceIterator();
        } else {
            NodeLabelIndexCursor cursor = ktx.cursors().allocateNodeLabelIndexCursor();
            ktx.dataRead().nodeLabelScan(labelId, cursor);
            return new NodeCursorResourceIterator(cursor, statement, this::newNodeProxy);
        }
    }

    public TraversalDescription traversalDescription() {
        return new MonoDirectionalTraversalDescription(this.statementContext);
    }

    public BidirectionalTraversalDescription bidirectionalTraversalDescription() {
        return new BidirectionalTraversalDescriptionImpl(this.statementContext);
    }

    public DependencyResolver getDependencyResolver() {
        return this.spi.resolver();
    }

    public StoreId storeId() {
        return this.spi.storeId();
    }

    public URL validateURLAccess(URL url) throws URLAccessValidationError {
        return this.spi.validateURLAccess(url);
    }

    public DatabaseLayout databaseLayout() {
        return this.spi.databaseLayout();
    }

    public String toString() {
        return this.spi.name() + " [" + this.databaseLayout() + "]";
    }

    public Statement statement() {
        return this.statementContext.get();
    }

    public KernelTransaction kernelTransaction() {
        return this.statementContext.getKernelTransactionBoundToThisThread(true);
    }

    public GraphDatabaseService getGraphDatabase() {
        return this;
    }

    public void assertInUnterminatedTransaction() {
        this.statementContext.assertInUnterminatedTransaction();
    }

    public void failTransaction() {
        this.statementContext.getKernelTransactionBoundToThisThread(true).failure();
    }

    public RelationshipProxy newRelationshipProxy(long id) {
        return new RelationshipProxy(this, id);
    }

    public RelationshipProxy newRelationshipProxy(long id, long startNodeId, int typeId, long endNodeId) {
        return new RelationshipProxy(this, id, startNodeId, typeId, endNodeId);
    }

    public NodeProxy newNodeProxy(long nodeId) {
        return new NodeProxy(this, nodeId);
    }

    public RelationshipType getRelationshipTypeById(int type) {
        try {
            String name = this.tokenHolders.relationshipTypeTokens().getTokenById(type).name();
            return RelationshipType.withName(name);
        } catch (TokenNotFoundException var3) {
            throw new IllegalStateException("Kernel API returned non-existent relationship type: " + type);
        }
    }

    public GraphPropertiesProxy newGraphPropertiesProxy() {
        return new GraphPropertiesProxy(this);
    }

    private void assertTransactionOpen() {
        assertTransactionOpen(this.statementContext.getKernelTransactionBoundToThisThread(true));
    }

    private static void assertTransactionOpen(KernelTransaction transaction) {
        if (transaction.isTerminated()) {
            Status terminationReason = (Status)transaction.getReasonIfTerminated().orElse(Status.Transaction.Terminated);
            throw new TransactionTerminatedException(terminationReason);
        }
    }

    //VersionGraphService
    //******************************************************************

    @Override
    public ResourceIterable<Node> getAllNodesInSingleVersion(long version) {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final NodeCursor cursor = ktx.cursors().allocateNodeCursor();
            ktx.dataRead().allNodesScan(cursor);
            return new PrefetchingResourceIterator<Node>() {
                protected Node fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                        while(DGVersion.getStartVersion(cursor.nodeVersion()) >= version || DGVersion.getEndVersion(cursor.nodeVersion()) < version){
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                        }
                        //DynamicGraph
                        return GraphDatabaseFacade.this.newNodeProxy(cursor.nodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
       // return null;
    }

    @Override
    public ResourceIterable<Relationship> getAllRelationshipsInSingleVersion(long version) {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final RelationshipScanCursor cursor = ktx.cursors().allocateRelationshipScanCursor();
            ktx.dataRead().allRelationshipsScan(cursor);
            return new PrefetchingResourceIterator<Relationship>() {
                protected Relationship fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                        while(DGVersion.getStartVersion(cursor.relVersion()) >= version || DGVersion.getEndVersion(cursor.relVersion()) < version){
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                        }
                        //DynamicGraph

                        return GraphDatabaseFacade.this.newRelationshipProxy(cursor.relationshipReference(), cursor.sourceNodeReference(), cursor.type(), cursor.targetNodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
    }

    @Override
    public ResourceIterable<Node> getAllNodesInVersionDelta(long startVersion, long endVersion) {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final NodeCursor cursor = ktx.cursors().allocateNodeCursor();
            ktx.dataRead().allNodesScan(cursor);
            return new PrefetchingResourceIterator<Node>() {
                protected Node fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                     /*   while( DGVersion.getStartVersion(cursor.nodeVersion()) < startVersion
                                || DGVersion.getStartVersion(cursor.nodeVersion()) >= endVersion
                               || DGVersion.getEndVersion(cursor.nodeVersion()) < startVersion
                               || (DGVersion.getEndVersion(cursor.nodeVersion()) >= endVersion && !DGVersion.hasEndVersion(cursor.nodeVersion()))) {
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                        }*/
                     //DynamicGraph
                     long cursorStartVersion = DGVersion.getStartVersion(cursor.nodeVersion());
                     long cursorEndVersion = DGVersion.getEndVersion(cursor.nodeVersion());
                        while(!((cursorStartVersion >= startVersion && cursorStartVersion < endVersion) || (cursorEndVersion >= startVersion && cursorEndVersion < endVersion))) {
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                            cursorStartVersion = DGVersion.getStartVersion(cursor.nodeVersion());
                            cursorEndVersion = DGVersion.getEndVersion(cursor.nodeVersion());
                        }
                        //DynamicGraph
                        return GraphDatabaseFacade.this.newNodeProxy(cursor.nodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
        // return null;
    }

    @Override
    public ResourceIterable<Relationship> getAllRelationshipsInVersionDelta(long startVersion, long endVersion) {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final RelationshipScanCursor cursor = ktx.cursors().allocateRelationshipScanCursor();
            ktx.dataRead().allRelationshipsScan(cursor);
            return new PrefetchingResourceIterator<Relationship>() {
                protected Relationship fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                        long cursorStartVersion = DGVersion.getStartVersion(cursor.relVersion());
                        long cursorEndVersion = DGVersion.getEndVersion(cursor.relVersion());
                        while(!((cursorStartVersion >= startVersion && cursorStartVersion < endVersion) || (cursorEndVersion >= startVersion && cursorEndVersion < endVersion))) {
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                            cursorStartVersion = DGVersion.getStartVersion(cursor.relVersion());
                            cursorEndVersion = DGVersion.getEndVersion(cursor.relVersion());
                        }
                        //DynamicGraph

                        return GraphDatabaseFacade.this.newRelationshipProxy(cursor.relationshipReference(), cursor.sourceNodeReference(), cursor.type(), cursor.targetNodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
    }

    @Override
    public ResourceIterable<Node> getAllNodesInVersions(long startVersion, long endVersion) {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final NodeCursor cursor = ktx.cursors().allocateNodeCursor();
            ktx.dataRead().allNodesScan(cursor);
            return new PrefetchingResourceIterator<Node>() {
                protected Node fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                        long cursorStartVersion = DGVersion.getStartVersion(cursor.nodeVersion());
                        long cursorEndVersion = DGVersion.getEndVersion(cursor.nodeVersion());
                        while(!(cursorStartVersion < startVersion && cursorEndVersion >= endVersion)) {
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                            cursorStartVersion = DGVersion.getStartVersion(cursor.nodeVersion());
                            cursorEndVersion = DGVersion.getEndVersion(cursor.nodeVersion());
                        }
                        //DynamicGraph
                        return GraphDatabaseFacade.this.newNodeProxy(cursor.nodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
    }

    @Override
    public ResourceIterable<Relationship> getAllRelationshipsInVersions(long startVersion, long endVersion) {
        KernelTransaction ktx = this.statementContext.getKernelTransactionBoundToThisThread(true);
        assertTransactionOpen(ktx);
        return () -> {
            final Statement statement = ktx.acquireStatement();
            final RelationshipScanCursor cursor = ktx.cursors().allocateRelationshipScanCursor();
            ktx.dataRead().allRelationshipsScan(cursor);
            return new PrefetchingResourceIterator<Relationship>() {
                protected Relationship fetchNextOrNull() {
                    if (cursor.next()) {
                        //DynamicGraph
                        long cursorStartVersion = DGVersion.getStartVersion(cursor.relVersion());
                        long cursorEndVersion = DGVersion.getEndVersion(cursor.relVersion());
                        while(!(cursorStartVersion < startVersion && cursorEndVersion >= endVersion)) {
                            if(!cursor.next()){
                                this.close();
                                return null;
                            }
                            cursorStartVersion = DGVersion.getStartVersion(cursor.relVersion());
                            cursorEndVersion = DGVersion.getEndVersion(cursor.relVersion());
                        }
                        //DynamicGraph

                        return GraphDatabaseFacade.this.newRelationshipProxy(cursor.relationshipReference(), cursor.sourceNodeReference(), cursor.type(), cursor.targetNodeReference());
                    } else {
                        this.close();
                        return null;
                    }
                }

                public void close() {
                    cursor.close();
                    statement.close();
                }
            };
        };
    }


    //******************************************************************
    //VersionGraphService


    private interface NodeFactory {
        NodeProxy make(long var1);
    }

    private abstract static class PrefetchingNodeResourceIterator implements ResourceIterator<Node> {
        private final Statement statement;
        private final NodeFactory nodeFactory;
        private long next;
        private boolean closed;
        private static final long NOT_INITIALIZED = -2L;
        protected static final long NO_ID = -1L;

        PrefetchingNodeResourceIterator(Statement statement, NodeFactory nodeFactory) {
            this.statement = statement;
            this.nodeFactory = nodeFactory;
            this.next = -2L;
        }

        public boolean hasNext() {
            if (this.next == -2L) {
                this.next = this.fetchNext();
            }

            return this.next != -1L;
        }

        public Node next() {
            if (!this.hasNext()) {
                this.close();
                throw new NoSuchElementException();
            } else {
                Node nodeProxy = this.nodeFactory.make(this.next);
                this.next = this.fetchNext();
                return nodeProxy;
            }
        }

        public void close() {
            if (!this.closed) {
                this.next = -1L;
                this.closeResources(this.statement);
                this.closed = true;
            }

        }

        abstract long fetchNext();

        abstract void closeResources(Statement var1);
    }

    private static final class NodeCursorResourceIterator<CURSOR extends NodeIndexCursor> extends PrefetchingNodeResourceIterator {
        private final CURSOR cursor;

        NodeCursorResourceIterator(CURSOR cursor, Statement statement, NodeFactory nodeFactory) {
            super(statement, nodeFactory);
            this.cursor = cursor;
        }

        long fetchNext() {
            if (this.cursor.next()) {
                return this.cursor.nodeReference();
            } else {
                this.close();
                return -1L;
            }
        }

        void closeResources(Statement statement) {
            IOUtils.closeAllSilently(new AutoCloseable[]{statement, this.cursor});
        }
    }

    private static class NodeLabelPropertyIterator extends PrefetchingNodeResourceIterator {
        private final Read read;
        private final NodeLabelIndexCursor nodeLabelCursor;
        private final NodeCursor nodeCursor;
        private final PropertyCursor propertyCursor;
        private final IndexQuery[] queries;

        NodeLabelPropertyIterator(Read read, NodeLabelIndexCursor nodeLabelCursor, NodeCursor nodeCursor, PropertyCursor propertyCursor, Statement statement, NodeFactory nodeFactory, IndexQuery... queries) {
            super(statement, nodeFactory);
            this.read = read;
            this.nodeLabelCursor = nodeLabelCursor;
            this.nodeCursor = nodeCursor;
            this.propertyCursor = propertyCursor;
            this.queries = queries;
        }

        protected long fetchNext() {
            boolean hasNext;
            do {
                hasNext = this.nodeLabelCursor.next();
            } while(hasNext && !this.hasPropertiesWithValues());

            if (hasNext) {
                return this.nodeLabelCursor.nodeReference();
            } else {
                this.close();
                return -1L;
            }
        }

        void closeResources(Statement statement) {
            IOUtils.closeAllSilently(new AutoCloseable[]{statement, this.nodeLabelCursor, this.nodeCursor, this.propertyCursor});
        }

        private boolean hasPropertiesWithValues() {
            int targetCount = this.queries.length;
            this.read.singleNode(this.nodeLabelCursor.nodeReference(), this.nodeCursor);
            if (this.nodeCursor.next()) {
                this.nodeCursor.properties(this.propertyCursor);

                while(this.propertyCursor.next()) {
                    IndexQuery[] var2 = this.queries;
                    int var3 = var2.length;

                    for(int var4 = 0; var4 < var3; ++var4) {
                        IndexQuery query = var2[var4];
                        if (this.propertyCursor.propertyKey() == query.propertyKeyId()) {
                            if (!query.acceptsValueAt(this.propertyCursor)) {
                                return false;
                            }

                            --targetCount;
                            if (targetCount == 0) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }

    public interface SPI {
        boolean databaseIsAvailable(long var1);

        DependencyResolver resolver();

        StoreId storeId();

        DatabaseLayout databaseLayout();

        String name();

        void shutdown();

        KernelTransaction beginTransaction(Type var1, LoginContext var2, long var3);

        Result executeQuery(String var1, MapValue var2, TransactionalContext var3);

        AutoIndexing autoIndexing();

        void registerKernelEventHandler(KernelEventHandler var1);

        void unregisterKernelEventHandler(KernelEventHandler var1);

        <T> void registerTransactionEventHandler(TransactionEventHandler<T> var1);

        <T> void unregisterTransactionEventHandler(TransactionEventHandler<T> var1);

        URL validateURLAccess(URL var1) throws URLAccessValidationError;

        GraphDatabaseQueryService queryService();

        Kernel kernel();
    }
}
