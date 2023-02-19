//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.graphdb;

import cn.DynamicGraph.graphdb.TransactionVersionCommit;
//import cn.DynamicGraph.kernel.impl.store.DbVersionStore;
//import scala.Function2;

public interface Transaction extends AutoCloseable {

    //DynamicGraph
    //****************************************************
   // void setFunction(Function2<Long, Boolean, Boolean> func);
    void setVersionstore(TransactionVersionCommit store);
    void setVersion(long version);
    long getVersion();
    //DynamicGraph
    //****************************************************


    void terminate();

    void failure();

    void success();

    void close();

    Lock acquireWriteLock(PropertyContainer var1);

    Lock acquireReadLock(PropertyContainer var1);
}
