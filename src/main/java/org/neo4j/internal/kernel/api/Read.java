//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.internal.kernel.api;

import org.neo4j.internal.kernel.api.IndexQuery.ExactPredicate;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.schema.IndexNotFoundKernelException;
import org.neo4j.values.storable.Value;

public interface Read {
    int ANY_LABEL = -1;
    int ANY_RELATIONSHIP_TYPE = -1;

    void nodeIndexSeek(IndexReference var1, NodeValueIndexCursor var2, IndexOrder var3, boolean var4, IndexQuery... var5) throws KernelException;

    void nodeIndexDistinctValues(IndexReference var1, NodeValueIndexCursor var2, boolean var3) throws IndexNotFoundKernelException;

    long lockingNodeUniqueIndexSeek(IndexReference var1, ExactPredicate... var2) throws KernelException;

    void nodeIndexScan(IndexReference var1, NodeValueIndexCursor var2, IndexOrder var3, boolean var4) throws KernelException;

    void nodeLabelScan(int var1, NodeLabelIndexCursor var2);

    void nodeLabelUnionScan(NodeLabelIndexCursor var1, int... var2);

    void nodeLabelIntersectionScan(NodeLabelIndexCursor var1, int... var2);

    Scan<NodeLabelIndexCursor> nodeLabelScan(int var1);

    void allNodesScan(NodeCursor var1);

    Scan<NodeCursor> allNodesScan();

    void singleNode(long var1, NodeCursor var3);

    boolean nodeExists(long var1);

    long countsForNode(int var1);

    long countsForNodeWithoutTxState(int var1);

    long countsForRelationship(int var1, int var2, int var3);

    long countsForRelationshipWithoutTxState(int var1, int var2, int var3);

    long nodesGetCount();

    long relationshipsGetCount();

    void singleRelationship(long var1, RelationshipScanCursor var3);

    boolean relationshipExists(long var1);

    void allRelationshipsScan(RelationshipScanCursor var1);

    Scan<RelationshipScanCursor> allRelationshipsScan();

    void relationshipTypeScan(int var1, RelationshipScanCursor var2);

    Scan<RelationshipScanCursor> relationshipTypeScan(int var1);

    void relationshipGroups(long var1, long var3, RelationshipGroupCursor var5);

    void relationships(long var1, long var3, RelationshipTraversalCursor var5);

    void nodeProperties(long var1, long var3, PropertyCursor var5);

    void relationshipProperties(long var1, long var3, PropertyCursor var5);

    boolean nodeDeletedInTransaction(long var1);

    boolean relationshipDeletedInTransaction(long var1);

    Value nodePropertyChangeInTransactionOrNull(long var1, int var3);

    void graphProperties(PropertyCursor var1);

    void futureNodeReferenceRead(long var1);

    void futureRelationshipsReferenceRead(long var1);

    void futureNodePropertyReferenceRead(long var1);

    void futureRelationshipPropertyReferenceRead(long var1);
}
