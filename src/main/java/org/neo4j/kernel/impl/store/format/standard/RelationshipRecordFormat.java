//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.format.standard;

import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.format.BaseOneByteHeaderRecordFormat;
import org.neo4j.kernel.impl.store.format.BaseRecordFormat;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;

public class RelationshipRecordFormat extends BaseOneByteHeaderRecordFormat<RelationshipRecord> {
    public static final int RECORD_SIZE = 38;

    public RelationshipRecordFormat() {
        super(fixedRecordSize(38), 0, 1, 35);
    }

    public RelationshipRecord newRecord() {
        return new RelationshipRecord(-1L);
    }

    public void read(RelationshipRecord record, PageCursor cursor, RecordLoad mode, int recordSize) {
        byte headerByte = cursor.getByte();
        boolean inUse = this.isInUse(headerByte);
        record.setInUse(inUse);
        if (mode.shouldLoad(inUse)) {
            long firstNode = (long)cursor.getInt() & 4294967295L;
            long firstNodeMod = ((long)headerByte & 14L) << 31;
            long secondNode = (long)cursor.getInt() & 4294967295L;
            long typeInt = (long)cursor.getInt();
            long secondNodeMod = (typeInt & 1879048192L) << 4;
            int type = (int)(typeInt & 65535L);
            long firstPrevRel = (long)cursor.getInt() & 4294967295L;
            long firstPrevRelMod = (typeInt & 234881024L) << 7;
            long firstNextRel = (long)cursor.getInt() & 4294967295L;
            long firstNextRelMod = (typeInt & 29360128L) << 10;
            long secondPrevRel = (long)cursor.getInt() & 4294967295L;
            long secondPrevRelMod = (typeInt & 3670016L) << 13;
            long secondNextRel = (long)cursor.getInt() & 4294967295L;
            long secondNextRelMod = (typeInt & 458752L) << 16;
            long nextProp = (long)cursor.getInt() & 4294967295L;
            long version = (long)cursor.getInt() & 4294967295L;
            long nextPropMod = ((long)headerByte & 240L) << 28;
            byte extraByte = cursor.getByte();
            record.initialize(inUse, BaseRecordFormat.longFromIntAndMod(nextProp, nextPropMod), BaseRecordFormat.longFromIntAndMod(firstNode, firstNodeMod), BaseRecordFormat.longFromIntAndMod(secondNode, secondNodeMod), type, BaseRecordFormat.longFromIntAndMod(firstPrevRel, firstPrevRelMod), BaseRecordFormat.longFromIntAndMod(firstNextRel, firstNextRelMod), BaseRecordFormat.longFromIntAndMod(secondPrevRel, secondPrevRelMod), BaseRecordFormat.longFromIntAndMod(secondNextRel, secondNextRelMod), (extraByte & 1) != 0, (extraByte & 2) != 0,version);
        } else {
            int nextOffset = cursor.getOffset() + recordSize - 1;
            cursor.setOffset(nextOffset);
        }

    }

    public void write(RelationshipRecord record, PageCursor cursor, int recordSize) {
        if (record.inUse()) {
            long firstNode = record.getFirstNode();
            short firstNodeMod = (short)((int)((firstNode & 30064771072L) >> 31));
            long secondNode = record.getSecondNode();
            long secondNodeMod = (secondNode & 30064771072L) >> 4;
            long firstPrevRel = record.getFirstPrevRel();
            long firstPrevRelMod = firstPrevRel == (long)Record.NO_NEXT_RELATIONSHIP.intValue() ? 0L : (firstPrevRel & 30064771072L) >> 7;
            long firstNextRel = record.getFirstNextRel();
            long firstNextRelMod = firstNextRel == (long)Record.NO_NEXT_RELATIONSHIP.intValue() ? 0L : (firstNextRel & 30064771072L) >> 10;
            long secondPrevRel = record.getSecondPrevRel();
            long secondPrevRelMod = secondPrevRel == (long)Record.NO_NEXT_RELATIONSHIP.intValue() ? 0L : (secondPrevRel & 30064771072L) >> 13;
            long secondNextRel = record.getSecondNextRel();
            long secondNextRelMod = secondNextRel == (long)Record.NO_NEXT_RELATIONSHIP.intValue() ? 0L : (secondNextRel & 30064771072L) >> 16;
            long nextProp = record.getNextProp();
            long nextPropMod = nextProp == (long)Record.NO_NEXT_PROPERTY.intValue() ? 0L : (nextProp & 64424509440L) >> 28;
            short inUseUnsignedByte = (short)((int)((long)((record.inUse() ? Record.IN_USE : Record.NOT_IN_USE).byteValue() | firstNodeMod) | nextPropMod));
            int typeInt = (int)((long)record.getType() | secondNodeMod | firstPrevRelMod | firstNextRelMod | secondPrevRelMod | secondNextRelMod);
            long firstInStartNodeChain = record.isFirstInFirstChain() ? 1L : 0L;
            long firstInEndNodeChain = record.isFirstInSecondChain() ? 2L : 0L;
            long version = record.getVersion();
            byte extraByte = (byte)((int)(firstInEndNodeChain | firstInStartNodeChain));
            cursor.putByte((byte)inUseUnsignedByte);
            cursor.putInt((int)firstNode);
            cursor.putInt((int)secondNode);
            cursor.putInt(typeInt);
            cursor.putInt((int)firstPrevRel);
            cursor.putInt((int)firstNextRel);
            cursor.putInt((int)secondPrevRel);
            cursor.putInt((int)secondNextRel);
            cursor.putInt((int)nextProp);
            cursor.putInt((int)version);
            cursor.putByte(extraByte);
        } else {
            this.markAsUnused(cursor);
        }

    }
}
