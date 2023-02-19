//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import java.nio.ByteBuffer;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.GeometryType;
import org.neo4j.kernel.impl.store.LongerShortString;
import org.neo4j.kernel.impl.store.PropertyStore;
import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.kernel.impl.store.ShortArray;
import org.neo4j.kernel.impl.store.TemporalType;
import org.neo4j.kernel.impl.store.record.PropertyBlock;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.util.Bits;
import org.neo4j.storageengine.api.StoragePropertyCursor;
import org.neo4j.string.UTF8;
import org.neo4j.values.storable.ArrayValue;
import org.neo4j.values.storable.BooleanValue;
import org.neo4j.values.storable.ByteValue;
import org.neo4j.values.storable.DoubleValue;
import org.neo4j.values.storable.FloatValue;
import org.neo4j.values.storable.IntValue;
import org.neo4j.values.storable.LongValue;
import org.neo4j.values.storable.ShortValue;
import org.neo4j.values.storable.TextValue;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueGroup;
import org.neo4j.values.storable.Values;

class RecordPropertyCursor extends PropertyRecord implements StoragePropertyCursor {
    private static final int MAX_BYTES_IN_SHORT_STRING_OR_SHORT_ARRAY = 32;
    private static final int INITIAL_POSITION = -1;
    private final PropertyStore read;
    private long next;
    private long nextHistory;
    private int block;
    public ByteBuffer buffer;
    private PageCursor page;
    private PageCursor stringPage;
    private PageCursor arrayPage;
    private boolean open;

    RecordPropertyCursor(PropertyStore read) {
        super(-1L);
        this.read = read;
    }

    public void init(long reference) {
        if (this.getId() != -1L) {
            this.clear();
        }

        this.block = 2147483647;
        if (reference != -1L && this.page == null) {
            this.page = this.propertyPage(reference);
        }

        this.next = reference;
        this.open = true;
        //this.nextHistory = this.getNextHisProp();
    }

    public boolean next() {
        while(true) {
            int numberOfBlocks = this.getNumberOfBlocks();
            if (this.block < numberOfBlocks) {
                label25: {
                    if (this.block == -1) {
                        this.block = 0;
                    } else {
                        long current = this.currentBlock();
                        PropertyType type = PropertyType.getPropertyTypeOrNull(current);
                        if (type == null) {
                            break label25;
                        }

                        this.block += type.calculateNumberOfBlocksUsed(current);
                    }

                    if (this.block < numberOfBlocks && this.type() != null) {
                        return true;
                    }
                }
            }

            if (this.next == -1L) {
                return false;
            }

            this.property(this, this.next, this.page);
            this.next = this.getNextProp();
            this.block = -1;
        }
    }

    private long currentBlock() {
        return this.getBlocks()[this.block];
    }

    public void reset() {
        if (this.open) {
            this.open = false;
            this.clear();
        }

    }

    public int propertyKey() {
        return PropertyBlock.keyIndexId(this.currentBlock());
    }

    @Override
    public boolean nextHistory() {
        while(true) {
            int numberOfBlocks = this.getNumberOfBlocks();
            if (this.block < numberOfBlocks) {
                label25: {
                    if (this.block == -1) {
                        this.block = 0;
                    } else {
                        long current = this.currentBlock();
                        PropertyType type = PropertyType.getPropertyTypeOrNull(current);
                        if (type == null) {
                            break label25;
                        }

                        this.block += type.calculateNumberOfBlocksUsed(current);
                    }

                    if (this.block < numberOfBlocks && this.type() != null) {
                        return true;
                    }
                }
            }
            this.nextHistory = this.getNextHisProp();
            if (this.nextHistory == -1L) {
                return false;
            }

            this.property(this, this.nextHistory, this.page);
            //this.nextHistory = this.getNextHisProp();
            this.block = -1;
        }
    }

    @Override
    public long propertyVersion() {
       return this.getVersion();
    }


    public ValueGroup propertyType() {
        PropertyType type = this.type();
        if (type == null) {
            return ValueGroup.NO_VALUE;
        } else {
            switch(type) {
                case BOOL:
                    return ValueGroup.BOOLEAN;
                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                    return ValueGroup.NUMBER;
                case STRING:
                case CHAR:
                case SHORT_STRING:
                    return ValueGroup.TEXT;
                case TEMPORAL:
                case GEOMETRY:
                case SHORT_ARRAY:
                case ARRAY:
                    return this.propertyValue().valueGroup();
                default:
                    throw new UnsupportedOperationException("not implemented");
            }
        }
    }

    private PropertyType type() {
        return PropertyType.getPropertyTypeOrNull(this.currentBlock());
    }

    public Value propertyValue() {
        return this.readValue();
    }

    private Value readValue() {
        PropertyType type = this.type();
        if (type == null) {
            return Values.NO_VALUE;
        } else {
            switch(type) {
                case BOOL:
                    return this.readBoolean();
                case BYTE:
                    return this.readByte();
                case SHORT:
                    return this.readShort();
                case INT:
                    return this.readInt();
                case LONG:
                    return this.readLong();
                case FLOAT:
                    return this.readFloat();
                case DOUBLE:
                    return this.readDouble();
                case STRING:
                    return this.readLongString();
                case CHAR:
                    return this.readChar();
                case SHORT_STRING:
                    return this.readShortString();
                case TEMPORAL:
                    return this.temporalValue();
                case GEOMETRY:
                    return this.geometryValue();
                case SHORT_ARRAY:
                    return this.readShortArray();
                case ARRAY:
                    return this.readLongArray();
                default:
                    throw new IllegalStateException("Unsupported PropertyType: " + type.name());
            }
        }
    }

    private Value geometryValue() {
        return GeometryType.decode(this.getBlocks(), this.block);
    }

    private Value temporalValue() {
        return TemporalType.decode(this.getBlocks(), this.block);
    }

    private ArrayValue readLongArray() {
        long reference = PropertyBlock.fetchLong(this.currentBlock());
        if (this.arrayPage == null) {
            this.arrayPage = this.arrayPage(reference);
        }

        return this.array(this, reference, this.arrayPage);
    }

    private TextValue readLongString() {
        long reference = PropertyBlock.fetchLong(this.currentBlock());
        if (this.stringPage == null) {
            this.stringPage = this.stringPage(reference);
        }

        return this.string(this, reference, this.stringPage);
    }

    private Value readShortArray() {
        Bits bits = Bits.bits(32);
        int blocksUsed = ShortArray.calculateNumberOfBlocksUsed(this.currentBlock());

        for(int i = 0; i < blocksUsed; ++i) {
            bits.put(this.getBlocks()[this.block + i]);
        }

        return ShortArray.decode(bits);
    }

    private TextValue readShortString() {
        return LongerShortString.decode(this.getBlocks(), this.block, LongerShortString.calculateNumberOfBlocksUsed(this.currentBlock()));
    }

    private TextValue readChar() {
        return Values.charValue((char)PropertyBlock.fetchShort(this.currentBlock()));
    }

    private DoubleValue readDouble() {
        return Values.doubleValue(Double.longBitsToDouble(this.getBlocks()[this.block + 1]));
    }

    private FloatValue readFloat() {
        return Values.floatValue(Float.intBitsToFloat(PropertyBlock.fetchInt(this.currentBlock())));
    }

    private LongValue readLong() {
        return PropertyBlock.valueIsInlined(this.currentBlock()) ? Values.longValue(PropertyBlock.fetchLong(this.currentBlock()) >>> 1) : Values.longValue(this.getBlocks()[this.block + 1]);
    }

    private IntValue readInt() {
        return Values.intValue(PropertyBlock.fetchInt(this.currentBlock()));
    }

    private ShortValue readShort() {
        return Values.shortValue(PropertyBlock.fetchShort(this.currentBlock()));
    }

    private ByteValue readByte() {
        return Values.byteValue(PropertyBlock.fetchByte(this.currentBlock()));
    }

    private BooleanValue readBoolean() {
        return Values.booleanValue(PropertyBlock.fetchByte(this.currentBlock()) == 1);
    }

    public String toString() {
        return !this.open ? "PropertyCursor[closed state]" : "PropertyCursor[id=" + this.getId() + ", open state with: block=" + this.block + ", next=" + this.next + ", underlying record=" + super.toString() + "]";
    }

    public void close() {
        if (this.stringPage != null) {
            this.stringPage.close();
            this.stringPage = null;
        }

        if (this.arrayPage != null) {
            this.arrayPage.close();
            this.arrayPage = null;
        }

        if (this.page != null) {
            this.page.close();
            this.page = null;
        }

    }

    private PageCursor propertyPage(long reference) {
        return this.read.openPageCursorForReading(reference);
    }

    private PageCursor stringPage(long reference) {
        return this.read.openStringPageCursor(reference);
    }

    private PageCursor arrayPage(long reference) {
        return this.read.openArrayPageCursor(reference);
    }

    private void property(PropertyRecord record, long reference, PageCursor pageCursor) {
        this.read.getRecordByCursor(reference, record, RecordLoad.FORCE, pageCursor);
    }

    private TextValue string(RecordPropertyCursor cursor, long reference, PageCursor page) {
        ByteBuffer buffer = cursor.buffer = this.read.loadString(reference, cursor.buffer, page);
        buffer.flip();
        return Values.stringValue(UTF8.decode(buffer.array(), 0, buffer.limit()));
    }

    private ArrayValue array(RecordPropertyCursor cursor, long reference, PageCursor page) {
        ByteBuffer buffer = cursor.buffer = this.read.loadArray(reference, cursor.buffer, page);
        buffer.flip();
        return PropertyStore.readArrayFromBuffer(buffer);
    }
}
