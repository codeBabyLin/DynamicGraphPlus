//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.ToIntFunction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.GeometryType.GeometryHeader;
import org.neo4j.kernel.impl.store.RecordStore.Processor;
import org.neo4j.kernel.impl.store.TemporalType.TemporalHeader;
import org.neo4j.kernel.impl.store.format.Capability;
import org.neo4j.kernel.impl.store.format.RecordFormats;
import org.neo4j.kernel.impl.store.format.UnsupportedFormatCapabilityException;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.PropertyBlock;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.util.Bits;
import org.neo4j.logging.LogProvider;
import org.neo4j.string.UTF8;
import org.neo4j.values.storable.ArrayValue;
import org.neo4j.values.storable.ByteArray;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.TextArray;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;
import org.neo4j.values.storable.ValueWriter.ArrayType;

public class PropertyStore extends CommonAbstractStore<PropertyRecord, NoStoreHeader> {
    public static final String TYPE_DESCRIPTOR = "PropertyStore";
    private final DynamicStringStore stringStore;
    private final PropertyKeyTokenStore propertyKeyTokenStore;
    private final DynamicArrayStore arrayStore;
    private final boolean allowStorePointsAndTemporal;

    public PropertyStore(File file, File idFile, Config configuration, IdGeneratorFactory idGeneratorFactory, PageCache pageCache, LogProvider logProvider, DynamicStringStore stringPropertyStore, PropertyKeyTokenStore propertyKeyTokenStore, DynamicArrayStore arrayPropertyStore, RecordFormats recordFormats, OpenOption... openOptions) {
        super(file, idFile, configuration, IdType.PROPERTY, idGeneratorFactory, pageCache, logProvider, "PropertyStore", recordFormats.property(), NoStoreHeaderFormat.NO_STORE_HEADER_FORMAT, recordFormats.storeVersion(), openOptions);
        this.stringStore = stringPropertyStore;
        this.propertyKeyTokenStore = propertyKeyTokenStore;
        this.arrayStore = arrayPropertyStore;
        this.allowStorePointsAndTemporal = recordFormats.hasCapability(Capability.POINT_PROPERTIES) && recordFormats.hasCapability(Capability.TEMPORAL_PROPERTIES);
    }

    public <FAILURE extends Exception> void accept(Processor<FAILURE> processor, PropertyRecord record) throws FAILURE {
        processor.processProperty(this, record);
    }

    public DynamicStringStore getStringStore() {
        return this.stringStore;
    }

    public DynamicArrayStore getArrayStore() {
        return this.arrayStore;
    }

    public PropertyKeyTokenStore getPropertyKeyTokenStore() {
        return this.propertyKeyTokenStore;
    }

    public void updateRecord(PropertyRecord record) {
        this.updatePropertyBlocks(record);
        super.updateRecord(record);
    }

    private void updatePropertyBlocks(PropertyRecord record) {
        if (record.inUse()) {
            Iterator var2 = record.iterator();

            while(var2.hasNext()) {
                PropertyBlock block = (PropertyBlock)var2.next();
                if (!block.isLight() && ((DynamicRecord)block.getValueRecords().get(0)).isCreated()) {
                    this.updateDynamicRecords(block.getValueRecords());
                }
            }
        }

        this.updateDynamicRecords(record.getDeletedRecords());
    }

    private void updateDynamicRecords(List<DynamicRecord> records) {
        Iterator var2 = records.iterator();

        while(var2.hasNext()) {
            DynamicRecord valueRecord = (DynamicRecord)var2.next();
            PropertyType recordType = valueRecord.getType();
            if (recordType == PropertyType.STRING) {
                this.stringStore.updateRecord(valueRecord);
            } else {
                if (recordType != PropertyType.ARRAY) {
                    throw new InvalidRecordException("Unknown dynamic record" + valueRecord);
                }

                this.arrayStore.updateRecord(valueRecord);
            }
        }

    }

    public void ensureHeavy(PropertyRecord record) {
        Iterator var2 = record.iterator();

        while(var2.hasNext()) {
            PropertyBlock block = (PropertyBlock)var2.next();
            this.ensureHeavy(block);
        }

    }

    public void ensureHeavy(PropertyBlock block) {
        if (block.isLight()) {
            PropertyType type = block.getType();
            RecordStore<DynamicRecord> dynamicStore = this.dynamicStoreForValueType(type);
            if (dynamicStore != null) {
                List<DynamicRecord> dynamicRecords = dynamicStore.getRecords(block.getSingleValueLong(), RecordLoad.NORMAL);
                Iterator var5 = dynamicRecords.iterator();

                while(var5.hasNext()) {
                    DynamicRecord dynamicRecord = (DynamicRecord)var5.next();
                    dynamicRecord.setType(type.intValue());
                }

                block.setValueRecords(dynamicRecords);
            }

        }
    }

    private RecordStore<DynamicRecord> dynamicStoreForValueType(PropertyType type) {
        switch(type) {
            case ARRAY:
                return this.arrayStore;
            case STRING:
                return this.stringStore;
            default:
                return null;
        }
    }

    public Value getValue(PropertyBlock propertyBlock) {
        return propertyBlock.getType().value(propertyBlock, this);
    }

    private static void allocateStringRecords(Collection<DynamicRecord> target, byte[] chars, DynamicRecordAllocator allocator) {
        AbstractDynamicStore.allocateRecordsFromBytes(target, chars, allocator);
    }

    private static void allocateArrayRecords(Collection<DynamicRecord> target, Object array, DynamicRecordAllocator allocator, boolean allowStorePoints) {
        DynamicArrayStore.allocateRecords(target, array, allocator, allowStorePoints);
    }

    public void encodeValue(PropertyBlock block, int keyId, Value value) {
        encodeValue(block, keyId, value, this.stringStore, this.arrayStore, this.allowStorePointsAndTemporal);
    }

    public static void encodeValue(PropertyBlock block, int keyId, Value value, DynamicRecordAllocator stringAllocator, DynamicRecordAllocator arrayAllocator, boolean allowStorePointsAndTemporal) {
        if (value instanceof ArrayValue) {
            Object asObject = value.asObject();
            if (ShortArray.encode(keyId, asObject, block, PropertyType.getPayloadSize())) {
                return;
            }

            List<DynamicRecord> arrayRecords = new ArrayList();
            allocateArrayRecords(arrayRecords, asObject, arrayAllocator, allowStorePointsAndTemporal);
            setSingleBlockValue(block, keyId, PropertyType.ARRAY, ((DynamicRecord)Iterables.first(arrayRecords)).getId());
            Iterator var8 = arrayRecords.iterator();

            while(var8.hasNext()) {
                DynamicRecord valueRecord = (DynamicRecord)var8.next();
                valueRecord.setType(PropertyType.ARRAY.intValue());
            }

            block.setValueRecords(arrayRecords);
        } else {
            value.writeTo(new PropertyBlockValueWriter(block, keyId, stringAllocator, allowStorePointsAndTemporal));
        }

    }

    public PageCursor openStringPageCursor(long reference) {
        return this.stringStore.openPageCursorForReading(reference);
    }

    public PageCursor openArrayPageCursor(long reference) {
        return this.arrayStore.openPageCursorForReading(reference);
    }

    public ByteBuffer loadString(long reference, ByteBuffer buffer, PageCursor page) {
        return readDynamic(this.stringStore, reference, buffer, page);
    }

    public ByteBuffer loadArray(long reference, ByteBuffer buffer, PageCursor page) {
        return readDynamic(this.arrayStore, reference, buffer, page);
    }

    private static ByteBuffer readDynamic(AbstractDynamicStore store, long reference, ByteBuffer buffer, PageCursor page) {
        if (buffer == null) {
            buffer = ByteBuffer.allocate(512);
        } else {
            buffer.clear();
        }

        DynamicRecord record = (DynamicRecord)store.newRecord();

        do {
            store.getRecordByCursor(reference, record, RecordLoad.FORCE, page);
            reference = record.getNextBlock();
            byte[] data = record.getData();
            if (buffer.remaining() < data.length) {
                buffer = grow(buffer, data.length);
            }

            buffer.put(data, 0, data.length);
        } while(reference != -1L);

        return buffer;
    }

    private static ByteBuffer grow(ByteBuffer buffer, int required) {
        buffer.flip();
        int capacity = buffer.capacity();

        do {
            capacity *= 2;
        } while(capacity - buffer.limit() < required);

        return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN).put(buffer);
    }

    public static void setSingleBlockValue(PropertyBlock block, int keyId, PropertyType type, long longValue) {
        block.setSingleBlock(singleBlockLongValue(keyId, type, longValue));
    }

    public static long singleBlockLongValue(int keyId, PropertyType type, long longValue) {
        return (long)keyId | (long)type.intValue() << 24 | longValue << 28;
    }

    public static byte[] encodeString(String string) {
        return UTF8.encode(string);
    }

    public static String decodeString(byte[] byteArray) {
        return UTF8.decode(byteArray);
    }

    String getStringFor(PropertyBlock propertyBlock) {
        this.ensureHeavy(propertyBlock);
        return this.getStringFor((Collection)propertyBlock.getValueRecords());
    }

    private String getStringFor(Collection<DynamicRecord> dynamicRecords) {
        Pair<byte[], byte[]> source = this.stringStore.readFullByteArray(dynamicRecords, PropertyType.STRING);
        return decodeString((byte[])source.other());
    }

    Value getArrayFor(PropertyBlock propertyBlock) {
        this.ensureHeavy(propertyBlock);
        return this.getArrayFor((Iterable)propertyBlock.getValueRecords());
    }

    private Value getArrayFor(Iterable<DynamicRecord> records) {
        return DynamicArrayStore.getRightArray(this.arrayStore.readFullByteArray(records, PropertyType.ARRAY));
    }

    public String toString() {
        return super.toString() + "[blocksPerRecord:" + PropertyType.getPayloadSizeLongs() + "]";
    }

    public Collection<PropertyRecord> getPropertyRecordChain(long firstRecordId) {
        long nextProp = firstRecordId;

        LinkedList toReturn;
        PropertyRecord propRecord;
        for(toReturn = new LinkedList(); nextProp != (long)Record.NO_NEXT_PROPERTY.intValue(); nextProp = propRecord.getNextProp()) {
            propRecord = new PropertyRecord(nextProp);
            this.getRecord(nextProp, propRecord, RecordLoad.NORMAL);
            toReturn.add(propRecord);
        }

        return toReturn;
    }

    public PropertyRecord newRecord() {
        return new PropertyRecord(-1L);
    }

    public boolean allowStorePointsAndTemporal() {
        return this.allowStorePointsAndTemporal;
    }

    public ToIntFunction<Value[]> newValueEncodedSizeCalculator() {
        return new PropertyValueRecordSizeCalculator(this);
    }

    public static ArrayValue readArrayFromBuffer(ByteBuffer buffer) {
        if (buffer.limit() <= 0) {
            throw new IllegalStateException("Given buffer is empty");
        } else {
            byte typeId = buffer.get();
            buffer.order(ByteOrder.BIG_ENDIAN);

            ArrayValue var5;
            try {
                if (typeId == PropertyType.STRING.intValue()) {
                    int arrayLength = buffer.getInt();
                    String[] result = new String[arrayLength];

                    for(int i = 0; i < arrayLength; ++i) {
                        int byteLength = buffer.getInt();
                        result[i] = UTF8.decode(buffer.array(), buffer.position(), byteLength);
                        buffer.position(buffer.position() + byteLength);
                    }

                    TextArray var19 = Values.stringArray(result);
                    return var19;
                }

                byte[] byteArray;
                ArrayValue var16;
                if (typeId == PropertyType.GEOMETRY.intValue()) {
                    GeometryHeader header = GeometryHeader.fromArrayHeaderByteBuffer(buffer);
                    byteArray = new byte[buffer.limit() - buffer.position()];
                    buffer.get(byteArray);
                    var16 = GeometryType.decodeGeometryArray(header, byteArray);
                    return var16;
                }

                if (typeId == PropertyType.TEMPORAL.intValue()) {
                    TemporalHeader header = TemporalHeader.fromArrayHeaderByteBuffer(buffer);
                    byteArray = new byte[buffer.limit() - buffer.position()];
                    buffer.get(byteArray);
                    var16 = TemporalType.decodeTemporalArray(header, byteArray);
                    return var16;
                }

                ShortArray type = ShortArray.typeOf(typeId);
                int bitsUsedInLastByte = buffer.get();
                int requiredBits = buffer.get();
                if (requiredBits != 0) {
                    if (type == ShortArray.BYTE && requiredBits == 8) {
                        //byte[] byteArray = new byte[buffer.limit() - buffer.position()];
                        byteArray = new byte[buffer.limit() - buffer.position()];
                        buffer.get(byteArray);
                        ByteArray var22 = Values.byteArray(byteArray);
                        return var22;
                    }

                    Bits bits = Bits.bitsFromBytes(buffer.array(), buffer.position());
                    int length = ((buffer.limit() - buffer.position()) * 8 - (8 - bitsUsedInLastByte)) / requiredBits;
                    ArrayValue var7 = type.createArray(length, bits, requiredBits);
                    return var7;
                }

                var5 = type.createEmptyArray();
            } finally {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }

            return var5;
        }
    }

    private static class PropertyBlockValueWriter extends TemporalValueWriterAdapter<IllegalArgumentException> {
        private final PropertyBlock block;
        private final int keyId;
        private final DynamicRecordAllocator stringAllocator;
        private final boolean allowStorePointsAndTemporal;

        PropertyBlockValueWriter(PropertyBlock block, int keyId, DynamicRecordAllocator stringAllocator, boolean allowStorePointsAndTemporal) {
            this.block = block;
            this.keyId = keyId;
            this.stringAllocator = stringAllocator;
            this.allowStorePointsAndTemporal = allowStorePointsAndTemporal;
        }

        public void writeNull() throws IllegalArgumentException {
            throw new IllegalArgumentException("Cannot write null values to the property store");
        }

        public void writeBoolean(boolean value) throws IllegalArgumentException {
            PropertyStore.setSingleBlockValue(this.block, this.keyId, PropertyType.BOOL, value ? 1L : 0L);
        }

        public void writeInteger(byte value) throws IllegalArgumentException {
            PropertyStore.setSingleBlockValue(this.block, this.keyId, PropertyType.BYTE, (long)value);
        }

        public void writeInteger(short value) throws IllegalArgumentException {
            PropertyStore.setSingleBlockValue(this.block, this.keyId, PropertyType.SHORT, (long)value);
        }

        public void writeInteger(int value) throws IllegalArgumentException {
            PropertyStore.setSingleBlockValue(this.block, this.keyId, PropertyType.INT, (long)value);
        }

        public void writeInteger(long value) throws IllegalArgumentException {
            long keyAndType = (long)this.keyId | (long)PropertyType.LONG.intValue() << 24;
            if (ShortArray.LONG.getRequiredBits(value) <= 35) {
                this.block.setSingleBlock(keyAndType | 268435456L | value << 29);
            } else {
                this.block.setValueBlocks(new long[]{keyAndType, value});
            }

        }

        public void writeFloatingPoint(float value) throws IllegalArgumentException {
            PropertyStore.setSingleBlockValue(this.block, this.keyId, PropertyType.FLOAT, (long)Float.floatToRawIntBits(value));
        }

        public void writeFloatingPoint(double value) throws IllegalArgumentException {
            this.block.setValueBlocks(new long[]{(long)this.keyId | (long)PropertyType.DOUBLE.intValue() << 24, Double.doubleToRawLongBits(value)});
        }

        public void writeString(String value) throws IllegalArgumentException {
            if (!LongerShortString.encode(this.keyId, value, this.block, PropertyType.getPayloadSize())) {
                byte[] encodedString = PropertyStore.encodeString(value);
                List<DynamicRecord> valueRecords = new ArrayList();
                PropertyStore.allocateStringRecords(valueRecords, encodedString, this.stringAllocator);
                PropertyStore.setSingleBlockValue(this.block, this.keyId, PropertyType.STRING, ((DynamicRecord)Iterables.first(valueRecords)).getId());
                Iterator var4 = valueRecords.iterator();

                while(var4.hasNext()) {
                    DynamicRecord valueRecord = (DynamicRecord)var4.next();
                    valueRecord.setType(PropertyType.STRING.intValue());
                }

                this.block.setValueRecords(valueRecords);
            }
        }

        public void writeString(char value) throws IllegalArgumentException {
            PropertyStore.setSingleBlockValue(this.block, this.keyId, PropertyType.CHAR, (long)value);
        }

        public void beginArray(int size, ArrayType arrayType) throws IllegalArgumentException {
            throw new IllegalArgumentException("Cannot persist arrays to property store using ValueWriter");
        }

        public void endArray() throws IllegalArgumentException {
            throw new IllegalArgumentException("Cannot persist arrays to property store using ValueWriter");
        }

        public void writeByteArray(byte[] value) throws IllegalArgumentException {
            throw new IllegalArgumentException("Cannot persist arrays to property store using ValueWriter");
        }

        public void writePoint(CoordinateReferenceSystem crs, double[] coordinate) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(GeometryType.encodePoint(this.keyId, crs, coordinate));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.POINT_PROPERTIES);
            }
        }

        public void writeDuration(long months, long days, long seconds, int nanos) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(TemporalType.encodeDuration(this.keyId, months, days, seconds, nanos));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.TEMPORAL_PROPERTIES);
            }
        }

        public void writeDate(long epochDay) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(TemporalType.encodeDate(this.keyId, epochDay));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.TEMPORAL_PROPERTIES);
            }
        }

        public void writeLocalTime(long nanoOfDay) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(TemporalType.encodeLocalTime(this.keyId, nanoOfDay));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.TEMPORAL_PROPERTIES);
            }
        }

        public void writeTime(long nanosOfDayUTC, int offsetSeconds) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(TemporalType.encodeTime(this.keyId, nanosOfDayUTC, offsetSeconds));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.TEMPORAL_PROPERTIES);
            }
        }

        public void writeLocalDateTime(long epochSecond, int nano) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(TemporalType.encodeLocalDateTime(this.keyId, epochSecond, (long)nano));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.TEMPORAL_PROPERTIES);
            }
        }

        public void writeDateTime(long epochSecondUTC, int nano, int offsetSeconds) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(TemporalType.encodeDateTime(this.keyId, epochSecondUTC, (long)nano, offsetSeconds));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.TEMPORAL_PROPERTIES);
            }
        }

        public void writeDateTime(long epochSecondUTC, int nano, String zoneId) throws IllegalArgumentException {
            if (this.allowStorePointsAndTemporal) {
                this.block.setValueBlocks(TemporalType.encodeDateTime(this.keyId, epochSecondUTC, (long)nano, zoneId));
            } else {
                throw new UnsupportedFormatCapabilityException(Capability.TEMPORAL_PROPERTIES);
            }
        }
    }
}
