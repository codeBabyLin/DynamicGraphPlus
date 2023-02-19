//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.newapi;

import org.eclipse.collections.api.iterator.LongIterator;
import org.eclipse.collections.impl.iterator.ImmutableEmptyLongIterator;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.neo4j.internal.kernel.api.RelationshipScanCursor;
import org.neo4j.storageengine.api.StorageRelationshipScanCursor;

class DefaultRelationshipScanCursor extends DefaultRelationshipCursor<StorageRelationshipScanCursor> implements RelationshipScanCursor {
    private int type;
    private long single;
    private LongIterator addedRelationships;

    DefaultRelationshipScanCursor(DefaultCursors pool, StorageRelationshipScanCursor storeCursor) {
        super(pool, storeCursor);
    }

    void scan(int type, Read read) {
        ((StorageRelationshipScanCursor)this.storeCursor).scan(type);
        this.type = type;
        this.single = -1L;
        this.init(read);
        this.addedRelationships = ImmutableEmptyLongIterator.INSTANCE;
    }

    void single(long reference, Read read) {
        ((StorageRelationshipScanCursor)this.storeCursor).single(reference);
        this.type = -1;
        this.single = reference;
        this.init(read);
        this.addedRelationships = ImmutableEmptyLongIterator.INSTANCE;
    }

    public boolean next() {
        boolean hasChanges = this.hasChanges();
        if (hasChanges && this.addedRelationships.hasNext()) {
            this.read.txState().relationshipVisit(this.addedRelationships.next(), this.storeCursor);
            return true;
        } else {
            do {
                if (!((StorageRelationshipScanCursor)this.storeCursor).next()) {
                    return false;
                }
            } while(hasChanges && this.read.txState().relationshipIsDeletedInThisTx(((StorageRelationshipScanCursor)this.storeCursor).entityReference()));

            return true;
        }
    }

    public void close() {
        if (!this.isClosed()) {
            this.read = null;
            ((StorageRelationshipScanCursor)this.storeCursor).close();
            this.pool.accept(this);
        }

    }

    public boolean isClosed() {
        return this.read == null;
    }

    public String toString() {
        return this.isClosed() ? "RelationshipScanCursor[closed state]" : "RelationshipScanCursor[id=" + ((StorageRelationshipScanCursor)this.storeCursor).entityReference() + ", open state with: single=" + this.single + ", type=" + this.type + ", " + ((StorageRelationshipScanCursor)this.storeCursor).toString() + "]";
    }

    protected void collectAddedTxStateSnapshot() {
        if (this.isSingle()) {
            this.addedRelationships = (LongIterator)(this.read.txState().relationshipIsAddedInThisTx(this.single) ? LongHashSet.newSetWith(new long[]{this.single}).longIterator() : ImmutableEmptyLongIterator.INSTANCE);
        } else {
            this.addedRelationships = this.read.txState().addedAndRemovedRelationships().getAdded().longIterator();
        }

    }

    private boolean isSingle() {
        return this.single != -1L;
    }

    public void release() {
        ((StorageRelationshipScanCursor)this.storeCursor).close();
    }


}
