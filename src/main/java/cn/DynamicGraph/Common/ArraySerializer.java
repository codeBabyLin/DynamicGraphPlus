package cn.DynamicGraph.Common;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Array;

public class ArraySerializer extends BaseSerializer {
    public static void writeLongArray(long [] data, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.LONG_ARRAY_ID);
        int size = data.length;
        byteBuf.writeInt(size);
        for(int i = 0;i<size;i++){
            byteBuf.writeLong(data[i]);
        }
    }
    public static long[] readLongArray(ByteBuf byteBuf){
        int size = byteBuf.readInt();
        long [] data = new long[size];
        for(int i= 0;i<size;i++){
            data[i] = byteBuf.readLong();
        }
        return data;
    }
    public static void writeIntArray(int [] data, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.INT_ARRAY_ID);
        int size = data.length;
        byteBuf.writeInt(size);
        for(int i = 0;i<size;i++){
            byteBuf.writeInt(data[i]);
        }
    }
    public static int[] readIntArray(ByteBuf byteBuf){
        int size = byteBuf.readInt();
        int [] data = new int[size];
        for(int i= 0;i<size;i++){
            data[i] = byteBuf.readInt();
        }
        return data;
    }
    public static void writeFloatArray(float [] data, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.FLOAT_ARRAY_ID);
        int size = data.length;
        byteBuf.writeInt(size);
        for(int i = 0;i<size;i++){
            byteBuf.writeFloat(data[i]);
        }
    }
    public static float[] readFloatArray(ByteBuf byteBuf){
        int size = byteBuf.readInt();
        float [] data = new float[size];
        for(int i= 0;i<size;i++){
            data[i] = byteBuf.readFloat();
        }
        return data;
    }
    public static void writeDoubleArray(double [] data, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.DOUBLE_ARRAY_ID);
        int size = data.length;
        byteBuf.writeInt(size);
        for(int i = 0;i<size;i++){
            byteBuf.writeDouble(data[i]);
        }
    }
    public static double[] readDoubleArray(ByteBuf byteBuf){
        int size = byteBuf.readInt();
        double [] data = new double[size];
        for(int i= 0;i<size;i++){
            data[i] = byteBuf.readDouble();
        }
        return data;
    }

    public static void writeBooleanArray(boolean [] data, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.BOOLEAN_ARRAY_ID);
        int size = data.length;
        byteBuf.writeInt(size);
        for(int i = 0;i<size;i++){
            byteBuf.writeBoolean(data[i]);
        }
    }
    public static boolean[] readBooleanArray(ByteBuf byteBuf){
        int size = byteBuf.readInt();
        boolean [] data = new boolean[size];
        for(int i= 0;i<size;i++){
            data[i] = byteBuf.readBoolean();
        }
        return data;
    }

    public static void writeStringArray(String [] data, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.BOOLEAN_ARRAY_ID);
        int size = data.length;
        byteBuf.writeInt(size);
        for(int i = 0;i<size;i++){
            _writeStringEx(data[i],byteBuf);
        }
    }
    public static String[] readStringArray(ByteBuf byteBuf){
        int size = byteBuf.readInt();
        String [] data = new String[size];
        for(int i= 0;i<size;i++){
            data[i] = _readStringEx(byteBuf);
        }
        return data;
    }

    public static void writeAnyArray(Object data,ByteBuf byteBuf){
        int size = Array.getLength(data);
        Object head = Array.get(data,0);
        if(head instanceof Boolean){
            boolean [] value = new boolean[size];
            for(int i = 0;i<size;i++){
                value[i] = (boolean)Array.get(data,i);
            }
            writeBooleanArray(value,byteBuf);
        }
        else if(head instanceof Long){
            long [] value = new long[size];
            for(int i = 0;i<size;i++){
                value[i] = (long)Array.get(data,i);
            }
            writeLongArray(value,byteBuf);
        }
        else if(head instanceof Integer){
            int [] value = new int[size];
            for(int i = 0;i<size;i++){
                value[i] = (int)Array.get(data,i);
            }
            writeIntArray(value,byteBuf);
        }
        else if(head instanceof Float){
            float [] value = new float[size];
            for(int i = 0;i<size;i++){
                value[i] = (float)Array.get(data,i);
            }
            writeFloatArray(value,byteBuf);
        }

        else if(head instanceof Double){
            double [] value = new double[size];
            for(int i = 0;i<size;i++){
                value[i] = (double)Array.get(data,i);
            }
            writeDoubleArray(value,byteBuf);
        }

        else if(head instanceof String){
            String [] value = new String[size];
            for(int i = 0;i<size;i++){
                value[i] = (String)Array.get(data,i);
            }
            writeStringArray(value,byteBuf);
        }

        else{

        }
    }

    public static Object readAnyArray(ByteBuf byteBuf,int type){
        Object value = null;
        //int size = byteBuf.readInt();
        switch(type) {
            case JavaSerialzerDataType.STRING_ARRAY_ID:
                value = readStringArray(byteBuf);
                break;
            case JavaSerialzerDataType.BOOLEAN_ARRAY_ID:
                value = readBooleanArray(byteBuf);
                break;
            case JavaSerialzerDataType.FLOAT_ARRAY_ID:
                value = readFloatArray(byteBuf);
                break;
            case JavaSerialzerDataType.INT_ARRAY_ID:
                value = readIntArray(byteBuf);
                break;
            case JavaSerialzerDataType.LONG_ARRAY_ID:
                value = readLongArray(byteBuf);
                break;
            case JavaSerialzerDataType.DOUBLE_ARRAY_ID:
                value = readDoubleArray(byteBuf);
                break;
            default:
                break;
        }

        return value;
    }


    public static void writeAny(Object value,ByteBuf byteBuf){
        if(value.getClass().isArray()){
            writeAnyArray(value,byteBuf);
        }
        else {
            _writeAny(value,byteBuf);
        }
    }
    public static Object readAny(ByteBuf byteBuf){
        int type = byteBuf.readByte();
        if(type <0 ){
            return readAnyArray(byteBuf,type);
        }
        else{
            return _readAny(byteBuf,type);
        }
    }



}
