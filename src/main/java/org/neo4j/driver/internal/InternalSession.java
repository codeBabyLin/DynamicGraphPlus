//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal;

import org.neo4j.driver.*;
import org.neo4j.driver.async.StatementResultCursor;
import org.neo4j.driver.internal.async.ExplicitTransaction;
import org.neo4j.driver.internal.async.NetworkSession;
import org.neo4j.driver.internal.spi.Connection;
import org.neo4j.driver.internal.util.Futures;

import java.util.Collections;
import java.util.Map;

public class InternalSession extends AbstractStatementRunner implements Session {
    private final NetworkSession session;

    public InternalSession(NetworkSession session) {
        this.session = session;
    }

    public StatementResult run(Statement statement) {
        return this.run(statement, TransactionConfig.empty());
    }

    @Override
    public StatementResult run(Statement statement, long version) {
        statement.setVersion(version);
        return this.run(statement);
    }

    public StatementResult run(String statement, TransactionConfig config) {
        return this.run(statement, Collections.emptyMap(), config);
    }

    public StatementResult run(String statement, Map<String, Object> parameters, TransactionConfig config) {
        return this.run(new Statement(statement, parameters), config);
    }

    public StatementResult run(Statement statement, TransactionConfig config) {
        StatementResultCursor cursor = (StatementResultCursor)Futures.blockingGet(this.session.runAsync(statement, config, false), () -> {
            this.terminateConnectionOnThreadInterrupt("Thread interrupted while running query in session");
        });
        Connection connection = (Connection)Futures.getNow(this.session.connectionAsync());
        return new InternalStatementResult(connection, cursor);
    }

    public boolean isOpen() {
        return this.session.isOpen();
    }

    public void close() {
        Futures.blockingGet(this.session.closeAsync(), () -> {
            this.terminateConnectionOnThreadInterrupt("Thread interrupted while closing the session");
        });
    }

    public Transaction beginTransaction() {
        return this.beginTransaction(TransactionConfig.empty());
    }

    public Transaction beginTransaction(TransactionConfig config) {
        ExplicitTransaction tx = (ExplicitTransaction)Futures.blockingGet(this.session.beginTransactionAsync(config), () -> {
            this.terminateConnectionOnThreadInterrupt("Thread interrupted while starting a transaction");
        });
        return new InternalTransaction(tx);
    }

    public <T> T readTransaction(TransactionWork<T> work) {
        return this.readTransaction(work, TransactionConfig.empty());
    }

    public <T> T readTransaction(TransactionWork<T> work, TransactionConfig config) {
        return this.transaction(AccessMode.READ, work, config);
    }

    public <T> T writeTransaction(TransactionWork<T> work) {
        return this.writeTransaction(work, TransactionConfig.empty());
    }

    public <T> T writeTransaction(TransactionWork<T> work, TransactionConfig config) {
        return this.transaction(AccessMode.WRITE, work, config);
    }

    public String lastBookmark() {
        return this.session.lastBookmark();
    }

    public void reset() {
        Futures.blockingGet(this.session.resetAsync(), () -> {
            this.terminateConnectionOnThreadInterrupt("Thread interrupted while resetting the session");
        });
    }

    private <T> T transaction(AccessMode mode, TransactionWork<T> work, TransactionConfig config) {

         return this.session.retryLogic().retry(() -> {
            Transaction tx = this.beginTransaction(mode, config);
            Throwable var5 = null;
            T var7;
            try {
                try {
                    T result = work.execute(tx);
                    tx.success();
                    var7 = result;
                } catch (Throwable var17) {
                    tx.failure();
                    throw var17;
                }
            } catch (Throwable var18) {
                var5 = var18;
                throw var18;
            } finally {
                if (tx != null) {
                    if (var5 != null) {
                        try {
                            tx.close();
                        } catch (Throwable var16) {
                            var5.addSuppressed(var16);
                        }
                    } else {
                        tx.close();
                    }
                }

            }

            return var7;
        });
    }

    private Transaction beginTransaction(AccessMode mode, TransactionConfig config) {
        ExplicitTransaction tx = (ExplicitTransaction)Futures.blockingGet(this.session.beginTransactionAsync(mode, config), () -> {
            this.terminateConnectionOnThreadInterrupt("Thread interrupted while starting a transaction");
        });
        return new InternalTransaction(tx);
    }

    private void terminateConnectionOnThreadInterrupt(String reason) {
        Connection connection = null;

        try {
            connection = (Connection)Futures.getNow(this.session.connectionAsync());
        } catch (Throwable var4) {
        }

        if (connection != null) {
            connection.terminateAndRelease(reason);
        }

    }
}
