//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import org.neo4j.kernel.impl.store.record.PropertyBlock;
import org.neo4j.kernel.impl.util.Bits;
import org.neo4j.values.storable.ArrayValue;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

public enum ShortArray {
    BOOLEAN(PropertyType.BOOL, 1, Boolean.class, Boolean.TYPE) {
        int getRequiredBits(Object array, int arrayLength) {
            return 1;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            boolean value;
            if (ShortArray.isPrimitive(array)) {
                boolean[] var5 = (boolean[])((boolean[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var5[var7];
                    result.put(value ? 1 : 0, 1);
                }
            } else {
                Boolean[] var9 = (Boolean[])((Boolean[])array);
                var6 = var9.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var9[var7];
                    result.put(value ? 1 : 0, 1);
                }
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_BOOLEAN_ARRAY;
            } else {
                boolean[] result = new boolean[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = bits.getByte(requiredBits) != 0;
                }

                return Values.booleanArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_BOOLEAN_ARRAY;
        }
    },
    BYTE(PropertyType.BYTE, 8, Byte.class, Byte.TYPE) {
        int getRequiredBits(byte value) {
            long mask = 1L << this.maxBits - 1;

            for(int i = this.maxBits; i > 0; mask >>= 1) {
                if ((mask & (long)value) != 0L) {
                    return i;
                }

                --i;
            }

            return 1;
        }

        int getRequiredBits(Object array, int arrayLength) {
            int highest = 1;
            int var5;
            int var6;
            byte value;
            if (ShortArray.isPrimitive(array)) {
                byte[] var4 = (byte[])((byte[])array);
                var5 = var4.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var4[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            } else {
                Byte[] var8 = (Byte[])((Byte[])array);
                var5 = var8.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var8[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            }

            return highest;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            byte b;
            if (ShortArray.isPrimitive(array)) {
                byte[] var5 = (byte[])((byte[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    b = var5[var7];
                    result.put(b, requiredBits);
                }
            } else {
                Byte[] var9 = (Byte[])((Byte[])array);
                var6 = var9.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    b = var9[var7];
                    result.put(b, requiredBits);
                }
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_BYTE_ARRAY;
            } else {
                byte[] result = new byte[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = bits.getByte(requiredBits);
                }

                return Values.byteArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_BYTE_ARRAY;
        }
    },
    SHORT(PropertyType.SHORT, 16, Short.class, Short.TYPE) {
        int getRequiredBits(short value) {
            long mask = 1L << this.maxBits - 1;

            for(int i = this.maxBits; i > 0; mask >>= 1) {
                if ((mask & (long)value) != 0L) {
                    return i;
                }

                --i;
            }

            return 1;
        }

        int getRequiredBits(Object array, int arrayLength) {
            int highest = 1;
            int var5;
            int var6;
            short value;
            if (ShortArray.isPrimitive(array)) {
                short[] var4 = (short[])((short[])array);
                var5 = var4.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var4[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            } else {
                Short[] var8 = (Short[])((Short[])array);
                var5 = var8.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var8[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            }

            return highest;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            short value;
            if (ShortArray.isPrimitive(array)) {
                short[] var5 = (short[])((short[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var5[var7];
                    result.put(value, requiredBits);
                }
            } else {
                Short[] var9 = (Short[])((Short[])array);
                var6 = var9.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var9[var7];
                    result.put(value, requiredBits);
                }
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_SHORT_ARRAY;
            } else {
                short[] result = new short[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = bits.getShort(requiredBits);
                }

                return Values.shortArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_SHORT_ARRAY;
        }
    },
    CHAR(PropertyType.CHAR, 16, Character.class, Character.TYPE) {
        int getRequiredBits(char value) {
            long mask = 1L << this.maxBits - 1;

            for(int i = this.maxBits; i > 0; mask >>= 1) {
                if ((mask & (long)value) != 0L) {
                    return i;
                }

                --i;
            }

            return 1;
        }

        int getRequiredBits(Object array, int arrayLength) {
            int highest = 1;
            int var5;
            int var6;
            char value;
            if (ShortArray.isPrimitive(array)) {
                char[] var4 = (char[])((char[])array);
                var5 = var4.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var4[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            } else {
                Character[] var8 = (Character[])((Character[])array);
                var5 = var8.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var8[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            }

            return highest;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            char value;
            if (ShortArray.isPrimitive(array)) {
                char[] var5 = (char[])((char[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var5[var7];
                    result.put(value, requiredBits);
                }
            } else {
                Character[] var9 = (Character[])((Character[])array);
                var6 = var9.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var9[var7];
                    result.put(value, requiredBits);
                }
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_CHAR_ARRAY;
            } else {
                char[] result = new char[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = (char)bits.getShort(requiredBits);
                }

                return Values.charArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_CHAR_ARRAY;
        }
    },
    INT(PropertyType.INT, 32, Integer.class, Integer.TYPE) {
        int getRequiredBits(int value) {
            long mask = 1L << this.maxBits - 1;

            for(int i = this.maxBits; i > 0; mask >>= 1) {
                if ((mask & (long)value) != 0L) {
                    return i;
                }

                --i;
            }

            return 1;
        }

        int getRequiredBits(Object array, int arrayLength) {
            int highest = 1;
            int var5;
            int var6;
            int value;
            if (ShortArray.isPrimitive(array)) {
                int[] var4 = (int[])((int[])array);
                var5 = var4.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var4[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            } else {
                Integer[] var8 = (Integer[])((Integer[])array);
                var5 = var8.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var8[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            }

            return highest;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            int value;
            if (ShortArray.isPrimitive(array)) {
                int[] var5 = (int[])((int[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var5[var7];
                    result.put(value, requiredBits);
                }
            } else {
                Integer[] var9 = (Integer[])((Integer[])array);
                var6 = var9.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var9[var7];
                    result.put(value, requiredBits);
                }
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_INT_ARRAY;
            } else {
                int[] result = new int[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = bits.getInt(requiredBits);
                }

                return Values.intArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_INT_ARRAY;
        }
    },
    LONG(PropertyType.LONG, 64, Long.class, Long.TYPE) {
        public int getRequiredBits(long value) {
            long mask = 1L << this.maxBits - 1;

            for(int i = this.maxBits; i > 0; mask >>= 1) {
                if ((mask & value) != 0L) {
                    return i;
                }

                --i;
            }

            return 1;
        }

        int getRequiredBits(Object array, int arrayLength) {
            int highest = 1;
            int var5;
            int var6;
            long value;
            if (ShortArray.isPrimitive(array)) {
                long[] var4 = (long[])((long[])array);
                var5 = var4.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var4[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            } else {
                Long[] var9 = (Long[])((Long[])array);
                var5 = var9.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var9[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            }

            return highest;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            long value;
            if (ShortArray.isPrimitive(array)) {
                long[] var5 = (long[])((long[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var5[var7];
                    result.put(value, requiredBits);
                }
            } else {
                Long[] var10 = (Long[])((Long[])array);
                var6 = var10.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var10[var7];
                    result.put(value, requiredBits);
                }
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_LONG_ARRAY;
            } else {
                long[] result = new long[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = bits.getLong(requiredBits);
                }

                return Values.longArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_LONG_ARRAY;
        }
    },
    FLOAT(PropertyType.FLOAT, 32, Float.class, Float.TYPE) {
        int getRequiredBits(float value) {
            int v = Float.floatToIntBits(value);
            long mask = 1L << this.maxBits - 1;

            for(int i = this.maxBits; i > 0; mask >>= 1) {
                if ((mask & (long)v) != 0L) {
                    return i;
                }

                --i;
            }

            return 1;
        }

        int getRequiredBits(Object array, int arrayLength) {
            int highest = 1;
            int var5;
            int var6;
            float value;
            if (ShortArray.isPrimitive(array)) {
                float[] var4 = (float[])((float[])array);
                var5 = var4.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var4[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            } else {
                Float[] var8 = (Float[])((Float[])array);
                var5 = var8.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var8[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            }

            return highest;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            float value;
            if (ShortArray.isPrimitive(array)) {
                float[] var5 = (float[])((float[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var5[var7];
                    result.put(Float.floatToIntBits(value), requiredBits);
                }
            } else {
                Float[] var9 = (Float[])((Float[])array);
                var6 = var9.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var9[var7];
                    result.put(Float.floatToIntBits(value), requiredBits);
                }
            }

        }

        public void writeAll(Object array, byte[] result, int offset) {
            int i;
            if (ShortArray.isPrimitive(array)) {
                float[] valuesx = (float[])((float[])array);

                for(i = 0; i < valuesx.length; ++i) {
                    this.writeFloat(valuesx[i], result, offset + i * 4);
                }
            } else {
                Float[] values = (Float[])((Float[])array);

                for(i = 0; i < values.length; ++i) {
                    this.writeFloat(values[i], result, offset + i * 4);
                }
            }

        }

        private void writeFloat(float floaValue, byte[] result, int offset) {
            long value = (long)Float.floatToIntBits(floaValue);

            for(int b = 0; b < 4; ++b) {
                result[offset + b] = (byte)((int)(value >> b * 8 & 255L));
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_FLOAT_ARRAY;
            } else {
                float[] result = new float[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = Float.intBitsToFloat(bits.getInt(requiredBits));
                }

                return Values.floatArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_FLOAT_ARRAY;
        }
    },
    DOUBLE(PropertyType.DOUBLE, 64, Double.class, Double.TYPE) {
        int getRequiredBits(double value) {
            long v = Double.doubleToLongBits(value);
            long mask = 1L << this.maxBits - 1;

            for(int i = this.maxBits; i > 0; mask >>= 1) {
                if ((mask & v) != 0L) {
                    return i;
                }

                --i;
            }

            return 1;
        }

        int getRequiredBits(Object array, int arrayLength) {
            int highest = 1;
            int var5;
            int var6;
            double value;
            if (ShortArray.isPrimitive(array)) {
                double[] var4 = (double[])((double[])array);
                var5 = var4.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var4[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            } else {
                Double[] var9 = (Double[])((Double[])array);
                var5 = var9.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    value = var9[var6];
                    highest = Math.max(this.getRequiredBits(value), highest);
                }
            }

            return highest;
        }

        public void writeAll(Object array, int length, int requiredBits, Bits result) {
            int var6;
            int var7;
            double value;
            if (ShortArray.isPrimitive(array)) {
                double[] var5 = (double[])((double[])array);
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var5[var7];
                    result.put(Double.doubleToLongBits(value), requiredBits);
                }
            } else {
                Double[] var10 = (Double[])((Double[])array);
                var6 = var10.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    value = var10[var7];
                    result.put(Double.doubleToLongBits(value), requiredBits);
                }
            }

        }

        public void writeAll(Object array, byte[] result, int offset) {
            int i;
            if (ShortArray.isPrimitive(array)) {
                double[] valuesx = (double[])((double[])array);

                for(i = 0; i < valuesx.length; ++i) {
                    this.writeDouble(valuesx[i], result, offset + i * 8);
                }
            } else {
                Double[] values = (Double[])((Double[])array);

                for(i = 0; i < values.length; ++i) {
                    this.writeDouble(values[i], result, offset + i * 8);
                }
            }

        }

        private void writeDouble(double doubleValue, byte[] result, int offset) {
            long value = Double.doubleToLongBits(doubleValue);

            for(int b = 0; b < 8; ++b) {
                result[offset + b] = (byte)((int)(value >> b * 8 & 255L));
            }

        }

        public ArrayValue createArray(int length, Bits bits, int requiredBits) {
            if (length == 0) {
                return Values.EMPTY_DOUBLE_ARRAY;
            } else {
                double[] result = new double[length];

                for(int i = 0; i < length; ++i) {
                    result[i] = Double.longBitsToDouble(bits.getLong(requiredBits));
                }

                return Values.doubleArray(result);
            }
        }

        public ArrayValue createEmptyArray() {
            return Values.EMPTY_DOUBLE_ARRAY;
        }
    };

    private static final ShortArray[] TYPES = values();
    private static final Map<Class<?>, ShortArray> all = new IdentityHashMap(TYPES.length * 2);
    final int maxBits;
    private final Class<?> boxedClass;
    private final Class<?> primitiveClass;
    private final PropertyType type;

    private static boolean isPrimitive(Object array) {
        return array.getClass().getComponentType().isPrimitive();
    }

    private ShortArray(PropertyType type, int maxBits, Class<?> boxedClass, Class<?> primitiveClass) {
        this.type = type;
        this.maxBits = maxBits;
        this.boxedClass = boxedClass;
        this.primitiveClass = primitiveClass;
    }

    public int intValue() {
        return this.type.intValue();
    }

    public int getMaxBits() {
        return maxBits;
    }

    public abstract ArrayValue createArray(int var1, Bits var2, int var3);

    public static boolean encode(int keyId, Object array, PropertyBlock target, int payloadSizeInBytes) {
        int arrayLength = Array.getLength(array);
        if (arrayLength > 63) {
            return false;
        } else {
            ShortArray type = typeOf(array);
            if (type == null) {
                return false;
            } else {
                int requiredBits = type.calculateRequiredBitsForArray(array, arrayLength);
                if (!willFit(requiredBits, arrayLength, payloadSizeInBytes)) {
                    return false;
                } else {
                    int numberOfBytes = calculateNumberOfBlocksUsed(arrayLength, requiredBits) * 8;
                    if (Bits.requiredLongs(numberOfBytes) > PropertyType.getPayloadSizeLongs()) {
                        return false;
                    } else {
                        Bits result = Bits.bits(numberOfBytes);
                        writeHeader(keyId, type, arrayLength, requiredBits, result);
                        type.writeAll(array, arrayLength, requiredBits, result);
                        target.setValueBlocks(result.getLongs());
                        return true;
                    }
                }
            }
        }
    }

    private static void writeHeader(int keyId, ShortArray type, int arrayLength, int requiredBits, Bits result) {
        result.put(keyId, 24);
        result.put(PropertyType.SHORT_ARRAY.intValue(), 4);
        result.put(type.type.intValue(), 4);
        result.put(arrayLength, 6);
        result.put(requiredBits, 6);
    }

    public static Value decode(PropertyBlock block) {
        Bits bits = Bits.bitsFromLongs(Arrays.copyOf(block.getValueBlocks(), block.getValueBlocks().length));
        return decode(bits);
    }

    public static Value decode(Bits bits) {
        bits.getInt(24);
        bits.getByte(4);
        int typeId = bits.getByte(4);
        int arrayLength = bits.getByte(6);
        int requiredBits = bits.getByte(6);
        if (requiredBits == 0) {
            requiredBits = 64;
        }

        ShortArray type = typeOf((byte)typeId);
        return type.createArray(arrayLength, bits, requiredBits);
    }

    private static boolean willFit(int requiredBits, int arrayLength, int payloadSizeInBytes) {
        int totalBitsRequired = requiredBits * arrayLength;
        int maxBits = payloadSizeInBytes * 8 - 24 - 4 - 4 - 6 - 6;
        return totalBitsRequired <= maxBits;
    }

    public int calculateRequiredBitsForArray(Object array, int arrayLength) {
        return arrayLength == 0 ? 0 : this.getRequiredBits(array, arrayLength);
    }

    public int getRequiredBits(long value) {
        int highest = 1;
        long mask = 1L;

        for(int i = 1; i <= this.maxBits; mask <<= 1) {
            if ((mask & value) != 0L) {
                highest = i;
            }

            ++i;
        }

        return highest;
    }

    abstract int getRequiredBits(Object var1, int var2);

    public static ShortArray typeOf(byte typeId) {
        return TYPES[typeId - 1];
    }

    public static ShortArray typeOf(Object array) {
        return (ShortArray)all.get(array.getClass().getComponentType());
    }

    public static int calculateNumberOfBlocksUsed(long firstBlock) {
        int highInt = (int)(firstBlock >>> 32);
        int arrayLength = highInt & 63;
        highInt >>>= 6;
        int requiredBits = highInt & 63;
        if (requiredBits == 0) {
            requiredBits = 64;
        }

        return calculateNumberOfBlocksUsed(arrayLength, requiredBits);
    }

    public static int calculateNumberOfBlocksUsed(int arrayLength, int requiredBits) {
        int bitsForItems = arrayLength * requiredBits;
        int totalBits = 44 + bitsForItems;
        int result = (totalBits - 1) / 64 + 1;
        return result;
    }

    public abstract void writeAll(Object var1, int var2, int var3, Bits var4);

    public void writeAll(Object array, byte[] result, int offset) {
        throw new IllegalStateException("Types that skip bit compaction should implement this method");
    }

    public abstract ArrayValue createEmptyArray();

    static {
        ShortArray[] var0 = TYPES;
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            ShortArray shortArray = var0[var2];
            all.put(shortArray.primitiveClass, shortArray);
            all.put(shortArray.boxedClass, shortArray);
        }

    }
}
