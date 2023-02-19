//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.storageengine.api.txstate;

import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.map.primitive.LongLongMap;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo4j.internal.kernel.api.schema.constraints.ConstraintDescriptor;
import org.neo4j.storageengine.api.StorageProperty;
import org.neo4j.storageengine.api.schema.IndexDescriptor;

import java.util.Iterator;
import java.util.function.Function;

public interface TxStateVisitor extends AutoCloseable {
    TxStateVisitor EMPTY = new Adapter();
    Decorator NO_DECORATION = (txStateVisitor) -> {
        return txStateVisitor;
    };
    //DynamicGraph
    //**********************************************************
    void visitNodeVersionChange(long var1, long var2);
    void visitRelVersionChange(long var1, long var2);
    void visitNodeVersionLabelChanges(long var1, LongSet var3, LongSet var4, LongLongMap var5) throws ConstraintValidationException;
    void visitNodePropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5, LongObjectMap versions) throws ConstraintValidationException;

    void visitRelPropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5, LongObjectMap versions) throws ConstraintValidationException;


    //DynamicGraph
    //**********************************************************
    void visitCreatedNode(long var1);

    void visitDeletedNode(long var1);

    void visitCreatedRelationship(long var1, int var3, long var4, long var6) throws ConstraintValidationException;

    void visitDeletedRelationship(long var1);

    void visitNodePropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5) throws ConstraintValidationException;

    void visitRelPropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5) throws ConstraintValidationException;

    void visitGraphPropertyChanges(Iterator<StorageProperty> var1, Iterator<StorageProperty> var2, IntIterable var3);

    void visitNodeLabelChanges(long var1, LongSet var3, LongSet var4) throws ConstraintValidationException;

    void visitAddedIndex(IndexDescriptor var1);

    void visitRemovedIndex(IndexDescriptor var1);

    void visitAddedConstraint(ConstraintDescriptor var1) throws CreateConstraintFailureException;

    void visitRemovedConstraint(ConstraintDescriptor var1);

    void visitCreatedLabelToken(long var1, String var3);

    void visitCreatedPropertyKeyToken(long var1, String var3);

    void visitCreatedRelationshipTypeToken(long var1, String var3);

    void close();

    public interface Decorator extends Function<TxStateVisitor, TxStateVisitor> {
    }

    public static class Delegator implements TxStateVisitor {
        private final TxStateVisitor actual;

        public Delegator(TxStateVisitor actual) {
            assert actual != null;

            this.actual = actual;
        }

        //DynamicGraph
        //**********************************************************

        @Override
        public void visitNodeVersionChange(long var1, long var2) {
            this.actual.visitNodeVersionChange(var1,var2);
        }

        @Override
        public void visitRelVersionChange(long var1, long var2) {
            this.actual.visitRelVersionChange(var1,var2);
        }

        @Override
        public void visitNodeVersionLabelChanges(long var1, LongSet var3, LongSet var4, LongLongMap var5) throws ConstraintValidationException {
            this.actual.visitNodeVersionLabelChanges(var1,var3,var4,var5);
        }

        @Override
        public void visitNodePropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5, LongObjectMap versions) throws ConstraintValidationException {
            this.actual.visitNodePropertyChanges(var1,var3,var4,var5,versions);
        }

        @Override
        public void visitRelPropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5, LongObjectMap versions) throws ConstraintValidationException {
            this.actual.visitRelPropertyChanges(var1,var3,var4,var5,versions);
        }

        //DynamicGraph
        //**********************************************************

        public void visitCreatedNode(long id) {
            this.actual.visitCreatedNode(id);
        }

        public void visitDeletedNode(long id) {
            this.actual.visitDeletedNode(id);
        }

        public void visitCreatedRelationship(long id, int type, long startNode, long endNode) throws ConstraintValidationException {
            this.actual.visitCreatedRelationship(id, type, startNode, endNode);
        }

        public void visitDeletedRelationship(long id) {
            this.actual.visitDeletedRelationship(id);
        }

        public void visitNodePropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) throws ConstraintValidationException {
            this.actual.visitNodePropertyChanges(id, added, changed, removed);
        }

        public void visitRelPropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) throws ConstraintValidationException {
            this.actual.visitRelPropertyChanges(id, added, changed, removed);
        }

        public void visitGraphPropertyChanges(Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) {
            this.actual.visitGraphPropertyChanges(added, changed, removed);
        }

        public void visitNodeLabelChanges(long id, LongSet added, LongSet removed) throws ConstraintValidationException {
            this.actual.visitNodeLabelChanges(id, added, removed);
        }

        public void visitAddedIndex(IndexDescriptor index) {
            this.actual.visitAddedIndex(index);
        }

        public void visitRemovedIndex(IndexDescriptor index) {
            this.actual.visitRemovedIndex(index);
        }

        public void visitAddedConstraint(ConstraintDescriptor constraint) throws CreateConstraintFailureException {
            this.actual.visitAddedConstraint(constraint);
        }

        public void visitRemovedConstraint(ConstraintDescriptor constraint) {
            this.actual.visitRemovedConstraint(constraint);
        }

        public void visitCreatedLabelToken(long id, String name) {
            this.actual.visitCreatedLabelToken(id, name);
        }

        public void visitCreatedPropertyKeyToken(long id, String name) {
            this.actual.visitCreatedPropertyKeyToken(id, name);
        }

        public void visitCreatedRelationshipTypeToken(long id, String name) {
            this.actual.visitCreatedRelationshipTypeToken(id, name);
        }

        public void close() {
            this.actual.close();
        }
    }

    public static class Adapter implements TxStateVisitor {
        public Adapter() {
        }


        //DynamicGraph
        //**********************************************************

        @Override
        public void visitNodePropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5, LongObjectMap versions) throws ConstraintValidationException {
            //this.actual.visitNodePropertyChanges(var1,var3,var4,var5,versions);
        }

        @Override
        public void visitRelPropertyChanges(long var1, Iterator<StorageProperty> var3, Iterator<StorageProperty> var4, IntIterable var5, LongObjectMap versions) throws ConstraintValidationException {
            //this.actual.visitNodePropertyChanges(var1,var3,var4,var5,versions);
        }




        @Override
        public void visitNodeVersionChange(long var1, long var2) {

        }

        @Override
        public void visitRelVersionChange(long var1, long var2) {

        }

        @Override
        public void visitNodeVersionLabelChanges(long var1, LongSet var3, LongSet var4, LongLongMap var5) throws ConstraintValidationException {

        }

        //DynamicGraph
        //**********************************************************

        public void visitCreatedNode(long id) {
        }

        public void visitDeletedNode(long id) {
        }

        public void visitCreatedRelationship(long id, int type, long startNode, long endNode) {
        }

        public void visitDeletedRelationship(long id) {
        }

        public void visitNodePropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) {
        }

        public void visitRelPropertyChanges(long id, Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) {
        }

        public void visitGraphPropertyChanges(Iterator<StorageProperty> added, Iterator<StorageProperty> changed, IntIterable removed) {
        }

        public void visitNodeLabelChanges(long id, LongSet added, LongSet removed) {
        }

        public void visitAddedIndex(IndexDescriptor index) {
        }

        public void visitRemovedIndex(IndexDescriptor index) {
        }

        public void visitAddedConstraint(ConstraintDescriptor element) throws CreateConstraintFailureException {
        }

        public void visitRemovedConstraint(ConstraintDescriptor element) {
        }

        public void visitCreatedLabelToken(long id, String name) {
        }

        public void visitCreatedPropertyKeyToken(long id, String name) {
        }

        public void visitCreatedRelationshipTypeToken(long id, String name) {
        }

        public void close() {
        }
    }
}
