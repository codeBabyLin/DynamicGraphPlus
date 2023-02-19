//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.api;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Stream;
import org.neo4j.collection.pool.Pool;
import org.neo4j.graphdb.NotInTransactionException;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.internal.kernel.api.CursorFactory;
import org.neo4j.internal.kernel.api.ExecutionStatistics;
import org.neo4j.internal.kernel.api.ExplicitIndexRead;
import org.neo4j.internal.kernel.api.ExplicitIndexWrite;
import org.neo4j.internal.kernel.api.Locks;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.Read;
import org.neo4j.internal.kernel.api.RelationshipScanCursor;
import org.neo4j.internal.kernel.api.SchemaRead;
import org.neo4j.internal.kernel.api.SchemaWrite;
import org.neo4j.internal.kernel.api.Token;
import org.neo4j.internal.kernel.api.TokenRead;
import org.neo4j.internal.kernel.api.TokenWrite;
import org.neo4j.internal.kernel.api.Write;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.exceptions.InvalidTransactionTypeKernelException;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo4j.internal.kernel.api.exceptions.schema.SchemaKernelException;
import org.neo4j.internal.kernel.api.schema.SchemaDescriptor;
import org.neo4j.internal.kernel.api.security.AccessMode;
import org.neo4j.internal.kernel.api.security.AuthSubject;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.io.pagecache.tracing.cursor.PageCursorTracer;
import org.neo4j.io.pagecache.tracing.cursor.PageCursorTracerSupplier;
import org.neo4j.io.pagecache.tracing.cursor.context.VersionContextSupplier;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.SilentTokenNameLookup;
import org.neo4j.kernel.api.KernelTransaction.CloseListener;
import org.neo4j.kernel.api.KernelTransaction.Revertable;
import org.neo4j.kernel.api.exceptions.ConstraintViolationTransactionFailureException;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.exceptions.Status.Transaction;
import org.neo4j.kernel.api.explicitindex.AutoIndexing;
import org.neo4j.kernel.api.txstate.ExplicitIndexTransactionState;
import org.neo4j.kernel.api.txstate.TransactionState;
import org.neo4j.kernel.api.txstate.TxStateHolder;
import org.neo4j.kernel.api.txstate.auxiliary.AuxiliaryTransactionState;
import org.neo4j.kernel.api.txstate.auxiliary.AuxiliaryTransactionStateCloseException;
import org.neo4j.kernel.api.txstate.auxiliary.AuxiliaryTransactionStateHolder;
import org.neo4j.kernel.api.txstate.auxiliary.AuxiliaryTransactionStateManager;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.api.TransactionHooks.TransactionHooksState;
import org.neo4j.kernel.impl.api.index.IndexingService;
import org.neo4j.kernel.impl.api.state.ConstraintIndexCreator;
import org.neo4j.kernel.impl.api.state.TxState;
import org.neo4j.kernel.impl.constraints.ConstraintSemantics;
import org.neo4j.kernel.impl.core.TokenHolders;
import org.neo4j.kernel.impl.factory.AccessCapability;
import org.neo4j.kernel.impl.index.ExplicitIndexStore;
import org.neo4j.kernel.impl.locking.ActiveLock;
import org.neo4j.kernel.impl.locking.StatementLocks;
import org.neo4j.kernel.impl.locking.Locks.Client;
import org.neo4j.kernel.impl.newapi.AllStoreHolder;
import org.neo4j.kernel.impl.newapi.DefaultCursors;
import org.neo4j.kernel.impl.newapi.IndexTxStateUpdater;
import org.neo4j.kernel.impl.newapi.KernelToken;
import org.neo4j.kernel.impl.newapi.Operations;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.impl.transaction.TransactionHeaderInformationFactory;
import org.neo4j.kernel.impl.transaction.TransactionMonitor;
import org.neo4j.kernel.impl.transaction.log.PhysicalTransactionRepresentation;
import org.neo4j.kernel.impl.transaction.tracing.CommitEvent;
import org.neo4j.kernel.impl.transaction.tracing.TransactionEvent;
import org.neo4j.kernel.impl.transaction.tracing.TransactionTracer;
import org.neo4j.kernel.impl.util.Dependencies;
import org.neo4j.kernel.impl.util.collection.CollectionsFactory;
import org.neo4j.kernel.impl.util.collection.CollectionsFactorySupplier;
import org.neo4j.resources.CpuClock;
import org.neo4j.resources.HeapAllocation;
import org.neo4j.storageengine.api.StorageCommand;
import org.neo4j.storageengine.api.StorageEngine;
import org.neo4j.storageengine.api.StorageReader;
import org.neo4j.storageengine.api.TransactionApplicationMode;
import org.neo4j.storageengine.api.lock.LockTracer;
import org.neo4j.storageengine.api.schema.IndexDescriptor;
import org.neo4j.storageengine.api.txstate.TxStateVisitor;
import org.neo4j.storageengine.api.txstate.TxStateVisitor.Adapter;

public class KernelTransactionImplementation implements KernelTransaction, TxStateHolder, ExecutionStatistics {
    private static final long NOT_COMMITTED_TRANSACTION_ID = -1L;
    private static final long NOT_COMMITTED_TRANSACTION_COMMIT_TIME = -1L;
    private final CollectionsFactory collectionsFactory;
    private final SchemaWriteGuard schemaWriteGuard;
    private final TransactionHooks hooks;
    private final ConstraintIndexCreator constraintIndexCreator;
    private final StorageEngine storageEngine;
    private final TransactionTracer transactionTracer;
    private final Pool<KernelTransactionImplementation> pool;
    private final AuxiliaryTransactionStateManager auxTxStateManager;
    private final TransactionHeaderInformationFactory headerInformationFactory;
    private final TransactionCommitProcess commitProcess;
    private final TransactionMonitor transactionMonitor;
    private final PageCursorTracerSupplier cursorTracerSupplier;
    private final VersionContextSupplier versionContextSupplier;
    private final StorageReader storageReader;
    private final ClockContext clocks;
    private final AccessCapability accessCapability;
    private final ConstraintSemantics constraintSemantics;
    private TxState txState;
    private AuxiliaryTransactionStateHolder auxTxStateHolder;
    private volatile TransactionWriteState writeState;
    private TransactionHooksState hooksState;
    private final KernelStatement currentStatement;
    private final List<CloseListener> closeListeners = new ArrayList(2);
    private SecurityContext securityContext;
    private volatile StatementLocks statementLocks;
    private volatile long userTransactionId;
    private boolean beforeHookInvoked;
    private volatile boolean closing;
    private volatile boolean closed;
    private boolean failure;
    private boolean success;
    private volatile Status terminationReason;
    private long startTimeMillis;
    private long timeoutMillis;
    private long lastTransactionIdWhenStarted;
    private volatile long lastTransactionTimestampWhenStarted;
    private final Statistics statistics;
    private TransactionEvent transactionEvent;
    private Type type;
    private long transactionId;
    private long commitTime;
    private volatile int reuseCount;
    private volatile Map<String, Object> userMetaData;
    private final Operations operations;
    private final Lock terminationReleaseLock = new ReentrantLock();
    //Dynamicgraph
    //**********************************
    //private long startVersion;
    //private long endVersion;
    private long version;
    //Dynamicgraph
    //**********************************

    public KernelTransactionImplementation(Config config, StatementOperationParts statementOperations, SchemaWriteGuard schemaWriteGuard, TransactionHooks hooks, ConstraintIndexCreator constraintIndexCreator, Procedures procedures, TransactionHeaderInformationFactory headerInformationFactory, TransactionCommitProcess commitProcess, TransactionMonitor transactionMonitor, AuxiliaryTransactionStateManager auxTxStateManager, Pool<KernelTransactionImplementation> pool, Clock clock, AtomicReference<CpuClock> cpuClockRef, AtomicReference<HeapAllocation> heapAllocationRef, TransactionTracer transactionTracer, LockTracer lockTracer, PageCursorTracerSupplier cursorTracerSupplier, StorageEngine storageEngine, AccessCapability accessCapability, AutoIndexing autoIndexing, ExplicitIndexStore explicitIndexStore, VersionContextSupplier versionContextSupplier, CollectionsFactorySupplier collectionsFactorySupplier, ConstraintSemantics constraintSemantics, SchemaState schemaState, IndexingService indexingService, TokenHolders tokenHolders, Dependencies dataSourceDependencies) {
        this.schemaWriteGuard = schemaWriteGuard;
        this.hooks = hooks;
        this.constraintIndexCreator = constraintIndexCreator;
        this.headerInformationFactory = headerInformationFactory;
        this.commitProcess = commitProcess;
        this.transactionMonitor = transactionMonitor;
        this.storageReader = storageEngine.newReader();
        this.storageEngine = storageEngine;
        this.auxTxStateManager = auxTxStateManager;
        this.pool = pool;
        this.clocks = new ClockContext(clock);
        this.transactionTracer = transactionTracer;
        this.cursorTracerSupplier = cursorTracerSupplier;
        this.versionContextSupplier = versionContextSupplier;
        this.currentStatement = new KernelStatement(this, this, this.storageReader, lockTracer, statementOperations, this.clocks, versionContextSupplier);
        this.accessCapability = accessCapability;
        this.statistics = new Statistics(this, cpuClockRef, heapAllocationRef);
        this.userMetaData = Collections.emptyMap();
        this.constraintSemantics = constraintSemantics;
        DefaultCursors cursors = new DefaultCursors(this.storageReader);
        AllStoreHolder allStoreHolder = new AllStoreHolder(this.storageReader, this, cursors, explicitIndexStore, procedures, schemaState, dataSourceDependencies);
        this.operations = new Operations(allStoreHolder, new IndexTxStateUpdater(allStoreHolder, indexingService), this.storageReader, this, new KernelToken(this.storageReader, this, tokenHolders), cursors, autoIndexing, constraintIndexCreator, constraintSemantics, indexingService, config);
        this.collectionsFactory = collectionsFactorySupplier.create();
    }

    public KernelTransactionImplementation initialize(long lastCommittedTx, long lastTimeStamp, StatementLocks statementLocks, Type type, SecurityContext frozenSecurityContext, long transactionTimeout, long userTransactionId) {
        this.type = type;
        this.statementLocks = statementLocks;
        this.userTransactionId = userTransactionId;
        this.terminationReason = null;
        this.closing = false;
        this.closed = false;
        this.beforeHookInvoked = false;
        this.failure = false;
        this.success = false;
        this.writeState = TransactionWriteState.NONE;
        this.startTimeMillis = this.clocks.systemClock().millis();
        this.timeoutMillis = transactionTimeout;
        this.lastTransactionIdWhenStarted = lastCommittedTx;
        this.lastTransactionTimestampWhenStarted = lastTimeStamp;
        this.transactionEvent = this.transactionTracer.beginTransaction();

        assert this.transactionEvent != null : "transactionEvent was null!";

        this.securityContext = frozenSecurityContext;
        this.transactionId = -1L;
        this.commitTime = -1L;
        PageCursorTracer pageCursorTracer = (PageCursorTracer)this.cursorTracerSupplier.get();
        this.statistics.init(Thread.currentThread().getId(), pageCursorTracer);
        this.currentStatement.initialize(statementLocks, pageCursorTracer);
        this.operations.initialize();
        return this;
    }
    //DynamicGraph
    //*****************************************************************************
    //Dynamicgraph Method
    //*********************************************************
    public Operations getOperations(){
        return this.operations;
    }
    //Dynamicgraph Method
    //*********************************************************
    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    public long getLastTransactionIdWhenStarted(){
        return this.lastTransactionIdWhenStarted;
    }
    public long getLastTransactionTimestampWhenStarted(){
        return this.lastTransactionTimestampWhenStarted;
    }
    //DynamicGraph
    //*****************************************************************************

    int getReuseCount() {
        return this.reuseCount;
    }

    public long startTime() {
        return this.startTimeMillis;
    }

    public long timeout() {
        return this.timeoutMillis;
    }

    public long lastTransactionIdWhenStarted() {
        return this.lastTransactionIdWhenStarted;
    }

    public void success() {
        this.success = true;
    }

    boolean isSuccess() {
        return this.success;
    }

    public void failure() {
        this.failure = true;
    }

    public Optional<Status> getReasonIfTerminated() {
        return Optional.ofNullable(this.terminationReason);
    }

    boolean markForTermination(long expectedReuseCount, Status reason) {
        this.terminationReleaseLock.lock();

        boolean var4;
        try {
            var4 = expectedReuseCount == (long)this.reuseCount && this.markForTerminationIfPossible(reason);
        } finally {
            this.terminationReleaseLock.unlock();
        }

        return var4;
    }

    public void markForTermination(Status reason) {
        this.terminationReleaseLock.lock();

        try {
            this.markForTerminationIfPossible(reason);
        } finally {
            this.terminationReleaseLock.unlock();
        }

    }

    public boolean isSchemaTransaction() {
        return this.writeState == TransactionWriteState.SCHEMA;
    }

    private boolean markForTerminationIfPossible(Status reason) {
        if (this.canBeTerminated()) {
            this.failure = true;
            this.terminationReason = reason;
            if (this.statementLocks != null) {
                this.statementLocks.stop();
            }

            this.transactionMonitor.transactionTerminated(this.hasTxStateWithChanges());
            return true;
        } else {
            return false;
        }
    }

    public boolean isOpen() {
        return !this.closed && !this.closing;
    }

    public SecurityContext securityContext() {
        if (this.securityContext == null) {
            throw new NotInTransactionException();
        } else {
            return this.securityContext;
        }
    }

    public AuthSubject subjectOrAnonymous() {
        SecurityContext context = this.securityContext;
        return context == null ? AuthSubject.ANONYMOUS : context.subject();
    }

    public void setMetaData(Map<String, Object> data) {
        this.userMetaData = data;
    }

    public Map<String, Object> getMetaData() {
        return this.userMetaData;
    }



    public KernelStatement acquireStatement() {
        this.assertTransactionOpen();
        this.currentStatement.acquire();
        return this.currentStatement;
    }

    public IndexDescriptor indexUniqueCreate(SchemaDescriptor schema, String provider) throws SchemaKernelException {
        return this.operations.indexUniqueCreate(schema, provider);
    }

    public long pageHits() {
        return ((PageCursorTracer)this.cursorTracerSupplier.get()).hits();
    }

    public long pageFaults() {
        return ((PageCursorTracer)this.cursorTracerSupplier.get()).faults();
    }

    ExecutingQueryList executingQueries() {
        return this.currentStatement.executingQueryList();
    }

    void upgradeToDataWrites() throws InvalidTransactionTypeKernelException {
        this.writeState = this.writeState.upgradeToDataWrites();
    }

    void upgradeToSchemaWrites() throws InvalidTransactionTypeKernelException {
        this.schemaWriteGuard.assertSchemaWritesAllowed();
        this.writeState = this.writeState.upgradeToSchemaWrites();
    }

    private void dropCreatedConstraintIndexes() throws TransactionFailureException {
        if (this.hasTxStateWithChanges()) {
            Iterator var1 = this.txState().constraintIndexesCreatedInTx().iterator();

            while(var1.hasNext()) {
                IndexDescriptor createdConstraintIndex = (IndexDescriptor)var1.next();
                this.constraintIndexCreator.dropUniquenessConstraintIndex(createdConstraintIndex);
            }
        }

    }

    public TransactionState txState() {
        if (this.txState == null) {
            this.transactionMonitor.upgradeToWriteTransaction();
            this.txState = new TxState(this.collectionsFactory);
        }

        return this.txState;
    }

    private AuxiliaryTransactionStateHolder getAuxTxStateHolder() {
        if (this.auxTxStateHolder == null) {
            this.auxTxStateHolder = this.auxTxStateManager.openStateHolder();
        }

        return this.auxTxStateHolder;
    }

    public AuxiliaryTransactionState auxiliaryTxState(Object providerIdentityKey) {
        return this.getAuxTxStateHolder().getState(providerIdentityKey);
    }

    public ExplicitIndexTransactionState explicitIndexTxState() {
        return (ExplicitIndexTransactionState)this.getAuxTxStateHolder().getState("EXPLICIT INDEX TX STATE PROVIDER");
    }

    public boolean hasTxStateWithChanges() {
        return this.txState != null && this.txState.hasChanges();
    }

    private void markAsClosed(long txId) {
        this.assertTransactionOpen();
        this.closed = true;
        this.notifyListeners(txId);
        this.closeCurrentStatementIfAny();
    }

    private void notifyListeners(long txId) {
        Iterator var3 = this.closeListeners.iterator();

        while(var3.hasNext()) {
            CloseListener closeListener = (CloseListener)var3.next();
            closeListener.notify(txId);
        }

    }

    private void closeCurrentStatementIfAny() {
        this.currentStatement.forceClose();
    }

    private void assertTransactionNotClosing() {
        if (this.closing) {
            throw new IllegalStateException("This transaction is already being closed.");
        }
    }

    private void assertTransactionOpen() {
        if (this.closed) {
            throw new IllegalStateException("This transaction has already been completed.");
        }
    }

    public void assertOpen() {
        Status reason = this.terminationReason;
        if (reason != null) {
            throw new TransactionTerminatedException(reason);
        } else if (this.closed) {
            throw new NotInTransactionException("The transaction has been closed.");
        }
    }

    private boolean hasChanges() {
        return this.hasTxStateWithChanges() || this.hasAuxTxStateChanges();
    }

    private boolean hasAuxTxStateChanges() {
        return this.auxTxStateHolder != null && this.getAuxTxStateHolder().hasChanges();
    }

    private boolean hasDataChanges() {
        return this.hasTxStateWithChanges() && this.txState.hasDataChanges();
    }

    public long closeTransaction() throws TransactionFailureException {
        this.assertTransactionOpen();
        this.assertTransactionNotClosing();
        this.closing = true;

        long var1;
        try {
            if (this.failure || !this.success || this.isTerminated()) {
                this.rollback();
                this.failOnNonExplicitRollbackIfNeeded();
                var1 = -1L;
                return var1;
            }

            var1 = this.commit();
        } finally {
            try {
                this.closed = true;
                this.closing = false;
                this.transactionEvent.setSuccess(this.success);
                this.transactionEvent.setFailure(this.failure);
                this.transactionEvent.setTransactionWriteState(this.writeState.name());
                this.transactionEvent.setReadOnly(this.txState == null || !this.txState.hasChanges());
                this.transactionEvent.close();
            } finally {
                this.release();
            }
        }

        return var1;
    }

    public boolean isClosing() {
        return this.closing;
    }

    private void failOnNonExplicitRollbackIfNeeded() throws TransactionFailureException {
        if (this.success && this.isTerminated()) {
            throw new TransactionTerminatedException(this.terminationReason);
        } else if (this.success) {
            throw new TransactionFailureException(Transaction.TransactionMarkedAsFailed, "Transaction rolled back even if marked as successful", new Object[0]);
        }
    }

    private long commit() throws TransactionFailureException {
        boolean success = false;
        Object txId = 0L;

        Throwable cause;
        try {
            CommitEvent commitEvent = this.transactionEvent.beginCommitEvent();
            Throwable var5 = null;

            try {
                if (this.hasDataChanges()) {
                    try {
                        this.hooksState = this.hooks.beforeCommit(this.txState, this, this.storageReader);
                        if (this.hooksState != null && this.hooksState.failed()) {
                            cause = this.hooksState.failure();
                            throw new TransactionFailureException(Transaction.TransactionHookFailed, cause, "", new Object[0]);
                        }
                    } finally {
                        this.beforeHookInvoked = true;
                    }
                }

                if (this.hasChanges()) {
                    this.statementLocks.prepareForCommit(this.currentStatement.lockTracer());
                    Client commitLocks = this.statementLocks.pessimistic();
                    Collection<StorageCommand> extractedCommands = new ArrayList();
                    this.storageEngine.createCommands(extractedCommands, this.txState, this.storageReader, commitLocks, this.lastTransactionIdWhenStarted, this::enforceConstraints);
                    if (this.hasAuxTxStateChanges()) {
                        this.auxTxStateHolder.extractCommands(extractedCommands);
                    }

                    if (!extractedCommands.isEmpty()) {
                        PhysicalTransactionRepresentation transactionRepresentation = new PhysicalTransactionRepresentation(extractedCommands);
                        TransactionHeaderInformation headerInformation = this.headerInformationFactory.create();
                        long timeCommitted = this.clocks.systemClock().millis();
                        transactionRepresentation.setHeader(headerInformation.getAdditionalHeader(), headerInformation.getMasterId(), headerInformation.getAuthorId(), this.startTimeMillis, this.lastTransactionIdWhenStarted, timeCommitted, commitLocks.getLockSessionId());
                        success = true;
                        TransactionToApply batch = new TransactionToApply(transactionRepresentation, this.versionContextSupplier.getVersionContext());
                        txId = this.transactionId = this.commitProcess.commit(batch, commitEvent, TransactionApplicationMode.INTERNAL);
                        this.commitTime = timeCommitted;
                    }
                }

                success = true;
                ///cause = (Throwable)txId;
                //cause = new Throwable();
               // cause = (Throwable) (Object)txId;
            } catch (Throwable var38) {
                cause = var38;
                var5 = var38;
                throw var38;
            } finally {
                if (commitEvent != null) {
                    if (var5 != null) {
                        try {
                            commitEvent.close();
                        } catch (Throwable var36) {
                            var5.addSuppressed(var36);
                        }
                    } else {
                        commitEvent.close();
                    }
                }

            }
        } catch (CreateConstraintFailureException | ConstraintValidationException var40) {
            throw new ConstraintViolationTransactionFailureException(var40.getUserMessage(new SilentTokenNameLookup(this.tokenRead())), var40);
        } finally {
            if (!success) {
                this.rollback();
            } else {
                this.afterCommit((long)txId);
            }

        }

        return (long )txId;//(long)(Object)cause;
    }

    private void rollback() throws TransactionFailureException {
        try {
            try {
                this.dropCreatedConstraintIndexes();
            } catch (SecurityException | IllegalStateException var7) {
                throw new TransactionFailureException(Transaction.TransactionRollbackFailed, var7, "Could not drop created constraint indexes", new Object[0]);
            }

            if (this.txState != null) {
                try {
                    this.txState.accept(new Adapter() {
                        public void visitCreatedNode(long id) {
                            KernelTransactionImplementation.this.storageReader.releaseNode(id);
                        }

                        public void visitCreatedRelationship(long id, int type, long startNode, long endNode) {
                            KernelTransactionImplementation.this.storageReader.releaseRelationship(id);
                        }
                    });
                } catch (CreateConstraintFailureException | ConstraintValidationException var6) {
                    throw new IllegalStateException("Releasing locks during rollback should perform no constraints checking.", var6);
                }
            }
        } finally {
            this.afterRollback();
        }

    }




    public Read dataRead() {
        this.assertAllows(AccessMode::allowsReads, "Read");
        return this.operations.dataRead();
    }

    public Write dataWrite() throws InvalidTransactionTypeKernelException {
        this.accessCapability.assertCanWrite();
        this.assertAllows(AccessMode::allowsWrites, "Write");
        this.upgradeToDataWrites();
        return this.operations;
    }

    public TokenWrite tokenWrite() {
        this.accessCapability.assertCanWrite();
        return this.operations.token();
    }

    public Token token() {
        this.accessCapability.assertCanWrite();
        return this.operations.token();
    }

    public TokenRead tokenRead() {
        this.assertAllows(AccessMode::allowsReads, "Read");
        return this.operations.token();
    }

    public ExplicitIndexRead indexRead() {
        this.assertAllows(AccessMode::allowsReads, "Read");
        return this.operations.indexRead();
    }

    public ExplicitIndexWrite indexWrite() throws InvalidTransactionTypeKernelException {
        this.accessCapability.assertCanWrite();
        this.assertAllows(AccessMode::allowsWrites, "Write");
        this.upgradeToDataWrites();
        return this.operations;
    }

    public SchemaRead schemaRead() {
        this.assertAllows(AccessMode::allowsReads, "Read");
        return this.operations.schemaRead();
    }

    public SchemaWrite schemaWrite() throws InvalidTransactionTypeKernelException {
        this.accessCapability.assertCanWrite();
        this.assertAllows(AccessMode::allowsSchemaWrites, "Schema");
        this.upgradeToSchemaWrites();
        return this.operations;
    }

    public Locks locks() {
        return this.operations.locks();
    }

    public StatementLocks statementLocks() {
        this.assertOpen();
        return this.statementLocks;
    }

    public CursorFactory cursors() {
        return this.operations.cursors();
    }

    public org.neo4j.internal.kernel.api.Procedures procedures() {
        return this.operations.procedures();
    }

    public ExecutionStatistics executionStatistics() {
        return this;
    }

    public LockTracer lockTracer() {
        return this.currentStatement.lockTracer();
    }

    public void assertAllows(Function<AccessMode, Boolean> allows, String mode) {
        AccessMode accessMode = this.securityContext().mode();
        if (!(Boolean)allows.apply(accessMode)) {
            throw accessMode.onViolation(String.format("%s operations are not allowed for %s.", mode, this.securityContext().description()));
        }
    }

    private void afterCommit(long txId) {
        try {
            this.markAsClosed(txId);
            if (this.beforeHookInvoked) {
                this.hooks.afterCommit(this.txState, this, this.hooksState);
            }
        } finally {
            this.transactionMonitor.transactionFinished(true, this.hasTxStateWithChanges());
        }

    }

    private void afterRollback() {
        try {
            this.markAsClosed(-1L);
            if (this.beforeHookInvoked) {
                this.hooks.afterRollback(this.txState, this, this.hooksState);
            }
        } finally {
            this.transactionMonitor.transactionFinished(false, this.hasTxStateWithChanges());
        }

    }

    private void release() {
        AuxiliaryTransactionStateCloseException auxStateCloseException = null;
        this.terminationReleaseLock.lock();

        try {
            this.statementLocks.close();
            this.statementLocks = null;
            this.terminationReason = null;
            this.type = null;
            this.securityContext = null;
            this.transactionEvent = null;
            if (this.auxTxStateHolder != null) {
                auxStateCloseException = this.closeAuxTxState();
            }

            this.txState = null;
            this.collectionsFactory.release();
            this.hooksState = null;
            this.closeListeners.clear();
            ++this.reuseCount;
            this.userMetaData = Collections.emptyMap();
            this.userTransactionId = 0L;
            this.statistics.reset();
            this.operations.release();
            this.pool.release(this);
        } finally {
            this.terminationReleaseLock.unlock();
        }

        if (auxStateCloseException != null) {
            throw auxStateCloseException;
        }
    }

    private AuxiliaryTransactionStateCloseException closeAuxTxState() {
        AuxiliaryTransactionStateHolder holder = this.auxTxStateHolder;
        this.auxTxStateHolder = null;

        try {
            holder.close();
            return null;
        } catch (AuxiliaryTransactionStateCloseException var3) {
            return var3;
        }
    }

    private boolean canBeTerminated() {
        return !this.closed && !this.isTerminated();
    }

    public boolean isTerminated() {
        return this.terminationReason != null;
    }

    public long lastTransactionTimestampWhenStarted() {
        return this.lastTransactionTimestampWhenStarted;
    }

    public void registerCloseListener(CloseListener listener) {
        assert listener != null;

        this.closeListeners.add(listener);
    }

    public Type transactionType() {
        return this.type;
    }

    public long getTransactionId() {
        if (this.transactionId == -1L) {
            throw new IllegalStateException("Transaction id is not assigned yet. It will be assigned during transaction commit.");
        } else {
            return this.transactionId;
        }
    }

    public long getCommitTime() {
        if (this.commitTime == -1L) {
            throw new IllegalStateException("Transaction commit time is not assigned yet. It will be assigned during transaction commit.");
        } else {
            return this.commitTime;
        }
    }

    public Revertable overrideWith(SecurityContext context) {
        SecurityContext oldContext = this.securityContext;
        this.securityContext = context;
        return () -> {
            this.securityContext = oldContext;
        };
    }

    public String toString() {
        String lockSessionId = this.statementLocks == null ? "statementLocks == null" : String.valueOf(this.statementLocks.pessimistic().getLockSessionId());
        return "KernelTransaction[" + lockSessionId + "]";
    }

    public void dispose() {
        this.storageReader.close();
    }

    public Stream<? extends ActiveLock> activeLocks() {
        StatementLocks locks = this.statementLocks;
        return locks == null ? Stream.empty() : locks.activeLocks();
    }

    long userTransactionId() {
        return this.userTransactionId;
    }

    public Statistics getStatistics() {
        return this.statistics;
    }

    private TxStateVisitor enforceConstraints(TxStateVisitor txStateVisitor) {
        return this.constraintSemantics.decorateTxStateVisitor(this.storageReader, this.operations.dataRead(), this.operations.cursors(), this.txState, txStateVisitor);
    }

    public long getTransactionDataRevision() {
        return this.hasDataChanges() ? this.txState.getDataRevision() : 0L;
    }

    public ClockContext clocks() {
        return this.clocks;
    }

    public NodeCursor ambientNodeCursor() {
        return this.operations.nodeCursor();
    }

    public RelationshipScanCursor ambientRelationshipCursor() {
        return this.operations.relationshipCursor();
    }

    public PropertyCursor ambientPropertyCursor() {
        return this.operations.propertyCursor();
    }

    private static enum TransactionWriteState {
        NONE,
        DATA {
            TransactionWriteState upgradeToSchemaWrites() throws InvalidTransactionTypeKernelException {
                throw new InvalidTransactionTypeKernelException("Cannot perform schema updates in a transaction that has performed data updates.");
            }
        },
        SCHEMA {
            TransactionWriteState upgradeToDataWrites() throws InvalidTransactionTypeKernelException {
                throw new InvalidTransactionTypeKernelException("Cannot perform data updates in a transaction that has performed schema updates.");
            }
        };

        private TransactionWriteState() {
        }

        TransactionWriteState upgradeToDataWrites() throws InvalidTransactionTypeKernelException {
            return DATA;
        }

        TransactionWriteState upgradeToSchemaWrites() throws InvalidTransactionTypeKernelException {
            return SCHEMA;
        }
    }

    public static class Statistics {
        private volatile long cpuTimeNanosWhenQueryStarted;
        private volatile long heapAllocatedBytesWhenQueryStarted;
        private volatile long waitingTimeNanos;
        private volatile long transactionThreadId;
        private volatile PageCursorTracer pageCursorTracer;
        private final KernelTransactionImplementation transaction;
        private final AtomicReference<CpuClock> cpuClockRef;
        private final AtomicReference<HeapAllocation> heapAllocationRef;
        private CpuClock cpuClock;
        private HeapAllocation heapAllocation;

        public Statistics(KernelTransactionImplementation transaction, AtomicReference<CpuClock> cpuClockRef, AtomicReference<HeapAllocation> heapAllocationRef) {
            this.pageCursorTracer = PageCursorTracer.NULL;
            this.transaction = transaction;
            this.cpuClockRef = cpuClockRef;
            this.heapAllocationRef = heapAllocationRef;
        }

        protected void init(long threadId, PageCursorTracer pageCursorTracer) {
            this.cpuClock = (CpuClock)this.cpuClockRef.get();
            this.heapAllocation = (HeapAllocation)this.heapAllocationRef.get();
            this.transactionThreadId = threadId;
            this.pageCursorTracer = pageCursorTracer;
            this.cpuTimeNanosWhenQueryStarted = this.cpuClock.cpuTimeNanos(this.transactionThreadId);
            this.heapAllocatedBytesWhenQueryStarted = this.heapAllocation.allocatedBytes(this.transactionThreadId);
        }

        long heapAllocatedBytes() {
            return this.heapAllocation.allocatedBytes(this.transactionThreadId) - this.heapAllocatedBytesWhenQueryStarted;
        }

        long directAllocatedBytes() {
            return this.transaction.collectionsFactory.getMemoryTracker().usedDirectMemory();
        }

        public long cpuTimeMillis() {
            long cpuTimeNanos = this.cpuClock.cpuTimeNanos(this.transactionThreadId) - this.cpuTimeNanosWhenQueryStarted;
            return TimeUnit.NANOSECONDS.toMillis(cpuTimeNanos);
        }

        long totalTransactionPageCacheHits() {
            return this.pageCursorTracer.accumulatedHits();
        }

        long totalTransactionPageCacheFaults() {
            return this.pageCursorTracer.accumulatedFaults();
        }

        void addWaitingTime(long waitTimeNanos) {
            this.waitingTimeNanos += waitTimeNanos;
        }

        long getWaitingTimeNanos(long nowNanos) {
            ExecutingQueryList queryList = this.transaction.executingQueries();
            long waitingTime = this.waitingTimeNanos;
            if (queryList != null) {
                Long latestQueryWaitingNanos = (Long)queryList.top((executingQuery) -> {
                    return executingQuery.totalWaitingTimeNanos(nowNanos);
                });
                waitingTime = latestQueryWaitingNanos != null ? waitingTime + latestQueryWaitingNanos : waitingTime;
            }

            return waitingTime;
        }

        void reset() {
            this.pageCursorTracer = PageCursorTracer.NULL;
            this.cpuTimeNanosWhenQueryStarted = 0L;
            this.heapAllocatedBytesWhenQueryStarted = 0L;
            this.waitingTimeNanos = 0L;
            this.transactionThreadId = -1L;
        }
    }
}
