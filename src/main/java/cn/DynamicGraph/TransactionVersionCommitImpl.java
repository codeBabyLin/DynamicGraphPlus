package cn.DynamicGraph;

import cn.DynamicGraph.graphdb.TransactionVersionCommit;
import cn.DynamicGraph.kernel.impl.store.DbVersionStore;

public class TransactionVersionCommitImpl implements TransactionVersionCommit {
    private DbVersionStore store;
    public TransactionVersionCommitImpl(DbVersionStore store){
        this.store = store;
    }

    @Override
    public void transactionCommit(long version, boolean isSuccess) {
        this.store.transactionCommit(version,isSuccess);
    }
}
