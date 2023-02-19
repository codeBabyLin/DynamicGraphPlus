//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.api.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import cn.DynamicGraph.Common.DGVersion;

import org.eclipse.collections.api.block.procedure.primitive.LongProcedure;
import org.eclipse.collections.api.iterator.LongIterator;
import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.map.primitive.MutableLongLongMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.UnmodifiableMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo4j.internal.kernel.api.schema.SchemaDescriptor;
import org.neo4j.internal.kernel.api.schema.SchemaDescriptorPredicates;
import org.neo4j.internal.kernel.api.schema.constraints.ConstraintDescriptor;
import org.neo4j.kernel.api.schema.constraints.IndexBackedConstraintDescriptor;
import org.neo4j.kernel.api.txstate.TransactionState;
import org.neo4j.kernel.impl.util.collection.CollectionsFactory;
import org.neo4j.kernel.impl.util.collection.OnHeapCollectionsFactory;
import org.neo4j.kernel.impl.util.diffsets.MutableDiffSets;
import org.neo4j.kernel.impl.util.diffsets.MutableDiffSetsImpl;
import org.neo4j.kernel.impl.util.diffsets.MutableLongDiffSets;
import org.neo4j.kernel.impl.util.diffsets.MutableLongDiffSetsImpl;
import org.neo4j.storageengine.api.RelationshipDirection;
import org.neo4j.storageengine.api.RelationshipVisitor;
import org.neo4j.storageengine.api.RelationshipVisitor.Home;
import org.neo4j.storageengine.api.schema.IndexDescriptor;
import org.neo4j.storageengine.api.txstate.*;
import org.neo4j.storageengine.api.txstate.DiffSets.Empty;
import org.neo4j.util.VisibleForTesting;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueTuple;
import org.neo4j.values.storable.Values;

public class TxState implements TransactionState, Home {
    private final CollectionsFactory collectionsFactory;
    private MutableLongObjectMap<MutableLongDiffSets> labelStatesMap;
    private MutableLongObjectMap<NodeStateImpl> nodeStatesMap;
    private MutableLongObjectMap<RelationshipStateImpl> relationshipStatesMap;
    private MutableLongObjectMap<String> createdLabelTokens;
    private MutableLongObjectMap<String> createdPropertyKeyTokens;
    private MutableLongObjectMap<String> createdRelationshipTypeTokens;
    private GraphStateImpl graphState;
    private MutableDiffSets<IndexDescriptor> indexChanges;
    private MutableDiffSets<ConstraintDescriptor> constraintsChanges;
    private RemovalsCountingDiffSets nodes;
    private RemovalsCountingDiffSets relationships;
    private MutableObjectLongMap<IndexBackedConstraintDescriptor> createdConstraintIndexesByConstraint;
    private Map<SchemaDescriptor, Map<ValueTuple, MutableLongDiffSets>> indexUpdates;
    private long revision;
    private long dataRevision;

    public TxState() {
        this(OnHeapCollectionsFactory.INSTANCE);
    }

    public TxState(CollectionsFactory collectionsFactory) {
        this.collectionsFactory = collectionsFactory;
    }

/*    public void accept(TxStateVisitor visitor)throws ConstraintValidationException, CreateConstraintFailureException{
        this.accept((DynamicTxStateVisitor)visitor);
    }*/



    public void accept(TxStateVisitor visitor) throws ConstraintValidationException, CreateConstraintFailureException {
        if (this.nodes != null) {
            this.nodes.getAdded().each(visitor::visitCreatedNode);
        }

        if (this.relationships != null) {
            LongIterator added = this.relationships.getAdded().longIterator();

            while(added.hasNext()) {
                long relId = added.next();
                visitor.getClass();
                if (!this.relationshipVisit(relId, visitor::visitCreatedRelationship)) {
                    throw new IllegalStateException("No RelationshipState for added relationship!");
                }
            }

            this.relationships.getRemoved().forEach(visitor::visitDeletedRelationship);
        }

        if (this.nodes != null) {
            this.nodes.getRemoved().each(visitor::visitDeletedNode);
        }

        Iterator var5 = this.modifiedNodes().iterator();

        while(var5.hasNext()) {
            NodeState node = (NodeState)var5.next();
            NodeStateImpl nsl = (NodeStateImpl)node;
            if (node.hasPropertyChanges()) {
                //visitor.visitNodePropertyChanges(node.getId(), node.addedProperties(), node.changedProperties(), node.removedProperties());
                visitor.visitNodePropertyChanges(node.getId(), node.addedProperties(), node.changedProperties(), node.removedProperties(),nsl.PropertiesWithVersions());
            }

            if(nsl.versionChanged()){
                visitor.visitNodeVersionChange(nsl.getId(),nsl.getNodeVersion());
            }
            LongDiffSets labelDiffSets = node.labelDiffSets();
            if (!labelDiffSets.isEmpty()) {
                //DynamicGraph
                //                //************************************************************

                //visitor.visitNodeLabelChanges(node.getId(), labelDiffSets.getAdded(), labelDiffSets.getRemoved());

                visitor.visitNodeVersionLabelChanges(node.getId(),labelDiffSets.getAdded(),labelDiffSets.getRemoved(),nsl.labelWithVersionMap());


                //DynamicGraph
                //                //************************************************************


            }
        }

        var5 = this.modifiedRelationships().iterator();

        while(var5.hasNext()) {
            RelationshipState rel = (RelationshipState)var5.next();
            RelationshipStateImpl rsl = (RelationshipStateImpl)rel;
            if(rsl.isVersionChanged()){
                visitor.visitRelVersionChange(rsl.getId(),rsl.getVersion());
            }
            visitor.visitRelPropertyChanges(rel.getId(), rel.addedProperties(), rel.changedProperties(), rel.removedProperties(),rsl.PropertiesWithVersions());
        }

        if (this.graphState != null) {
            visitor.visitGraphPropertyChanges(this.graphState.addedProperties(), this.graphState.changedProperties(), this.graphState.removedProperties());
        }

        if (this.indexChanges != null) {
            this.indexChanges.getAdded().forEach(visitor::visitAddedIndex);
            this.indexChanges.getRemoved().forEach(visitor::visitRemovedIndex);
        }

        if (this.constraintsChanges != null) {
            var5 = this.constraintsChanges.getAdded().iterator();

            while(var5.hasNext()) {
                ConstraintDescriptor added = (ConstraintDescriptor)var5.next();
                visitor.visitAddedConstraint(added);
            }

            this.constraintsChanges.getRemoved().forEach(visitor::visitRemovedConstraint);
        }

        if (this.createdLabelTokens != null) {
            this.createdLabelTokens.forEachKeyValue(visitor::visitCreatedLabelToken);
        }

        if (this.createdPropertyKeyTokens != null) {
            this.createdPropertyKeyTokens.forEachKeyValue(visitor::visitCreatedPropertyKeyToken);
        }

        if (this.createdRelationshipTypeTokens != null) {
            this.createdRelationshipTypeTokens.forEachKeyValue(visitor::visitCreatedRelationshipTypeToken);
        }

    }

    public boolean hasChanges() {
        return this.revision != 0L;
    }

    public Iterable<NodeState> modifiedNodes() {
        return this.nodeStatesMap == null ? Iterables.empty() : Iterables.cast(this.nodeStatesMap.values());
    }

    @VisibleForTesting
    MutableLongDiffSets getOrCreateLabelStateNodeDiffSets(long labelId) {
        if (this.labelStatesMap == null) {
            this.labelStatesMap = new LongObjectHashMap();
        }

        return (MutableLongDiffSets)this.labelStatesMap.getIfAbsentPut(labelId, () -> {
            return new MutableLongDiffSetsImpl(this.collectionsFactory);
        });
    }

    private LongDiffSets getLabelStateNodeDiffSets(long labelId) {
        if (this.labelStatesMap == null) {
            return LongDiffSets.EMPTY;
        } else {
            LongDiffSets nodeDiffSets = (LongDiffSets)this.labelStatesMap.get(labelId);
            return nodeDiffSets == null ? LongDiffSets.EMPTY : nodeDiffSets;
        }
    }

    public LongDiffSets nodeStateLabelDiffSets(long nodeId) {
        return this.getNodeState(nodeId).labelDiffSets();
    }

    private MutableLongDiffSets getOrCreateNodeStateLabelDiffSets(long nodeId) {
        return this.getOrCreateNodeState(nodeId).getOrCreateLabelDiffSets();
    }

    public boolean nodeIsAddedInThisTx(long nodeId) {
        return this.nodes != null && this.nodes.isAdded(nodeId);
    }

    public boolean relationshipIsAddedInThisTx(long relationshipId) {
        return this.relationships != null && this.relationships.isAdded(relationshipId);
    }

    private void changed() {
        ++this.revision;
    }

    private void dataChanged() {
        this.changed();
        this.dataRevision = this.revision;
    }

    public void nodeDoCreate(long id) {
        this.nodes().add(id);
        this.dataChanged();
    }

    public void nodeDoDelete(long nodeId) {
        this.nodes().remove(nodeId);
        if (this.nodeStatesMap != null) {
            NodeStateImpl nodeState = (NodeStateImpl)this.nodeStatesMap.remove(nodeId);
            if (nodeState != null) {
                LongDiffSets diff = nodeState.labelDiffSets();
                diff.getAdded().each((label) -> {
                    this.getOrCreateLabelStateNodeDiffSets(label).remove(nodeId);
                });
                nodeState.clearIndexDiffs(nodeId);
                nodeState.clear();
            }
        }

        this.dataChanged();
    }

    public void relationshipDoCreate(long id, int relationshipTypeId, long startNodeId, long endNodeId) {
        this.relationships().add(id);
        if (startNodeId == endNodeId) {
            this.getOrCreateNodeState(startNodeId).addRelationship(id, relationshipTypeId, RelationshipDirection.LOOP);
        } else {
            this.getOrCreateNodeState(startNodeId).addRelationship(id, relationshipTypeId, RelationshipDirection.OUTGOING);
            this.getOrCreateNodeState(endNodeId).addRelationship(id, relationshipTypeId, RelationshipDirection.INCOMING);
        }

        this.getOrCreateRelationshipState(id).setMetaData(startNodeId, endNodeId, relationshipTypeId);
        this.dataChanged();
    }

    public boolean nodeIsDeletedInThisTx(long nodeId) {
        return this.nodes != null && this.nodes.wasRemoved(nodeId);
    }

    public void relationshipDoDelete(long id, int type, long startNodeId, long endNodeId) {
        this.relationships().remove(id);
        if (startNodeId == endNodeId) {
            this.getOrCreateNodeState(startNodeId).removeRelationship(id, type, RelationshipDirection.LOOP);
        } else {
            this.getOrCreateNodeState(startNodeId).removeRelationship(id, type, RelationshipDirection.OUTGOING);
            this.getOrCreateNodeState(endNodeId).removeRelationship(id, type, RelationshipDirection.INCOMING);
        }

        if (this.relationshipStatesMap != null) {
            RelationshipStateImpl removed = (RelationshipStateImpl)this.relationshipStatesMap.remove(id);
            if (removed != null) {
                removed.clear();
            }
        }

        this.dataChanged();
    }

    public void relationshipDoDeleteAddedInThisTx(long relationshipId) {
        this.getRelationshipState(relationshipId).accept(this::relationshipDoDelete);
    }

    public boolean relationshipIsDeletedInThisTx(long relationshipId) {
        return this.relationships != null && this.relationships.wasRemoved(relationshipId);
    }

    public void nodeDoAddProperty(long nodeId, int newPropertyKeyId, Value value) {
        NodeStateImpl nodeState = this.getOrCreateNodeState(nodeId);
        nodeState.addProperty(newPropertyKeyId, value);
        this.dataChanged();
    }

    public void nodeDoChangeProperty(long nodeId, int propertyKeyId, Value newValue) {
        NodeStateImpl nodeState = this.getOrCreateNodeState(nodeId);
        nodeState.changeProperty(propertyKeyId, newValue);
        this.dataChanged();
    }


    //DynamicGraph method
    //*******************
    public void nodeDoRemoveLabel(long labelId, long nodeId, long version) {
        //this.getOrCreateLabelStateNodeDiffSets(labelId).remove(nodeId);
        //this.getOrCreateNodeStateLabelDiffSets(nodeId).remove(labelId);
        //this.dataChanged();
        this.nodeDoAddLabel(labelId,nodeId,version);
    }

    @Override
    public void nodeDoAddLabel(long nodeId, long labelId, long version) {
        this.getOrCreateLabelStateNodeDiffSets(labelId).add(nodeId);
        this.getOrCreateNodeStateLabelDiffSets(nodeId).add(labelId);
        this.getOrCreateNodeStateLabelWithVersionMap(nodeId).put(labelId,version);
        this.dataChanged();
    }

    @Override
    public void nodeDoDelete(long nodeId, long version) {
        this.nodeDoChangeVersion(nodeId,version);
    }

    @Override
    public void nodeDoCreate(long id, long version) {
        this.nodes().add(id);
        this.getOrCreateNodeState(id).setNodeVersion(version);
        this.dataChanged();
    }

    @Override
    public void relationshipDoCreate(long id, int relationshipTypeId, long startNodeId, long endNodeId, long version) {
        this.relationships().add(id);
        if (startNodeId == endNodeId) {
            this.getOrCreateNodeState(startNodeId).addRelationship(id, relationshipTypeId, RelationshipDirection.LOOP);
        } else {
            this.getOrCreateNodeState(startNodeId).addRelationship(id, relationshipTypeId, RelationshipDirection.OUTGOING);
            this.getOrCreateNodeState(endNodeId).addRelationship(id, relationshipTypeId, RelationshipDirection.INCOMING);
        }

        this.getOrCreateRelationshipState(id).setMetaData(startNodeId, endNodeId, relationshipTypeId);
        this.getOrCreateRelationshipState(id).setVersion(version);
        this.dataChanged();
    }

    @Override
    public void relationshipDoDelete(long id, int type, long startNodeId, long endNodeId, long version) {
        this.relDoChangeVersion(id,version);
    }

    @Override
    public void nodeDoAddProperty(long nodeId, int newPropertyKeyId, Value value, long version) {
        NodeStateImpl nodeState = this.getOrCreateNodeState(nodeId);
        nodeState.addProperty(newPropertyKeyId, value,version);
        this.dataChanged();
    }

    @Override
    public void nodeDoChangeProperty(long nodeId, int propertyKeyId, Value newValue, long version) {
        NodeStateImpl nodeState = this.getOrCreateNodeState(nodeId);
        nodeState.changeProperty(propertyKeyId, newValue,version);
        this.dataChanged();
    }

    @Override
    public void nodeDoRemoveProperty(long nodeId, int propertyKeyId, long version) {
        this.getOrCreateNodeState(nodeId).removeProperty(propertyKeyId,version);
        this.dataChanged();
    }

    @Override
    public void relationshipDoReplaceProperty(long relationshipId, int propertyKeyId, Value replacedValue, Value newValue, long version) {
        if (replacedValue != Values.NO_VALUE) {
            this.getOrCreateRelationshipState(relationshipId).changeProperty(propertyKeyId, newValue,version);
        } else {
            this.getOrCreateRelationshipState(relationshipId).addProperty(propertyKeyId, newValue,version);
        }

        this.dataChanged();
    }

    @Override
    public void relationshipDoRemoveProperty(long relationshipId, int propertyKeyId, long version) {
        this.getOrCreateRelationshipState(relationshipId).removeProperty(propertyKeyId,version);
        this.dataChanged();
    }

    public void nodeDoChangeVersion(long nodeId, long version){
        this.getOrCreateNodeState(nodeId).setNodeVersion(version);
        this.dataChanged();
    }

    @Override
    public void relDoChangeVersion(long relId, long version) {
        this.getOrCreateRelationshipState(relId).setVersion(version);
        this.dataChanged();
    }


    private MutableLongLongMap getOrCreateNodeStateLabelWithVersionMap(long nodeId) {
        return this.getOrCreateNodeState(nodeId).getOrCreateLabelWithVersionMap();
    }
    @Override
    public LongLongMap nodesWithVersionLabelChanged(long nodeId) {
        return this.getOrCreateNodeStateLabelWithVersionMap(nodeId);
    }

    @Override
    public Map<Long, Long> augmentLabelsMap(Map<Long, Long> labels, NodeState nodeState) {
        LongDiffSets labelDiffSets = nodeState.labelDiffSets();
        LongLongMap versionLabels = this.getOrCreateNodeStateLabelWithVersionMap(nodeState.getId());
        if (!labelDiffSets.isEmpty()) {
            labelDiffSets.getRemoved().forEach(labels::remove);
            labelDiffSets.getAdded().forEach(new LongProcedure() {
                @Override
                public void value(long l) {
                    labels.put(l,versionLabels.get(l));
                }
            });
        }

        return labels;
        //return null;
    }

    //*******************
    //DynamicGraph method

    public void relationshipDoReplaceProperty(long relationshipId, int propertyKeyId, Value replacedValue, Value newValue) {
        if (replacedValue != Values.NO_VALUE) {
            this.getOrCreateRelationshipState(relationshipId).changeProperty(propertyKeyId, newValue);
        } else {
            this.getOrCreateRelationshipState(relationshipId).addProperty(propertyKeyId, newValue);
        }

        this.dataChanged();
    }

    public void graphDoReplaceProperty(int propertyKeyId, Value replacedValue, Value newValue) {
        if (replacedValue != Values.NO_VALUE) {
            this.getOrCreateGraphState().changeProperty(propertyKeyId, newValue);
        } else {
            this.getOrCreateGraphState().addProperty(propertyKeyId, newValue);
        }

        this.dataChanged();
    }

    public void nodeDoRemoveProperty(long nodeId, int propertyKeyId) {
        this.getOrCreateNodeState(nodeId).removeProperty(propertyKeyId);
        this.dataChanged();
    }

    public void relationshipDoRemoveProperty(long relationshipId, int propertyKeyId) {
        this.getOrCreateRelationshipState(relationshipId).removeProperty(propertyKeyId);
        this.dataChanged();
    }

    public void graphDoRemoveProperty(int propertyKeyId) {
        this.getOrCreateGraphState().removeProperty(propertyKeyId);
        this.dataChanged();
    }

    public void nodeDoAddLabel(long labelId, long nodeId) {
        this.getOrCreateLabelStateNodeDiffSets(labelId).add(nodeId);
        this.getOrCreateNodeStateLabelDiffSets(nodeId).add(labelId);
        this.dataChanged();
    }

    public void nodeDoRemoveLabel(long labelId, long nodeId) {
        this.getOrCreateLabelStateNodeDiffSets(labelId).remove(nodeId);
        this.getOrCreateNodeStateLabelDiffSets(nodeId).remove(labelId);
        this.dataChanged();
    }

    public void labelDoCreateForName(String labelName, long id) {
        if (this.createdLabelTokens == null) {
            this.createdLabelTokens = new LongObjectHashMap();
        }

        this.createdLabelTokens.put(id, labelName);
        this.changed();
    }

    public void propertyKeyDoCreateForName(String propertyKeyName, int id) {
        if (this.createdPropertyKeyTokens == null) {
            this.createdPropertyKeyTokens = new LongObjectHashMap();
        }

        this.createdPropertyKeyTokens.put((long)id, propertyKeyName);
        this.changed();
    }

    public void relationshipTypeDoCreateForName(String labelName, int id) {
        if (this.createdRelationshipTypeTokens == null) {
            this.createdRelationshipTypeTokens = new LongObjectHashMap();
        }

        this.createdRelationshipTypeTokens.put((long)id, labelName);
        this.changed();
    }

    public NodeState getNodeState(long id) {
        if (this.nodeStatesMap == null) {
            return NodeStateImpl.EMPTY;
        } else {
            NodeState nodeState = (NodeState)this.nodeStatesMap.get(id);
            return nodeState == null ? NodeStateImpl.EMPTY : nodeState;
        }
    }

    public RelationshipState getRelationshipState(long id) {
        if (this.relationshipStatesMap == null) {
            return RelationshipStateImpl.EMPTY;
        } else {
            RelationshipStateImpl relationshipState = (RelationshipStateImpl)this.relationshipStatesMap.get(id);
            return (RelationshipState)(relationshipState == null ? RelationshipStateImpl.EMPTY : relationshipState);
        }
    }

    public GraphState getGraphState() {
        return this.graphState;
    }

    public MutableLongSet augmentLabels(MutableLongSet labels, NodeState nodeState) {
        LongDiffSets labelDiffSets = nodeState.labelDiffSets();
        if (!labelDiffSets.isEmpty()) {
            labelDiffSets.getRemoved().forEach(labels::remove);
            labelDiffSets.getAdded().forEach(labels::add);
        }

        return labels;
    }

    public LongDiffSets nodesWithLabelChanged(long label) {
        return this.getLabelStateNodeDiffSets(label);
    }

    public void indexDoAdd(IndexDescriptor descriptor) {
        MutableDiffSets<IndexDescriptor> diff = this.indexChangesDiffSets();
        if (!diff.unRemove(descriptor)) {
            diff.add(descriptor);
        }

        this.changed();
    }

    public void indexDoDrop(IndexDescriptor descriptor) {
        this.indexChangesDiffSets().remove(descriptor);
        this.changed();
    }

    public boolean indexDoUnRemove(IndexDescriptor descriptor) {
        return this.indexChangesDiffSets().unRemove(descriptor);
    }

    public DiffSets<IndexDescriptor> indexDiffSetsByLabel(int labelId) {
        return this.indexChangesDiffSets().filterAdded(SchemaDescriptorPredicates.hasLabel(labelId));
    }

    public DiffSets<IndexDescriptor> indexDiffSetsBySchema(SchemaDescriptor schema) {
        return this.indexChangesDiffSets().filterAdded((indexDescriptor) -> {
            return indexDescriptor.schema().equals(schema);
        });
    }

    public DiffSets<IndexDescriptor> indexChanges() {
        return Empty.ifNull(this.indexChanges);
    }

    private MutableDiffSets<IndexDescriptor> indexChangesDiffSets() {
        if (this.indexChanges == null) {
            this.indexChanges = new MutableDiffSetsImpl();
        }

        return this.indexChanges;
    }

    public LongDiffSets addedAndRemovedNodes() {
        return (LongDiffSets)(this.nodes == null ? LongDiffSets.EMPTY : this.nodes);
    }

    private RemovalsCountingDiffSets nodes() {
        if (this.nodes == null) {
            this.nodes = new RemovalsCountingDiffSets();
        }

        return this.nodes;
    }

    public LongDiffSets addedAndRemovedRelationships() {
        return (LongDiffSets)(this.relationships == null ? LongDiffSets.EMPTY : this.relationships);
    }

    private RemovalsCountingDiffSets relationships() {
        if (this.relationships == null) {
            this.relationships = new RemovalsCountingDiffSets();
        }

        return this.relationships;
    }

    public Iterable<RelationshipState> modifiedRelationships() {
        return this.relationshipStatesMap == null ? Iterables.empty() : Iterables.cast(this.relationshipStatesMap.values());
    }

    @VisibleForTesting
    NodeStateImpl getOrCreateNodeState(long nodeId) {
        if (this.nodeStatesMap == null) {
            this.nodeStatesMap = new LongObjectHashMap();
        }

        return (NodeStateImpl)this.nodeStatesMap.getIfAbsentPut(nodeId, () -> {
            return new NodeStateImpl(nodeId, this.collectionsFactory);
        });
    }

    private RelationshipStateImpl getOrCreateRelationshipState(long relationshipId) {
        if (this.relationshipStatesMap == null) {
            this.relationshipStatesMap = new LongObjectHashMap();
        }

        return (RelationshipStateImpl)this.relationshipStatesMap.getIfAbsentPut(relationshipId, () -> {
            return new RelationshipStateImpl(relationshipId, this.collectionsFactory);
        });
    }

    @VisibleForTesting
    GraphStateImpl getOrCreateGraphState() {
        if (this.graphState == null) {
            this.graphState = new GraphStateImpl(this.collectionsFactory);
        }

        return this.graphState;
    }

    public void constraintDoAdd(IndexBackedConstraintDescriptor constraint, long indexId) {
        this.constraintsChangesDiffSets().add(constraint);
        this.createdConstraintIndexesByConstraint().put(constraint, indexId);
        this.changed();
    }

    public void constraintDoAdd(ConstraintDescriptor constraint) {
        this.constraintsChangesDiffSets().add(constraint);
        this.changed();
    }

    public DiffSets<ConstraintDescriptor> constraintsChangesForLabel(int labelId) {
        return this.constraintsChangesDiffSets().filterAdded(SchemaDescriptorPredicates.hasLabel(labelId));
    }

    public DiffSets<ConstraintDescriptor> constraintsChangesForSchema(SchemaDescriptor descriptor) {
        return this.constraintsChangesDiffSets().filterAdded(SchemaDescriptor.equalTo(descriptor));
    }

    public DiffSets<ConstraintDescriptor> constraintsChangesForRelationshipType(int relTypeId) {
        return this.constraintsChangesDiffSets().filterAdded(SchemaDescriptorPredicates.hasRelType(relTypeId));
    }

    public DiffSets<ConstraintDescriptor> constraintsChanges() {
        return Empty.ifNull(this.constraintsChanges);
    }

    private MutableDiffSets<ConstraintDescriptor> constraintsChangesDiffSets() {
        if (this.constraintsChanges == null) {
            this.constraintsChanges = new MutableDiffSetsImpl();
        }

        return this.constraintsChanges;
    }

    public void constraintDoDrop(ConstraintDescriptor constraint) {
        this.constraintsChangesDiffSets().remove(constraint);
        if (constraint.enforcesUniqueness()) {
            this.indexDoDrop(getIndexForIndexBackedConstraint((IndexBackedConstraintDescriptor)constraint));
        }

        this.changed();
    }

    public boolean constraintDoUnRemove(ConstraintDescriptor constraint) {
        return this.constraintsChangesDiffSets().unRemove(constraint);
    }

    public Iterable<IndexDescriptor> constraintIndexesCreatedInTx() {
        return this.createdConstraintIndexesByConstraint != null && !this.createdConstraintIndexesByConstraint.isEmpty() ? Iterables.map(TxState::getIndexForIndexBackedConstraint, this.createdConstraintIndexesByConstraint.keySet()) : Iterables.empty();
    }

    public Long indexCreatedForConstraint(ConstraintDescriptor constraint) {
        return this.createdConstraintIndexesByConstraint == null ? null : this.createdConstraintIndexesByConstraint.get(constraint);
    }


    public UnmodifiableMap<ValueTuple, ? extends LongDiffSets> getIndexUpdates(SchemaDescriptor schema) {
        if (this.indexUpdates == null) {
            return null;
        } else {
            Map<ValueTuple, MutableLongDiffSets> updates = (Map)this.indexUpdates.get(schema);
            return updates == null ? null : new UnmodifiableMap(updates);
        }
    }


    public NavigableMap<ValueTuple, ? extends LongDiffSets> getSortedIndexUpdates(SchemaDescriptor descriptor) {
        if (this.indexUpdates == null) {
            return null;
        } else {
            Map<ValueTuple, MutableLongDiffSets> updates = (Map)this.indexUpdates.get(descriptor);
            if (updates == null) {
                return null;
            } else {
                TreeMap sortedUpdates;
                if (updates instanceof TreeMap) {
                    sortedUpdates = (TreeMap)updates;
                } else {
                    sortedUpdates = new TreeMap(ValueTuple.COMPARATOR);
                    sortedUpdates.putAll(updates);
                    this.indexUpdates.put(descriptor, sortedUpdates);
                }

                return Collections.unmodifiableNavigableMap(sortedUpdates);
            }
        }
    }

    public void indexDoUpdateEntry(SchemaDescriptor descriptor, long nodeId, ValueTuple propertiesBefore, ValueTuple propertiesAfter) {
        NodeStateImpl nodeState = this.getOrCreateNodeState(nodeId);
        Map<ValueTuple, MutableLongDiffSets> updates = this.getOrCreateIndexUpdatesByDescriptor(descriptor);
        MutableLongDiffSets after;
        if (propertiesBefore != null) {
            after = this.getOrCreateIndexUpdatesForSeek(updates, propertiesBefore);
            after.remove(nodeId);
            if (after.getRemoved().contains(nodeId)) {
                nodeState.addIndexDiff(after);
            } else {
                nodeState.removeIndexDiff(after);
            }
        }

        if (propertiesAfter != null) {
            after = this.getOrCreateIndexUpdatesForSeek(updates, propertiesAfter);
            after.add(nodeId);
            if (after.getAdded().contains(nodeId)) {
                nodeState.addIndexDiff(after);
            } else {
                nodeState.removeIndexDiff(after);
            }
        }

    }

    @VisibleForTesting
    MutableLongDiffSets getOrCreateIndexUpdatesForSeek(Map<ValueTuple, MutableLongDiffSets> updates, ValueTuple values) {
        return (MutableLongDiffSets)updates.computeIfAbsent(values, (value) -> {
            return new MutableLongDiffSetsImpl(this.collectionsFactory);
        });
    }

    private Map<ValueTuple, MutableLongDiffSets> getOrCreateIndexUpdatesByDescriptor(SchemaDescriptor schema) {
        if (this.indexUpdates == null) {
            this.indexUpdates = new HashMap();
        }

        return (Map)this.indexUpdates.computeIfAbsent(schema, (k) -> {
            return new HashMap();
        });
    }

    private MutableObjectLongMap<IndexBackedConstraintDescriptor> createdConstraintIndexesByConstraint() {
        if (this.createdConstraintIndexesByConstraint == null) {
            this.createdConstraintIndexesByConstraint = new ObjectLongHashMap();
        }

        return this.createdConstraintIndexesByConstraint;
    }

    private static IndexDescriptor getIndexForIndexBackedConstraint(IndexBackedConstraintDescriptor constraint) {
        return constraint.ownedIndexDescriptor();
    }

    public <EX extends Exception> boolean relationshipVisit(long relId, RelationshipVisitor<EX> visitor) throws EX {
        return this.getRelationshipState(relId).accept(visitor);
    }

    public boolean hasDataChanges() {
        return this.dataRevision != 0L;
    }

    public long getDataRevision() {
        return this.dataRevision;
    }

    private class RemovalsCountingDiffSets extends MutableLongDiffSetsImpl {
        private MutableLongSet removedFromAdded;

        RemovalsCountingDiffSets() {
            super(TxState.this.collectionsFactory);
        }

        public boolean remove(long elem) {
            if (this.isAdded(elem) && super.remove(elem)) {
                if (this.removedFromAdded == null) {
                    this.removedFromAdded = TxState.this.collectionsFactory.newLongSet();
                }

                this.removedFromAdded.add(elem);
                return true;
            } else {
                return super.remove(elem);
            }
        }

        private boolean wasRemoved(long id) {
            return this.removedFromAdded != null && this.removedFromAdded.contains(id) || super.isRemoved(id);
        }
    }
}
