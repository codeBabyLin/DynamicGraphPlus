//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.api.state;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.iterator.LongIterator;
import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.map.primitive.MutableLongLongMap;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.iterator.ImmutableEmptyLongIterator;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.neo4j.kernel.impl.api.state.RelationshipChangesForNode.DiffStrategy;
import org.neo4j.kernel.impl.util.collection.CollectionsFactory;
import org.neo4j.kernel.impl.util.diffsets.MutableLongDiffSets;
import org.neo4j.kernel.impl.util.diffsets.MutableLongDiffSetsImpl;
import org.neo4j.storageengine.api.RelationshipDirection;
import org.neo4j.storageengine.api.StorageProperty;
import org.neo4j.storageengine.api.txstate.LongDiffSets;
import org.neo4j.storageengine.api.txstate.NodeState;
import org.neo4j.values.storable.Value;

class NodeStateImpl extends PropertyContainerStateImpl implements NodeState {
    static final NodeState EMPTY = new NodeState() {
        public Iterator<StorageProperty> addedProperties() {
            return Collections.emptyIterator();
        }

        public Iterator<StorageProperty> changedProperties() {
            return Collections.emptyIterator();
        }

        public IntIterable removedProperties() {
            return IntSets.immutable.empty();
        }

        public Iterator<StorageProperty> addedAndChangedProperties() {
            return Collections.emptyIterator();
        }

        public boolean hasPropertyChanges() {
            return false;
        }

        public LongDiffSets labelDiffSets() {
            return LongDiffSets.EMPTY;
        }

        public int augmentDegree(RelationshipDirection direction, int degree, int typeId) {
            return degree;
        }

        public long getId() {
            throw new UnsupportedOperationException("id not defined");
        }

        public boolean isPropertyChangedOrRemoved(int propertyKey) {
            return false;
        }

        public Value propertyValue(int propertyKey) {
            return null;
        }

        public LongIterator getAddedRelationships() {
            return ImmutableEmptyLongIterator.INSTANCE;
        }

        public LongIterator getAddedRelationships(RelationshipDirection direction, int relType) {
            return ImmutableEmptyLongIterator.INSTANCE;
        }
    };
    private MutableLongDiffSets labelDiffSets;
    private MutableLongLongMap labelsWithVersionMap;
    private RelationshipChangesForNode relationshipsAdded;
    private RelationshipChangesForNode relationshipsRemoved;
    private Set<MutableLongDiffSets> indexDiffs;
    private Long nodeVersion;
    private Boolean versionChanged = false;

    NodeStateImpl(long id, CollectionsFactory collectionsFactory) {
        super(id, collectionsFactory);
    }

    //Dynamicgraph method
    //****************************************************************************

    public void setNodeVersion(Long nodeVersion){
        this.nodeVersion = nodeVersion;
        this.versionChanged = true;
    }
    public Boolean versionChanged(){
        return this.versionChanged;
    }

    public Long getNodeVersion(){
        return this.nodeVersion;
    }
    public LongDiffSets labelDiffSets() {
        return (LongDiffSets)(this.labelDiffSets == null ? LongDiffSets.EMPTY : this.labelDiffSets);
    }
    public LongLongMap labelWithVersionMap() {
        return (LongLongMap)(this.labelsWithVersionMap == null ? new LongLongHashMap() : this.labelsWithVersionMap);
    }

    MutableLongLongMap getOrCreateLabelWithVersionMap() {
        if (this.labelsWithVersionMap == null) {
            this.labelsWithVersionMap = new LongLongHashMap();
        }

        return this.labelsWithVersionMap;
    }


    //Dynamicgraph method
    //****************************************************************************

    MutableLongDiffSets getOrCreateLabelDiffSets() {
        if (this.labelDiffSets == null) {
            this.labelDiffSets = new MutableLongDiffSetsImpl(this.collectionsFactory);
        }

        return this.labelDiffSets;
    }

    public void addRelationship(long relId, int typeId, RelationshipDirection direction) {
        if (!this.hasAddedRelationships()) {
            this.relationshipsAdded = new RelationshipChangesForNode(DiffStrategy.ADD);
        }

        this.relationshipsAdded.addRelationship(relId, typeId, direction);
    }

    public void removeRelationship(long relId, int typeId, RelationshipDirection direction) {
        if (!this.hasAddedRelationships() || !this.relationshipsAdded.removeRelationship(relId, typeId, direction)) {
            if (!this.hasRemovedRelationships()) {
                this.relationshipsRemoved = new RelationshipChangesForNode(DiffStrategy.REMOVE);
            }

            this.relationshipsRemoved.addRelationship(relId, typeId, direction);
        }
    }

    public void clear() {
        super.clear();
        if (this.relationshipsAdded != null) {
            this.relationshipsAdded.clear();
        }

        if (this.relationshipsRemoved != null) {
            this.relationshipsRemoved.clear();
        }

        if (this.labelDiffSets != null) {
            this.labelDiffSets = null;
        }

        if (this.indexDiffs != null) {
            this.indexDiffs.clear();
        }

    }

    public int augmentDegree(RelationshipDirection direction, int degree, int typeId) {
        if (this.hasAddedRelationships()) {
            degree = this.relationshipsAdded.augmentDegree(direction, degree, typeId);
        }

        if (this.hasRemovedRelationships()) {
            degree = this.relationshipsRemoved.augmentDegree(direction, degree, typeId);
        }

        return degree;
    }

    private boolean hasAddedRelationships() {
        return this.relationshipsAdded != null;
    }

    private boolean hasRemovedRelationships() {
        return this.relationshipsRemoved != null;
    }

    void addIndexDiff(MutableLongDiffSets diff) {
        if (this.indexDiffs == null) {
            this.indexDiffs = Collections.newSetFromMap(new IdentityHashMap());
        }

        this.indexDiffs.add(diff);
    }

    void removeIndexDiff(MutableLongDiffSets diff) {
        if (this.indexDiffs != null) {
            this.indexDiffs.remove(diff);
        }

    }

    void clearIndexDiffs(long nodeId) {
        if (this.indexDiffs != null) {
            Iterator var3 = this.indexDiffs.iterator();

            while(var3.hasNext()) {
                MutableLongDiffSets diff = (MutableLongDiffSets)var3.next();
                if (diff.getAdded().contains(nodeId)) {
                    diff.remove(nodeId);
                } else if (diff.getRemoved().contains(nodeId)) {
                    diff.add(nodeId);
                }
            }
        }

    }

    public LongIterator getAddedRelationships() {
        return (LongIterator)(this.relationshipsAdded != null ? this.relationshipsAdded.getRelationships() : ImmutableEmptyLongIterator.INSTANCE);
    }

    public LongIterator getAddedRelationships(RelationshipDirection direction, int relType) {
        return (LongIterator)(this.relationshipsAdded != null ? this.relationshipsAdded.getRelationships(direction, relType) : ImmutableEmptyLongIterator.INSTANCE);
    }
}
