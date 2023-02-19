//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal;

import org.neo4j.driver.Statement;
import org.neo4j.driver.StatementResult;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.async.StatementResultCursor;
import org.neo4j.driver.internal.async.ExplicitTransaction;
import org.neo4j.driver.internal.util.Futures;

public class InternalTransaction extends AbstractStatementRunner implements Transaction {
    private final ExplicitTransaction tx;

    public InternalTransaction(ExplicitTransaction tx) {
        this.tx = tx;
    }

    public void success() {
        this.tx.success();
    }

    public void failure() {
        this.tx.failure();
    }

    public void close() {
        Futures.blockingGet(this.tx.closeAsync(), () -> {
            this.terminateConnectionOnThreadInterrupt("Thread interrupted while closing the transaction");
        });
    }

    public StatementResult run(Statement statement) {
        StatementResultCursor cursor = (StatementResultCursor)Futures.blockingGet(this.tx.runAsync(statement, false), () -> {
            this.terminateConnectionOnThreadInterrupt("Thread interrupted while running query in transaction");
        });
        return new InternalStatementResult(this.tx.connection(), cursor);
    }

    @Override
    public StatementResult run(Statement statement, long version) {
        statement.setVersion(version);
        return this.run(statement);
    }

    public boolean isOpen() {
        return this.tx.isOpen();
    }

    private void terminateConnectionOnThreadInterrupt(String reason) {
        this.tx.connection().terminateAndRelease(reason);
    }
}
