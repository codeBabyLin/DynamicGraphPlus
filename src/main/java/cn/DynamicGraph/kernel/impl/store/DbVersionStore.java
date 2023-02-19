package cn.DynamicGraph.kernel.impl.store;

import cn.DynamicGraph.kernel.impl.store.record.DbVersionRecord;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.CommonAbstractStore;
import org.neo4j.kernel.impl.store.NoStoreHeader;
import org.neo4j.kernel.impl.store.StoreHeaderFormat;
import org.neo4j.kernel.impl.store.format.RecordFormat;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.logging.LogProvider;

import java.io.File;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.List;

public class DbVersionStore extends CommonAbstractStore<DbVersionRecord, NoStoreHeader> {


    public DbVersionStore(File file, File idFile, Config configuration, IdType idType, IdGeneratorFactory idGeneratorFactory, PageCache pageCache, LogProvider logProvider, String typeDescriptor, RecordFormat<DbVersionRecord> recordFormat, StoreHeaderFormat<NoStoreHeader> storeHeaderFormat, String storeVersion, OpenOption... openOptions) {
        super(file, idFile, configuration, idType, idGeneratorFactory, pageCache, logProvider, typeDescriptor, recordFormat, storeHeaderFormat, storeVersion, openOptions);
    }



    public long getNextVersion(){
        return this.nextId();
    }

    public long getCurrentHighestVersion(){
        return this.getHighestPossibleIdInUse();
    }
    public long[] listAllVersionsSuccessCommited() throws Exception {
        //int size = super.getRecordSize()
        List<Long> versions = new ArrayList<>();
        super.scanAllRecords(new Visitor<DbVersionRecord, Exception>() {
            @Override
            public boolean visit(DbVersionRecord dbVersionRecord) throws Exception {
                versions.add(dbVersionRecord.getId());
                return true;
            }
        });
        int size = versions.size();
        long [] res = new long[size];

        for(int i = 0;i< size;i++){
            res[i] = versions.get(i);
        }

        return res;
    }


    public long[] getNextVersions(long versionCount){
        long versions[] = new long[(int) versionCount];
        for(int i = 0;i< versionCount;i++){
            versions[i] = this.getNextVersion();
        }
        return versions;
    }
    public boolean transactionsCommit(long ids[]){
        for(int i =0 ;i<ids.length;i++){
            this.transactionCommit(ids[i]);
        }
        return true;
    }

    public boolean transactionCommit(long id){
        DbVersionRecord record = new DbVersionRecord(id);
        record.initialize(true,0);
        updateRecord(record);
        return true;
    }
    public boolean transactionCommit(long id, boolean isSuccessCommit){
        DbVersionRecord record = new DbVersionRecord(id);
        record.initialize(isSuccessCommit,0);
        updateRecord(record);
        return true;

    }

    public boolean transactionCommit(long id,long value  ,long nodeCounts,long relCounts){
        DbVersionRecord record = new DbVersionRecord(id);
        record.initialize(true,value,nodeCounts,relCounts);
        updateRecord(record);
        return true;

    }
    public boolean transactionCommit(long id,boolean isSuccessCommit,long value  ,long nodeCounts,long relCounts){
        DbVersionRecord record = new DbVersionRecord(id);
        record.initialize(isSuccessCommit,value,nodeCounts,relCounts);
        updateRecord(record);
        return true;

    }

    @Override
    public void updateRecord(DbVersionRecord record) {
        super.updateRecord(record);
    }

    @Override
    public <FAILURE extends Exception> void accept(Processor<FAILURE> var1, DbVersionRecord var2) throws FAILURE {

    }
}
