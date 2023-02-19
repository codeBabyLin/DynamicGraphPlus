//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.runtime;

import java.time.Duration;
import java.util.Map;
import org.neo4j.bolt.v1.runtime.bookmarking.Bookmark;
import org.neo4j.function.ThrowingConsumer;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.values.virtual.MapValue;

public interface StatementProcessor {
    StatementProcessor EMPTY = new StatementProcessor() {
        public void beginTransaction(Bookmark bookmark) throws KernelException {
            throw new UnsupportedOperationException("Unable to run statements");
        }

        public void beginTransaction(Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetadata) throws KernelException {
            throw new UnsupportedOperationException("Unable to begin a transaction");
        }

        public StatementMetadata run(String statement, MapValue params) throws KernelException {
            throw new UnsupportedOperationException("Unable to run statements");
        }

        @Override
        public StatementMetadata run(String var1, MapValue var2, long version) throws KernelException {
            throw new UnsupportedOperationException("Unable to run statements");
        }

        public StatementMetadata run(String statement, MapValue params, Bookmark bookmark, Duration txTimeout, Map<String, Object> txMetaData) throws KernelException {
            throw new UnsupportedOperationException("Unable to run statements");
        }

        @Override
        public StatementMetadata run(String var1, MapValue var2, Bookmark var3, Duration var4, Map<String, Object> var5, long version) throws KernelException {
            throw new UnsupportedOperationException("Unable to run statements");
        }

        public Bookmark streamResult(ThrowingConsumer<BoltResult, Exception> resultConsumer) throws Exception {
            throw new UnsupportedOperationException("Unable to stream results");
        }

        public Bookmark commitTransaction() throws KernelException {
            throw new UnsupportedOperationException("Unable to commit a transaction");
        }

        public void rollbackTransaction() throws KernelException {
            throw new UnsupportedOperationException("Unable to rollback a transaction");
        }

        public void reset() throws TransactionFailureException {
        }

        public void markCurrentTransactionForTermination() {
        }

        public boolean hasTransaction() {
            return false;
        }

        public boolean hasOpenStatement() {
            return false;
        }

        public void validateTransaction() throws KernelException {
        }
    };

    void beginTransaction(Bookmark var1) throws KernelException;

    void beginTransaction(Bookmark var1, Duration var2, Map<String, Object> var3) throws KernelException;

    StatementMetadata run(String var1, MapValue var2) throws KernelException;
    StatementMetadata run(String var1, MapValue var2, long version) throws KernelException;

    StatementMetadata run(String var1, MapValue var2, Bookmark var3, Duration var4, Map<String, Object> var5) throws KernelException;
    StatementMetadata run(String var1, MapValue var2, Bookmark var3, Duration var4, Map<String, Object> var5, long version) throws KernelException;

    Bookmark streamResult(ThrowingConsumer<BoltResult, Exception> var1) throws Exception;

    Bookmark commitTransaction() throws KernelException;

    void rollbackTransaction() throws KernelException;

    void reset() throws TransactionFailureException;

    void markCurrentTransactionForTermination();

    boolean hasTransaction();

    boolean hasOpenStatement();

    void validateTransaction() throws KernelException;
}
