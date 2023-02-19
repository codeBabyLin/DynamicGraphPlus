//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.api.txstate;

import java.util.function.LongConsumer;

import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.neo4j.internal.kernel.api.exceptions.EntityNotFoundException;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.kernel.impl.api.CountsRecordState;
import org.neo4j.kernel.impl.api.DegreeVisitor;
import org.neo4j.kernel.impl.api.RelationshipDataExtractor;
import org.neo4j.storageengine.api.StorageNodeCursor;
import org.neo4j.storageengine.api.StorageReader;
import org.neo4j.storageengine.api.StorageRelationshipGroupCursor;
import org.neo4j.storageengine.api.txstate.LongDiffSets;
import org.neo4j.storageengine.api.txstate.ReadableTransactionState;
import org.neo4j.storageengine.api.txstate.TxStateVisitor;
import org.neo4j.storageengine.api.txstate.TxStateVisitor.Delegator;

public class TransactionCountingStateVisitor extends Delegator {
    private final RelationshipDataExtractor edge = new RelationshipDataExtractor();
    private final StorageReader storageReader;
    private final CountsRecordState counts;
    private final ReadableTransactionState txState;
    private final StorageNodeCursor nodeCursor;
    private final StorageRelationshipGroupCursor groupCursor;

    public TransactionCountingStateVisitor(TxStateVisitor next, StorageReader storageReader, ReadableTransactionState txState, CountsRecordState counts) {
        super(next);
        this.storageReader = storageReader;
        this.txState = txState;
        this.counts = counts;
        this.nodeCursor = storageReader.allocateNodeCursor();
        this.groupCursor = storageReader.allocateRelationshipGroupCursor();
    }

    public void visitCreatedNode(long id) {
        this.counts.incrementNodeCount(-1L, 1L);
        super.visitCreatedNode(id);
    }

    //DynamicGraph
    //**********************************************************

    @Override
    public void visitNodeVersionChange(long var1, long var2) {
        super.visitNodeVersionChange(var1, var2);
    }

    @Override
    public void visitRelVersionChange(long var1, long var2) {
        super.visitRelVersionChange(var1, var2);
    }

    @Override
    public void visitNodeVersionLabelChanges(long var1, LongSet var3, LongSet var4, LongLongMap var5) throws ConstraintValidationException {
        super.visitNodeVersionLabelChanges(var1, var3, var4, var5);
    }

    //DynamicGraph
    //**********************************************************

    public void visitDeletedNode(long id) {
        this.counts.incrementNodeCount(-1L, -1L);
        this.nodeCursor.single(id);
        if (this.nodeCursor.next()) {
            this.decrementCountForLabelsAndRelationships(this.nodeCursor);
        }

        super.visitDeletedNode(id);
    }

    private void decrementCountForLabelsAndRelationships(StorageNodeCursor node) {
        long[] labelIds = node.labels();
        long[] var3 = labelIds;
        int var4 = labelIds.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            long labelId = var3[var5];
            this.counts.incrementNodeCount(labelId, -1L);
        }

        this.visitDegrees(node, (type, out, in) -> {
            this.updateRelationshipsCountsFromDegrees(labelIds, type, -out, -in);
        });
    }

    private void visitDegrees(StorageNodeCursor node, DegreeVisitor visitor) {
        this.groupCursor.init(node.entityReference(), node.relationshipGroupReference());

        while(this.groupCursor.next()) {
            int loopCount = this.groupCursor.loopCount();
            visitor.visitDegree(this.groupCursor.type(), (long)(this.groupCursor.outgoingCount() + loopCount), (long)(this.groupCursor.incomingCount() + loopCount));
        }

    }

    public void visitCreatedRelationship(long id, int type, long startNode, long endNode) throws ConstraintValidationException {
        this.updateRelationshipCount(startNode, type, endNode, 1);
        super.visitCreatedRelationship(id, type, startNode, endNode);
    }

    public void visitDeletedRelationship(long id) {
        try {
            this.storageReader.relationshipVisit(id, this.edge);
            this.updateRelationshipCount(this.edge.startNode(), this.edge.type(), this.edge.endNode(), -1);
        } catch (EntityNotFoundException var4) {
            throw new IllegalStateException("Relationship being deleted should exist along with its nodes.", var4);
        }

        super.visitDeletedRelationship(id);
    }

    public void visitNodeLabelChanges(long id, LongSet added, LongSet removed) throws ConstraintValidationException {
        if (!added.isEmpty() || !removed.isEmpty()) {
            added.each((label) -> {
                this.counts.incrementNodeCount(label, 1L);
            });
            removed.each((label) -> {
                this.counts.incrementNodeCount(label, -1L);
            });
            this.nodeCursor.single(id);
            if (this.nodeCursor.next()) {
                this.visitDegrees(this.nodeCursor, (type, out, in) -> {
                    added.forEach((label) -> {
                        this.updateRelationshipsCountsFromDegrees(type, label, out, in);
                    });
                    removed.forEach((label) -> {
                        this.updateRelationshipsCountsFromDegrees(type, label, -out, -in);
                    });
                });
            }
        }

        super.visitNodeLabelChanges(id, added, removed);
    }

    private void updateRelationshipsCountsFromDegrees(long[] labels, int type, long outgoing, long incoming) {
        long[] var7 = labels;
        int var8 = labels.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            long label = var7[var9];
            this.updateRelationshipsCountsFromDegrees(type, label, outgoing, incoming);
        }

    }

    private boolean updateRelationshipsCountsFromDegrees(int type, long label, long outgoing, long incoming) {
        this.counts.incrementRelationshipCount(label, -1, -1L, outgoing);
        this.counts.incrementRelationshipCount(-1L, -1, label, incoming);
        this.counts.incrementRelationshipCount(label, type, -1L, outgoing);
        this.counts.incrementRelationshipCount(-1L, type, label, incoming);
        return false;
    }

    private void updateRelationshipCount(long startNode, int type, long endNode, int delta) {
        this.updateRelationshipsCountsFromDegrees(type, -1L, (long)delta, 0L);
        this.visitLabels(startNode, (labelId) -> {
            this.updateRelationshipsCountsFromDegrees(type, labelId, (long)delta, 0L);
        });
        this.visitLabels(endNode, (labelId) -> {
            this.updateRelationshipsCountsFromDegrees(type, labelId, 0L, (long)delta);
        });
    }

    private void visitLabels(long nodeId, LongConsumer visitor) {
        if (!this.txState.nodeIsDeletedInThisTx(nodeId)) {
            if (this.txState.nodeIsAddedInThisTx(nodeId)) {
                this.txState.getNodeState(nodeId).labelDiffSets().getAdded().forEach(visitor::accept);
            } else {
                this.nodeCursor.single(nodeId);
                if (this.nodeCursor.next()) {
                    long[] labels = this.nodeCursor.labels();
                    LongDiffSets labelDiff = this.txState.getNodeState(nodeId).labelDiffSets();
                    labelDiff.getAdded().forEach(visitor::accept);
                    long[] var6 = labels;
                    int var7 = labels.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        long label = var6[var8];
                        if (!labelDiff.isRemoved(label)) {
                            visitor.accept(label);
                        }
                    }
                }
            }

        }
    }
}
