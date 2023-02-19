//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.RelationshipStore;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.storageengine.api.RelationshipVisitor;
import org.neo4j.storageengine.api.StorageRelationshipCursor;

abstract class RecordRelationshipCursor extends RelationshipRecord implements RelationshipVisitor<RuntimeException>, StorageRelationshipCursor {
    final RelationshipStore relationshipStore;

    RecordRelationshipCursor(RelationshipStore relationshipStore) {
        super(-1L);
        this.relationshipStore = relationshipStore;
    }

    @Override
    public long relVersion() {
        return this.getVersion();
    }

    public long entityReference() {
        return this.getId();
    }

    public int type() {
        return this.getType();
    }

    public boolean hasProperties() {
        return this.nextProp != -1L;
    }

    public long sourceNodeReference() {
        return this.getFirstNode();
    }

    public long targetNodeReference() {
        return this.getSecondNode();
    }

    public long propertiesReference() {
        return this.getNextProp();
    }

    public void visit(long relationshipId, int typeId, long startNodeId, long endNodeId) {
        this.setId(relationshipId);
        this.initialize(true, -1L, startNodeId, endNodeId, typeId, -1L, -1L, -1L, -1L, false, false);
    }

    PageCursor relationshipPage(long reference) {
        return this.relationshipStore.openPageCursorForReading(reference);
    }

    void relationship(RelationshipRecord record, long reference, PageCursor pageCursor) {
        this.relationshipStore.getRecordByCursor(reference, record, RecordLoad.CHECK, pageCursor);
    }

    void relationshipFull(RelationshipRecord record, long reference, PageCursor pageCursor) {
        this.relationshipStore.getRecordByCursor(reference, record, RecordLoad.FORCE, pageCursor);
    }

    long relationshipHighMark() {
        return this.relationshipStore.getHighestPossibleIdInUse();
    }
}
