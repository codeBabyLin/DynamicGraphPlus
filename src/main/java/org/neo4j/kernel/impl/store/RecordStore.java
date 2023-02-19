//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.helpers.progress.ProgressListener;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.kernel.impl.store.id.IdRange;
import org.neo4j.kernel.impl.store.id.IdSequence;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.LabelTokenRecord;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.PropertyKeyTokenRecord;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.store.record.RelationshipGroupRecord;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.kernel.impl.store.record.RelationshipTypeTokenRecord;

public interface RecordStore<RECORD extends AbstractBaseRecord> extends IdSequence {
    Predicate<AbstractBaseRecord> IN_USE = AbstractBaseRecord::inUse;

    File getStorageFile();

    long getHighId();

    long getHighestPossibleIdInUse();

    void setHighestPossibleIdInUse(long var1);

    RECORD newRecord();

    RECORD getRecord(long var1, RECORD var3, RecordLoad var4) throws InvalidRecordException;

    PageCursor openPageCursorForReading(long var1);

    void getRecordByCursor(long var1, RECORD var3, RecordLoad var4, PageCursor var5) throws InvalidRecordException;

    void nextRecordByCursor(RECORD var1, RecordLoad var2, PageCursor var3) throws InvalidRecordException;

    void ensureHeavy(RECORD var1);

    List<RECORD> getRecords(long var1, RecordLoad var3) throws InvalidRecordException;

    long getNextRecordReference(RECORD var1);

    void updateRecord(RECORD var1);

    <FAILURE extends Exception> void accept(Processor<FAILURE> var1, RECORD var2) throws FAILURE;

    int getRecordSize();

    /** @deprecated */
    @Deprecated
    int getRecordDataSize();

    int getRecordsPerPage();

    void close();

    void flush();

    int getNumberOfReservedLowIds();

    int getStoreHeaderInt();

    void prepareForCommit(RECORD var1);

    void prepareForCommit(RECORD var1, IdSequence var2);

    <EXCEPTION extends Exception> void scanAllRecords(Visitor<RECORD, EXCEPTION> var1) throws EXCEPTION;

    void freeId(long var1);

    static <R extends AbstractBaseRecord> R getRecord(RecordStore<R> store, long id, RecordLoad mode) {
        R record = store.newRecord();
        store.getRecord(id, record, mode);
        return record;
    }

    static <R extends AbstractBaseRecord> R getRecord(RecordStore<R> store, long id) {
        return getRecord(store, id, RecordLoad.NORMAL);
    }

    public abstract static class Processor<FAILURE extends Exception> {
        private volatile boolean shouldStop;

        public Processor() {
        }

        public void stop() {
            this.shouldStop = true;
        }

        public abstract void processSchema(RecordStore<DynamicRecord> var1, DynamicRecord var2) throws FAILURE;

        public abstract void processNode(RecordStore<NodeRecord> var1, NodeRecord var2) throws FAILURE;
       // public abstract void processNode(RecordStore<NodeVersionRecord> var1, NodeVersionRecord var2) throws FAILURE;

        public abstract void processRelationship(RecordStore<RelationshipRecord> var1, RelationshipRecord var2) throws FAILURE;

        public abstract void processProperty(RecordStore<PropertyRecord> var1, PropertyRecord var2) throws FAILURE;

        public abstract void processString(RecordStore<DynamicRecord> var1, DynamicRecord var2, IdType var3) throws FAILURE;

        public abstract void processArray(RecordStore<DynamicRecord> var1, DynamicRecord var2) throws FAILURE;

        public abstract void processLabelArrayWithOwner(RecordStore<DynamicRecord> var1, DynamicRecord var2) throws FAILURE;

        public abstract void processRelationshipTypeToken(RecordStore<RelationshipTypeTokenRecord> var1, RelationshipTypeTokenRecord var2) throws FAILURE;

        public abstract void processPropertyKeyToken(RecordStore<PropertyKeyTokenRecord> var1, PropertyKeyTokenRecord var2) throws FAILURE;

        public abstract void processLabelToken(RecordStore<LabelTokenRecord> var1, LabelTokenRecord var2) throws FAILURE;

        public abstract void processRelationshipGroup(RecordStore<RelationshipGroupRecord> var1, RelationshipGroupRecord var2) throws FAILURE;

        protected <R extends AbstractBaseRecord> R getRecord(RecordStore<R> store, long id, R into) {
            store.getRecord(id, into, RecordLoad.FORCE);
            return into;
        }

        public <R extends AbstractBaseRecord> void applyFiltered(RecordStore<R> store, Predicate<? super R>... filters) throws FAILURE {
            this.apply(store, ProgressListener.NONE, filters);
        }

        public <R extends AbstractBaseRecord> void applyFiltered(RecordStore<R> store, ProgressListener progressListener, Predicate<? super R>... filters) throws FAILURE {
            this.apply(store, progressListener, filters);
        }

        private <R extends AbstractBaseRecord> void apply(RecordStore<R> store, ProgressListener progressListener, Predicate<? super R>... filters) throws FAILURE {
            ResourceIterable<R> iterable = Scanner.scan(store, true, filters);
            ResourceIterator<R> scan = iterable.iterator();
            Throwable var6 = null;

            try {
                while(scan.hasNext()) {
                    R record = (R) scan.next();
                    if (this.shouldStop) {
                        break;
                    }

                    store.accept(this, record);
                    progressListener.set(record.getId());
                }

                progressListener.done();
            } catch (Throwable var15) {
                var6 = var15;
                throw var15;
            } finally {
                if (scan != null) {
                    if (var6 != null) {
                        try {
                            scan.close();
                        } catch (Throwable var14) {
                            var6.addSuppressed(var14);
                        }
                    } else {
                        scan.close();
                    }
                }

            }

        }
    }

    public static class Delegator<R extends AbstractBaseRecord> implements RecordStore<R> {
        private final RecordStore<R> actual;

        public void setHighestPossibleIdInUse(long highestIdInUse) {
            this.actual.setHighestPossibleIdInUse(highestIdInUse);
        }

        public R newRecord() {
            return this.actual.newRecord();
        }

        public R getRecord(long id, R target, RecordLoad mode) throws InvalidRecordException {
            return this.actual.getRecord(id, target, mode);
        }

        public PageCursor openPageCursorForReading(long id) {
            return this.actual.openPageCursorForReading(id);
        }

        public void getRecordByCursor(long id, R target, RecordLoad mode, PageCursor cursor) throws InvalidRecordException {
            this.actual.getRecordByCursor(id, target, mode, cursor);
        }

        public void nextRecordByCursor(R target, RecordLoad mode, PageCursor cursor) throws InvalidRecordException {
            this.actual.nextRecordByCursor(target, mode, cursor);
        }

        public List<R> getRecords(long firstId, RecordLoad mode) throws InvalidRecordException {
            return this.actual.getRecords(firstId, mode);
        }

        public long getNextRecordReference(R record) {
            return this.actual.getNextRecordReference(record);
        }

        public Delegator(RecordStore<R> actual) {
            this.actual = actual;
        }

        public long nextId() {
            return this.actual.nextId();
        }

        public IdRange nextIdBatch(int size) {
            return this.actual.nextIdBatch(size);
        }

        public File getStorageFile() {
            return this.actual.getStorageFile();
        }

        public long getHighId() {
            return this.actual.getHighId();
        }

        public long getHighestPossibleIdInUse() {
            return this.actual.getHighestPossibleIdInUse();
        }

        public void updateRecord(R record) {
            this.actual.updateRecord(record);
        }

        public <FAILURE extends Exception> void accept(Processor<FAILURE> processor, R record) throws FAILURE {
            this.actual.accept(processor, record);
        }

        public int getRecordSize() {
            return this.actual.getRecordSize();
        }

        public int getRecordDataSize() {
            return this.actual.getRecordDataSize();
        }

        public int getRecordsPerPage() {
            return this.actual.getRecordsPerPage();
        }

        public int getStoreHeaderInt() {
            return this.actual.getStoreHeaderInt();
        }

        public void close() {
            this.actual.close();
        }

        public int getNumberOfReservedLowIds() {
            return this.actual.getNumberOfReservedLowIds();
        }

        public void flush() {
            this.actual.flush();
        }

        public void ensureHeavy(R record) {
            this.actual.ensureHeavy(record);
        }

        public void prepareForCommit(R record) {
            this.actual.prepareForCommit(record);
        }

        public void prepareForCommit(R record, IdSequence idSequence) {
            this.actual.prepareForCommit(record, idSequence);
        }

        public <EXCEPTION extends Exception> void scanAllRecords(Visitor<R, EXCEPTION> visitor) throws EXCEPTION {
            this.actual.scanAllRecords(visitor);
        }

        public void freeId(long id) {
            this.actual.freeId(id);
        }
    }
}
