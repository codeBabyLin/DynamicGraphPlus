//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import java.util.Iterator;
import java.util.Optional;

//import DynamicGraph.state.DynamicRecordState;
import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo4j.internal.kernel.api.schema.constraints.ConstraintDescriptor;
import org.neo4j.kernel.api.exceptions.schema.DuplicateSchemaRuleException;
import org.neo4j.kernel.api.exceptions.schema.SchemaRuleNotFoundException;
import org.neo4j.kernel.api.schema.constraints.IndexBackedConstraintDescriptor;
import org.neo4j.kernel.api.schema.constraints.NodeKeyConstraintDescriptor;
import org.neo4j.kernel.api.schema.constraints.UniquenessConstraintDescriptor;
import org.neo4j.kernel.impl.api.SchemaState;
import org.neo4j.kernel.impl.constraints.ConstraintSemantics;
import org.neo4j.kernel.impl.store.SchemaStorage;
import org.neo4j.storageengine.api.StorageProperty;
import org.neo4j.storageengine.api.schema.IndexDescriptor;
import org.neo4j.storageengine.api.schema.StoreIndexDescriptor;
//import org.neo4j.storageengine.api.txstate.DynamicTxStateVisitor;
import org.neo4j.storageengine.api.txstate.TxStateVisitor.Adapter;

class TransactionToRecordStateVisitor extends Adapter {
    private boolean clearSchemaState;
    private final TransactionRecordState recordState;
    private final SchemaState schemaState;
    private final SchemaStorage schemaStorage;
    private final ConstraintSemantics constraintSemantics;

    TransactionToRecordStateVisitor(TransactionRecordState recordState, SchemaState schemaState, SchemaStorage schemaStorage, ConstraintSemantics constraintSemantics) {
        this.recordState = recordState;
        this.schemaState = schemaState;
        this.schemaStorage = schemaStorage;
        this.constraintSemantics = constraintSemantics;
    }

    public void close() {
        try {
            if (this.clearSchemaState) {
                this.schemaState.clear();
            }
        } finally {
            this.clearSchemaState = false;
        }

    }

    public void visitCreatedNode(long id) {
        this.recordState.nodeCreate(id);
    }

    public void visitDeletedNode(long id) {
        this.recordState.nodeDelete(id);
    }

    public void visitCreatedRelationship(long id, int type, long startNode, long endNode) {
        this.recordState.relCreate(id, type, startNode, endNode);
    }


    //DynamicGraph
    //**********************************************************


    @Override
    public void visitNodePropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed, LongObjectMap versions) throws ConstraintValidationException {
        removed.each((propId) -> {
            this.recordState.nodeRemoveProperty(id, propId, (Long) versions.get(propId));
        });

        StorageProperty prop;
        while(changed.hasNext()) {
            prop = (StorageProperty)changed.next();
            this.recordState.nodeChangeProperty(id, prop.propertyKeyId(), prop.value(), (Long) versions.get(prop.propertyKeyId()));
        }

        while(added.hasNext()) {
            prop = (StorageProperty)added.next();
            this.recordState.nodeAddProperty(id, prop.propertyKeyId(), prop.value(), (Long) versions.get(prop.propertyKeyId()));
        }
    }

    @Override
    public void visitRelPropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed, LongObjectMap versions) throws ConstraintValidationException {
        removed.each((relId) -> {
            this.recordState.relRemoveProperty(id, relId, (Long) versions.get(relId));
        });

        StorageProperty prop;
        while(changed.hasNext()) {
            prop = (StorageProperty)changed.next();
            this.recordState.relChangeProperty(id, prop.propertyKeyId(), prop.value(), (Long) versions.get(prop.propertyKeyId()));
        }

        while(added.hasNext()) {
            prop = (StorageProperty)added.next();
            this.recordState.relAddProperty(id, prop.propertyKeyId(), prop.value(),(Long) versions.get(prop.propertyKeyId()));
        }
    }

    @Override
    public void visitNodeVersionChange(long var1, long var2) {
        this.recordState.nodeVersionChange(var1,var2);
    }

    @Override
    public void visitRelVersionChange(long var1, long var2) {
        this.recordState.relVersionChange(var1,var2);
    }

    @Override
    public void visitNodeVersionLabelChanges(long id, LongSet added, LongSet removed, LongLongMap labelVersionMap) throws ConstraintValidationException {
        removed.each((label) -> {
            this.recordState.removeLabelFromNode(label, id);
        });
        added.each((label) -> {
            //this.recordState.addLabelToNode(label, id);
            this.recordState.addVersionLabelToNode(label,id,labelVersionMap.get(label));
        });
    }

    //DynamicGraph
    //**********************************************************

    public void visitDeletedRelationship(long id) {
        this.recordState.relDelete(id);
    }

    @Override
    public void visitNodePropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) {
        removed.each((propId) -> {
            this.recordState.nodeRemoveProperty(id, propId);
        });

        StorageProperty prop;
        while(changed.hasNext()) {
            prop = (StorageProperty)changed.next();
            this.recordState.nodeChangeProperty(id, prop.propertyKeyId(), prop.value());
        }

        while(added.hasNext()) {
            prop = (StorageProperty)added.next();
            this.recordState.nodeAddProperty(id, prop.propertyKeyId(), prop.value());
        }

    }

    @Override
    public void visitRelPropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) {
        removed.each((relId) -> {
            this.recordState.relRemoveProperty(id, relId);
        });

        StorageProperty prop;
        while(changed.hasNext()) {
            prop = (StorageProperty)changed.next();
            this.recordState.relChangeProperty(id, prop.propertyKeyId(), prop.value());
        }

        while(added.hasNext()) {
            prop = (StorageProperty)added.next();
            this.recordState.relAddProperty(id, prop.propertyKeyId(), prop.value());
        }

    }

    public void visitGraphPropertyChanges(Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) {
        TransactionRecordState var10001 = this.recordState;
        removed.each(var10001::graphRemoveProperty);

        StorageProperty prop;
        while(changed.hasNext()) {
            prop = (StorageProperty)changed.next();
            this.recordState.graphChangeProperty(prop.propertyKeyId(), prop.value());
        }

        while(added.hasNext()) {
            prop = (StorageProperty)added.next();
            this.recordState.graphAddProperty(prop.propertyKeyId(), prop.value());
        }

    }

    public void visitNodeLabelChanges(long id, LongSet added, LongSet removed) {
        removed.each((label) -> {
            this.recordState.removeLabelFromNode(label, id);
        });
        added.each((label) -> {
            this.recordState.addLabelToNode(label, id);
        });
    }

    public void visitAddedIndex(IndexDescriptor index) {
        StoreIndexDescriptor rule = index.withId(this.schemaStorage.newRuleId());
        this.recordState.createSchemaRule(rule);
    }

    public void visitRemovedIndex(IndexDescriptor index) {
        Optional<String> name = index.getUserSuppliedName();
        StoreIndexDescriptor rule;
        if (name.isPresent()) {
            String indexName = (String)name.get();
            rule = this.schemaStorage.indexGetForName(indexName);
        } else {
            rule = this.schemaStorage.indexGetForSchema(index, true);
            if (rule == null) {
                rule = this.schemaStorage.indexGetForSchema(index, false);
            }
        }

        if (rule != null) {
            this.recordState.dropSchemaRule(rule);
        }

    }

    public void visitAddedConstraint(ConstraintDescriptor constraint) throws CreateConstraintFailureException {
        this.clearSchemaState = true;
        long constraintId = this.schemaStorage.newRuleId();
        switch(constraint.type()) {
            case UNIQUE:
                this.visitAddedUniquenessConstraint((UniquenessConstraintDescriptor)constraint, constraintId);
                break;
            case UNIQUE_EXISTS:
                this.visitAddedNodeKeyConstraint((NodeKeyConstraintDescriptor)constraint, constraintId);
                break;
            case EXISTS:
                this.recordState.createSchemaRule(this.constraintSemantics.createExistenceConstraint(this.schemaStorage.newRuleId(), constraint));
                break;
            default:
                throw new IllegalStateException(constraint.type().toString());
        }

    }

    private void visitAddedUniquenessConstraint(UniquenessConstraintDescriptor uniqueConstraint, long constraintId) {
        StoreIndexDescriptor indexRule = this.schemaStorage.indexGetForSchema(uniqueConstraint.ownedIndexDescriptor());
        this.recordState.createSchemaRule(this.constraintSemantics.createUniquenessConstraintRule(constraintId, uniqueConstraint, indexRule.getId()));
        this.recordState.setConstraintIndexOwner(indexRule, constraintId);
    }

    private void visitAddedNodeKeyConstraint(NodeKeyConstraintDescriptor uniqueConstraint, long constraintId) throws CreateConstraintFailureException {
        StoreIndexDescriptor indexRule = this.schemaStorage.indexGetForSchema(uniqueConstraint.ownedIndexDescriptor());
        this.recordState.createSchemaRule(this.constraintSemantics.createNodeKeyConstraintRule(constraintId, uniqueConstraint, indexRule.getId()));
        this.recordState.setConstraintIndexOwner(indexRule, constraintId);
    }

    public void visitRemovedConstraint(ConstraintDescriptor constraint) {
        this.clearSchemaState = true;

        try {
            this.recordState.dropSchemaRule(this.schemaStorage.constraintsGetSingle(constraint));
        } catch (SchemaRuleNotFoundException var3) {
            throw new IllegalStateException("Constraint to be removed should exist, since its existence should have been validated earlier and the schema should have been locked.");
        } catch (DuplicateSchemaRuleException var4) {
            throw new IllegalStateException("Multiple constraints found for specified label and property.");
        }

        if (constraint.enforcesUniqueness()) {
            this.visitRemovedIndex(((IndexBackedConstraintDescriptor)constraint).ownedIndexDescriptor());
        }

    }

    public void visitCreatedLabelToken(long id, String name) {
        this.recordState.createLabelToken(name, id);
    }

    public void visitCreatedPropertyKeyToken(long id, String name) {
        this.recordState.createPropertyKeyToken(name, id);
    }

    public void visitCreatedRelationshipTypeToken(long id, String name) {
        this.recordState.createRelationshipTypeToken(name, id);
    }

}
