package cn.DynamicGraph.graphdb;

public interface TransactionVersionCommit {

    void transactionCommit(long version, boolean isSuccess);
}
