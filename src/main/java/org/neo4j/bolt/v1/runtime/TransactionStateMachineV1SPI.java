//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v1.runtime;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.neo4j.bolt.BoltChannel;
import org.neo4j.bolt.runtime.BoltResult;
import org.neo4j.bolt.runtime.BoltResultHandle;
import org.neo4j.bolt.runtime.TransactionStateMachineSPI;
import org.neo4j.cypher.internal.javacompat.QueryResultProvider;
import org.neo4j.graphdb.Result;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.kernel.GraphDatabaseQueryService;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.txtracking.TransactionIdTracker;
import org.neo4j.kernel.availability.AvailabilityGuard;
import org.neo4j.kernel.availability.DatabaseAvailabilityGuard;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.kernel.impl.coreapi.PropertyContainerLocker;
import org.neo4j.kernel.impl.query.Neo4jTransactionalContextFactory;
import org.neo4j.kernel.impl.query.QueryExecutionEngine;
import org.neo4j.kernel.impl.query.QueryExecutionKernelException;
import org.neo4j.kernel.impl.query.TransactionalContext;
import org.neo4j.kernel.impl.query.TransactionalContextFactory;
import org.neo4j.kernel.impl.transaction.log.TransactionIdStore;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.values.virtual.MapValue;

public class TransactionStateMachineV1SPI implements TransactionStateMachineSPI {
    private static final PropertyContainerLocker locker = new PropertyContainerLocker();
    private final GraphDatabaseAPI db;
    private final BoltChannel boltChannel;
    private final ThreadToStatementContextBridge txBridge;
    private final QueryExecutionEngine queryExecutionEngine;
    private final TransactionIdTracker transactionIdTracker;
    private final TransactionalContextFactory contextFactory;
    private final Duration txAwaitDuration;
    private final Clock clock;

    public TransactionStateMachineV1SPI(GraphDatabaseAPI db, BoltChannel boltChannel, Duration txAwaitDuration, Clock clock) {
        this.db = db;
        this.boltChannel = boltChannel;
        this.txBridge = (ThreadToStatementContextBridge)resolveDependency(db, ThreadToStatementContextBridge.class);
        this.queryExecutionEngine = (QueryExecutionEngine)resolveDependency(db, QueryExecutionEngine.class);
        this.transactionIdTracker = newTransactionIdTracker(db);
        this.contextFactory = newTransactionalContextFactory(db);
        this.txAwaitDuration = txAwaitDuration;
        this.clock = clock;
    }

    public void awaitUpToDate(long oldestAcceptableTxId) throws TransactionFailureException {
        this.transactionIdTracker.awaitUpToDate(oldestAcceptableTxId, this.txAwaitDuration);
    }

    public long newestEncounteredTxId() {
        return this.transactionIdTracker.newestEncounteredTxId();
    }

    public KernelTransaction beginTransaction(LoginContext loginContext, Duration txTimeout, Map<String, Object> txMetadata) {
        this.beginTransaction(Type.explicit, loginContext, txTimeout, txMetadata);
        return this.txBridge.getKernelTransactionBoundToThisThread(false);
    }

    public void bindTransactionToCurrentThread(KernelTransaction tx) {
        this.txBridge.bindTransactionToCurrentThread(tx);
    }

    public void unbindTransactionFromCurrentThread() {
        this.txBridge.unbindTransactionFromCurrentThread();
    }

    public boolean isPeriodicCommit(String query) {
        return this.queryExecutionEngine.isPeriodicCommit(query);
    }

    public BoltResultHandle executeQuery(LoginContext loginContext, String statement, MapValue params, Duration txTimeout, Map<String, Object> txMetadata) {
        InternalTransaction internalTransaction = this.beginTransaction(Type.implicit, loginContext, txTimeout, txMetadata);
        TransactionalContext transactionalContext = this.contextFactory.newContext(this.boltChannel.info(), internalTransaction, statement, params);
        return this.newBoltResultHandle(statement, params, transactionalContext);
    }

    @Override
    public BoltResultHandle executeQuery(LoginContext loginContext, String statement, MapValue params, Duration txTimeout, Map<String, Object> txMetadata, long version) {
        InternalTransaction internalTransaction = this.beginTransaction(Type.implicit, loginContext, txTimeout, txMetadata,version);
        TransactionalContext transactionalContext = this.contextFactory.newContext(this.boltChannel.info(), internalTransaction, statement, params);
        return this.newBoltResultHandle(statement, params, transactionalContext);
    }

    protected BoltResultHandle newBoltResultHandle(String statement, MapValue params, TransactionalContext transactionalContext) {
        return new BoltResultHandleV1(statement, params, transactionalContext);
    }

    private InternalTransaction beginTransaction(Type type, LoginContext loginContext, Duration txTimeout, Map<String, Object> txMetadata) {
        InternalTransaction tx;
        if (txTimeout == null) {
            tx = this.db.beginTransaction(type, loginContext);
        } else {
            tx = this.db.beginTransaction(type, loginContext, txTimeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        if (txMetadata != null) {
            tx.setMetaData(txMetadata);
        }

        return tx;
    }
    private InternalTransaction beginTransaction(Type type, LoginContext loginContext, Duration txTimeout, Map<String, Object> txMetadata,long version) {
        InternalTransaction tx;
        if (txTimeout == null) {
            tx = this.db.beginTransaction(type, loginContext,version);
        } else {
            tx = this.db.beginTransaction(type, loginContext, txTimeout.toMillis(), TimeUnit.MILLISECONDS,version);
        }

        if (txMetadata != null) {
            tx.setMetaData(txMetadata);
        }

        return tx;
    }


    private static TransactionIdTracker newTransactionIdTracker(GraphDatabaseAPI db) {
        Supplier<TransactionIdStore> transactionIdStoreSupplier = db.getDependencyResolver().provideDependency(TransactionIdStore.class);
        AvailabilityGuard guard = (AvailabilityGuard)resolveDependency(db, DatabaseAvailabilityGuard.class);
        return new TransactionIdTracker(transactionIdStoreSupplier, guard);
    }

    private static TransactionalContextFactory newTransactionalContextFactory(GraphDatabaseAPI db) {
        GraphDatabaseQueryService queryService = (GraphDatabaseQueryService)resolveDependency(db, GraphDatabaseQueryService.class);
        return Neo4jTransactionalContextFactory.create(queryService, locker);
    }

    private static <T> T resolveDependency(GraphDatabaseAPI db, Class<T> clazz) {
        return db.getDependencyResolver().resolveDependency(clazz);
    }

    public class BoltResultHandleV1 implements BoltResultHandle {
        private final String statement;
        private final MapValue params;
        private final TransactionalContext transactionalContext;

        public BoltResultHandleV1(String statement, MapValue params, TransactionalContext transactionalContext) {
            this.statement = statement;
            this.params = params;
            this.transactionalContext = transactionalContext;
        }

        public BoltResult start() throws KernelException {
            try {
                Result result = TransactionStateMachineV1SPI.this.queryExecutionEngine.executeQuery(this.statement, this.params, this.transactionalContext);
                if (result instanceof QueryResultProvider) {
                    return this.newBoltResult((QueryResultProvider)result, TransactionStateMachineV1SPI.this.clock);
                } else {
                    throw new IllegalStateException(String.format("Unexpected query execution result. Expected to get instance of %s but was %s.", QueryResultProvider.class.getName(), result.getClass().getName()));
                }
            } catch (KernelException var2) {
                this.close(false);
                throw new QueryExecutionKernelException(var2);
            } catch (Throwable var3) {
                this.close(false);
                throw var3;
            }
        }

        protected BoltResult newBoltResult(QueryResultProvider result, Clock clock) {
            return new CypherAdapterStream(result.queryResult(), clock);
        }

        public void close(boolean success) {
            this.transactionalContext.close(success);
        }

        public void terminate() {
            this.transactionalContext.terminate();
        }
    }
}
