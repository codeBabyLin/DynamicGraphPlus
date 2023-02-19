//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v1.runtime;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.neo4j.bolt.runtime.BoltResult;
import org.neo4j.bolt.runtime.BoltResultHandle;
import org.neo4j.bolt.runtime.StatementMetadata;
import org.neo4j.bolt.runtime.StatementProcessor;
import org.neo4j.bolt.runtime.TransactionStateMachineSPI;
import org.neo4j.bolt.security.auth.AuthenticationResult;
import org.neo4j.bolt.v1.runtime.bookmarking.Bookmark;
import org.neo4j.bolt.v1.runtime.spi.BookmarkResult;
import org.neo4j.cypher.InvalidSemanticsException;
import org.neo4j.function.ThrowingConsumer;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.exceptions.Status.Transaction;
import org.neo4j.kernel.impl.query.QueryExecutionKernelException;
import org.neo4j.util.Preconditions;
import org.neo4j.values.virtual.MapValue;

public class TransactionStateMachine implements StatementProcessor {
    final TransactionStateMachineSPI spi;
    final MutableTransactionState ctx;
    State state;

    TransactionStateMachine(TransactionStateMachineSPI spi, AuthenticationResult authenticationResult, Clock clock) {
        this.state = State.AUTO_COMMIT;
        this.spi = spi;
        this.ctx = new MutableTransactionState(authenticationResult, clock);
    }

    public State state() {
        return this.state;
    }

    private void before() {
        if (this.ctx.currentTransaction != null) {
            this.spi.bindTransactionToCurrentThread(this.ctx.currentTransaction);
        }

    }

    public void beginTransaction(Bookmark bookmark) throws KernelException {
        this.beginTransaction(bookmark, (Duration)null, (Map)null);
    }

    public void beginTransaction(Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetadata) throws KernelException {
        this.before();

        try {
            this.ensureNoPendingTerminationNotice();
            this.state = this.state.beginTransaction(this.ctx, this.spi, bookmark, txTimeout, txMetadata);
        } finally {
            this.after();
        }

    }

    public StatementMetadata run(String statement, MapValue params) throws KernelException {
        return this.run(statement, params, (Bookmark)null, (Duration)null, (Map)null);
    }

    @Override
    public StatementMetadata run(String statement, MapValue params, long version) throws KernelException {
        return this.run(statement, params, (Bookmark)null, (Duration)null, (Map)null,version);
    }

    public StatementMetadata run(String statement, MapValue params, Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetaData) throws KernelException {
        this.before();

        StatementMetadata var6;
        try {
            this.ensureNoPendingTerminationNotice();
            this.state = this.state.run(this.ctx, this.spi, statement, params, bookmark, txTimeout, txMetaData);
            var6 = this.ctx.currentStatementMetadata;
        } finally {
            this.after();
        }

        return var6;
    }

    @Override
    public StatementMetadata run(String statement, MapValue params, Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetaData, long version) throws KernelException {
        this.before();

        StatementMetadata var6;
        try {
            this.ensureNoPendingTerminationNotice();
            //this.state = this.state.run(this.ctx, this.spi, statement, params, bookmark, txTimeout, txMetaData);
            this.state = this.state.run(this.ctx, this.spi, statement, params, bookmark, txTimeout, txMetaData,version);
            var6 = this.ctx.currentStatementMetadata;
        } finally {
            this.after();
        }

        return var6;
    }

    public Bookmark streamResult(ThrowingConsumer<BoltResult, Exception> resultConsumer) throws Exception {
        this.before();

        Bookmark var2;
        try {
            this.ensureNoPendingTerminationNotice();
            var2 = this.state.streamResult(this.ctx, this.spi, resultConsumer);
        } finally {
            this.after();
        }

        return var2;
    }

    public Bookmark commitTransaction() throws KernelException {
        this.before();

        Bookmark var1;
        try {
            this.ensureNoPendingTerminationNotice();
            this.state = this.state.commitTransaction(this.ctx, this.spi);
            var1 = newestBookmark(this.spi);
        } catch (TransactionFailureException var5) {
            this.state = State.AUTO_COMMIT;
            throw var5;
        } finally {
            this.after();
        }

        return var1;
    }

    public void rollbackTransaction() throws KernelException {
        this.before();

        try {
            this.ensureNoPendingTerminationNotice();
            this.state = this.state.rollbackTransaction(this.ctx, this.spi);
        } finally {
            this.after();
        }

    }

    public boolean hasOpenStatement() {
        return this.ctx.currentResultHandle != null;
    }

    public void reset() throws TransactionFailureException {
        this.state.terminateQueryAndRollbackTransaction(this.ctx);
        this.state = State.AUTO_COMMIT;
    }

    private void after() {
        this.spi.unbindTransactionFromCurrentThread();
    }

    public void markCurrentTransactionForTermination() {
        KernelTransaction tx = this.ctx.currentTransaction;
        if (tx != null) {
            tx.markForTermination(Transaction.Terminated);
        }

    }

    public void validateTransaction() throws KernelException {
        KernelTransaction tx = this.ctx.currentTransaction;
        if (tx != null) {
            Optional<Status> statusOpt = tx.getReasonIfTerminated();
            if (statusOpt.isPresent() && ((Status)statusOpt.get()).code().classification().rollbackTransaction()) {
                this.ctx.pendingTerminationNotice = (Status)statusOpt.get();
                this.reset();
            }
        }

    }

    private void ensureNoPendingTerminationNotice() {
        if (this.ctx.pendingTerminationNotice != null) {
            Status status = this.ctx.pendingTerminationNotice;
            this.ctx.pendingTerminationNotice = null;
            throw new TransactionTerminatedException(status);
        }
    }

    public boolean hasTransaction() {
        return this.state == State.EXPLICIT_TRANSACTION;
    }

    private static void waitForBookmark(TransactionStateMachineSPI spi, Bookmark bookmark) throws TransactionFailureException {
        if (bookmark != null) {
            spi.awaitUpToDate(bookmark.txId());
        }

    }

    private static Bookmark newestBookmark(TransactionStateMachineSPI spi) {
        long txId = spi.newestEncounteredTxId();
        return new Bookmark(txId);
    }

    static class MutableTransactionState {
        final LoginContext loginContext;
        KernelTransaction currentTransaction;
        Status pendingTerminationNotice;
        String lastStatement;
        BoltResult currentResult;
        BoltResultHandle currentResultHandle;
        final Clock clock;
        private final StatementMetadata currentStatementMetadata;

        private MutableTransactionState(AuthenticationResult authenticationResult, Clock clock) {
            this.lastStatement = "";
            this.currentStatementMetadata = new StatementMetadata() {
                public String[] fieldNames() {
                    return MutableTransactionState.this.currentResult.fieldNames();
                }
            };
            this.clock = clock;
            this.loginContext = authenticationResult.getLoginContext();
        }
    }

    static enum State {
        AUTO_COMMIT {
            State beginTransaction(MutableTransactionState ctx, TransactionStateMachineSPI spi, Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetadata) throws KernelException {
                TransactionStateMachine.waitForBookmark(spi, bookmark);
                ctx.currentResult = BoltResult.EMPTY;
                ctx.currentTransaction = spi.beginTransaction(ctx.loginContext, txTimeout, txMetadata);
                return EXPLICIT_TRANSACTION;
            }

            State run(MutableTransactionState ctx, TransactionStateMachineSPI spi, String statement, MapValue params, Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetadata) throws KernelException {
                statement = this.parseStatement(ctx, statement);
                TransactionStateMachine.waitForBookmark(spi, bookmark);
                this.execute(ctx, spi, statement, params, spi.isPeriodicCommit(statement), txTimeout, txMetadata);
                return AUTO_COMMIT;
            }

            @Override
            State run(MutableTransactionState ctx, TransactionStateMachineSPI spi, String statement, MapValue params, Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetadata, long version) throws KernelException {
                statement = this.parseStatement(ctx, statement);
                TransactionStateMachine.waitForBookmark(spi, bookmark);
                this.execute(ctx, spi, statement, params, spi.isPeriodicCommit(statement), txTimeout, txMetadata,version);
                return AUTO_COMMIT;
            }

            private String parseStatement(MutableTransactionState ctx, String statement) {
                if (statement.isEmpty()) {
                    statement = ctx.lastStatement;
                } else {
                    ctx.lastStatement = statement;
                }

                return statement;
            }

            void execute(MutableTransactionState ctx, TransactionStateMachineSPI spi, String statement, MapValue params, boolean isPeriodicCommit, Duration txTimeout, Map<String, Object> txMetadata) throws KernelException {
                if (!isPeriodicCommit) {
                    ctx.currentTransaction = spi.beginTransaction(ctx.loginContext, txTimeout, txMetadata);
                }

                boolean failed = true;

                try {
                    BoltResultHandle resultHandle = spi.executeQuery(ctx.loginContext, statement, params, txTimeout, txMetadata);
                    this.startExecution(ctx, resultHandle);
                    failed = false;
                } finally {
                    if (!isPeriodicCommit) {
                        if (failed) {
                            this.closeTransaction(ctx, false);
                        }
                    } else {
                        ctx.currentTransaction = spi.beginTransaction(ctx.loginContext, txTimeout, txMetadata);
                    }

                }

            }
            void execute(MutableTransactionState ctx, TransactionStateMachineSPI spi, String statement, MapValue params, boolean isPeriodicCommit, Duration txTimeout, Map<String, Object> txMetadata, long version) throws KernelException {
                if (!isPeriodicCommit) {
                    ctx.currentTransaction = spi.beginTransaction(ctx.loginContext, txTimeout, txMetadata);
                }

                boolean failed = true;

                try {
                    BoltResultHandle resultHandle = spi.executeQuery(ctx.loginContext, statement, params, txTimeout, txMetadata,version);
                    this.startExecution(ctx, resultHandle);
                    failed = false;
                } finally {
                    if (!isPeriodicCommit) {
                        if (failed) {
                            this.closeTransaction(ctx, false);
                        }
                    } else {
                        ctx.currentTransaction = spi.beginTransaction(ctx.loginContext, txTimeout, txMetadata);
                    }

                }

            }

            Bookmark streamResult(MutableTransactionState ctx, TransactionStateMachineSPI spi, ThrowingConsumer<BoltResult, Exception> resultConsumer) throws Exception {
                assert ctx.currentResult != null;

                Bookmark var4;
                try {
                    this.consumeResult(ctx, resultConsumer);
                    this.closeTransaction(ctx, true);
                    var4 = TransactionStateMachine.newestBookmark(spi);
                } finally {
                    this.closeTransaction(ctx, false);
                }

                return var4;
            }

            State commitTransaction(MutableTransactionState ctx, TransactionStateMachineSPI spi) throws KernelException {
                throw new QueryExecutionKernelException(new InvalidSemanticsException("No current transaction to commit."));
            }

            State rollbackTransaction(MutableTransactionState ctx, TransactionStateMachineSPI spi) {
                ctx.currentResult = BoltResult.EMPTY;
                return AUTO_COMMIT;
            }
        },
        EXPLICIT_TRANSACTION {
            State beginTransaction(MutableTransactionState ctx, TransactionStateMachineSPI spi, Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetadata) throws KernelException {
                throw new QueryExecutionKernelException(new InvalidSemanticsException("Nested transactions are not supported."));
            }

            State run(MutableTransactionState ctx, TransactionStateMachineSPI spi, String statement, MapValue params, Bookmark bookmark, Duration ignored1, Map<String, Object> ignored2) throws KernelException {
                Preconditions.checkState(ignored1 == null, "Explicit Transaction should not run with tx_timeout");
                Preconditions.checkState(ignored2 == null, "Explicit Transaction should not run with tx_metadata");
                if (statement.isEmpty()) {
                    statement = ctx.lastStatement;
                } else {
                    ctx.lastStatement = statement;
                }

                if (spi.isPeriodicCommit(statement)) {
                    throw new QueryExecutionKernelException(new InvalidSemanticsException("Executing queries that use periodic commit in an open transaction is not possible."));
                } else {
                    BoltResultHandle resultHandle = spi.executeQuery(ctx.loginContext, statement, params, (Duration)null, (Map)null);
                    this.startExecution(ctx, resultHandle);
                    return EXPLICIT_TRANSACTION;
                }
            }

            @Override
            State run(MutableTransactionState ctx, TransactionStateMachineSPI spi, String statement, MapValue params, Bookmark bookmark, Duration ignored1, Map<String, Object> ignored2, long version) throws KernelException {
                Preconditions.checkState(ignored1 == null, "Explicit Transaction should not run with tx_timeout");
                Preconditions.checkState(ignored2 == null, "Explicit Transaction should not run with tx_metadata");
                if (statement.isEmpty()) {
                    statement = ctx.lastStatement;
                } else {
                    ctx.lastStatement = statement;
                }

                if (spi.isPeriodicCommit(statement)) {
                    throw new QueryExecutionKernelException(new InvalidSemanticsException("Executing queries that use periodic commit in an open transaction is not possible."));
                } else {
                    BoltResultHandle resultHandle = spi.executeQuery(ctx.loginContext, statement, params, (Duration)null, (Map)null,version);
                    this.startExecution(ctx, resultHandle);
                    return EXPLICIT_TRANSACTION;
                }
            }

            Bookmark streamResult(MutableTransactionState ctx, TransactionStateMachineSPI spi, ThrowingConsumer<BoltResult, Exception> resultConsumer) throws Exception {
                assert ctx.currentResult != null;

                this.consumeResult(ctx, resultConsumer);
                return null;
            }

            State commitTransaction(MutableTransactionState ctx, TransactionStateMachineSPI spi) throws KernelException {
                this.closeTransaction(ctx, true);
                Bookmark bookmark = TransactionStateMachine.newestBookmark(spi);
                ctx.currentResult = new BookmarkResult(bookmark);
                return AUTO_COMMIT;
            }

            State rollbackTransaction(MutableTransactionState ctx, TransactionStateMachineSPI spi) throws KernelException {
                this.closeTransaction(ctx, false);
                ctx.currentResult = BoltResult.EMPTY;
                return AUTO_COMMIT;
            }
        };

        private State() {
        }

        abstract State beginTransaction(MutableTransactionState var1, TransactionStateMachineSPI var2, Bookmark var3, Duration var4, Map<String, Object> var5) throws KernelException;

        abstract State run(MutableTransactionState var1, TransactionStateMachineSPI var2, String var3, MapValue var4, Bookmark var5, Duration var6, Map<String, Object> var7) throws KernelException;
        abstract State run(MutableTransactionState var1, TransactionStateMachineSPI var2, String var3, MapValue var4, Bookmark var5, Duration var6, Map<String, Object> var7, long version) throws KernelException;

        abstract Bookmark streamResult(MutableTransactionState var1, TransactionStateMachineSPI var2, ThrowingConsumer<BoltResult, Exception> var3) throws Exception;

        abstract State commitTransaction(MutableTransactionState var1, TransactionStateMachineSPI var2) throws KernelException;

        abstract State rollbackTransaction(MutableTransactionState var1, TransactionStateMachineSPI var2) throws KernelException;

        void terminateQueryAndRollbackTransaction(MutableTransactionState ctx) throws TransactionFailureException {
            if (ctx.currentResultHandle != null) {
                ctx.currentResultHandle.terminate();
                ctx.currentResultHandle = null;
            }

            if (ctx.currentResult != null) {
                ctx.currentResult.close();
                ctx.currentResult = null;
            }

            this.closeTransaction(ctx, false);
        }

        void closeTransaction(MutableTransactionState ctx, boolean success) throws TransactionFailureException {
            KernelTransaction tx = ctx.currentTransaction;
            ctx.currentTransaction = null;
            if (tx != null) {
                try {
                    if (success) {
                        tx.success();
                    } else {
                        tx.failure();
                    }

                    if (tx.isOpen()) {
                        tx.close();
                    }
                } finally {
                    ctx.currentTransaction = null;
                }
            }

        }

        boolean consumeResult(MutableTransactionState ctx, ThrowingConsumer<BoltResult, Exception> resultConsumer) throws Exception {
            boolean success = false;

            try {
                resultConsumer.accept(ctx.currentResult);
                success = true;
            } finally {
                ctx.currentResult.close();
                ctx.currentResult = null;
                if (ctx.currentResultHandle != null) {
                    ctx.currentResultHandle.close(success);
                    ctx.currentResultHandle = null;
                }

            }

            return success;
        }

        void startExecution(MutableTransactionState ctx, BoltResultHandle resultHandle) throws KernelException {
            ctx.currentResultHandle = resultHandle;

            try {
                ctx.currentResult = resultHandle.start();
            } catch (Throwable var4) {
                ctx.currentResultHandle.close(false);
                ctx.currentResultHandle = null;
                throw var4;
            }
        }
    }
}
