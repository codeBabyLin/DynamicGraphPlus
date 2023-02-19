//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.newapi;

import org.eclipse.collections.api.block.procedure.primitive.LongLongProcedure;
import org.eclipse.collections.api.iterator.LongIterator;
import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import org.eclipse.collections.impl.iterator.ImmutableEmptyLongIterator;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.neo4j.internal.kernel.api.LabelSet;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.RelationshipGroupCursor;
import org.neo4j.internal.kernel.api.RelationshipTraversalCursor;
import org.neo4j.kernel.api.txstate.TransactionState;
import org.neo4j.kernel.impl.storageengine.impl.recordstorage.RecordNodeCursor;
import org.neo4j.storageengine.api.StorageNodeCursor;
import org.neo4j.storageengine.api.txstate.LongDiffSets;

import java.util.HashMap;
import java.util.Map;

class DefaultNodeCursor implements NodeCursor {
    private Read read;
    private HasChanges hasChanges;
    private LongIterator addedNodes;
    private StorageNodeCursor storeCursor;
    private long single;
    private final DefaultCursors pool;

    DefaultNodeCursor(DefaultCursors pool, StorageNodeCursor storeCursor) {
        this.hasChanges = HasChanges.MAYBE;
        this.pool = pool;
        this.storeCursor = storeCursor;
    }

    void scan(Read read) {
        this.storeCursor.scan();
        this.read = read;
        this.single = -1L;
        this.hasChanges = HasChanges.MAYBE;
        this.addedNodes = ImmutableEmptyLongIterator.INSTANCE;
    }

    public RecordNodeCursor getRecordNodeCursor(){
        return (RecordNodeCursor)this.storeCursor;
    }
    void single(long reference, Read read) {
        this.storeCursor.single(reference);
        this.read = read;
        this.single = reference;
        this.hasChanges = HasChanges.MAYBE;
        this.addedNodes = ImmutableEmptyLongIterator.INSTANCE;
    }

    public long nodeReference() {
        return this.storeCursor.entityReference();
    }

    public LabelSet labels() {
        if (!this.hasChanges()) {
            return Labels.from(this.storeCursor.labels());
        } else {
            TransactionState txState = this.read.txState();
            if (txState.nodeIsAddedInThisTx(this.storeCursor.entityReference())) {
                return Labels.from(txState.nodeStateLabelDiffSets(this.storeCursor.entityReference()).getAdded());
            } else {
                long[] longs = this.storeCursor.labels();
                MutableLongSet labels = new LongHashSet();
                long[] var4 = longs;
                int var5 = longs.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    long labelToken = var4[var6];
                    labels.add(labelToken);
                }

                return Labels.from(txState.augmentLabels(labels, txState.getNodeState(this.storeCursor.entityReference())));
            }
        }
    }

    public boolean hasLabel(int label) {
        if (this.hasChanges()) {
            TransactionState txState = this.read.txState();
            LongDiffSets diffSets = txState.nodeStateLabelDiffSets(this.storeCursor.entityReference());
            if (diffSets.getAdded().contains((long)label)) {
                return true;
            }

            if (diffSets.getRemoved().contains((long)label)) {
                return false;
            }
        }

        return this.storeCursor.hasLabel(label);
    }

    public void relationships(RelationshipGroupCursor cursor) {
        ((DefaultRelationshipGroupCursor)cursor).init(this.nodeReference(), this.relationshipGroupReference(), this.read);
    }

    public void allRelationships(RelationshipTraversalCursor cursor) {
        ((DefaultRelationshipTraversalCursor)cursor).init(this.nodeReference(), this.allRelationshipsReference(), this.read);
    }

    //Dynamicgraph method
    //****************************************************************************
    @Override
    public long nodeVersion() {

        return this.storeCursor.nodeVersion();
    }
    public LabelSet labels(long version) {
        if (!this.hasChanges()) {
            return Labels.from(this.storeCursor.labels());
        } else {
            TransactionState txState = this.read.txState();
            if (txState.nodeIsAddedInThisTx(this.storeCursor.entityReference())) {
                return Labels.from(txState.nodeStateLabelDiffSets(this.storeCursor.entityReference()).getAdded());
            } else {
                long[] longs = this.storeCursor.labels();
                MutableLongSet labels = new LongHashSet();
                long[] var4 = longs;
                int var5 = longs.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    long labelToken = var4[var6];
                    labels.add(labelToken);
                }

                return Labels.from(txState.augmentLabels(labels, txState.getNodeState(this.storeCursor.entityReference())));
            }
        }
    }

    @Override
    public Map<Long, Long> versionLabels() {
        if (!this.hasChanges()) {
            return this.storeCursor.versionLabels();
        } else {
            TransactionState txState = this.read.txState();
            if (txState.nodeIsAddedInThisTx(this.storeCursor.entityReference())) {
                Map<Long,Long> labels = new HashMap<Long, Long>();
                LongLongMap lMap = txState.nodesWithVersionLabelChanged(this.storeCursor.entityReference());
                LongLongProcedure longLongProcedure = new LongLongProcedure() {
                    @Override
                    public void value(long l, long l1) {
                        labels.put(l,l1);
                    }
                };
                lMap.forEachKeyValue(longLongProcedure);
                return labels;
                //return (NodeStateImpl)txState.getNodeState(this.storeCursor.entityReference()).
                //return Labels.from(txState.nodeStateLabelDiffSets(this.storeCursor.entityReference()).getAdded());
            } else {
               /* long[] longs = this.storeCursor.labels();
                MutableLongSet labels = new LongHashSet();
                long[] var4 = longs;
                int var5 = longs.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    long labelToken = var4[var6];
                    labels.add(labelToken);
                }*/
               return txState.augmentLabelsMap(this.storeCursor.versionLabels(),txState.getNodeState(this.storeCursor.entityReference()));
                //return Labels.from(txState.augmentLabels(labels, txState.getNodeState(this.storeCursor.entityReference())));
            }
        }
        //return new HashMap<Long, Long>();
    }

    //Dynamicgraph method
    //****************************************************************************
    public void properties(PropertyCursor cursor) {
        ((DefaultPropertyCursor)cursor).initNode(this.nodeReference(), this.propertiesReference(), this.read, this.read);
    }

    public long relationshipGroupReference() {
        return this.storeCursor.relationshipGroupReference();
    }

    public long allRelationshipsReference() {
        return this.storeCursor.allRelationshipsReference();
    }

    public long propertiesReference() {
        return this.storeCursor.propertiesReference();
    }

    public boolean isDense() {
        return this.storeCursor.isDense();
    }

    public boolean next() {
        boolean hasChanges = this.hasChanges();
        if (hasChanges && this.addedNodes.hasNext()) {
            //this.storeCursor.setCurrent(this.addedNodes.next());
            this.storeCursor.setCurrent(this.addedNodes.next(),this.read.ktx.getLastTransactionIdWhenStarted());
            return true;
        } else {
            do {
                if (!this.storeCursor.next()) {
                    return false;
                }
            } while(hasChanges && this.read.txState().nodeIsDeletedInThisTx(this.storeCursor.entityReference()));

            return true;
        }
    }

    public void close() {
        if (!this.isClosed()) {
            this.read = null;
            this.hasChanges = HasChanges.MAYBE;
            this.addedNodes = ImmutableEmptyLongIterator.INSTANCE;
            this.storeCursor.reset();
            this.pool.accept(this);
        }

    }

    public boolean isClosed() {
        return this.read == null;
    }

    private boolean hasChanges() {
        switch(this.hasChanges) {
            case MAYBE:
                boolean changes = this.read.hasTxStateWithChanges();
                if (changes) {
                    if (this.single != -1L) {
                        this.addedNodes = (LongIterator)(this.read.txState().nodeIsAddedInThisTx(this.single) ? LongSets.immutable.of(this.single).longIterator() : ImmutableEmptyLongIterator.INSTANCE);
                    } else {
                        this.addedNodes = this.read.txState().addedAndRemovedNodes().getAdded().freeze().longIterator();
                    }

                    this.hasChanges = HasChanges.YES;
                } else {
                    this.hasChanges = HasChanges.NO;
                }

                return changes;
            case YES:
                return true;
            case NO:
                return false;
            default:
                throw new IllegalStateException("Style guide, why are you making me do this");
        }
    }

    public String toString() {
        return this.isClosed() ? "NodeCursor[closed state]" : "NodeCursor[id=" + this.nodeReference() + ", " + this.storeCursor.toString() + "]";
    }

    void release() {
        this.storeCursor.close();
    }
}
