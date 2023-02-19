package cn.DynamicGraph.Common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;
import java.time.*;

public class BaseSerializer {
    ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

    public static void _writeAny(Object value,ByteBuf byteBuf) {
        if(value instanceof String){
            _writeStringEx((String) value,byteBuf);
        }
        else if(value instanceof Integer){
            _writeInt((int) value,byteBuf);
        }
        else if(value instanceof Long){
            _writeLong((Long) value,byteBuf);
        }
        else if(value instanceof Double){
            _writeDouble((Double) value,byteBuf);
        }
        else if(value instanceof Float){
            _writeFloat((Float) value,byteBuf);
        }
        else if(value instanceof Boolean){
            _writeBoolean((Boolean) value,byteBuf);
        }
        else{
        }
    }


    public static Object _readAny(ByteBuf byteBuf){
        int type = (int )byteBuf.readByte();
        return _readAny(byteBuf,type);
    }
    public static Object _readAny(ByteBuf byteBuf,int type){
        //int type = (int )byteBuf.readByte();
        Object value = null;
        switch(type){
            case JavaSerialzerDataType.STRING_ID:
                value =  _readStringEx(byteBuf);
                break;
            case JavaSerialzerDataType.BOOLEAN_ID:
                value = _readBoolean(byteBuf);
                break;
            case JavaSerialzerDataType.FLOAT_ID:
                value = _readFloat(byteBuf);
                break;
            case JavaSerialzerDataType.INT_ID:
                value = _readInt(byteBuf);
                break;
            case JavaSerialzerDataType.LONG_ID:
                value = _readLong(byteBuf);
                break;
            case JavaSerialzerDataType.DOUBLE_ID:
                value = _readDouble(byteBuf);
                break;
            default:
                break;
        }
        return value;
    }

    public static String _readStringEx(ByteBuf byteBuf){
        int length = byteBuf.readInt();
        byte[] data = new byte[length];
        byteBuf.readBytes(data);

        return new String(data);
    }
    public static void _writeStringEx(String str,ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.STRING_ID);
        byte[] data = str.getBytes();
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }

    public static String _readString(ByteBuf byteBuf){
        int length = byteBuf.readInt();
        return (String) byteBuf.readCharSequence(length,StandardCharsets.UTF_8);
    }
    public static void _writeString(String str,ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.STRING_ID);
        byteBuf.writeInt(str.length());
        byteBuf.writeCharSequence(str, StandardCharsets.UTF_8);
    }
    public static int _readInt(ByteBuf byteBuf){
        return byteBuf.readInt();
    }
    public static void _writeInt(int i,ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.INT_ID);
        byteBuf.writeInt(i);
    }
    public static Double _readDouble(ByteBuf byteBuf){
        return byteBuf.readDouble();
    }
    public static void _writeDouble(Double d, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.DOUBLE_ID);
        byteBuf.writeDouble(d);
    }

    public static long _readLong(ByteBuf byteBuf){
        return byteBuf.readLong();
    }
    public static void _writeLong(long l, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.LONG_ID);
        byteBuf.writeLong(l);
    }
    public static Float _readFloat(ByteBuf byteBuf){
        return byteBuf.readFloat();
    }
    public static void _writeFloat(Float f,ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.FLOAT_ID);
        byteBuf.writeFloat(f);
    }


    public static Boolean _readBoolean(ByteBuf byteBuf){
        return byteBuf.readBoolean();
    }
    public static void _writeBoolean(Boolean value, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.BOOLEAN_ID);
        byteBuf.writeBoolean(value);
    }

    public static  void _writeDate(LocalDate localDate, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.DATE_ID);
        byteBuf.writeLong(localDate.toEpochDay());
    }

    public static LocalDate _readDate(ByteBuf byteBuf){
        long epochDay = byteBuf.readLong();
        return LocalDate.ofEpochDay(epochDay);
    }

    public static LocalDateTime _readLocalDateTime(ByteBuf byteBuf){
        long epochSecond = byteBuf.readLong();
        int nano = byteBuf.readInt();
        return LocalDateTime.ofEpochSecond(epochSecond,nano,ZoneOffset.UTC);
    }
    public static void _writeLocalDataTime(LocalDateTime localDateTime,ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.LOCAL_DATE_TIME_ID);
        long epochSecond = localDateTime.toEpochSecond(ZoneOffset.UTC);
        int nano =localDateTime.getNano();
        byteBuf.writeLong(epochSecond);
        byteBuf.writeInt(nano);
    }


    public static void _writeTime(OffsetTime offsetTime, ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.TIME_ID);
        long localNanosOfDay = offsetTime.toLocalTime().toNanoOfDay();
        int offsetSeconds = offsetTime.getOffset().getTotalSeconds();
        byteBuf.writeLong(localNanosOfDay);
        byteBuf.writeInt(offsetSeconds);
    }
    public static OffsetTime _readTime(ByteBuf byteBuf){
        long localNanosOfDay = byteBuf.readLong();
        int offsetSeconds = byteBuf.readInt();
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offsetSeconds);
        OffsetTime offsetTime = OffsetTime.of(LocalTime.ofNanoOfDay(localNanosOfDay),zoneOffset);
        return offsetTime;
    }
    public static void _writeLocalTime(LocalTime localTime,ByteBuf byteBuf){
        byteBuf.writeByte(JavaSerialzerDataType.LOCAL_TIME_ID);
        long localNanosOfDay = localTime.toNanoOfDay();
        byteBuf.writeLong(localNanosOfDay);
    }
    public static LocalTime _readLocalTime(ByteBuf byteBuf){
        long localNanosOfDay = byteBuf.readLong();
        return LocalTime.ofNanoOfDay(localNanosOfDay);
    }
    public static byte[] exportBytes(ByteBuf byteBuf){
        byte[] data = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(data);
        return data;
    }


}
