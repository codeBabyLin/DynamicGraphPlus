//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.format.standard;

import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.format.BaseOneByteHeaderRecordFormat;
import org.neo4j.kernel.impl.store.format.BaseRecordFormat;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RecordLoad;

public class NodeRecordFormat extends BaseOneByteHeaderRecordFormat<NodeRecord> {
    public static final int RECORD_SIZE = 19;

    public NodeRecordFormat() {
        super(fixedRecordSize(19), 0, 1, 35);
    }

    public NodeRecord newRecord() {
        return new NodeRecord(-1L);
    }

    public void read(NodeRecord record, PageCursor cursor, RecordLoad mode, int recordSize) {
        byte headerByte = cursor.getByte();
        boolean inUse = this.isInUse(headerByte);
        record.setInUse(inUse);
        if (mode.shouldLoad(inUse)) {
            long nextRel = (long)cursor.getInt() & 4294967295L;
            long nextProp = (long)cursor.getInt() & 4294967295L;
            long version = (long)cursor.getInt() & 4294967295L;
            long relModifier = ((long)headerByte & 14L) << 31;
            long propModifier = ((long)headerByte & 240L) << 28;
            long lsbLabels = (long)cursor.getInt() & 4294967295L;
            long hsbLabels = (long)(cursor.getByte() & 255);
            long labels = lsbLabels | hsbLabels << 32;
            byte extra = cursor.getByte();
            boolean dense = (extra & 1) > 0;
            record.initialize(inUse, BaseRecordFormat.longFromIntAndMod(nextProp, propModifier), dense, BaseRecordFormat.longFromIntAndMod(nextRel, relModifier), labels,version);
        } else {
            int nextOffset = cursor.getOffset() + recordSize - 1;
            cursor.setOffset(nextOffset);
        }

    }

    public void write(NodeRecord record, PageCursor cursor, int recordSize) {
        if (record.inUse()) {
            long nextRel = record.getNextRel();
            long nextProp = record.getNextProp();
            long version = record.getVersion();
            short relModifier = nextRel == (long)Record.NO_NEXT_RELATIONSHIP.intValue() ? 0 : (short)((int)((nextRel & 30064771072L) >> 31));
            short propModifier = nextProp == (long)Record.NO_NEXT_PROPERTY.intValue() ? 0 : (short)((int)((nextProp & 64424509440L) >> 28));
            short inUseUnsignedByte = (short)(record.inUse() ? Record.IN_USE : Record.NOT_IN_USE).byteValue();
            inUseUnsignedByte = (short)(inUseUnsignedByte | relModifier | propModifier);
            cursor.putByte((byte)inUseUnsignedByte);
            cursor.putInt((int)nextRel);
            cursor.putInt((int)nextProp);
            cursor.putInt((int)version);
            long labelField = record.getLabelField();
            cursor.putInt((int)labelField);
            cursor.putByte((byte)((int)((labelField & 1095216660480L) >> 32)));
            byte extra = (byte) (record.isDense() ? 1 : 0);
            cursor.putByte((byte)extra);
        } else {
            this.markAsUnused(cursor);
        }

    }
}
