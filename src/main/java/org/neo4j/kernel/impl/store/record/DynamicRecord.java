//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.record;

import org.neo4j.kernel.impl.store.PropertyStore;
import org.neo4j.kernel.impl.store.PropertyType;

public class DynamicRecord extends AbstractBaseRecord {
    public static final byte[] NO_DATA = new byte[0];
    private static final int MAX_BYTES_IN_TO_STRING = 8;
    private static final int MAX_CHARS_IN_TO_STRING = 16;
    private byte[] data;
    private int length;
    private long nextBlock;
    private int type;
    private boolean startRecord;
    private long version;

    /** @deprecated */
    @Deprecated
    public static DynamicRecord dynamicRecord(long id, boolean inUse) {
        DynamicRecord record = new DynamicRecord(id);
        record.setInUse(inUse);
        return record;
    }

    /** @deprecated */
    @Deprecated
    public static DynamicRecord dynamicRecord(long id, boolean inUse, boolean isStartRecord, long nextBlock, int type, byte[] data) {
        DynamicRecord record = new DynamicRecord(id);
        record.setInUse(inUse);
        record.setStartRecord(isStartRecord);
        record.setNextBlock(nextBlock);
        record.setType(type);
        record.setData(data);
        return record;
    }

    public DynamicRecord(long id) {
        super(id);
    }

    public DynamicRecord initialize(boolean inUse, boolean isStartRecord, long nextBlock, int type, int length,long version) {
        this.version = version;
        return initialize(inUse,isStartRecord,nextBlock,type,length);
    }

    public DynamicRecord initialize(boolean inUse, boolean isStartRecord, long nextBlock, int type, int length) {
        super.initialize(inUse);
        this.startRecord = isStartRecord;
        this.nextBlock = nextBlock;
        this.type = type;
        this.data = NO_DATA;
        this.length = length;
        return this;
    }

    public void clear() {
        this.initialize(false, true, (long)Record.NO_NEXT_BLOCK.intValue(), -1, 0);
    }

    public void setStartRecord(boolean startRecord) {
        this.startRecord = startRecord;
    }

    public boolean isStartRecord() {
        return this.startRecord;
    }

    public PropertyType getType() {
        return PropertyType.getPropertyTypeOrNull((long)(this.type << 24));
    }

    public long getVersion(){return this.version;}
    public void setVersion(long version){
        this.version = version;
    }

    public int getTypeAsInt() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setInUse(boolean inUse, int type) {
        this.type = type;
        this.setInUse(inUse);
    }

    public void setData(byte[] data) {
        this.length = data.length;
        this.data = data;
    }

    public int getLength() {
        return this.length;
    }

    public byte[] getData() {
        return this.data;
    }

    public long getNextBlock() {
        return this.nextBlock;
    }

    public void setNextBlock(long nextBlock) {
        this.nextBlock = nextBlock;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("DynamicRecord[").append(this.getId()).append(",used=").append(this.inUse()).append(',').append('(').append(this.length).append("),type=");
        PropertyType type = this.getType();
        if (type == null) {
            buf.append(this.type);
        } else {
            buf.append(type.name());
        }

        buf.append(",data=");
        if (type == PropertyType.STRING && this.data.length <= 16) {
            buf.append('"');
            buf.append(PropertyStore.decodeString(this.data));
            buf.append("\",");
        } else {
            buf.append("byte[");
            if (this.data.length <= 8) {
                for(int i = 0; i < this.data.length; ++i) {
                    if (i != 0) {
                        buf.append(',');
                    }

                    buf.append(this.data[i]);
                }
            } else {
                buf.append("size=").append(this.data.length);
            }

            buf.append("],");
        }

        buf.append("start=").append(this.startRecord);
        buf.append(",next=").append(this.nextBlock).append(']');
        return buf.toString();
    }

    public DynamicRecord clone() {
        DynamicRecord clone = (new DynamicRecord(this.getId())).initialize(this.inUse(), this.startRecord, this.nextBlock, this.type, this.length);
        if (this.data != null) {
            clone.setData((byte[])this.data.clone());
        }

        return clone;
    }
}
