//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.coreapi;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import cn.DynamicGraph.Common.DGVersion;
import cn.DynamicGraph.graphdb.TransactionVersionCommit;
import cn.DynamicGraph.kernel.impl.store.DbVersionStore;
import javafx.util.Pair;
import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.graphdb.TransientFailureException;
import org.neo4j.graphdb.TransientTransactionFailureException;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.KernelTransaction.Revertable;
import org.neo4j.kernel.api.exceptions.ConstraintViolationTransactionFailureException;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.exceptions.Status.Classification;
import org.neo4j.kernel.api.exceptions.Status.Code;
import org.neo4j.kernel.api.exceptions.Status.HasStatus;
import org.neo4j.kernel.api.exceptions.Status.Transaction;
//import scala.Function2;

public class TopLevelTransaction implements InternalTransaction {
    private static final PropertyContainerLocker locker = new PropertyContainerLocker();
    private boolean successCalled;
    private final KernelTransaction transaction;
    //private Function2<Long,Boolean,Boolean> func;
    private TransactionVersionCommit store;
    public TopLevelTransaction(KernelTransaction transaction) {
        this.transaction = transaction;
    }
    public TopLevelTransaction(long version,KernelTransaction transaction) {
        this.transaction = transaction;
        this.transaction.setVersion(version);
    }

    public void failure() {
        this.transaction.failure();
    }

    public void success() {
        this.successCalled = true;
        this.transaction.success();
    }
/*

    @Override
    public void setFunction(Function2<Long,Boolean,Boolean> func) {
        this.func = func;
    }

    @Override
    public void setVersionstore(DbVersionStore store) {
        this.store = store;
    }
*/

    @Override
    public void setVersionstore(TransactionVersionCommit store) {
        this.store = store;
    }

    @Override
    public void setVersion(long version) {
        this.transaction.setVersion(version);
    }

    @Override
    public long getVersion() {
        return this.transaction.getVersion();
    }

    public final void terminate() {
        this.transaction.markForTermination(Transaction.Terminated);
    }

    public void close() {
        try {
            if (this.transaction.isOpen()) {
                this.transaction.close();
                if(this.store != null) {
                    this.store.transactionCommit(DGVersion.getStartVersion(this.transaction.getVersion()),true);
                }
            }

        } catch (TransientFailureException var3) {
            throw var3;
        } catch (ConstraintViolationTransactionFailureException var4) {
            throw new ConstraintViolationException(var4.getMessage(), var4);
        } catch (TransactionTerminatedException | KernelException var5) {
            Code statusCode = ((HasStatus)var5).status().code();
            if (statusCode.classification() == Classification.TransientError) {
                throw new TransientTransactionFailureException(this.closeFailureMessage() + ": " + statusCode.description(), var5);
            } else {
                throw new TransactionFailureException(this.closeFailureMessage(), var5);
            }
        } catch (Exception var6) {
            throw new TransactionFailureException(this.closeFailureMessage(), var6);
        }
    }

    private String closeFailureMessage() {
        return this.successCalled ? "Transaction was marked as successful, but unable to commit transaction so rolled back." : "Unable to rollback transaction";
    }

    public Lock acquireWriteLock(PropertyContainer entity) {
        return locker.exclusiveLock(this.transaction, entity);
    }

    public Lock acquireReadLock(PropertyContainer entity) {
        return locker.sharedLock(this.transaction, entity);
    }

    public Type transactionType() {
        return this.transaction.transactionType();
    }

    public SecurityContext securityContext() {
        return this.transaction.securityContext();
    }

    public Revertable overrideWith(SecurityContext context) {
        return this.transaction.overrideWith(context);
    }

    public Optional<Status> terminationReason() {
        return this.transaction.getReasonIfTerminated();
    }

    public void setMetaData(Map<String, Object> txMeta) {
        this.transaction.setMetaData(txMeta);
    }
}
