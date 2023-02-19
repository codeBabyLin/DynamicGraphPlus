package cn.DynamicGraph.Common;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class Mapserializer extends ArraySerializer {


    public static void writeMap(Map<Integer,Object> data, ByteBuf byteBuf){
        int size = data.size();
        byteBuf.writeInt(size);
        for(Map.Entry<Integer,Object> entry: data.entrySet()){
            byteBuf.writeInt(entry.getKey());
            writeAny(entry.getValue(),byteBuf);
        }
    /*    data.forEach(new BiConsumer<Integer, Object>() {
            @Override
            public void accept(Integer integer, Object o) {
                byteBuf.writeInt(integer);
                writeAny(o,byteBuf);
            }
        });*/
    }
    public static Map<Integer,Object> readMap(ByteBuf byteBuf){
        Map<Integer,Object> data = new HashMap<Integer, Object>();
        int size = byteBuf.readInt();
        for(int i =0;i<size;i++){
            int key = byteBuf.readInt();
            Object value = readAny(byteBuf);
            data.put(key,value);
        }
        return data;
    }
}
