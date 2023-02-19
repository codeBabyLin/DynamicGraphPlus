//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.io.pagecache.PageCache;
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
import org.neo4j.kernel.impl.util.Bits;
import org.neo4j.logging.LogProvider;
import org.neo4j.values.storable.DurationValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

public class DynamicArrayStore extends AbstractDynamicStore {
    public static final int NUMBER_HEADER_SIZE = 3;
    public static final int STRING_HEADER_SIZE = 5;
    public static final int GEOMETRY_HEADER_SIZE = 6;
    public static final int TEMPORAL_HEADER_SIZE = 2;
    public static final String TYPE_DESCRIPTOR = "ArrayPropertyStore";
    private final boolean allowStorePointsAndTemporal;

    public DynamicArrayStore(File file, File idFile, Config configuration, IdType idType, IdGeneratorFactory idGeneratorFactory, PageCache pageCache, LogProvider logProvider, int dataSizeFromConfiguration, RecordFormats recordFormats, OpenOption... openOptions) {
        super(file, idFile, configuration, idType, idGeneratorFactory, pageCache, logProvider, "ArrayPropertyStore", dataSizeFromConfiguration, recordFormats.dynamic(), recordFormats.storeVersion(), openOptions);
        this.allowStorePointsAndTemporal = recordFormats.hasCapability(Capability.POINT_PROPERTIES) && recordFormats.hasCapability(Capability.TEMPORAL_PROPERTIES);
    }

    public <FAILURE extends Exception> void accept(Processor<FAILURE> processor, DynamicRecord record) throws FAILURE {
        processor.processArray(this, record);
    }

    public static byte[] encodeFromNumbers(Object array, int offsetBytes) {
        ShortArray type = ShortArray.typeOf(array);
        if (type == null) {
            throw new IllegalArgumentException(array + " not a valid array type.");
        } else {
            return type != ShortArray.DOUBLE && type != ShortArray.FLOAT ? createBitCompactedArray(type, array, offsetBytes) : createUncompactedArray(type, array, offsetBytes);
        }
    }

    private static byte[] createBitCompactedArray(ShortArray type, Object array, int offsetBytes) {
        Class<?> componentType = array.getClass().getComponentType();
        boolean isPrimitiveByteArray = componentType.equals(Byte.TYPE);
        boolean isByteArray = componentType.equals(Byte.class) || isPrimitiveByteArray;
        int arrayLength = Array.getLength(array);
        int requiredBits = isByteArray ? 8 : type.calculateRequiredBitsForArray(array, arrayLength);
        int totalBits = requiredBits * arrayLength;
        int bitsUsedInLastByte = totalBits % 8;
        bitsUsedInLastByte = bitsUsedInLastByte == 0 ? 8 : bitsUsedInLastByte;
        if (isByteArray) {
            return createBitCompactedByteArray(type, isPrimitiveByteArray, array, bitsUsedInLastByte, requiredBits, offsetBytes);
        } else {
            int numberOfBytes = (totalBits - 1) / 8 + 1;
            numberOfBytes += 3;
            Bits bits = Bits.bits(numberOfBytes);
            bits.put((byte)type.intValue());
            bits.put((byte)bitsUsedInLastByte);
            bits.put((byte)requiredBits);
            type.writeAll(array, arrayLength, requiredBits, bits);
            return bits.asBytes(offsetBytes);
        }
    }

    private static byte[] createBitCompactedByteArray(ShortArray type, boolean isPrimitiveByteArray, Object array, int bitsUsedInLastByte, int requiredBits, int offsetBytes) {
        int arrayLength = Array.getLength(array);
        byte[] bytes = new byte[3 + arrayLength + offsetBytes];
        bytes[offsetBytes + 0] = (byte)type.intValue();
        bytes[offsetBytes + 1] = (byte)bitsUsedInLastByte;
        bytes[offsetBytes + 2] = (byte)requiredBits;
        if (isPrimitiveByteArray) {
            System.arraycopy(array, 0, bytes, 3 + offsetBytes, arrayLength);
        } else {
            Byte[] source = (Byte[])((Byte[])array);

            for(int i = 0; i < source.length; ++i) {
                bytes[3 + offsetBytes + i] = source[i];
            }
        }

        return bytes;
    }

    private static byte[] createUncompactedArray(ShortArray type, Object array, int offsetBytes) {
        int arrayLength = Array.getLength(array);
        int bytesPerElement = type.maxBits / 8;
        byte[] bytes = new byte[3 + bytesPerElement * arrayLength + offsetBytes];
        bytes[offsetBytes + 0] = (byte)type.intValue();
        bytes[offsetBytes + 1] = 8;
        bytes[offsetBytes + 2] = (byte)type.maxBits;
        type.writeAll(array, bytes, 3 + offsetBytes);
        return bytes;
    }

    public static void allocateFromNumbers(Collection<DynamicRecord> target, Object array, DynamicRecordAllocator recordAllocator) {
        byte[] bytes = encodeFromNumbers(array, 0);
        allocateRecordsFromBytes(target, bytes, recordAllocator);
    }

    private static void allocateFromCompositeType(Collection<DynamicRecord> target, byte[] bytes, DynamicRecordAllocator recordAllocator, boolean allowsStorage, Capability storageCapability) {
        if (allowsStorage) {
            allocateRecordsFromBytes(target, bytes, recordAllocator);
        } else {
            throw new UnsupportedFormatCapabilityException(storageCapability);
        }
    }

    private static void allocateFromString(Collection<DynamicRecord> target, String[] array, DynamicRecordAllocator recordAllocator) {
        byte[][] stringsAsBytes = new byte[array.length][];
        int totalBytesRequired = 5;

        for(int i = 0; i < array.length; ++i) {
            String string = array[i];
            byte[] bytes = PropertyStore.encodeString(string);
            stringsAsBytes[i] = bytes;
            totalBytesRequired += 4 + bytes.length;
        }

        ByteBuffer buf = ByteBuffer.allocate(totalBytesRequired);
        buf.put(PropertyType.STRING.byteValue());
        buf.putInt(array.length);
        byte[][] var11 = stringsAsBytes;
        int var12 = stringsAsBytes.length;

        for(int var8 = 0; var8 < var12; ++var8) {
            byte[] stringAsBytes = var11[var8];
            buf.putInt(stringAsBytes.length);
            buf.put(stringAsBytes);
        }

        allocateRecordsFromBytes(target, buf.array(), recordAllocator);
    }

    public void allocateRecords(Collection<DynamicRecord> target, Object array) {
        allocateRecords(target, array, this, this.allowStorePointsAndTemporal);
    }

    public static void allocateRecords(Collection<DynamicRecord> target, Object array, DynamicRecordAllocator recordAllocator, boolean allowStorePointsAndTemporal) {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(array + " not an array");
        } else {
            Class<?> type = array.getClass().getComponentType();
            if (type.equals(String.class)) {
                allocateFromString(target, (String[])((String[])array), recordAllocator);
            } else if (type.equals(PointValue.class)) {
                allocateFromCompositeType(target, GeometryType.encodePointArray((PointValue[])((PointValue[])array)), recordAllocator, allowStorePointsAndTemporal, Capability.POINT_PROPERTIES);
            } else if (type.equals(LocalDate.class)) {
                allocateFromCompositeType(target, TemporalType.encodeDateArray((LocalDate[])((LocalDate[])array)), recordAllocator, allowStorePointsAndTemporal, Capability.TEMPORAL_PROPERTIES);
            } else if (type.equals(LocalTime.class)) {
                allocateFromCompositeType(target, TemporalType.encodeLocalTimeArray((LocalTime[])((LocalTime[])array)), recordAllocator, allowStorePointsAndTemporal, Capability.TEMPORAL_PROPERTIES);
            } else if (type.equals(LocalDateTime.class)) {
                allocateFromCompositeType(target, TemporalType.encodeLocalDateTimeArray((LocalDateTime[])((LocalDateTime[])array)), recordAllocator, allowStorePointsAndTemporal, Capability.TEMPORAL_PROPERTIES);
            } else if (type.equals(OffsetTime.class)) {
                allocateFromCompositeType(target, TemporalType.encodeTimeArray((OffsetTime[])((OffsetTime[])array)), recordAllocator, allowStorePointsAndTemporal, Capability.TEMPORAL_PROPERTIES);
            } else if (type.equals(ZonedDateTime.class)) {
                allocateFromCompositeType(target, TemporalType.encodeDateTimeArray((ZonedDateTime[])((ZonedDateTime[])array)), recordAllocator, allowStorePointsAndTemporal, Capability.TEMPORAL_PROPERTIES);
            } else if (type.equals(DurationValue.class)) {
                allocateFromCompositeType(target, TemporalType.encodeDurationArray((DurationValue[])((DurationValue[])array)), recordAllocator, allowStorePointsAndTemporal, Capability.TEMPORAL_PROPERTIES);
            } else {
                allocateFromNumbers(target, array, recordAllocator);
            }

        }
    }

    public static Value getRightArray(Pair<byte[], byte[]> data) {
        byte[] header = (byte[])data.first();
        byte[] bArray = (byte[])data.other();
        byte typeId = header[0];
        int length;
        if (typeId != PropertyType.STRING.intValue()) {
            if (typeId == PropertyType.GEOMETRY.intValue()) {
                GeometryHeader geometryHeader = GeometryHeader.fromArrayHeaderBytes(header);
                return GeometryType.decodeGeometryArray(geometryHeader, bArray);
            } else if (typeId == PropertyType.TEMPORAL.intValue()) {
                TemporalHeader temporalHeader = TemporalHeader.fromArrayHeaderBytes(header);
                return TemporalType.decodeTemporalArray(temporalHeader, bArray);
            } else {
                ShortArray type = ShortArray.typeOf(typeId);
                int bitsUsedInLastByte = header[1];
                int requiredBits = header[2];
                if (requiredBits == 0) {
                    return type.createEmptyArray();
                } else if (type == ShortArray.BYTE && requiredBits == 8) {
                    return Values.byteArray(bArray);
                } else {
                    Bits bits = Bits.bitsFromBytes(bArray);
                    length = (bArray.length * 8 - (8 - bitsUsedInLastByte)) / requiredBits;
                    return type.createArray(length, bits, requiredBits);
                }
            }
        } else {
            ByteBuffer headerBuffer = ByteBuffer.wrap(header, 1, header.length - 1);
            int arrayLength = headerBuffer.getInt();
            String[] result = new String[arrayLength];
            ByteBuffer dataBuffer = ByteBuffer.wrap(bArray);

            for(length = 0; length < arrayLength; ++length) {
                int byteLength = dataBuffer.getInt();
                byte[] stringByteArray = new byte[byteLength];
                dataBuffer.get(stringByteArray);
                result[length] = PropertyStore.decodeString(stringByteArray);
            }

            return Values.stringArray(result);
        }
    }

    public Object getArrayFor(Iterable<DynamicRecord> records) {
        return getRightArray(this.readFullByteArray(records, PropertyType.ARRAY)).asObject();
    }

    //DynamicGraph
    //*************************************************************************************
    public static byte[] readFullByteArrayFromHeavyRecordsEx(Iterable<DynamicRecord> records, PropertyType propertyType) {
        byte[] header = null;
        List<byte[]> byteList = new ArrayList();
        int totalSize = 0;
        int i = 0;

        DynamicRecord record;
        int offset;
        for(Iterator var6 = records.iterator(); var6.hasNext(); totalSize += record.getData().length - offset) {
            record = (DynamicRecord)var6.next();
            offset = 0;
            if (i++ == 0) {
                header = propertyType.readDynamicRecordHeader(record.getData());
                offset = header.length;
            }

            byteList.add(record.getData());
        }

        byte[] bArray = new byte[totalSize+header.length];

        assert header != null : "header should be non-null since records should not be empty: " + Iterables.toString(records, ", ");

        int sourceOffset = header.length;
        offset = 0;

        //System.arraycopy(header,0,bArray,offset,header.length);
        for(Iterator var9 = byteList.iterator(); var9.hasNext(); sourceOffset = 0) {
            byte[] currentArray = (byte[])var9.next();
            System.arraycopy(currentArray, 0, bArray, offset, currentArray.length);
            offset += currentArray.length;
        }

        return bArray;
    }


    //DynamicGraph
    //*************************************************************************************

}
