//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.api.txstate;

import org.neo4j.internal.kernel.api.schema.SchemaDescriptor;
import org.neo4j.internal.kernel.api.schema.constraints.ConstraintDescriptor;
import org.neo4j.kernel.api.schema.constraints.IndexBackedConstraintDescriptor;
import org.neo4j.storageengine.api.schema.IndexDescriptor;
import org.neo4j.storageengine.api.txstate.ReadableTransactionState;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueTuple;

public interface TransactionState extends ReadableTransactionState {

    //Dynamicgraph method
    //****************************************************************************

    void nodeDoAddProperty(long var1, int var3, Value var4, long version);

    void nodeDoChangeProperty(long var1, int var3, Value var4, long version);
    void nodeDoRemoveProperty(long var1, int var3, long version);
    void relationshipDoReplaceProperty(long var1, int var3, Value var4, Value var5, long version);
    void relationshipDoRemoveProperty(long var1, int var3, long version);


    void nodeDoChangeVersion(long var1, long var3);
    void relDoChangeVersion(long var1, long var3);



    void nodeDoAddLabel(long nodeId, long labelId, long version);
    void nodeDoRemoveLabel(long nodeId, long labelId, long version);


    void nodeDoDelete(long var1, long version);
    void nodeDoCreate(long var1, long version);
    void relationshipDoCreate(long var1, int var3, long var4, long var6, long version);
    void relationshipDoDelete(long var1, int var3, long var4, long var6, long version);
    //Dynamicgraph method
    //****************************************************************************
    void relationshipDoCreate(long var1, int var3, long var4, long var6);

    void nodeDoCreate(long var1);

    void relationshipDoDelete(long var1, int var3, long var4, long var6);

    void relationshipDoDeleteAddedInThisTx(long var1);

    void nodeDoDelete(long var1);

    void nodeDoAddProperty(long var1, int var3, Value var4);

    void nodeDoChangeProperty(long var1, int var3, Value var4);

    void relationshipDoReplaceProperty(long var1, int var3, Value var4, Value var5);

    void graphDoReplaceProperty(int var1, Value var2, Value var3);

    void nodeDoRemoveProperty(long var1, int var3);

    void relationshipDoRemoveProperty(long var1, int var3);

    void graphDoRemoveProperty(int var1);

    void nodeDoAddLabel(long var1, long var3);

    void nodeDoRemoveLabel(long var1, long var3);

    void labelDoCreateForName(String var1, long var2);

    void propertyKeyDoCreateForName(String var1, int var2);

    void relationshipTypeDoCreateForName(String var1, int var2);

    void indexDoAdd(IndexDescriptor var1);

    void indexDoDrop(IndexDescriptor var1);

    boolean indexDoUnRemove(IndexDescriptor var1);

    void constraintDoAdd(ConstraintDescriptor var1);

    void constraintDoAdd(IndexBackedConstraintDescriptor var1, long var2);

    void constraintDoDrop(ConstraintDescriptor var1);

    boolean constraintDoUnRemove(ConstraintDescriptor var1);

    void indexDoUpdateEntry(SchemaDescriptor var1, long var2, ValueTuple var4, ValueTuple var5);
}
