//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.format.standard;

import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.format.BaseOneByteHeaderRecordFormat;
import org.neo4j.kernel.impl.store.format.BaseRecordFormat;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RecordLoad;

public class DynamicRecordFormat extends BaseOneByteHeaderRecordFormat<DynamicRecord> {
    public static final int RECORD_HEADER_SIZE = 8;

    public DynamicRecordFormat() {
        super(INT_STORE_HEADER_READER, 8, 16, 36);
    }

    public DynamicRecord newRecord() {
        return new DynamicRecord(-1L);
    }

    public void read(DynamicRecord record, PageCursor cursor, RecordLoad mode, int recordSize) {
        long firstInteger = (long)cursor.getInt() & 4294967295L;
        boolean isStartRecord = (firstInteger & -2147483648L) == 0L;
        boolean inUse = (firstInteger & 268435456L) != 0L;
        if (mode.shouldLoad(inUse)) {
            int dataSize = recordSize - this.getRecordHeaderSize();
            int nrOfBytes = (int)(firstInteger & 16777215L);
            if (nrOfBytes > recordSize) {
                cursor.setCursorException(payloadTooBigErrorMessage(record, recordSize, nrOfBytes));
                return;
            }

            long nextBlock = (long)cursor.getInt() & 4294967295L;
            long version = (long)cursor.getInt() & 4294967295L;
            long nextModifier = (firstInteger & 251658240L) << 8;
            long longNextBlock = BaseRecordFormat.longFromIntAndMod(nextBlock, nextModifier);
            record.initialize(inUse, isStartRecord, longNextBlock, -1, nrOfBytes,version);
            if (longNextBlock != (long)Record.NO_NEXT_BLOCK.intValue() && nrOfBytes < dataSize || nrOfBytes > dataSize) {
                cursor.setCursorException(this.illegalBlockSizeMessage(record, dataSize));
                return;
            }

            readData(record, cursor);
        } else {
            record.setInUse(inUse);
        }

    }

    public static String payloadTooBigErrorMessage(DynamicRecord record, int recordSize, int nrOfBytes) {
        return String.format("DynamicRecord[%s] claims to have a payload of %s bytes, which is larger than the record size of %s bytes.", record.getId(), nrOfBytes, recordSize);
    }

    private String illegalBlockSizeMessage(DynamicRecord record, int dataSize) {
        return String.format("Next block set[%d] current block illegal size[%d/%d]", record.getNextBlock(), record.getLength(), dataSize);
    }

    public static void readData(DynamicRecord record, PageCursor cursor) {
        int len = record.getLength();
        if (len == 0) {
            record.setData(DynamicRecord.NO_DATA);
        } else {
            byte[] data = record.getData();
            if (data == null || data.length != len) {
                data = new byte[len];
            }

            cursor.getBytes(data);
            record.setData(data);
        }
    }

    public void write(DynamicRecord record, PageCursor cursor, int recordSize) {
        if (record.inUse()) {
            long nextBlock = record.getNextBlock();
            long version = record.getVersion();
            int highByteInFirstInteger = nextBlock == (long)Record.NO_NEXT_BLOCK.intValue() ? 0 : (int)((nextBlock & 64424509440L) >> 8);
            highByteInFirstInteger |= Record.IN_USE.byteValue() << 28;
            highByteInFirstInteger |= (record.isStartRecord() ? 0 : 1) << 31;
            int firstInteger = record.getLength();

            assert firstInteger < 16777215;

            firstInteger |= highByteInFirstInteger;
            cursor.putInt(firstInteger);
            cursor.putInt((int)nextBlock);
            cursor.putInt((int)version);
            cursor.putBytes(record.getData());
        } else {
            cursor.putByte(Record.NOT_IN_USE.byteValue());
        }

    }

    public long getNextRecordReference(DynamicRecord record) {
        return record.getNextBlock();
    }
}
