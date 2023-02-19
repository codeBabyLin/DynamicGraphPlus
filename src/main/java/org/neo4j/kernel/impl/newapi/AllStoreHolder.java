//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.newapi;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import org.neo4j.collection.RawIterator;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.internal.kernel.api.IndexReference;
import org.neo4j.internal.kernel.api.InternalIndexState;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.ProcedureException;
import org.neo4j.internal.kernel.api.exceptions.explicitindex.ExplicitIndexNotFoundKernelException;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo4j.internal.kernel.api.exceptions.schema.IndexNotFoundKernelException;
import org.neo4j.internal.kernel.api.procs.ProcedureHandle;
import org.neo4j.internal.kernel.api.procs.ProcedureSignature;
import org.neo4j.internal.kernel.api.procs.QualifiedName;
import org.neo4j.internal.kernel.api.procs.UserAggregator;
import org.neo4j.internal.kernel.api.procs.UserFunctionHandle;
import org.neo4j.internal.kernel.api.schema.IndexProviderDescriptor;
import org.neo4j.internal.kernel.api.schema.SchemaDescriptor;
import org.neo4j.internal.kernel.api.schema.SchemaUtil;
import org.neo4j.internal.kernel.api.schema.constraints.ConstraintDescriptor;
import org.neo4j.internal.kernel.api.security.AccessMode;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.internal.kernel.api.security.AccessMode.Static;
import org.neo4j.kernel.api.ExplicitIndex;
import org.neo4j.kernel.api.Statement;
import org.neo4j.kernel.api.KernelTransaction.Revertable;
import org.neo4j.kernel.api.exceptions.schema.SchemaRuleNotFoundException;
import org.neo4j.kernel.api.proc.BasicContext;
import org.neo4j.kernel.api.proc.Context;
import org.neo4j.kernel.api.schema.LabelSchemaDescriptor;
import org.neo4j.kernel.api.schema.SchemaDescriptorFactory;
import org.neo4j.kernel.api.txstate.TransactionCountingStateVisitor;
import org.neo4j.kernel.api.txstate.TransactionState;
import org.neo4j.kernel.impl.api.ClockContext;
import org.neo4j.kernel.impl.api.CountsRecordState;
import org.neo4j.kernel.impl.api.KernelTransactionImplementation;
import org.neo4j.kernel.impl.api.SchemaState;
import org.neo4j.kernel.impl.api.security.OverriddenAccessMode;
import org.neo4j.kernel.impl.api.security.RestrictedAccessMode;
import org.neo4j.kernel.impl.index.ExplicitIndexStore;
import org.neo4j.kernel.impl.index.IndexEntityType;
import org.neo4j.kernel.impl.locking.ResourceTypes;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.impl.util.Dependencies;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.register.Registers;
import org.neo4j.register.Register.DoubleLongRegister;
import org.neo4j.storageengine.api.StorageReader;
import org.neo4j.storageengine.api.schema.CapableIndexDescriptor;
import org.neo4j.storageengine.api.schema.IndexDescriptor;
import org.neo4j.storageengine.api.schema.IndexDescriptorFactory;
import org.neo4j.storageengine.api.schema.IndexReader;
import org.neo4j.storageengine.api.schema.LabelScanReader;
import org.neo4j.storageengine.api.schema.PopulationProgress;
import org.neo4j.storageengine.api.schema.StoreIndexDescriptor;
import org.neo4j.storageengine.api.schema.SchemaRule.Kind;
import org.neo4j.storageengine.api.txstate.DiffSets;
import org.neo4j.storageengine.api.txstate.TxStateVisitor;
import org.neo4j.values.AnyValue;
import org.neo4j.values.ValueMapper;
import org.neo4j.values.storable.Value;

public class AllStoreHolder extends Read {
    private final StorageReader storageReader;
    private final ExplicitIndexStore explicitIndexStore;
    private final Procedures procedures;
    private final SchemaState schemaState;
    private final Dependencies dataSourceDependencies;

    public AllStoreHolder(StorageReader storageReader, KernelTransactionImplementation ktx, DefaultCursors cursors, ExplicitIndexStore explicitIndexStore, Procedures procedures, SchemaState schemaState, Dependencies dataSourceDependencies) {
        super(cursors, ktx);
        this.storageReader = storageReader;
        this.explicitIndexStore = explicitIndexStore;
        this.procedures = procedures;
        this.schemaState = schemaState;
        this.dataSourceDependencies = dataSourceDependencies;
    }

    public boolean nodeExists(long reference) {
        this.ktx.assertOpen();
        if (this.hasTxStateWithChanges()) {
            TransactionState txState = this.txState();
            if (txState.nodeIsDeletedInThisTx(reference)) {
                return false;
            }

            if (txState.nodeIsAddedInThisTx(reference)) {
                return true;
            }
        }

        return this.storageReader.nodeExists(reference);
    }

    public boolean nodeDeletedInTransaction(long node) {
        this.ktx.assertOpen();
        return this.hasTxStateWithChanges() && this.txState().nodeIsDeletedInThisTx(node);
    }

    public boolean relationshipDeletedInTransaction(long relationship) {
        this.ktx.assertOpen();
        return this.hasTxStateWithChanges() && this.txState().relationshipIsDeletedInThisTx(relationship);
    }

    public Value nodePropertyChangeInTransactionOrNull(long node, int propertyKeyId) {
        this.ktx.assertOpen();
        return this.hasTxStateWithChanges() ? this.txState().getNodeState(node).propertyValue(propertyKeyId) : null;
    }

    public long countsForNode(int labelId) {
        long count = this.countsForNodeWithoutTxState(labelId);
        if (this.ktx.hasTxStateWithChanges()) {
            CountsRecordState counts = new CountsRecordState();

            try {
                TransactionState txState = this.ktx.txState();
                txState.accept(new TransactionCountingStateVisitor(TxStateVisitor.EMPTY, this.storageReader, txState, counts));
                if (counts.hasChanges()) {
                    count += counts.nodeCount(labelId, Registers.newDoubleLongRegister()).readSecond();
                }
            } catch (CreateConstraintFailureException | ConstraintValidationException var6) {
                throw new IllegalArgumentException("Unexpected error: " + var6.getMessage());
            }
        }

        return count;
    }

    public long countsForNodeWithoutTxState(int labelId) {
        return this.storageReader.countsForNode(labelId);
    }

    public long countsForRelationship(int startLabelId, int typeId, int endLabelId) {
        long count = this.countsForRelationshipWithoutTxState(startLabelId, typeId, endLabelId);
        if (this.ktx.hasTxStateWithChanges()) {
            CountsRecordState counts = new CountsRecordState();

            try {
                TransactionState txState = this.ktx.txState();
                txState.accept(new TransactionCountingStateVisitor(TxStateVisitor.EMPTY, this.storageReader, txState, counts));
                if (counts.hasChanges()) {
                    count += counts.relationshipCount(startLabelId, typeId, endLabelId, Registers.newDoubleLongRegister()).readSecond();
                }
            } catch (CreateConstraintFailureException | ConstraintValidationException var8) {
                throw new IllegalArgumentException("Unexpected error: " + var8.getMessage());
            }
        }

        return count;
    }

    public long countsForRelationshipWithoutTxState(int startLabelId, int typeId, int endLabelId) {
        return this.storageReader.countsForRelationship(startLabelId, typeId, endLabelId);
    }

    public boolean relationshipExists(long reference) {
        this.ktx.assertOpen();
        if (this.hasTxStateWithChanges()) {
            TransactionState txState = this.txState();
            if (txState.relationshipIsDeletedInThisTx(reference)) {
                return false;
            }

            if (txState.relationshipIsAddedInThisTx(reference)) {
                return true;
            }
        }

        return this.storageReader.relationshipExists(reference);
    }

    long graphPropertiesReference() {
        return this.storageReader.getGraphPropertyReference();
    }

    public IndexReader indexReader(IndexReference index, boolean fresh) throws IndexNotFoundKernelException {
        assertValidIndex(index);
        return fresh ? this.storageReader.getFreshIndexReader((IndexDescriptor)index) : this.storageReader.getIndexReader((IndexDescriptor)index);
    }

    LabelScanReader labelScanReader() {
        return this.storageReader.getLabelScanReader();
    }

    ExplicitIndex explicitNodeIndex(String indexName) throws ExplicitIndexNotFoundKernelException {
        this.ktx.assertOpen();
        return this.explicitIndexTxState().nodeChanges(indexName);
    }

    ExplicitIndex explicitRelationshipIndex(String indexName) throws ExplicitIndexNotFoundKernelException {
        this.ktx.assertOpen();
        return this.explicitIndexTxState().relationshipChanges(indexName);
    }

    public String[] nodeExplicitIndexesGetAll() {
        this.ktx.assertOpen();
        return this.explicitIndexStore.getAllNodeIndexNames();
    }

    public boolean nodeExplicitIndexExists(String indexName, Map<String, String> customConfiguration) {
        this.ktx.assertOpen();
        return this.explicitIndexTxState().checkIndexExistence(IndexEntityType.Node, indexName, customConfiguration);
    }

    public Map<String, String> nodeExplicitIndexGetConfiguration(String indexName) throws ExplicitIndexNotFoundKernelException {
        this.ktx.assertOpen();
        return this.explicitIndexStore.getNodeIndexConfiguration(indexName);
    }

    public String[] relationshipExplicitIndexesGetAll() {
        this.ktx.assertOpen();
        return this.explicitIndexStore.getAllRelationshipIndexNames();
    }

    public boolean relationshipExplicitIndexExists(String indexName, Map<String, String> customConfiguration) {
        this.ktx.assertOpen();
        return this.explicitIndexTxState().checkIndexExistence(IndexEntityType.Relationship, indexName, customConfiguration);
    }

    public Map<String, String> relationshipExplicitIndexGetConfiguration(String indexName) throws ExplicitIndexNotFoundKernelException {
        this.ktx.assertOpen();
        return this.explicitIndexStore.getRelationshipIndexConfiguration(indexName);
    }

    public IndexReference index(int label, int... properties) {
        this.ktx.assertOpen();

        LabelSchemaDescriptor descriptor;
        try {
            descriptor = SchemaDescriptorFactory.forLabel(label, properties);
        } catch (IllegalArgumentException var7) {
            return IndexReference.NO_INDEX;
        }

        CapableIndexDescriptor indexDescriptor = this.storageReader.indexGetForSchema(descriptor);
        if (this.ktx.hasTxStateWithChanges()) {
            DiffSets<IndexDescriptor> diffSets = this.ktx.txState().indexDiffSetsByLabel(label);
            if (indexDescriptor != null) {
                return (IndexReference)(diffSets.isRemoved(indexDescriptor) ? IndexReference.NO_INDEX : indexDescriptor);
            } else {
                Iterator<IndexDescriptor> fromTxState = Iterators.filter(SchemaDescriptor.equalTo(descriptor), diffSets.getAdded().iterator());
                return fromTxState.hasNext() ? (IndexReference)fromTxState.next() : IndexReference.NO_INDEX;
            }
        } else {
            return (IndexReference)(indexDescriptor != null ? indexDescriptor : IndexReference.NO_INDEX);
        }
    }

    public IndexReference index(SchemaDescriptor schema) {
        this.ktx.assertOpen();
        CapableIndexDescriptor indexDescriptor = this.storageReader.indexGetForSchema(schema);
        if (this.ktx.hasTxStateWithChanges()) {
            DiffSets<IndexDescriptor> diffSets = this.ktx.txState().indexDiffSetsBySchema(schema);
            if (indexDescriptor != null) {
                return (IndexReference)(diffSets.isRemoved(indexDescriptor) ? IndexReference.NO_INDEX : indexDescriptor);
            } else {
                Iterator<IndexDescriptor> fromTxState = Iterators.filter(SchemaDescriptor.equalTo(schema), diffSets.getAdded().iterator());
                return fromTxState.hasNext() ? (IndexReference)fromTxState.next() : IndexReference.NO_INDEX;
            }
        } else {
            return (IndexReference)(indexDescriptor != null ? indexDescriptor : IndexReference.NO_INDEX);
        }
    }

    public IndexReference indexReferenceUnchecked(int label, int... properties) {
        return IndexDescriptorFactory.forSchema(SchemaDescriptorFactory.forLabel(label, properties), Optional.empty(), IndexProviderDescriptor.UNDECIDED);
    }

    public IndexReference indexReferenceUnchecked(SchemaDescriptor schema) {
        return IndexDescriptorFactory.forSchema(schema, Optional.empty(), IndexProviderDescriptor.UNDECIDED);
    }

    public Iterator<IndexReference> indexesGetForLabel(int labelId) {
        this.acquireSharedLock(ResourceTypes.LABEL, (long)labelId);
        this.ktx.assertOpen();
        Iterator<? extends IndexDescriptor> iterator = this.storageReader.indexesGetForLabel(labelId);
        if (this.ktx.hasTxStateWithChanges()) {
            iterator = this.ktx.txState().indexDiffSetsByLabel(labelId).apply(iterator);
        }
        ArrayList<IndexReference> sites = new ArrayList<IndexReference>();
        while(iterator.hasNext()){
            sites.add(iterator.next());
        }
        return sites.iterator();
    }

    public IndexReference indexGetForName(String name) {
        this.ktx.assertOpen();
        IndexDescriptor index = this.storageReader.indexGetForName(name);
        if (this.ktx.hasTxStateWithChanges()) {
            Predicate<IndexDescriptor> namePredicate = (indexDescriptor) -> {
                try {
                    return ((String)indexDescriptor.getUserSuppliedName().get()).equals(name);
                } catch (NoSuchElementException var3) {
                    return false;
                }
            };
            Iterator<IndexDescriptor> indexes = this.ktx.txState().indexChanges().filterAdded(namePredicate).apply(Iterators.iterator(index));
            index = (IndexDescriptor)Iterators.singleOrNull(indexes);
        }

        if (index == null) {
            return IndexReference.NO_INDEX;
        } else {
            this.acquireSharedSchemaLock(((IndexDescriptor)index).schema());
            return (IndexReference)index;
        }
    }

    public Iterator<IndexReference> indexesGetAll() {
        this.ktx.assertOpen();
        Iterator<? extends IndexDescriptor> iterator = this.storageReader.indexesGetAll();
        if (this.ktx.hasTxStateWithChanges()) {
            iterator = this.ktx.txState().indexChanges().apply(this.storageReader.indexesGetAll());
        }

        return Iterators.map((indexDescriptor) -> {
            this.acquireSharedSchemaLock(indexDescriptor.schema());
            return indexDescriptor;
        }, iterator);
    }

    public InternalIndexState indexGetState(IndexReference index) throws IndexNotFoundKernelException {
        assertValidIndex(index);
        this.acquireSharedSchemaLock(index.schema());
        this.ktx.assertOpen();
        return this.indexGetState((IndexDescriptor)index);
    }

    public PopulationProgress indexGetPopulationProgress(IndexReference index) throws IndexNotFoundKernelException {
        assertValidIndex(index);
        this.acquireSharedSchemaLock(index.schema());
        this.ktx.assertOpen();
        return this.ktx.hasTxStateWithChanges() && this.checkIndexState((IndexDescriptor)index, this.ktx.txState().indexDiffSetsBySchema(index.schema())) ? PopulationProgress.NONE : this.storageReader.indexGetPopulationProgress(index.schema());
    }

    public Long indexGetOwningUniquenessConstraintId(IndexReference index) {
        this.acquireSharedSchemaLock(index.schema());
        this.ktx.assertOpen();
        return index instanceof StoreIndexDescriptor ? ((StoreIndexDescriptor)index).getOwningConstraint() : null;
    }

    public long indexGetCommittedId(IndexReference index) throws SchemaRuleNotFoundException {
        this.acquireSharedSchemaLock(index.schema());
        this.ktx.assertOpen();
        if (index instanceof StoreIndexDescriptor) {
            return ((StoreIndexDescriptor)index).getId();
        } else {
            throw new SchemaRuleNotFoundException(Kind.INDEX_RULE, index.schema());
        }
    }

    public String indexGetFailure(IndexReference index) throws IndexNotFoundKernelException {
        assertValidIndex(index);
        return this.storageReader.indexGetFailure(index.schema());
    }

    public double indexUniqueValuesSelectivity(IndexReference index) throws IndexNotFoundKernelException {
        assertValidIndex(index);
        SchemaDescriptor schema = index.schema();
        this.acquireSharedSchemaLock(schema);
        this.ktx.assertOpen();
        return this.storageReader.indexUniqueValuesPercentage(schema);
    }

    public long indexSize(IndexReference index) throws IndexNotFoundKernelException {
        assertValidIndex(index);
        SchemaDescriptor schema = index.schema();
        this.acquireSharedSchemaLock(schema);
        this.ktx.assertOpen();
        return this.storageReader.indexSize(schema);
    }

    public long nodesCountIndexed(IndexReference index, long nodeId, int propertyKeyId, Value value) throws KernelException {
        this.ktx.assertOpen();
        assertValidIndex(index);
        IndexReader reader = this.storageReader.getIndexReader((IndexDescriptor)index);
        return reader.countIndexedNodes(nodeId, new int[]{propertyKeyId}, new Value[]{value});
    }

    public long nodesGetCount() {
        this.ktx.assertOpen();
        long base = this.storageReader.nodesGetCount();
        return this.ktx.hasTxStateWithChanges() ? base + (long)this.ktx.txState().addedAndRemovedNodes().delta() : base;
    }

    public long relationshipsGetCount() {
        this.ktx.assertOpen();
        long base = this.storageReader.relationshipsGetCount();
        return this.ktx.hasTxStateWithChanges() ? base + (long)this.ktx.txState().addedAndRemovedRelationships().delta() : base;
    }

    public DoubleLongRegister indexUpdatesAndSize(IndexReference index, DoubleLongRegister target) throws IndexNotFoundKernelException {
        this.ktx.assertOpen();
        assertValidIndex(index);
        return this.storageReader.indexUpdatesAndSize(index.schema(), target);
    }

    public DoubleLongRegister indexSample(IndexReference index, DoubleLongRegister target) throws IndexNotFoundKernelException {
        this.ktx.assertOpen();
        assertValidIndex(index);
        return this.storageReader.indexSample(index.schema(), target);
    }

    IndexReference indexGetCapability(IndexDescriptor schemaIndexDescriptor) {
        try {
            return this.storageReader.indexReference(schemaIndexDescriptor);
        } catch (IndexNotFoundKernelException var3) {
            throw new IllegalStateException("Could not find capability for index " + schemaIndexDescriptor, var3);
        }
    }

    InternalIndexState indexGetState(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return this.ktx.hasTxStateWithChanges() && this.checkIndexState(descriptor, this.ktx.txState().indexDiffSetsBySchema(descriptor.schema())) ? InternalIndexState.POPULATING : this.storageReader.indexGetState(descriptor);
    }

    Long indexGetOwningUniquenessConstraintId(IndexDescriptor index) {
        return this.storageReader.indexGetOwningUniquenessConstraintId(index);
    }

    IndexDescriptor indexGetForSchema(SchemaDescriptor descriptor) {
        IndexDescriptor indexDescriptor = this.storageReader.indexGetForSchema(descriptor);
        Iterator<IndexDescriptor> indexes = Iterators.iterator(indexDescriptor);
        if (this.ktx.hasTxStateWithChanges()) {
            indexes = Iterators.filter(SchemaDescriptor.equalTo(descriptor), this.ktx.txState().indexDiffSetsBySchema(descriptor).apply(indexes));
        }

        return (IndexDescriptor)Iterators.singleOrNull(indexes);
    }

    private boolean checkIndexState(IndexDescriptor index, DiffSets<IndexDescriptor> diffSet) throws IndexNotFoundKernelException {
        if (diffSet.isAdded(index)) {
            return true;
        } else if (diffSet.isRemoved(index)) {
            throw new IndexNotFoundKernelException(String.format("Index on %s has been dropped in this transaction.", index.userDescription(SchemaUtil.idTokenNameLookup)));
        } else {
            return false;
        }
    }

    public Iterator<ConstraintDescriptor> constraintsGetForSchema(SchemaDescriptor descriptor) {
        this.acquireSharedSchemaLock(descriptor);
        this.ktx.assertOpen();
        Iterator<ConstraintDescriptor> constraints = this.storageReader.constraintsGetForSchema(descriptor);
        return this.ktx.hasTxStateWithChanges() ? this.ktx.txState().constraintsChangesForSchema(descriptor).apply(constraints) : constraints;
    }

    public boolean constraintExists(ConstraintDescriptor descriptor) {
        SchemaDescriptor schema = descriptor.schema();
        this.acquireSharedSchemaLock(schema);
        this.ktx.assertOpen();
        boolean inStore = this.storageReader.constraintExists(descriptor);
        if (!this.ktx.hasTxStateWithChanges()) {
            return inStore;
        } else {
            DiffSets<ConstraintDescriptor> diffSet = this.ktx.txState().constraintsChangesForSchema(descriptor.schema());
            return diffSet.isAdded(descriptor) || inStore && !diffSet.isRemoved(descriptor);
        }
    }

    public Iterator<ConstraintDescriptor> constraintsGetForLabel(int labelId) {
        this.acquireSharedLock(ResourceTypes.LABEL, (long)labelId);
        this.ktx.assertOpen();
        Iterator<ConstraintDescriptor> constraints = this.storageReader.constraintsGetForLabel(labelId);
        return this.ktx.hasTxStateWithChanges() ? this.ktx.txState().constraintsChangesForLabel(labelId).apply(constraints) : constraints;
    }

    public Iterator<ConstraintDescriptor> constraintsGetAll() {
        this.ktx.assertOpen();
        Iterator<ConstraintDescriptor> constraints = this.storageReader.constraintsGetAll();
        if (this.ktx.hasTxStateWithChanges()) {
            constraints = this.ktx.txState().constraintsChanges().apply(constraints);
        }

        return Iterators.map(this::lockConstraint, constraints);
    }

    public Iterator<ConstraintDescriptor> constraintsGetForRelationshipType(int typeId) {
        this.acquireSharedLock(ResourceTypes.RELATIONSHIP_TYPE, (long)typeId);
        this.ktx.assertOpen();
        Iterator<ConstraintDescriptor> constraints = this.storageReader.constraintsGetForRelationshipType(typeId);
        return this.ktx.hasTxStateWithChanges() ? this.ktx.txState().constraintsChangesForRelationshipType(typeId).apply(constraints) : constraints;
    }

    boolean nodeExistsInStore(long id) {
        return this.storageReader.nodeExists(id);
    }

    void getOrCreateNodeIndexConfig(String indexName, Map<String, String> customConfig) {
        this.explicitIndexStore.getOrCreateNodeIndexConfig(indexName, customConfig);
    }

    void getOrCreateRelationshipIndexConfig(String indexName, Map<String, String> customConfig) {
        this.explicitIndexStore.getOrCreateRelationshipIndexConfig(indexName, customConfig);
    }

    String indexGetFailure(IndexDescriptor descriptor) throws IndexNotFoundKernelException {
        return this.storageReader.indexGetFailure(descriptor.schema());
    }

    public UserFunctionHandle functionGet(QualifiedName name) {
        this.ktx.assertOpen();
        return this.procedures.function(name);
    }

    public ProcedureHandle procedureGet(QualifiedName name) throws ProcedureException {
        this.ktx.assertOpen();
        return this.procedures.procedure(name);
    }

    public Set<ProcedureSignature> proceduresGetAll() {
        this.ktx.assertOpen();
        return this.procedures.getAllProcedures();
    }

    public UserFunctionHandle aggregationFunctionGet(QualifiedName name) {
        this.ktx.assertOpen();
        return this.procedures.aggregationFunction(name);
    }

    public RawIterator<Object[], ProcedureException> procedureCallRead(int id, Object[] arguments) throws ProcedureException {
        AccessMode accessMode = this.ktx.securityContext().mode();
        if (!accessMode.allowsReads()) {
            throw accessMode.onViolation(String.format("Read operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callProcedure(id, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.READ));
        }
    }

    public RawIterator<Object[], ProcedureException> procedureCallReadOverride(int id, Object[] arguments) throws ProcedureException {
        return this.callProcedure(id, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.READ));
    }

    public RawIterator<Object[], ProcedureException> procedureCallWrite(int id, Object[] arguments) throws ProcedureException {
        AccessMode accessMode = this.ktx.securityContext().mode();
        if (!accessMode.allowsWrites()) {
            throw accessMode.onViolation(String.format("Write operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callProcedure(id, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.TOKEN_WRITE));
        }
    }

    public RawIterator<Object[], ProcedureException> procedureCallWriteOverride(int id, Object[] arguments) throws ProcedureException {
        return this.callProcedure(id, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.TOKEN_WRITE));
    }

    public RawIterator<Object[], ProcedureException> procedureCallSchema(int id, Object[] arguments) throws ProcedureException {
        AccessMode accessMode = this.ktx.securityContext().mode();
        if (!accessMode.allowsSchemaWrites()) {
            throw accessMode.onViolation(String.format("Schema operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callProcedure(id, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.FULL));
        }
    }

    public RawIterator<Object[], ProcedureException> procedureCallSchemaOverride(int id, Object[] arguments) throws ProcedureException {
        return this.callProcedure(id, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.FULL));
    }

    public RawIterator<Object[], ProcedureException> procedureCallRead(QualifiedName name, Object[] arguments) throws ProcedureException {
        AccessMode accessMode = this.ktx.securityContext().mode();
        if (!accessMode.allowsReads()) {
            throw accessMode.onViolation(String.format("Read operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callProcedure(name, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.READ));
        }
    }

    public RawIterator<Object[], ProcedureException> procedureCallReadOverride(QualifiedName name, Object[] arguments) throws ProcedureException {
        return this.callProcedure(name, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.READ));
    }

    public RawIterator<Object[], ProcedureException> procedureCallWrite(QualifiedName name, Object[] arguments) throws ProcedureException {
        AccessMode accessMode = this.ktx.securityContext().mode();
        if (!accessMode.allowsWrites()) {
            throw accessMode.onViolation(String.format("Write operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callProcedure(name, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.TOKEN_WRITE));
        }
    }

    public RawIterator<Object[], ProcedureException> procedureCallWriteOverride(QualifiedName name, Object[] arguments) throws ProcedureException {
        return this.callProcedure(name, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.TOKEN_WRITE));
    }

    public RawIterator<Object[], ProcedureException> procedureCallSchema(QualifiedName name, Object[] arguments) throws ProcedureException {
        AccessMode accessMode = this.ktx.securityContext().mode();
        if (!accessMode.allowsSchemaWrites()) {
            throw accessMode.onViolation(String.format("Schema operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callProcedure(name, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.FULL));
        }
    }

    public RawIterator<Object[], ProcedureException> procedureCallSchemaOverride(QualifiedName name, Object[] arguments) throws ProcedureException {
        return this.callProcedure(name, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.FULL));
    }

    public AnyValue functionCall(int id, AnyValue[] arguments) throws ProcedureException {
        if (!this.ktx.securityContext().mode().allowsReads()) {
            throw this.ktx.securityContext().mode().onViolation(String.format("Read operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callFunction(id, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.READ));
        }
    }

    public AnyValue functionCall(QualifiedName name, AnyValue[] arguments) throws ProcedureException {
        if (!this.ktx.securityContext().mode().allowsReads()) {
            throw this.ktx.securityContext().mode().onViolation(String.format("Read operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.callFunction(name, arguments, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.READ));
        }
    }

    public AnyValue functionCallOverride(int id, AnyValue[] arguments) throws ProcedureException {
        return this.callFunction(id, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.READ));
    }

    public AnyValue functionCallOverride(QualifiedName name, AnyValue[] arguments) throws ProcedureException {
        return this.callFunction(name, arguments, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.READ));
    }

    public UserAggregator aggregationFunction(int id) throws ProcedureException {
        if (!this.ktx.securityContext().mode().allowsReads()) {
            throw this.ktx.securityContext().mode().onViolation(String.format("Read operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.aggregationFunction(id, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.READ));
        }
    }

    public UserAggregator aggregationFunction(QualifiedName name) throws ProcedureException {
        if (!this.ktx.securityContext().mode().allowsReads()) {
            throw this.ktx.securityContext().mode().onViolation(String.format("Read operations are not allowed for %s.", this.ktx.securityContext().description()));
        } else {
            return this.aggregationFunction(name, new RestrictedAccessMode(this.ktx.securityContext().mode(), Static.READ));
        }
    }

    public UserAggregator aggregationFunctionOverride(int id) throws ProcedureException {
        return this.aggregationFunction(id, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.READ));
    }

    public UserAggregator aggregationFunctionOverride(QualifiedName name) throws ProcedureException {
        return this.aggregationFunction(name, new OverriddenAccessMode(this.ktx.securityContext().mode(), Static.READ));
    }

    public ValueMapper<Object> valueMapper() {
        return this.procedures.valueMapper();
    }

    public <K, V> V schemaStateGetOrCreate(K key, Function<K, V> creator) {
        return this.schemaState.getOrCreate(key, creator);
    }

    public void schemaStateFlush() {
        this.schemaState.clear();
    }

    ExplicitIndexStore explicitIndexStore() {
        return this.explicitIndexStore;
    }

    private RawIterator<Object[], ProcedureException> callProcedure(int id, Object[] input, AccessMode override) throws ProcedureException {
        this.ktx.assertOpen();
        SecurityContext procedureSecurityContext = this.ktx.securityContext().withMode(override);
        Revertable ignore = this.ktx.overrideWith(procedureSecurityContext);
        Throwable var7 = null;

        RawIterator procedureCall;
        try {
            Statement statement = this.ktx.acquireStatement();
            Throwable var9 = null;

            try {
                procedureCall = this.procedures.callProcedure(this.prepareContext(procedureSecurityContext), id, input, statement);
            } catch (Throwable var32) {
                var9 = var32;
                throw var32;
            } finally {
                if (statement != null) {
                    if (var9 != null) {
                        try {
                            statement.close();
                        } catch (Throwable var31) {
                            var9.addSuppressed(var31);
                        }
                    } else {
                        statement.close();
                    }
                }

            }
        } catch (Throwable var34) {
            var7 = var34;
            throw var34;
        } finally {
            if (ignore != null) {
                if (var7 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var30) {
                        var7.addSuppressed(var30);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return this.createIterator(procedureSecurityContext, procedureCall);
    }

    private RawIterator<Object[], ProcedureException> callProcedure(QualifiedName name, Object[] input, AccessMode override) throws ProcedureException {
        this.ktx.assertOpen();
        SecurityContext procedureSecurityContext = this.ktx.securityContext().withMode(override);
        Revertable ignore = this.ktx.overrideWith(procedureSecurityContext);
        Throwable var7 = null;

        RawIterator procedureCall;
        try {
            Statement statement = this.ktx.acquireStatement();
            Throwable var9 = null;

            try {
                procedureCall = this.procedures.callProcedure(this.prepareContext(procedureSecurityContext), name, input, statement);
            } catch (Throwable var32) {
                var9 = var32;
                throw var32;
            } finally {
                if (statement != null) {
                    if (var9 != null) {
                        try {
                            statement.close();
                        } catch (Throwable var31) {
                            var9.addSuppressed(var31);
                        }
                    } else {
                        statement.close();
                    }
                }

            }
        } catch (Throwable var34) {
            var7 = var34;
            throw var34;
        } finally {
            if (ignore != null) {
                if (var7 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var30) {
                        var7.addSuppressed(var30);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return this.createIterator(procedureSecurityContext, procedureCall);
    }

    private RawIterator<Object[], ProcedureException> createIterator(final SecurityContext procedureSecurityContext, final RawIterator<Object[], ProcedureException> procedureCall) {
        return new RawIterator<Object[], ProcedureException>() {
            public boolean hasNext() throws ProcedureException {
                Revertable ignore = AllStoreHolder.this.ktx.overrideWith(procedureSecurityContext);
                Throwable var2 = null;

                boolean var3;
                try {
                    var3 = procedureCall.hasNext();
                } catch (Throwable var12) {
                    var2 = var12;
                    throw var12;
                } finally {
                    if (ignore != null) {
                        if (var2 != null) {
                            try {
                                ignore.close();
                            } catch (Throwable var11) {
                                var2.addSuppressed(var11);
                            }
                        } else {
                            ignore.close();
                        }
                    }

                }

                return var3;
            }

            public Object[] next() throws ProcedureException {
                Revertable ignore = AllStoreHolder.this.ktx.overrideWith(procedureSecurityContext);
                Throwable var2 = null;

                Object[] var3;
                try {
                    var3 = (Object[])procedureCall.next();
                } catch (Throwable var12) {
                    var2 = var12;
                    throw var12;
                } finally {
                    if (ignore != null) {
                        if (var2 != null) {
                            try {
                                ignore.close();
                            } catch (Throwable var11) {
                                var2.addSuppressed(var11);
                            }
                        } else {
                            ignore.close();
                        }
                    }

                }

                return var3;
            }
        };
    }

    private AnyValue callFunction(int id, AnyValue[] input, AccessMode mode) throws ProcedureException {
        this.ktx.assertOpen();
        SecurityContext securityContext = this.ktx.securityContext().withMode(mode);
        Revertable ignore = this.ktx.overrideWith(securityContext);
        Throwable var6 = null;

        AnyValue var7;
        try {
            var7 = this.procedures.callFunction(this.prepareContext(securityContext), id, input);
        } catch (Throwable var16) {
            var6 = var16;
            throw var16;
        } finally {
            if (ignore != null) {
                if (var6 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var15) {
                        var6.addSuppressed(var15);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return var7;
    }

    private AnyValue callFunction(QualifiedName name, AnyValue[] input, AccessMode mode) throws ProcedureException {
        this.ktx.assertOpen();
        SecurityContext securityContext = this.ktx.securityContext().withMode(mode);
        Revertable ignore = this.ktx.overrideWith(securityContext);
        Throwable var6 = null;

        AnyValue var7;
        try {
            var7 = this.procedures.callFunction(this.prepareContext(securityContext), name, input);
        } catch (Throwable var16) {
            var6 = var16;
            throw var16;
        } finally {
            if (ignore != null) {
                if (var6 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var15) {
                        var6.addSuppressed(var15);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return var7;
    }

    private UserAggregator aggregationFunction(int id, AccessMode mode) throws ProcedureException {
        this.ktx.assertOpen();
        SecurityContext securityContext = this.ktx.securityContext().withMode(mode);
        Revertable ignore = this.ktx.overrideWith(securityContext);
        Throwable var5 = null;

        UserAggregator var6;
        try {
            var6 = this.procedures.createAggregationFunction(this.prepareContext(securityContext), id);
        } catch (Throwable var15) {
            var5 = var15;
            throw var15;
        } finally {
            if (ignore != null) {
                if (var5 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var14) {
                        var5.addSuppressed(var14);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return var6;
    }

    private UserAggregator aggregationFunction(QualifiedName name, AccessMode mode) throws ProcedureException {
        this.ktx.assertOpen();
        SecurityContext securityContext = this.ktx.securityContext().withMode(mode);
        Revertable ignore = this.ktx.overrideWith(securityContext);
        Throwable var5 = null;

        UserAggregator var6;
        try {
            var6 = this.procedures.createAggregationFunction(this.prepareContext(securityContext), name);
        } catch (Throwable var15) {
            var5 = var15;
            throw var15;
        } finally {
            if (ignore != null) {
                if (var5 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var14) {
                        var5.addSuppressed(var14);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return var6;
    }

    private BasicContext prepareContext(SecurityContext securityContext) {
        BasicContext ctx = new BasicContext();
        ctx.put(Context.KERNEL_TRANSACTION, this.ktx);
        ctx.put(Context.DATABASE_API, this.dataSourceDependencies.resolveDependency(GraphDatabaseAPI.class));
        ctx.put(Context.DEPENDENCY_RESOLVER, this.dataSourceDependencies);
        ctx.put(Context.THREAD, Thread.currentThread());
        ClockContext clocks = this.ktx.clocks();
        ctx.put(Context.SYSTEM_CLOCK, clocks.systemClock());
        ctx.put(Context.STATEMENT_CLOCK, clocks.statementClock());
        ctx.put(Context.TRANSACTION_CLOCK, clocks.transactionClock());
        ctx.put(Context.SECURITY_CONTEXT, securityContext);
        return ctx;
    }

    private static void assertValidIndex(IndexReference index) throws IndexNotFoundKernelException {
        if (index == IndexReference.NO_INDEX) {
            throw new IndexNotFoundKernelException("No index was found");
        }
    }

    private ConstraintDescriptor lockConstraint(ConstraintDescriptor constraint) {
        SchemaDescriptor schema = constraint.schema();
        this.ktx.statementLocks().pessimistic().acquireShared(this.ktx.lockTracer(), schema.keyType(), new long[]{(long)schema.keyId()});
        return constraint;
    }
}
