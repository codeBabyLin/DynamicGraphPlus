//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import cn.DynamicGraph.Common.DGVersion;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.newapi.RelationshipReferenceEncoding;
import org.neo4j.kernel.impl.store.NodeLabelsField;
import org.neo4j.kernel.impl.store.NodeStore;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.storageengine.api.StorageNodeCursor;

import java.util.Map;

public class RecordNodeCursor extends NodeRecord implements StorageNodeCursor {
    private NodeStore read;
    private PageCursor pageCursor;
    private long next;
    private long highMark;
    private long nextStoreReference;
    private boolean open;

    RecordNodeCursor(NodeStore read) {
        super(-1L);
        this.read = read;
    }

    public void scan() {
        if (this.getId() != -1L) {
            this.resetState();
        }

        if (this.pageCursor == null) {
            this.pageCursor = this.nodePage(0L);
        }

        this.next = 0L;
        this.highMark = this.nodeHighMark();
        this.nextStoreReference = -1L;
        this.open = true;
    }

    public void single(long reference) {
        if (this.getId() != -1L) {
            this.resetState();
        }

        if (this.pageCursor == null) {
            this.pageCursor = this.nodePage(reference);
        }

        this.next = reference >= 0L ? reference : -1L;
        this.highMark = -1L;
        this.nextStoreReference = -1L;
        this.open = true;
    }

    public long entityReference() {
        return this.getId();
    }

    public long[] labels() {
        return NodeLabelsField.get(this, this.read);
    }

    public boolean hasLabel(int label) {
        long[] longs = NodeLabelsField.get(this, this.read);
        long[] var3 = longs;
        int var4 = longs.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            long labelToken = var3[var5];
            if (labelToken == (long)label) {
                assert (long)((int)labelToken) == labelToken : "value too big to be represented as and int";

                return true;
            }
        }

        return false;
    }

    public boolean hasProperties() {
        return this.nextProp != -1L;
    }

    public long relationshipGroupReference() {
        return this.isDense() ? this.getNextRel() : GroupReferenceEncoding.encodeRelationship(this.getNextRel());
    }

    public long allRelationshipsReference() {
        return this.isDense() ? RelationshipReferenceEncoding.encodeGroup(this.getNextRel()) : this.getNextRel();
    }

    public long propertiesReference() {
        return this.getNextProp();
    }

    public boolean next() {
        if (this.next == -1L) {
            this.resetState();
            return false;
        } else {
            do {
                if (this.nextStoreReference == this.next) {
                    this.nodeAdvance(this, this.pageCursor);
                    ++this.next;
                    ++this.nextStoreReference;
                } else {
                    this.node(this, (long)(this.next++), this.pageCursor);
                    this.nextStoreReference = this.next;
                }

                if (this.next > this.highMark) {
                    if (this.isSingle()) {
                        this.next = -1L;
                        return this.inUse();
                    }

                    this.highMark = this.nodeHighMark();
                    if (this.next > this.highMark) {
                        this.next = -1L;
                        return this.inUse();
                    }
                }
            } while(!this.inUse());

            return true;
        }
    }

    public void setCurrent(long nodeReference) {
        this.setId(nodeReference);
        this.setInUse(true);
    }



    //DynamicGraph
    //**************************

    @Override
    public void setCurrent(long nodeReference, long startVersion) {
        this.setVersion(DGVersion.setStartVersion(startVersion));
        this.setId(nodeReference);
        this.setInUse(true);
    }


    @Override
    public long nodeVersion() {
        return this.getVersion();
    }

    public long[] labels(long version) {
        return NodeLabelsField.get(this, this.read);
    }

    @Override
    public Map<Long, Long> versionLabels() {
        NodeLabelsField.getVersionLabels(this,this.read);
        return this.getVersionLabelsMap();
    }

    //DynamicGraph
    //**************************

    public void reset() {
        if (this.open) {
            this.open = false;
            this.resetState();
        }

    }

    private void resetState() {
        this.next = -1L;
        this.setId(-1L);
        this.clear();
    }

    private boolean isSingle() {
        return this.highMark == -1L;
    }

    public String toString() {
        return !this.open ? "RecordNodeCursor[closed state]" : "RecordNodeCursor[id=" + this.getId() + ", open state with: highMark=" + this.highMark + ", next=" + this.next + ", underlying record=" + super.toString() + "]";
    }

    public void close() {
        if (this.pageCursor != null) {
            this.pageCursor.close();
            this.pageCursor = null;
        }

    }

    private PageCursor nodePage(long reference) {
        return this.read.openPageCursorForReading(reference);
    }

    private long nodeHighMark() {
        return this.read.getHighestPossibleIdInUse();
    }

    private void node(NodeRecord record, long reference, PageCursor pageCursor) {
        this.read.getRecordByCursor(reference, record, RecordLoad.CHECK, pageCursor);
    }

    private void nodeAdvance(NodeRecord record, PageCursor pageCursor) {
        this.read.nextRecordByCursor(record, RecordLoad.CHECK, pageCursor);
    }
}
