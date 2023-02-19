//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.coreapi;

import java.util.Map;
import java.util.Optional;

import cn.DynamicGraph.Common.DGVersion;
import cn.DynamicGraph.graphdb.TransactionVersionCommit;
import cn.DynamicGraph.kernel.impl.store.DbVersionStore;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.KernelTransaction.Revertable;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.exceptions.Status.Transaction;
//import scala.Function2;

public class PlaceboTransaction implements InternalTransaction {
    private static final PropertyContainerLocker locker = new PropertyContainerLocker();
    private final KernelTransaction currentTransaction;
    private boolean success;
    //private Function2<Long,Boolean,Boolean> func;
    private TransactionVersionCommit store;
    public PlaceboTransaction(KernelTransaction currentTransaction) {
        this.currentTransaction = currentTransaction;
    }
    public PlaceboTransaction(long version,KernelTransaction currentTransaction) {

        this.currentTransaction = currentTransaction;
        this.currentTransaction.setVersion(version);
    }

 /*   @Override
    public void setFunction(Function2<Long,Boolean,Boolean> func) {
        this.func = func;
    }
*/
    @Override
    public void setVersionstore(TransactionVersionCommit store) {
        this.store = store;
    }

    @Override
    public void setVersion(long version) {
        this.currentTransaction.setVersion(version);
    }

    @Override
    public long getVersion() {
        return this.currentTransaction.getVersion();

    }

    public void terminate() {
        this.currentTransaction.markForTermination(Transaction.Terminated);
    }

    public void failure() {
        this.currentTransaction.failure();
    }

    public void success() {
        this.success = true;
    }

    public void close() {
        if (!this.success) {
            this.currentTransaction.failure();
        }
        if(this.store != null) {
            this.store.transactionCommit(DGVersion.getStartVersion(this.currentTransaction.getVersion()), true);
        }
    }

    public Lock acquireWriteLock(PropertyContainer entity) {
        return locker.exclusiveLock(this.currentTransaction, entity);
    }

    public Lock acquireReadLock(PropertyContainer entity) {
        return locker.sharedLock(this.currentTransaction, entity);
    }

    public Type transactionType() {
        return this.currentTransaction.transactionType();
    }

    public SecurityContext securityContext() {
        return this.currentTransaction.securityContext();
    }

    public Revertable overrideWith(SecurityContext context) {
        return this.currentTransaction.overrideWith(context);
    }

    public Optional<Status> terminationReason() {
        return this.currentTransaction.getReasonIfTerminated();
    }

    public void setMetaData(Map<String, Object> txMeta) {
        this.currentTransaction.setMetaData(txMeta);
    }
}
