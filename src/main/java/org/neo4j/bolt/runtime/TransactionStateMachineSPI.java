//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.runtime;

import java.time.Duration;
import java.util.Map;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.values.virtual.MapValue;

public interface TransactionStateMachineSPI {
    void awaitUpToDate(long var1) throws TransactionFailureException;

    long newestEncounteredTxId();

    KernelTransaction beginTransaction(LoginContext var1, Duration var2, Map<String, Object> var3);

    void bindTransactionToCurrentThread(KernelTransaction var1);

    void unbindTransactionFromCurrentThread();

    boolean isPeriodicCommit(String var1);

    BoltResultHandle executeQuery(LoginContext var1, String var2, MapValue var3, Duration var4, Map<String, Object> var5);
    BoltResultHandle executeQuery(LoginContext var1, String var2, MapValue var3, Duration var4, Map<String, Object> var5, long version);
}
