package cn.DynamicGraph.KVStore;

import org.rocksdb.*;

import java.io.*;
import java.util.Properties;

public class RocksDBConfigBuilder {
    private String rocksdbConfigFilePath;
    public RocksDBConfigBuilder(String rocksdbConfigFilePath){
        this.rocksdbConfigFilePath = rocksdbConfigFilePath;
    }
    public Options getOptions () throws IOException {
        File file = new File(rocksdbConfigFilePath);
        if(!file.exists()){
            return null;
        }
        Properties properties = new Properties();
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
        properties.load(is);
        Options options = new Options();
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        properties.forEach((key, value) -> {
            String k = key.toString();
            //String v = value.toString();
            switch (k){
                case "options.setCompressionType": {
                    String v = value.toString();
                    switch (v) {
                        case "BZLIB2_COMPRESSION":
                            options.setCompressionType(CompressionType.BZLIB2_COMPRESSION);
                            break;
                        case "DISABLE_COMPRESSION_OPTION":
                            options.setCompressionType(CompressionType.DISABLE_COMPRESSION_OPTION);
                            break;
                        case "LZ4_COMPRESSION":
                            options.setCompressionType(CompressionType.LZ4_COMPRESSION);
                            break;
                        case "LZ4HC_COMPRESSION":
                            options.setCompressionType(CompressionType.LZ4HC_COMPRESSION);
                            break;
                        case "NO_COMPRESSION":
                            options.setCompressionType(CompressionType.NO_COMPRESSION);
                            break;
                        case "SNAPPY_COMPRESSION":
                            options.setCompressionType(CompressionType.SNAPPY_COMPRESSION);
                            break;
                        case "XPRESS_COMPRESSION":
                            options.setCompressionType(CompressionType.XPRESS_COMPRESSION);
                            break;
                        case "ZLIB_COMPRESSION":
                            options.setCompressionType(CompressionType.ZLIB_COMPRESSION);
                            break;
                        case "ZSTD_COMPRESSION":
                            options.setCompressionType(CompressionType.ZSTD_COMPRESSION);
                            break;
                        default:
                            options.setCompressionType(CompressionType.ZSTD_COMPRESSION);
                            break;
                    }
                }

                case "options.setOptimizeFiltersForHits()" : {
                    options.setOptimizeFiltersForHits((Boolean) value);break;
                }

                case "options.setCreateIfMissing" : {
                    options.setCreateIfMissing((Boolean) value);break;
                }

                case "options.setLevel0StopWritesTrigger" : {
                    options.setLevel0StopWritesTrigger((Integer) value);break;
                }

                case "options.setTargetFileSizeMultiplier" : {
                    options.setTargetFileSizeMultiplier((Integer) value);break;
                }
                case "options.setLevel0SlowdownWritesTrigger" : {
                    options.setLevel0SlowdownWritesTrigger((Integer) value);break;
                }
                case "options.setDisableAutoCompactions" : {
                    options.setDisableAutoCompactions((Boolean) value);break;
                }
                case "options.setMinWriteBufferNumberToMerge" : {
                    options.setMinWriteBufferNumberToMerge((Integer) value);break;
                }
                case "options.setMaxBytesForLevelBase" : {
                    options.setMaxBytesForLevelBase((Long) value);break;
                }
                case "options.setMaxWriteBufferNumber" : {
                    options.setMaxWriteBufferNumber((Integer) value);break;
                }
                case "tableConfig.setBlockSize" : {
                    tableConfig.setBlockSize((Long) value);break;
                }
                case "tableConfig.setBlockCache" : {
                    //val num = prop.getProperty("tableConfig.setBlockCache").toLong
                    tableConfig.setBlockCache(new LRUCache((Long) value));break;
                }
                case "options.setTargetFileSizeBase" : {
                    options.setTargetFileSizeBase((Long) value);break;
                }
                case "options.setSkipCheckingSstFileSizesOnDbOpen" :{
                    options.setSkipCheckingSstFileSizesOnDbOpen((Boolean) value);break;
                }
                case "tableConfig.setCacheIndexAndFilterBlocks" : {
                    tableConfig.setCacheIndexAndFilterBlocks((Boolean) value);break;
                }
                case "options.setMaxBackgroundJobs" : {
                    options.setMaxBackgroundJobs((Integer) value);break;
                }
                case "options.setWriteBufferSize" : {
                    options.setWriteBufferSize((Long) value);break;
                }
                case "options.setMaxOpenFiles" : {
                    options.setMaxOpenFiles((Integer) value);break;
                }
                case "options.setLevel0FileNumCompactionTrigger" : {
                    options.setLevel0FileNumCompactionTrigger((Integer) value);break;
                }
                case "options.setAllowConcurrentMemtableWrite" : {
                    options.setAllowConcurrentMemtableWrite((Boolean) value);break;
                }
                case "options.setMaxBytesForLevelMultiplier" : {
                    options.setMaxBytesForLevelMultiplier((Double) value);break;
                }
                case "options.setLevelCompactionDynamicLevelBytes" : {
                    options.setLevelCompactionDynamicLevelBytes((Boolean) value);break;
                }
                case "options.setCompactionStyle" :{
                    String str = value.toString();
                    switch (str){
                        case "LEVEL" : options.setCompactionStyle(CompactionStyle.LEVEL);break;
                        case "FIFO" : options.setCompactionStyle(CompactionStyle.FIFO);break;
                        case "NONE" : options.setCompactionStyle(CompactionStyle.NONE);break;
                        case "UNIVERSAL" : options.setCompactionStyle(CompactionStyle.UNIVERSAL);break;
                        default:options.setCompactionStyle(CompactionStyle.UNIVERSAL);break;
                    }
                }
                default:break;
            }
        });
        tableConfig.setFilterPolicy(new BloomFilter(15,false));
        options.setTableFormatConfig(tableConfig);
        return options;
    }

}
