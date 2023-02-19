//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.format.standard;

import java.util.Iterator;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.kernel.impl.store.format.BaseRecordFormat;
import org.neo4j.kernel.impl.store.record.PropertyBlock;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RecordLoad;

public class PropertyRecordFormat extends BaseRecordFormat<PropertyRecord> {
    public static final int DEFAULT_DATA_BLOCK_SIZE = 120;
    public static final int DEFAULT_PAYLOAD_SIZE = 32;
    //public static final int RECORD_SIZE = 41;
    public static final int RECORD_SIZE = 53;

    //public PropertyRecordFormat() { super(fixedRecordSize(41), 0, 36); }
    public PropertyRecordFormat() { super(fixedRecordSize(53), 0, 36); }

    public PropertyRecord newRecord() {
        return new PropertyRecord(-1L);
    }

    public void read(PropertyRecord record, PageCursor cursor, RecordLoad mode, int recordSize) {
        int offsetAtBeginning = cursor.getOffset();
        byte modifiers = cursor.getByte();
        long prevMod = ((long)modifiers & 240L) << 28;
        long nextMod = ((long)modifiers & 15L) << 32;
        long prevProp = (long)cursor.getInt() & 4294967295L;
        long nextProp = (long)cursor.getInt() & 4294967295L;
        long version = (long)cursor.getInt() & 4294967295L;
        long prevHistProp = (long)cursor.getInt() & 4294967295L;
        long nextHisProp = (long)cursor.getInt() & 4294967295L;
        //record.initialize(false, BaseRecordFormat.longFromIntAndMod(prevProp, prevMod), BaseRecordFormat.longFromIntAndMod(nextProp, nextMod));
        //record.initialize(false, BaseRecordFormat.longFromIntAndMod(prevProp, prevMod), BaseRecordFormat.longFromIntAndMod(nextProp, nextMod));
        record.initialize(false, BaseRecordFormat.longFromIntAndMod(prevProp, prevMod), BaseRecordFormat.longFromIntAndMod(nextProp, nextMod),version,BaseRecordFormat.longFromIntAndMod(prevHistProp, 0),BaseRecordFormat.longFromIntAndMod(nextHisProp, 0));

        while(cursor.getOffset() - offsetAtBeginning < 53) {
            long block = cursor.getLong();
            PropertyType type = PropertyType.getPropertyTypeOrNull(block);
            if (type == null) {
                break;
            }

            record.setInUse(true);
            record.addLoadedBlock(block);
            int numberOfBlocksUsed = type.calculateNumberOfBlocksUsed(block);
            if (numberOfBlocksUsed == -1) {
                cursor.setCursorException("Invalid type or encoding of property block: " + block + " (type = " + type + ")");
                return;
            }

            int additionalBlocks = numberOfBlocksUsed - 1;
            if (additionalBlocks * 8 > 53 - (cursor.getOffset() - offsetAtBeginning)) {
                cursor.setCursorException("PropertyRecord claims to have more property blocks than can fit in a record");
                return;
            }

            while(additionalBlocks-- > 0) {
                record.addLoadedBlock(cursor.getLong());
            }
        }

    }

    public void write(PropertyRecord record, PageCursor cursor, int recordSize) {
        if (record.inUse()) {
            short prevModifier = record.getPrevProp() == (long)Record.NO_NEXT_RELATIONSHIP.intValue() ? 0 : (short)((int)((record.getPrevProp() & 64424509440L) >> 28));
            short nextModifier = record.getNextProp() == (long)Record.NO_NEXT_RELATIONSHIP.intValue() ? 0 : (short)((int)((record.getNextProp() & 64424509440L) >> 32));
            byte modifiers = (byte)(prevModifier | nextModifier);
            cursor.putByte(modifiers);
            cursor.putInt((int)record.getPrevProp());
            cursor.putInt((int)record.getNextProp());
            cursor.putInt((int)record.getVersion());
            cursor.putInt((int)record.getPrevHisProp());
            cursor.putInt((int)record.getNextHisProp());
            int longsAppended = 0;

            long[] propBlockValues;
            for(Iterator var8 = record.iterator(); var8.hasNext(); longsAppended += propBlockValues.length) {
                PropertyBlock block = (PropertyBlock)var8.next();
                propBlockValues = block.getValueBlocks();
                long[] var11 = propBlockValues;
                int var12 = propBlockValues.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    long propBlockValue = var11[var13];
                    cursor.putLong(propBlockValue);
                }
            }

            if (longsAppended < PropertyType.getPayloadSizeLongs()) {
                cursor.putLong(0L);
            }
        } else {
            cursor.setOffset(cursor.getOffset() + 9);
            cursor.putLong(0L);
        }

    }

    public long getNextRecordReference(PropertyRecord record) {
        return record.getNextProp();
    }

    public boolean isInUse(PageCursor cursor) {
        cursor.setOffset(cursor.getOffset() + 1 + 4 + 4);
        int blocks = PropertyType.getPayloadSizeLongs();

        for(int i = 0; i < blocks; ++i) {
            long block = cursor.getLong();
            if (i == 0 && block == 0L) {
                return false;
            }

            if (PropertyType.getPropertyTypeOrNull(block) != null) {
                return true;
            }
        }

        return false;
    }
}
