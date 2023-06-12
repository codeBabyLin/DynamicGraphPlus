package cn.DynamicGraph.KVStore;

import org.rocksdb.*;

import java.io.File;
import java.io.IOException;

public class RocksDBFactory {
    static {
        RocksDB.loadLibrary();
    }
    public static RocksDBStorage getDB(String path) {
        RocksDBStorage db = null;
        try {
            db = getDB(path, true, true, false, 0, "default");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return db;
    }
    public static RocksDBStorage getDB(String path,boolean createIfMissing,
                                boolean withBloomFilter,
                                boolean isHDD,int prefix,String rocksdbConfigPath) throws RocksDBException, IOException {

        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(rocksdbConfigPath.equals("default")){
            Options options = new Options();
            BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
            tableConfig.setFilterPolicy(new BloomFilter(15,false))
                    .setBlockSize(32L*1024L)
                    .setBlockCache(new LRUCache(512L*1024L*1024L));

            options.setTableFormatConfig(tableConfig)
                    .setCreateIfMissing(createIfMissing)
                    .setCompressionType(CompressionType.NO_COMPRESSION)
                    .setCompactionStyle(CompactionStyle.LEVEL)
                    .setDisableAutoCompactions(false)
                    .setMaxBackgroundJobs(5)
                    .setSkipCheckingSstFileSizesOnDbOpen(true)
                    .setLevelCompactionDynamicLevelBytes(true)
                    .setAllowConcurrentMemtableWrite(true)
                    .setMaxOpenFiles(-1)
                    .setWriteBufferSize(256l*1024l*1024L)
                    .setMinWriteBufferNumberToMerge(3)
                    .setLevel0FileNumCompactionTrigger(10)
                    .setMaxWriteBufferNumber(8)
                    .setLevel0SlowdownWritesTrigger(20)
                    .setLevel0StopWritesTrigger(40)
                    .setMaxBytesForLevelBase(256L*1024L*1024L*15L)
                    .setMaxBytesForLevelMultiplier(10)
                    .setTargetFileSizeBase(256L*1024L*1024L)
                    .setTargetFileSizeMultiplier(4);

            return new RocksDBStorage(RocksDB.open(options,path));
        }
        else{
            RocksDBConfigBuilder builder = new RocksDBConfigBuilder(rocksdbConfigPath);
            return new RocksDBStorage(RocksDB.open(builder.getOptions(),path));
        }

    }

}
