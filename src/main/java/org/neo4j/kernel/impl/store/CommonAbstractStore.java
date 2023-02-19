package org.neo4j.kernel.impl.store;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.ArrayUtil;
import org.neo4j.helpers.Exceptions;
import org.neo4j.helpers.collection.Visitor;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.PageCacheOpenOptions;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.PagedFile;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.format.RecordFormat;
import org.neo4j.kernel.impl.store.id.IdGenerator;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdRange;
import org.neo4j.kernel.impl.store.id.IdSequence;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.impl.store.id.validation.IdValidator;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.Logger;

public abstract class CommonAbstractStore<RECORD extends AbstractBaseRecord, HEADER extends StoreHeader> implements RecordStore<RECORD>, AutoCloseable {
    static final String UNKNOWN_VERSION = "Unknown";
    protected final Config configuration;
    protected final PageCache pageCache;
    protected final IdType idType;
    protected final IdGeneratorFactory idGeneratorFactory;
    protected final Log log;
    protected final String storeVersion;
    protected final RecordFormat<RECORD> recordFormat;
    final File storageFile;
    private final File idFile;
    private final String typeDescriptor;
    protected PagedFile pagedFile;
    protected int recordSize;
    private IdGenerator idGenerator;
    private boolean storeOk = true;
    private RuntimeException causeOfStoreNotOk;
    private final StoreHeaderFormat<HEADER> storeHeaderFormat;
    private HEADER storeHeader;
    private final OpenOption[] openOptions;

    public CommonAbstractStore(File file, File idFile, Config configuration, IdType idType, IdGeneratorFactory idGeneratorFactory, PageCache pageCache, LogProvider logProvider, String typeDescriptor, RecordFormat<RECORD> recordFormat, StoreHeaderFormat<HEADER> storeHeaderFormat, String storeVersion, OpenOption... openOptions) {
        this.storageFile = file;
        this.idFile = idFile;
        this.configuration = configuration;
        this.idGeneratorFactory = idGeneratorFactory;
        this.pageCache = pageCache;
        this.idType = idType;
        this.typeDescriptor = typeDescriptor;
        this.recordFormat = recordFormat;
        this.storeHeaderFormat = storeHeaderFormat;
        this.storeVersion = storeVersion;
        this.openOptions = openOptions;
        this.log = logProvider.getLog(this.getClass());
    }

    public void initialise(boolean createIfNotExists) {
        try {
            this.checkAndLoadStorage(createIfNotExists);
        } catch (Exception var3) {
            this.closeAndThrow(var3);
        }

    }

    private void closeAndThrow(Exception e) {
        if (this.pagedFile != null) {
            try {
                this.closeStoreFile();
            } catch (IOException var3) {
                e.addSuppressed(var3);
            }
        }

        Exceptions.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    public String getTypeDescriptor() {
        return this.typeDescriptor;
    }

    protected void checkAndLoadStorage(boolean createIfNotExists) {
        int pageSize = this.pageCache.pageSize();

        int filePageSize;
        try {
            PagedFile pagedFile = this.pageCache.map(this.storageFile, pageSize, new OpenOption[]{PageCacheOpenOptions.ANY_PAGE_SIZE});
            Throwable var5 = null;

            try {
                this.extractHeaderRecord(pagedFile);
                filePageSize = this.pageCache.pageSize() - this.pageCache.pageSize() % this.getRecordSize();
            } catch (Throwable var17) {
                var5 = var17;
                throw var17;
            } finally {
                if (pagedFile != null) {
                    if (var5 != null) {
                        try {
                            pagedFile.close();
                        } catch (Throwable var16) {
                            var5.addSuppressed(var16);
                        }
                    } else {
                        pagedFile.close();
                    }
                }

            }
        } catch (StoreNotFoundException | NoSuchFileException var20) {
            if (createIfNotExists) {
                try {
                    this.createStore(pageSize);
                    return;
                } catch (IOException var18) {
                    var20.addSuppressed(var18);
                }
            }

            if (var20 instanceof StoreNotFoundException) {
                throw (StoreNotFoundException)var20;
            }

            throw new StoreNotFoundException("Store file not found: " + this.storageFile, var20);
        } catch (IOException var21) {
            throw new UnderlyingStorageException("Unable to open store file: " + this.storageFile, var21);
        }

        this.loadStorage(filePageSize);
    }

    private void createStore(int pageSize) throws IOException {
        PagedFile file = this.pageCache.map(this.storageFile, pageSize, new OpenOption[]{StandardOpenOption.CREATE});
        Throwable var3 = null;

        try {
            this.initialiseNewStoreFile(file);
        } catch (Throwable var12) {
            var3 = var12;
            throw var12;
        } finally {
            if (file != null) {
                if (var3 != null) {
                    try {
                        file.close();
                    } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                    }
                } else {
                    file.close();
                }
            }

        }

        this.checkAndLoadStorage(false);
    }

    private void loadStorage(int filePageSize) {
        try {
            this.pagedFile = this.pageCache.map(this.storageFile, filePageSize, this.openOptions);
            this.loadIdGenerator();
        } catch (IOException var3) {
            throw new UnderlyingStorageException("Unable to open store file: " + this.storageFile, var3);
        }
    }

    protected void initialiseNewStoreFile(PagedFile file) throws IOException {
        if (this.getNumberOfReservedLowIds() > 0) {
            PageCursor pageCursor = file.io(0L, 2);
            Throwable var3 = null;

            try {
                if (pageCursor.next()) {
                    pageCursor.setOffset(0);
                    this.createHeaderRecord(pageCursor);
                    if (pageCursor.checkAndClearBoundsFlag()) {
                        throw new UnderlyingStorageException("Out of page bounds when writing header; page size too small: " + this.pageCache.pageSize() + " bytes.");
                    }
                }
            } catch (Throwable var12) {
                var3 = var12;
                throw var12;
            } finally {
                if (pageCursor != null) {
                    if (var3 != null) {
                        try {
                            pageCursor.close();
                        } catch (Throwable var11) {
                            var3.addSuppressed(var11);
                        }
                    } else {
                        pageCursor.close();
                    }
                }

            }
        }

        this.recordSize = this.determineRecordSize();
        this.idGeneratorFactory.create(this.idFile, (long)this.getNumberOfReservedLowIds(), false);
    }

    private void createHeaderRecord(PageCursor cursor) {
        int offset = cursor.getOffset();
        this.storeHeaderFormat.writeHeader(cursor);
        cursor.setOffset(offset);
        this.readHeaderAndInitializeRecordFormat(cursor);
    }

    private void extractHeaderRecord(PagedFile pagedFile) throws IOException {
        if (this.getNumberOfReservedLowIds() > 0) {
            PageCursor pageCursor = pagedFile.io(0L, 1);
            Throwable var3 = null;

            try {
                if (!pageCursor.next()) {
                    throw new StoreNotFoundException("Fail to read header record of store file: " + this.storageFile);
                }

                do {
                    pageCursor.setOffset(0);
                    this.readHeaderAndInitializeRecordFormat(pageCursor);
                } while(pageCursor.shouldRetry());

                if (pageCursor.checkAndClearBoundsFlag()) {
                    throw new UnderlyingStorageException("Out of page bounds when reading header; page size too small: " + this.pageCache.pageSize() + " bytes.");
                }
            } catch (Throwable var12) {
                var3 = var12;
                throw var12;
            } finally {
                if (pageCursor != null) {
                    if (var3 != null) {
                        try {
                            pageCursor.close();
                        } catch (Throwable var11) {
                            var3.addSuppressed(var11);
                        }
                    } else {
                        pageCursor.close();
                    }
                }

            }
        } else {
            this.readHeaderAndInitializeRecordFormat((PageCursor)null);
        }

        this.recordSize = this.determineRecordSize();
    }

    protected long pageIdForRecord(long id) {
        return RecordPageLocationCalculator.pageIdForRecord(id, this.pagedFile.pageSize(), this.recordSize);
    }

    protected int offsetForId(long id) {
        return RecordPageLocationCalculator.offsetForId(id, this.pagedFile.pageSize(), this.recordSize);
    }

    public int getRecordsPerPage() {
        return this.pagedFile.pageSize() / this.recordSize;
    }

    public byte[] getRawRecordData(long id) throws IOException {
        byte[] data = new byte[this.recordSize];
        long pageId = this.pageIdForRecord(id);
        int offset = this.offsetForId(id);
        PageCursor cursor = this.pagedFile.io(pageId, 1);
        Throwable var8 = null;

        try {
            if (cursor.next()) {
                cursor.setOffset(offset);
                cursor.mark();

                do {
                    cursor.setOffsetToMark();
                    cursor.getBytes(data);
                } while(cursor.shouldRetry());

                this.checkForDecodingErrors(cursor, id, RecordLoad.CHECK);
            }
        } catch (Throwable var17) {
            var8 = var17;
            throw var17;
        } finally {
            if (cursor != null) {
                if (var8 != null) {
                    try {
                        cursor.close();
                    } catch (Throwable var16) {
                        var8.addSuppressed(var16);
                    }
                } else {
                    cursor.close();
                }
            }

        }

        return data;
    }

    private void readHeaderAndInitializeRecordFormat(PageCursor cursor) {
        this.storeHeader = this.storeHeaderFormat.readHeader(cursor);
    }

    private void loadIdGenerator() {
        try {
            if (this.storeOk) {
                this.openIdGenerator();
            }
        } catch (InvalidIdGeneratorException var5) {
            this.setStoreNotOk(var5);
        } finally {
            if (!this.getStoreOk()) {
                this.log.debug(this.storageFile + " non clean shutdown detected");
            }

        }

    }

    public boolean isInUse(long id) {
        long pageId = this.pageIdForRecord(id);
        int offset = this.offsetForId(id);

        try {
            PageCursor cursor = this.pagedFile.io(pageId, 1);
            Throwable var7 = null;

            boolean var9;
            try {
                boolean recordIsInUse = false;
                if (cursor.next()) {
                    cursor.setOffset(offset);
                    cursor.mark();

                    do {
                        cursor.setOffsetToMark();
                        recordIsInUse = this.isInUse(cursor);
                    } while(cursor.shouldRetry());

                    this.checkForDecodingErrors(cursor, id, RecordLoad.NORMAL);
                }

                var9 = recordIsInUse;
            } catch (Throwable var19) {
                var7 = var19;
                throw var19;
            } finally {
                if (cursor != null) {
                    if (var7 != null) {
                        try {
                            cursor.close();
                        } catch (Throwable var18) {
                            var7.addSuppressed(var18);
                        }
                    } else {
                        cursor.close();
                    }
                }

            }

            return var9;
        } catch (IOException var21) {
            throw new UnderlyingStorageException(var21);
        }
    }

    public PageCursor openPageCursorForReading(long id) {
        try {
            long pageId = this.pageIdForRecord(id);
            return this.pagedFile.io(pageId, 1);
        } catch (IOException var5) {
            throw new UnderlyingStorageException(var5);
        }
    }

    final void rebuildIdGenerator() {
        int blockSize = this.getRecordSize();
        if (blockSize <= 0) {
            throw new InvalidRecordException("Illegal blockSize: " + blockSize);
        } else {
            this.log.info("Rebuilding id generator for[" + this.getStorageFile() + "] ...");
            this.closeIdGenerator();
            this.createIdGenerator(this.idFile);
            this.openIdGenerator();
            long defraggedCount = 0L;
            boolean fastRebuild = this.isOnlyFastIdGeneratorRebuildEnabled(this.configuration);

            try {
                long foundHighId = this.scanForHighId();
                this.setHighId(foundHighId);
                if (!fastRebuild) {
                    PageCursor cursor = this.pagedFile.io(0L, 10);
                    Throwable var8 = null;

                    try {
                        defraggedCount = this.rebuildIdGeneratorSlow(cursor, this.getRecordsPerPage(), blockSize, foundHighId);
                    } catch (Throwable var18) {
                        var8 = var18;
                        throw var18;
                    } finally {
                        if (cursor != null) {
                            if (var8 != null) {
                                try {
                                    cursor.close();
                                } catch (Throwable var17) {
                                    var8.addSuppressed(var17);
                                }
                            } else {
                                cursor.close();
                            }
                        }

                    }
                }
            } catch (IOException var20) {
                throw new UnderlyingStorageException("Unable to rebuild id generator " + this.getStorageFile(), var20);
            }

            this.log.info("[" + this.getStorageFile() + "] high id=" + this.getHighId() + " (defragged=" + defraggedCount + ")");
            this.log.info(this.getStorageFile() + " rebuild id generator, highId=" + this.getHighId() + " defragged count=" + defraggedCount);
            if (!fastRebuild) {
                this.closeIdGenerator();
                this.openIdGenerator();
            }

        }
    }

    protected boolean isOnlyFastIdGeneratorRebuildEnabled(Config config) {
        return (Boolean)config.get(GraphDatabaseSettings.rebuild_idgenerators_fast);
    }

    private long rebuildIdGeneratorSlow(PageCursor cursor, int recordsPerPage, int blockSize, long foundHighId) throws IOException {
        if (!cursor.isWriteLocked()) {
            throw new IllegalArgumentException("The store scanning id generator rebuild process requires a page cursor that is write-locked");
        } else {
            long defragCount = 0L;
            long[] freedBatch = new long[recordsPerPage];
            int startingId = this.getNumberOfReservedLowIds();

            for(boolean done = false; !done && cursor.next(); startingId = 0) {
                long idPageOffset = cursor.getCurrentPageId() * (long)recordsPerPage;
                int defragged = 0;

                int i;
                for(i = startingId; i < recordsPerPage; ++i) {
                    int offset = i * blockSize;
                    cursor.setOffset(offset);
                    long recordId = idPageOffset + (long)i;
                    if (recordId >= foundHighId) {
                        done = true;
                        break;
                    }

                    if (!this.isInUse(cursor)) {
                        freedBatch[defragged++] = recordId;
                    } else if (this.isRecordReserved(cursor)) {
                        cursor.setOffset(offset);
                        cursor.putByte(Record.NOT_IN_USE.byteValue());
                        cursor.putInt(0);
                        freedBatch[defragged++] = recordId;
                    }
                }

                this.checkIdScanCursorBounds(cursor);

                for(i = 0; i < defragged; ++i) {
                    this.freeId(freedBatch[i]);
                }

                defragCount += (long)defragged;
            }

            return defragCount;
        }
    }

    private void checkIdScanCursorBounds(PageCursor cursor) {
        if (cursor.checkAndClearBoundsFlag()) {
            throw new UnderlyingStorageException("Out of bounds access on page " + cursor.getCurrentPageId() + " detected while scanning the " + this.storageFile + " file for deleted records");
        }
    }

    void setStoreNotOk(RuntimeException cause) {
        this.storeOk = false;
        this.causeOfStoreNotOk = cause;
        this.idGenerator = null;
    }

    boolean getStoreOk() {
        return this.storeOk;
    }

    void checkStoreOk() {
        if (!this.storeOk) {
            throw this.causeOfStoreNotOk;
        }
    }

    public long nextId() {
        this.assertIdGeneratorInitialized();
        return this.idGenerator.nextId();
    }

    private void assertIdGeneratorInitialized() {
        if (this.idGenerator == null) {
            throw new IllegalStateException("IdGenerator is not initialized");
        }
    }

    public IdRange nextIdBatch(int size) {
        this.assertIdGeneratorInitialized();
        return this.idGenerator.nextIdBatch(size);
    }

    public void freeId(long id) {
        IdGenerator generator = this.idGenerator;
        if (generator != null) {
            generator.freeId(id);
        }

    }

    public long getHighId() {
        return this.idGenerator != null ? this.idGenerator.getHighId() : this.scanForHighId();
    }

    public void setHighId(long highId) {
        IdGenerator generator = this.idGenerator;
        if (generator != null) {
            synchronized(generator) {
                if (highId > generator.getHighId()) {
                    generator.setHighId(highId);
                }
            }
        }

    }

    void makeStoreOk() {
        if (!this.storeOk) {
            this.rebuildIdGenerator();
            this.storeOk = true;
            this.causeOfStoreNotOk = null;
        }

    }

    public File getStorageFile() {
        return this.storageFile;
    }

    void openIdGenerator() {
        this.idGenerator = this.idGeneratorFactory.open(this.idFile, this.getIdType(), this::scanForHighId, this.recordFormat.getMaxId());
    }

    protected long scanForHighId() {
        try {
            PageCursor cursor = this.pagedFile.io(0L, 1);
            Throwable var2 = null;

            long chunkStartId;
            try {
                int recordsPerPage = this.getRecordsPerPage();
                int recordSize = this.getRecordSize();
                long highestId = (long)this.getNumberOfReservedLowIds();
                long chunkSizeInPages = 256L;

                for(long chunkEndId = this.pagedFile.getLastPageId(); chunkEndId >= 0L; chunkEndId = chunkStartId - 1L) {
                    chunkStartId = Math.max(chunkEndId - 256L, 0L);
                    preFetchChunk(cursor, chunkStartId, chunkEndId);

                    for(long currentId = chunkEndId; currentId >= chunkStartId && cursor.next(currentId); --currentId) {
                        boolean found;
                        do {
                            found = false;

                            for(int offset = recordsPerPage * recordSize - recordSize; offset >= 0; offset -= recordSize) {
                                cursor.setOffset(offset);
                                if (this.isInUse(cursor)) {
                                    highestId = cursor.getCurrentPageId() * (long)recordsPerPage + (long)(offset / recordSize) + 1L;
                                    found = true;
                                    break;
                                }
                            }
                        } while(cursor.shouldRetry());

                        this.checkIdScanCursorBounds(cursor);
                        if (found) {
                            long var31 = highestId;
                            return var31;
                        }
                    }
                }

                chunkStartId = (long)this.getNumberOfReservedLowIds();
            } catch (Throwable var28) {
                var2 = var28;
                throw var28;
            } finally {
                if (cursor != null) {
                    if (var2 != null) {
                        try {
                            cursor.close();
                        } catch (Throwable var27) {
                            var2.addSuppressed(var27);
                        }
                    } else {
                        cursor.close();
                    }
                }

            }

            return chunkStartId;
        } catch (IOException var30) {
            throw new UnderlyingStorageException("Unable to find high id by scanning backwards " + this.getStorageFile(), var30);
        }
    }

    private static void preFetchChunk(PageCursor cursor, long pageIdStart, long pageIdEnd) throws IOException {
        for(long currentPageId = pageIdStart; currentPageId <= pageIdEnd; ++currentPageId) {
            cursor.next(currentPageId);
        }

    }

    protected int determineRecordSize() {
        return this.recordFormat.getRecordSize(this.storeHeader);
    }

    public final int getRecordSize() {
        return this.recordSize;
    }

    public int getRecordDataSize() {
        return this.recordSize - this.recordFormat.getRecordHeaderSize();
    }

    private boolean isInUse(PageCursor cursor) {
        return this.recordFormat.isInUse(cursor);
    }

    protected boolean isRecordReserved(PageCursor cursor) {
        return false;
    }

    private void createIdGenerator(File fileName) {
        this.idGeneratorFactory.create(fileName, 0L, false);
    }

    void closeIdGenerator() {
        if (this.idGenerator != null) {
            this.idGenerator.close();
        }

    }

    public void flush() {
        try {
            this.pagedFile.flushAndForce();
        } catch (IOException var2) {
            throw new UnderlyingStorageException("Failed to flush", var2);
        }
    }

    protected void assertNotClosed() {
        if (this.pagedFile == null) {
            throw new IllegalStateException(this + " for file '" + this.storageFile + "' is closed");
        }
    }

    public void close() {
        try {
            this.closeStoreFile();
        } catch (IllegalStateException | IOException var2) {
            throw new UnderlyingStorageException("Failed to close store file: " + this.getStorageFile(), var2);
        }
    }

    private void closeStoreFile() throws IOException {
        try {
            if (this.pagedFile != null) {
                this.pagedFile.close();
            }

            if (this.idGenerator != null) {
                if (ArrayUtil.contains(this.openOptions, StandardOpenOption.DELETE_ON_CLOSE)) {
                    this.idGenerator.delete();
                } else {
                    this.idGenerator.close();
                }
            }
        } finally {
            this.pagedFile = null;
        }

    }

    public long getHighestPossibleIdInUse() {
        return this.idGenerator != null ? this.idGenerator.getHighestPossibleIdInUse() : this.scanForHighId() - 1L;
    }

    public void setHighestPossibleIdInUse(long highId) {
        this.setHighId(highId + 1L);
    }

    public long getNumberOfIdsInUse() {
        this.assertIdGeneratorInitialized();
        return this.idGenerator.getNumberOfIdsInUse();
    }

    public int getNumberOfReservedLowIds() {
        return this.storeHeaderFormat.numberOfReservedRecords();
    }

    public IdType getIdType() {
        return this.idType;
    }

    void logVersions(Logger logger) {
        logger.log("  " + this.getTypeDescriptor() + " " + this.storeVersion);
    }

    void logIdUsage(Logger logger) {
        logger.log(String.format("  %s: used=%s high=%s", this.getTypeDescriptor(), this.getNumberOfIdsInUse(), this.getHighestPossibleIdInUse()));
    }

    void visitStore(Visitor<CommonAbstractStore<RECORD, HEADER>, RuntimeException> visitor) {
        visitor.visit(this);
    }

    final void deleteIdGenerator() {
        if (this.idGenerator != null) {
            this.idGenerator.delete();
            this.idGenerator = null;
            this.setStoreNotOk(new IllegalStateException("IdGenerator is not initialized"));
        }

    }

    public long getNextRecordReference(RECORD record) {
        return this.recordFormat.getNextRecordReference(record);
    }

    public RECORD newRecord() {
        return this.recordFormat.newRecord();
    }

    public RECORD getRecord(long id, RECORD record, RecordLoad mode) {
        try {
            PageCursor cursor = this.pagedFile.io((long)this.getNumberOfReservedLowIds(), 1);
            Throwable var6 = null;

            AbstractBaseRecord var7;
            try {
                this.readIntoRecord(id, record, mode, cursor);
                var7 = record;
            } catch (Throwable var17) {
                var6 = var17;
                throw var17;
            } finally {
                if (cursor != null) {
                    if (var6 != null) {
                        try {
                            cursor.close();
                        } catch (Throwable var16) {
                            var6.addSuppressed(var16);
                        }
                    } else {
                        cursor.close();
                    }
                }

            }

            return (RECORD) var7;
        } catch (IOException var19) {
            throw new UnderlyingStorageException(var19);
        }
    }

    public void getRecordByCursor(long id, RECORD record, RecordLoad mode, PageCursor cursor) throws UnderlyingStorageException {
        try {
            this.readIntoRecord(id, record, mode, cursor);
        } catch (IOException var7) {
            throw new UnderlyingStorageException(var7);
        }
    }

    private void readIntoRecord(long id, RECORD record, RecordLoad mode, PageCursor cursor) throws IOException {
        record.setId(id);
        long pageId = this.pageIdForRecord(id);
        int offset = this.offsetForId(id);
        if (cursor.next(pageId)) {
            cursor.setOffset(offset);
            this.readRecordFromPage(id, record, mode, cursor);
        } else {
            this.verifyAfterNotRead(record, mode);
        }

    }

    public void nextRecordByCursor(RECORD record, RecordLoad mode, PageCursor cursor) throws UnderlyingStorageException {
        if (cursor.getCurrentPageId() < -1L) {
            throw new IllegalArgumentException("Pages are assumed to be positive or -1 if not initialized");
        } else {
            try {
                int offset = cursor.getOffset();
                long id = record.getId() + 1L;
                record.setId(id);
                long pageId = cursor.getCurrentPageId();
                if (offset >= this.pagedFile.pageSize() || pageId < 0L) {
                    if (!cursor.next()) {
                        this.verifyAfterNotRead(record, mode);
                        return;
                    }

                    cursor.setOffset(0);
                }

                this.readRecordFromPage(id, record, mode, cursor);
            } catch (IOException var9) {
                throw new UnderlyingStorageException(var9);
            }
        }
    }

    private void readRecordFromPage(long id, RECORD record, RecordLoad mode, PageCursor cursor) throws IOException {
        cursor.mark();

        do {
            this.prepareForReading(cursor, record);
            this.recordFormat.read(record, cursor, mode, this.recordSize);
        } while(cursor.shouldRetry());

        this.checkForDecodingErrors(cursor, id, mode);
        this.verifyAfterReading(record, mode);
    }

    public void updateRecord(RECORD record) {
        long id = record.getId();
        IdValidator.assertValidId(this.getIdType(), id, this.recordFormat.getMaxId());
        long pageId = this.pageIdForRecord(id);
        int offset = this.offsetForId(id);

        try {
            PageCursor cursor = this.pagedFile.io(pageId, 2);
            Throwable var8 = null;

            try {
                if (cursor.next()) {
                    cursor.setOffset(offset);
                    this.recordFormat.write(record, cursor, this.recordSize);
                    this.checkForDecodingErrors(cursor, id, RecordLoad.NORMAL);
                    if (!record.inUse()) {
                        this.freeId(id);
                    }

                    if ((!record.inUse() || !record.requiresSecondaryUnit()) && record.hasSecondaryUnitId()) {
                        this.freeId(record.getSecondaryUnitId());
                    }
                }
            } catch (Throwable var18) {
                var8 = var18;
                throw var18;
            } finally {
                if (cursor != null) {
                    if (var8 != null) {
                        try {
                            cursor.close();
                        } catch (Throwable var17) {
                            var8.addSuppressed(var17);
                        }
                    } else {
                        cursor.close();
                    }
                }

            }

        } catch (IOException var20) {
            throw new UnderlyingStorageException(var20);
        }
    }

    public void prepareForCommit(RECORD record) {
        this.prepareForCommit(record, this);
    }

    public void prepareForCommit(RECORD record, IdSequence idSequence) {
        if (record.inUse()) {
            this.recordFormat.prepare(record, this.recordSize, idSequence);
        }

    }

    public <EXCEPTION extends Exception> void scanAllRecords(Visitor<RECORD, EXCEPTION> visitor) throws EXCEPTION {
        PageCursor cursor = this.openPageCursorForReading(0L);
        Throwable var3 = null;

        try {
            RECORD record = this.newRecord();
            long highId = this.getHighId();

            for(long id = (long)this.getNumberOfReservedLowIds(); id < highId; ++id) {
                this.getRecordByCursor(id, record, RecordLoad.CHECK, cursor);
                if (record.inUse()) {
                    visitor.visit(record);
                }
            }
        } catch (Throwable var16) {
            var3 = var16;
            throw var16;
        } finally {
            if (cursor != null) {
                if (var3 != null) {
                    try {
                        cursor.close();
                    } catch (Throwable var15) {
                        var3.addSuppressed(var15);
                    }
                } else {
                    cursor.close();
                }
            }

        }

    }

    public List<RECORD> getRecords(long firstId, RecordLoad mode) {
        if (Record.NULL_REFERENCE.is(firstId)) {
            return Collections.emptyList();
        } else {
            List<RECORD> records = new ArrayList();
            long id = firstId;
            PageCursor cursor = this.openPageCursorForReading(firstId);
            Throwable var8 = null;

            try {
                do {
                    RECORD record = this.newRecord();
                    this.getRecordByCursor(id, record, mode, cursor);
                    records.add(record);
                    id = this.getNextRecordReference(record);
                } while(!Record.NULL_REFERENCE.is(id));
            } catch (Throwable var17) {
                var8 = var17;
                throw var17;
            } finally {
                if (cursor != null) {
                    if (var8 != null) {
                        try {
                            cursor.close();
                        } catch (Throwable var16) {
                            var8.addSuppressed(var16);
                        }
                    } else {
                        cursor.close();
                    }
                }

            }

            return records;
        }
    }

    private void verifyAfterNotRead(RECORD record, RecordLoad mode) {
        record.clear();
        mode.verify(record);
    }

    final void checkForDecodingErrors(PageCursor cursor, long recordId, RecordLoad mode) {
        if (mode.checkForOutOfBounds(cursor)) {
            this.throwOutOfBoundsException(recordId);
        }

        mode.clearOrThrowCursorError(cursor);
    }

    private void throwOutOfBoundsException(long recordId) {
        RECORD record = this.newRecord();
        record.setId(recordId);
        long pageId = this.pageIdForRecord(recordId);
        int offset = this.offsetForId(recordId);
        throw new UnderlyingStorageException(buildOutOfBoundsExceptionMessage(record, pageId, offset, this.recordSize, this.pagedFile.pageSize(), this.storageFile.getAbsolutePath()));
    }

    protected static String buildOutOfBoundsExceptionMessage(AbstractBaseRecord record, long pageId, int offset, int recordSize, int pageSize, String filename) {
        return "Access to record " + record + " went out of bounds of the page. The record size is " + recordSize + " bytes, and the access was at offset " + offset + " bytes into page " + pageId + ", and the pages have a capacity of " + pageSize + " bytes. The mapped store file in question is " + filename;
    }

    private void verifyAfterReading(RECORD record, RecordLoad mode) {
        if (!mode.verify(record)) {
            record.clear();
        }

    }

    private void prepareForReading(PageCursor cursor, RECORD record) {
        record.setInUse(false);
        cursor.setOffsetToMark();
    }

    public void ensureHeavy(RECORD record) {
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public int getStoreHeaderInt() {
        return ((IntStoreHeader)this.storeHeader).value();
    }
}

