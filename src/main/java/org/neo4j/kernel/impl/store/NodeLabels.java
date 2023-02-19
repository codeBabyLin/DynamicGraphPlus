//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.util.Collection;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
//import scala.collection.mutable.Map;

public interface NodeLabels {
    long[] get(NodeStore var1);

    long[] getIfLoaded();

    Collection<DynamicRecord> put(long[] var1, NodeStore var2, DynamicRecordAllocator var3);

    Collection<DynamicRecord> add(long var1, NodeStore var3, DynamicRecordAllocator var4);

    Collection<DynamicRecord> remove(long var1, NodeStore var3);

    //DynamicGraph
    //******************************************************************************


    //Collection<DynamicRecord> put(long[] var1, NodeStore var2, DynamicRecordAllocator var3, Map<Long, Long> versionMap);

    Collection<DynamicRecord> add(long var1, NodeStore var3, DynamicRecordAllocator var4, long version);

    Collection<DynamicRecord> remove(long var1, NodeStore var3, long version);

    //DynamicGraph
    //******************************************************************************


    boolean isInlined();
}
