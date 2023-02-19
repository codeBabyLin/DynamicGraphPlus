//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.storageengine.api;

import java.util.Map;

public interface StorageNodeCursor extends StorageEntityScanCursor {

    long[] labels();

    boolean hasLabel(int var1);

    long relationshipGroupReference();

    long allRelationshipsReference();

    void setCurrent(long var1);


    //Dynamicgraph
    //*************************************************

    void setCurrent(long var1, long version);
    Map<Long,Long> versionLabels();
    long[] labels(long version);

    long nodeVersion();

    //Dynamicgraph
    //*************************************************

    boolean isDense();
}
