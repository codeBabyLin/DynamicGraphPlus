package cn.DynamicGraph.kernel.impl.store.format.standard;

import cn.DynamicGraph.kernel.impl.store.record.DbVersionRecord;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.StoreHeader;
import org.neo4j.kernel.impl.store.format.BaseOneByteHeaderRecordFormat;
import org.neo4j.kernel.impl.store.record.RecordLoad;

import java.io.IOException;
import java.util.function.Function;

public class DbVersionRecordFormat extends BaseOneByteHeaderRecordFormat<DbVersionRecord> {
    public static final int RECORD_SIZE = 13;
    public DbVersionRecordFormat(Function<StoreHeader, Integer> recordSize, int recordHeaderSize, int inUseBitMaskForFirstByte, int idBits) {
        super(recordSize, recordHeaderSize, inUseBitMaskForFirstByte, idBits);
    }
    public DbVersionRecordFormat(){
        super(fixedRecordSize(13), 0, 1, 35);
    }

    @Override
    public DbVersionRecord newRecord() {
        return new DbVersionRecord(-1);
    }

    @Override
    public void read(DbVersionRecord record, PageCursor pageCursor, RecordLoad recordLoad, int i) throws IOException {
        byte header =pageCursor.getByte();
        boolean inuse = header == 1? true:false;
        long value = (long) pageCursor.getInt();
        long nodeCounts = (long)pageCursor.getInt();
        long relCounts = (long)pageCursor.getInt();
        record.initialize(inuse,value,nodeCounts,relCounts);
    }

    @Override
    public void write(DbVersionRecord record, PageCursor pageCursor, int i) throws IOException {
        //assert record.inUse();
        byte inuse = (byte) (record.inUse()?1:0);
        pageCursor.putByte(inuse);
        pageCursor.putInt((int) record.getValue());
        pageCursor.putInt((int) record.getNodeCounts());
        pageCursor.putInt((int) record.getRelCounts());
    }
}
