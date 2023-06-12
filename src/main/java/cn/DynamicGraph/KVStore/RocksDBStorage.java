package cn.DynamicGraph.KVStore;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class RocksDBStorage {
    private RocksDB rockDB;
    public RocksDBStorage(RocksDB rockDB){
        this.rockDB = rockDB;
    }

    public byte[] get(byte[]key){
        byte[] res = null;
        try {
            res = rockDB.get(key);
        }catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
    public void put(byte[]key,byte[]value) throws RocksDBException {
        try {
            rockDB.put(key,value);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

