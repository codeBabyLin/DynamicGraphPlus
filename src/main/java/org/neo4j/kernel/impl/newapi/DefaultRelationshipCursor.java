//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.newapi;

import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.RelationshipDataAccessor;
import org.neo4j.storageengine.api.StorageRelationshipCursor;

abstract class DefaultRelationshipCursor<STORECURSOR extends StorageRelationshipCursor> implements RelationshipDataAccessor {
    private boolean hasChanges;
    private boolean checkHasChanges;
    final DefaultCursors pool;
    Read read;
    final STORECURSOR storeCursor;

    DefaultRelationshipCursor(DefaultCursors pool, STORECURSOR storeCursor) {
        this.pool = pool;
        this.storeCursor = storeCursor;
    }

    protected void init(Read read) {
        this.read = read;
        this.checkHasChanges = true;
    }

    public long relVersion(){
        return storeCursor.relVersion();
    }
    public long relationshipReference() {
        return this.storeCursor.entityReference();
    }

    public int type() {
        return this.storeCursor.type();
    }

    public void source(NodeCursor cursor) {
        this.read.singleNode(this.sourceNodeReference(), cursor);
    }

    public void target(NodeCursor cursor) {
        this.read.singleNode(this.targetNodeReference(), cursor);
    }

    public void properties(PropertyCursor cursor) {
        ((DefaultPropertyCursor)cursor).initRelationship(this.relationshipReference(), this.propertiesReference(), this.read, this.read);
    }

    public long sourceNodeReference() {
        return this.storeCursor.sourceNodeReference();
    }

    public long targetNodeReference() {
        return this.storeCursor.targetNodeReference();
    }

    public long propertiesReference() {
        return this.storeCursor.propertiesReference();
    }

    protected abstract void collectAddedTxStateSnapshot();

    protected boolean hasChanges() {
        if (this.checkHasChanges) {
            this.hasChanges = this.read.hasTxStateWithChanges();
            if (this.hasChanges) {
                this.collectAddedTxStateSnapshot();
            }

            this.checkHasChanges = false;
        }

        return this.hasChanges;
    }
}
