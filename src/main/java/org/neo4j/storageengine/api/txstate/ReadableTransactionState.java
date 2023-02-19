//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.storageengine.api.txstate;

import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.UnmodifiableMap;

import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo4j.internal.kernel.api.schema.SchemaDescriptor;
import org.neo4j.internal.kernel.api.schema.constraints.ConstraintDescriptor;
import org.neo4j.storageengine.api.RelationshipVisitor;
import org.neo4j.storageengine.api.schema.IndexDescriptor;
import org.neo4j.values.storable.ValueTuple;

import java.util.Map;
import java.util.NavigableMap;

public interface ReadableTransactionState {

    //DynamicGraph
    //****************************************************
    LongLongMap nodesWithVersionLabelChanged(long version);
    Map<Long,Long> augmentLabelsMap(Map<Long, Long> var1, NodeState var2);
    //DynamicGraph
    //****************************************************

    void accept(TxStateVisitor var1) throws ConstraintValidationException, CreateConstraintFailureException;

    boolean hasChanges();

    LongDiffSets nodesWithLabelChanged(long var1);

    LongDiffSets addedAndRemovedNodes();

    LongDiffSets addedAndRemovedRelationships();

    Iterable<NodeState> modifiedNodes();

    Iterable<RelationshipState> modifiedRelationships();

    boolean relationshipIsAddedInThisTx(long var1);

    boolean relationshipIsDeletedInThisTx(long var1);

    LongDiffSets nodeStateLabelDiffSets(long var1);

    boolean nodeIsAddedInThisTx(long var1);

    boolean nodeIsDeletedInThisTx(long var1);

    <EX extends Exception> boolean relationshipVisit(long var1, RelationshipVisitor<EX> var3) throws EX;

    DiffSets<IndexDescriptor> indexDiffSetsByLabel(int var1);

    DiffSets<IndexDescriptor> indexDiffSetsBySchema(SchemaDescriptor var1);

    DiffSets<IndexDescriptor> indexChanges();

    Iterable<IndexDescriptor> constraintIndexesCreatedInTx();

    DiffSets<ConstraintDescriptor> constraintsChanges();

    DiffSets<ConstraintDescriptor> constraintsChangesForLabel(int var1);

    DiffSets<ConstraintDescriptor> constraintsChangesForSchema(SchemaDescriptor var1);

    DiffSets<ConstraintDescriptor> constraintsChangesForRelationshipType(int var1);

    Long indexCreatedForConstraint(ConstraintDescriptor var1);

   // @Nullable
    UnmodifiableMap<ValueTuple, ? extends LongDiffSets> getIndexUpdates(SchemaDescriptor var1);

   // @Nullable
    NavigableMap<ValueTuple, ? extends LongDiffSets> getSortedIndexUpdates(SchemaDescriptor var1);

    NodeState getNodeState(long var1);

    RelationshipState getRelationshipState(long var1);

    GraphState getGraphState();

    MutableLongSet augmentLabels(MutableLongSet var1, NodeState var2);

    boolean hasDataChanges();
}
