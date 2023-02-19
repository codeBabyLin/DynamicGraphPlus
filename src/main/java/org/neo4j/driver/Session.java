//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver;

import org.neo4j.driver.util.Resource;

import java.util.Map;

public interface Session extends Resource, StatementRunner {
    Transaction beginTransaction();

    Transaction beginTransaction(TransactionConfig var1);

    <T> T readTransaction(TransactionWork<T> var1);

    <T> T readTransaction(TransactionWork<T> var1, TransactionConfig var2);

    <T> T writeTransaction(TransactionWork<T> var1);

    <T> T writeTransaction(TransactionWork<T> var1, TransactionConfig var2);

    StatementResult run(String var1, TransactionConfig var2);

    StatementResult run(String var1, Map<String, Object> var2, TransactionConfig var3);

    StatementResult run(Statement var1, TransactionConfig var2);

    String lastBookmark();

    /** @deprecated */
    @Deprecated
    void reset();

    void close();
}
