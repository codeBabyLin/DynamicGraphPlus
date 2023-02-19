//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

//import DynamicGraph.store.Node.NodeVersionStore;
//import DynamicGraph.store.record.DynamicVersionRecord;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.kernel.impl.store.MetaDataStore;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.NodeLabelsField;
import org.neo4j.kernel.impl.store.NodeStore;
import org.neo4j.kernel.impl.store.PropertyStore;
import org.neo4j.kernel.impl.store.RecordStore;
import org.neo4j.kernel.impl.store.RelationshipStore;
import org.neo4j.kernel.impl.store.SchemaStore;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.LabelTokenRecord;
import org.neo4j.kernel.impl.store.record.NeoStoreRecord;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.PropertyKeyTokenRecord;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.store.record.RelationshipGroupRecord;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.kernel.impl.store.record.RelationshipTypeTokenRecord;
import org.neo4j.kernel.impl.store.record.SchemaRecord;
import org.neo4j.kernel.impl.transaction.command.Command;
import org.neo4j.kernel.impl.transaction.command.Command.LabelTokenCommand;
import org.neo4j.kernel.impl.transaction.command.Command.Mode;
import org.neo4j.kernel.impl.transaction.command.Command.NeoStoreCommand;
import org.neo4j.kernel.impl.transaction.command.Command.NodeCommand;
import org.neo4j.kernel.impl.transaction.command.Command.PropertyCommand;
import org.neo4j.kernel.impl.transaction.command.Command.PropertyKeyTokenCommand;
import org.neo4j.kernel.impl.transaction.command.Command.RelationshipCommand;
import org.neo4j.kernel.impl.transaction.command.Command.RelationshipGroupCommand;
import org.neo4j.kernel.impl.transaction.command.Command.RelationshipTypeTokenCommand;
import org.neo4j.kernel.impl.transaction.command.Command.SchemaRuleCommand;
import org.neo4j.kernel.impl.transaction.state.IntegrityValidator;
import org.neo4j.kernel.impl.transaction.state.RecordAccessSet;
import org.neo4j.kernel.impl.transaction.state.RecordChangeSet;
import org.neo4j.kernel.impl.transaction.state.RecordChanges;
import org.neo4j.kernel.impl.transaction.state.TokenCreator;
import org.neo4j.kernel.impl.transaction.state.RecordAccess.Loader;
import org.neo4j.kernel.impl.transaction.state.RecordAccess.RecordProxy;
import org.neo4j.kernel.impl.util.statistics.IntCounter;
import org.neo4j.storageengine.api.StorageCommand;
import org.neo4j.storageengine.api.StorageProperty;
import org.neo4j.storageengine.api.lock.ResourceLocker;
import org.neo4j.storageengine.api.schema.SchemaRule;
import org.neo4j.storageengine.api.schema.StoreIndexDescriptor;
import org.neo4j.values.storable.Value;

public class TransactionRecordState implements RecordState {
    private static final CommandComparator COMMAND_COMPARATOR = new CommandComparator();
    private static final Command[] EMPTY_COMMANDS = new Command[0];
    private final NeoStores neoStores;
    private final IntegrityValidator integrityValidator;
    private final NodeStore nodeStore;
    private final RelationshipStore relationshipStore;
    private final PropertyStore propertyStore;
    private final RecordStore<RelationshipGroupRecord> relationshipGroupStore;
    private final MetaDataStore metaDataStore;
    private final SchemaStore schemaStore;
    private final RecordAccessSet recordChangeSet;
    private final long lastCommittedTxWhenTransactionStarted;
    private final ResourceLocker locks;
    private final RelationshipCreator relationshipCreator;
    private final RelationshipDeleter relationshipDeleter;
    private final PropertyCreator propertyCreator;
    private final PropertyDeleter propertyDeleter;
    private RecordChanges<NeoStoreRecord, Void> neoStoreRecord;
    private boolean prepared;

    TransactionRecordState(NeoStores neoStores, IntegrityValidator integrityValidator, RecordChangeSet recordChangeSet, long lastCommittedTxWhenTransactionStarted, ResourceLocker locks, RelationshipCreator relationshipCreator, RelationshipDeleter relationshipDeleter, PropertyCreator propertyCreator, PropertyDeleter propertyDeleter) {
        this.neoStores = neoStores;
        this.nodeStore = neoStores.getNodeStore();
        this.relationshipStore = neoStores.getRelationshipStore();
        this.propertyStore = neoStores.getPropertyStore();
        this.relationshipGroupStore = neoStores.getRelationshipGroupStore();
        this.metaDataStore = neoStores.getMetaDataStore();
        this.schemaStore = neoStores.getSchemaStore();
        this.integrityValidator = integrityValidator;
        this.recordChangeSet = recordChangeSet;
        this.lastCommittedTxWhenTransactionStarted = lastCommittedTxWhenTransactionStarted;
        this.locks = locks;
        this.relationshipCreator = relationshipCreator;
        this.relationshipDeleter = relationshipDeleter;
        this.propertyCreator = propertyCreator;
        this.propertyDeleter = propertyDeleter;
    }

    public boolean hasChanges() {
        return this.recordChangeSet.hasChanges() || this.neoStoreRecord != null && this.neoStoreRecord.changeSize() > 0;
    }

    public void extractCommands(Collection<StorageCommand> commands) throws TransactionFailureException {
        assert !this.prepared : "Transaction has already been prepared";

        this.integrityValidator.validateTransactionStartKnowledge(this.lastCommittedTxWhenTransactionStarted);
        int noOfCommands = this.recordChangeSet.changeSize() + (this.neoStoreRecord != null ? this.neoStoreRecord.changeSize() : 0);
        Iterator var3 = this.recordChangeSet.getLabelTokenChanges().changes().iterator();

        RecordProxy record;
        while(var3.hasNext()) {
            record = (RecordProxy)var3.next();
            commands.add(new LabelTokenCommand((LabelTokenRecord)record.getBefore(), (LabelTokenRecord)record.forReadingLinkage()));
        }

        var3 = this.recordChangeSet.getRelationshipTypeTokenChanges().changes().iterator();

        while(var3.hasNext()) {
            record = (RecordProxy)var3.next();
            commands.add(new RelationshipTypeTokenCommand((RelationshipTypeTokenRecord)record.getBefore(), (RelationshipTypeTokenRecord)record.forReadingLinkage()));
        }

        var3 = this.recordChangeSet.getPropertyKeyTokenChanges().changes().iterator();

        while(var3.hasNext()) {
            record = (RecordProxy)var3.next();
            commands.add(new PropertyKeyTokenCommand((PropertyKeyTokenRecord)record.getBefore(), (PropertyKeyTokenRecord)record.forReadingLinkage()));
        }

        Command[] nodeCommands = EMPTY_COMMANDS;
        int skippedCommands = 0;
        if (this.recordChangeSet.getNodeRecords().changeSize() > 0) {
            nodeCommands = new Command[this.recordChangeSet.getNodeRecords().changeSize()];
            int i = 0;

            RecordProxy change;
            NodeRecord record1;
            for(Iterator var6 = this.recordChangeSet.getNodeRecords().changes().iterator(); var6.hasNext(); nodeCommands[i++] = new NodeCommand((NodeRecord)change.getBefore(), record1)) {
                change = (RecordProxy)var6.next();
                record1 = (NodeRecord)this.prepared(change, this.nodeStore);
                this.integrityValidator.validateNodeRecord(record1);
            }

            Arrays.sort(nodeCommands, COMMAND_COMPARATOR);
        }

        Command[] relCommands = EMPTY_COMMANDS;
        if (this.recordChangeSet.getRelRecords().changeSize() > 0) {
            relCommands = new Command[this.recordChangeSet.getRelRecords().changeSize()];
            int i = 0;

            RecordProxy change;
            for(Iterator var17 = this.recordChangeSet.getRelRecords().changes().iterator(); var17.hasNext(); relCommands[i++] = new RelationshipCommand((RelationshipRecord)change.getBefore(), (RelationshipRecord)this.prepared(change, this.relationshipStore))) {
                change = (RecordProxy)var17.next();
            }

            Arrays.sort(relCommands, COMMAND_COMPARATOR);
        }

        Command[] propCommands = EMPTY_COMMANDS;
        RecordProxy change1;
        Iterator var21;
        if (this.recordChangeSet.getPropertyRecords().changeSize() > 0) {
            propCommands = new Command[this.recordChangeSet.getPropertyRecords().changeSize()];
            int i = 0;

            for(var21 = this.recordChangeSet.getPropertyRecords().changes().iterator(); var21.hasNext(); propCommands[i++] = new PropertyCommand((PropertyRecord)change1.getBefore(), (PropertyRecord)this.prepared(change1, this.propertyStore))) {
                change1 = (RecordProxy)var21.next();
            }

            Arrays.sort(propCommands, COMMAND_COMPARATOR);
        }

        Command[] relGroupCommands = EMPTY_COMMANDS;
        RecordProxy change;
        Iterator var23;
        if (this.recordChangeSet.getRelGroupRecords().changeSize() > 0) {
            relGroupCommands = new Command[this.recordChangeSet.getRelGroupRecords().changeSize()];
            int i = 0;
            var23 = this.recordChangeSet.getRelGroupRecords().changes().iterator();

            while(true) {
                while(var23.hasNext()) {
                    change = (RecordProxy)var23.next();
                    if (change.isCreated() && !((RelationshipGroupRecord)change.forReadingLinkage()).inUse()) {
                        ++skippedCommands;
                    } else {
                        relGroupCommands[i++] = new RelationshipGroupCommand((RelationshipGroupRecord)change.getBefore(), (RelationshipGroupRecord)this.prepared(change, this.relationshipGroupStore));
                    }
                }

                relGroupCommands = i < relGroupCommands.length ? (Command[])Arrays.copyOf(relGroupCommands, i) : relGroupCommands;
                Arrays.sort(relGroupCommands, COMMAND_COMPARATOR);
                break;
            }
        }

        this.addFiltered(commands, Mode.CREATE, propCommands, relCommands, relGroupCommands, nodeCommands);
        this.addFiltered(commands, Mode.UPDATE, propCommands, relCommands, relGroupCommands, nodeCommands);
        this.addFiltered(commands, Mode.DELETE, propCommands, relCommands, relGroupCommands, nodeCommands);
        if (this.neoStoreRecord != null) {
            var21 = this.neoStoreRecord.changes().iterator();

            while(var21.hasNext()) {
                change = (RecordProxy)var21.next();
                commands.add(new NeoStoreCommand((NeoStoreRecord)change.getBefore(), (NeoStoreRecord)change.forReadingData()));
            }
        }

        List<Command>[] schemaChangeByMode = new List[Mode.values().length];

        for(int i = 0; i < schemaChangeByMode.length; ++i) {
            schemaChangeByMode[i] = new ArrayList();
        }

        var23 = this.recordChangeSet.getSchemaRuleChanges().changes().iterator();

        while(var23.hasNext()) {
            change = (RecordProxy)var23.next();
            if (((SchemaRecord)change.forReadingLinkage()).inUse()) {
                this.integrityValidator.validateSchemaRule((SchemaRule)change.getAdditionalData());
            }

            SchemaRuleCommand cmd = new SchemaRuleCommand((SchemaRecord)change.getBefore(), (SchemaRecord)change.forChangingData(), (SchemaRule)change.getAdditionalData());
            schemaChangeByMode[cmd.getMode().ordinal()].add(cmd);
        }

        commands.addAll(schemaChangeByMode[Mode.DELETE.ordinal()]);
        commands.addAll(schemaChangeByMode[Mode.CREATE.ordinal()]);
        commands.addAll(schemaChangeByMode[Mode.UPDATE.ordinal()]);

        assert commands.size() == noOfCommands - skippedCommands : String.format("Expected %d final commands, got %d instead, with %d skipped", noOfCommands, commands.size(), skippedCommands);

        this.prepared = true;
    }

    private <RECORD extends AbstractBaseRecord> RECORD prepared(RecordProxy<RECORD, ?> proxy, RecordStore<RECORD> store) {
        RECORD after = (RECORD) proxy.forReadingLinkage();
        store.prepareForCommit(after);
        return after;
    }

    void relCreate(long id, int typeId, long startNodeId, long endNodeId) {
        this.relationshipCreator.relationshipCreate(id, typeId, startNodeId, endNodeId, this.recordChangeSet, this.locks);
    }

    void relDelete(long relId) {
        this.relationshipDeleter.relDelete(relId, this.recordChangeSet, this.locks);
    }

    @SafeVarargs
    private final void addFiltered(Collection<StorageCommand> target, Mode mode, Command[]... commands) {
        Command[][] var4 = commands;
        int var5 = commands.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Command[] c = var4[var6];
            Command[] var8 = c;
            int var9 = c.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                Command command = var8[var10];
                if (command.getMode() == mode) {
                    target.add(command);
                }
            }
        }

    }

    public void nodeDelete(long nodeId) {
        NodeRecord nodeRecord = (NodeRecord)this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null).forChangingData();
        if (!nodeRecord.inUse()) {
            throw new IllegalStateException("Unable to delete Node[" + nodeId + "] since it has already been deleted.");
        } else {
            nodeRecord.setInUse(false);
            nodeRecord.setLabelField((long)Record.NO_LABELS_FIELD.intValue(), this.markNotInUse(nodeRecord.getDynamicLabelRecords()));
            this.getAndDeletePropertyChain(nodeRecord);
        }
    }

    private Collection<DynamicRecord> markNotInUse(Collection<DynamicRecord> dynamicLabelRecords) {
        Iterator var2 = dynamicLabelRecords.iterator();

        while(var2.hasNext()) {
            DynamicRecord record = (DynamicRecord)var2.next();
            record.setInUse(false);
        }

        return dynamicLabelRecords;
    }

    private void getAndDeletePropertyChain(NodeRecord nodeRecord) {
        this.propertyDeleter.deletePropertyChain(nodeRecord, this.recordChangeSet.getPropertyRecords());
    }

    void relRemoveProperty(long relId, int propertyKey) {
        RecordProxy<RelationshipRecord, Void> rel = this.recordChangeSet.getRelRecords().getOrLoad(relId, (Void)null);
        this.propertyDeleter.removeProperty(rel, propertyKey, this.recordChangeSet.getPropertyRecords());
    }

    //DynamicGraph
    //**********************************************************

    public void nodeRemoveProperty(long nodeId, int propertyKey,long version) {
        RecordProxy<NodeRecord, Void> node = this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null);
        this.propertyDeleter.removeProperty(node, propertyKey, this.recordChangeSet.getPropertyRecords(),version);
    }

    void nodeAddProperty(long nodeId, int propertyKey, Value value,long version) {
        RecordProxy<NodeRecord, Void> node = this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null);
        this.propertyCreator.primitiveSetProperty(node, propertyKey, value, this.recordChangeSet.getPropertyRecords(),version);
    }
    void nodeChangeProperty(long nodeId, int propertyKey, Value value,long version) {
        RecordProxy<NodeRecord, Void> node = this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null);
        this.propertyCreator.primitiveSetProperty(node, propertyKey, value, this.recordChangeSet.getPropertyRecords(),version);
    }

    void relAddProperty(long relId, int propertyKey, Value value,long version) {
        RecordProxy<RelationshipRecord, Void> rel = this.recordChangeSet.getRelRecords().getOrLoad(relId, (Void)null);
        this.propertyCreator.primitiveSetProperty(rel, propertyKey, value, this.recordChangeSet.getPropertyRecords(),version);
    }
    void relChangeProperty(long relId, int propertyKey, Value value,long version) {
        RecordProxy<RelationshipRecord, Void> rel = this.recordChangeSet.getRelRecords().getOrLoad(relId, (Void)null);
        this.propertyCreator.primitiveSetProperty(rel, propertyKey, value, this.recordChangeSet.getPropertyRecords(),version);
    }
    void relRemoveProperty(long relId, int propertyKey,long version) {
        RecordProxy<RelationshipRecord, Void> rel = this.recordChangeSet.getRelRecords().getOrLoad(relId, (Void)null);
        this.propertyDeleter.removeProperty(rel, propertyKey, this.recordChangeSet.getPropertyRecords(),version);
    }



    public void relVersionChange(long relId,long version){
        RecordProxy<RelationshipRecord, Void> rel = this.recordChangeSet.getRelRecords().getOrLoad(relId, (Void)null);
        //node.forChangingLinkage().setVersion(version);
        rel.forChangingData().setVersion(version);
        //rel.forChangingData().setVersion(version);
    }
    public void nodeVersionChange(long nodeId,long version){
        RecordProxy<NodeRecord, Void> node = this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null);
        //node.forChangingLinkage().setVersion(version);
        node.forChangingData().setVersion(version);
    }

    void addVersionLabelToNode(long labelId, long nodeId, long version) {
        NodeRecord nodeRecord = (NodeRecord)this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null).forChangingData();
        NodeLabelsField.parseLabelsField(nodeRecord).add(labelId, this.nodeStore, this.nodeStore.getDynamicLabelStore(),version);
    }

    //DynamicGraph
    //**********************************************************

    public void nodeRemoveProperty(long nodeId, int propertyKey) {
        RecordProxy<NodeRecord, Void> node = this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null);
        this.propertyDeleter.removeProperty(node, propertyKey, this.recordChangeSet.getPropertyRecords());
    }

    void relChangeProperty(long relId, int propertyKey, Value value) {
        RecordProxy<RelationshipRecord, Void> rel = this.recordChangeSet.getRelRecords().getOrLoad(relId, (Void)null);
        this.propertyCreator.primitiveSetProperty(rel, propertyKey, value, this.recordChangeSet.getPropertyRecords());
    }

    void nodeChangeProperty(long nodeId, int propertyKey, Value value) {
        RecordProxy<NodeRecord, Void> node = this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null);
        this.propertyCreator.primitiveSetProperty(node, propertyKey, value, this.recordChangeSet.getPropertyRecords());
    }

    void relAddProperty(long relId, int propertyKey, Value value) {
        RecordProxy<RelationshipRecord, Void> rel = this.recordChangeSet.getRelRecords().getOrLoad(relId, (Void)null);
        this.propertyCreator.primitiveSetProperty(rel, propertyKey, value, this.recordChangeSet.getPropertyRecords());
    }

    void nodeAddProperty(long nodeId, int propertyKey, Value value) {
        RecordProxy<NodeRecord, Void> node = this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null);
        this.propertyCreator.primitiveSetProperty(node, propertyKey, value, this.recordChangeSet.getPropertyRecords());
    }

    //DynamicGraph
    //***********************************************************
    public void nodeCreate(long nodeId) {
        NodeRecord nodeRecord = (NodeRecord)this.recordChangeSet.getNodeRecords().create(nodeId, (Void)null).forChangingData();
        nodeRecord.setInUse(true);
        nodeRecord.setVersion(this.lastCommittedTxWhenTransactionStarted);
        nodeRecord.setCreated();
    }

    //DynamicGraph
    //***********************************************************

    void createPropertyKeyToken(String key, long id) {
        TokenCreator<PropertyKeyTokenRecord> creator = new TokenCreator(this.neoStores.getPropertyKeyTokenStore());
        creator.createToken(key, id, this.recordChangeSet.getPropertyKeyTokenChanges());
    }

    void createLabelToken(String name, long id) {
        TokenCreator<LabelTokenRecord> creator = new TokenCreator(this.neoStores.getLabelTokenStore());
        creator.createToken(name, id, this.recordChangeSet.getLabelTokenChanges());
    }

    void createRelationshipTypeToken(String name, long id) {
        TokenCreator<RelationshipTypeTokenRecord> creator = new TokenCreator(this.neoStores.getRelationshipTypeTokenStore());
        creator.createToken(name, id, this.recordChangeSet.getRelationshipTypeTokenChanges());
    }

    private RecordProxy<NeoStoreRecord, Void> getOrLoadNeoStoreRecord() {
        if (this.neoStoreRecord == null) {
            this.neoStoreRecord = new RecordChanges(new Loader<NeoStoreRecord, Void>() {
                public NeoStoreRecord newUnused(long key, Void additionalData) {
                    throw new UnsupportedOperationException();
                }

                public NeoStoreRecord load(long key, Void additionalData) {
                    return TransactionRecordState.this.metaDataStore.graphPropertyRecord();
                }

                public void ensureHeavy(NeoStoreRecord record) {
                }

                public NeoStoreRecord clone(NeoStoreRecord neoStoreRecord) {
                    return neoStoreRecord.clone();
                }
            }, new IntCounter());
        }

        return this.neoStoreRecord.getOrLoad(0L, (Void)null);
    }

    void graphAddProperty(int propertyKey, Value value) {
        this.propertyCreator.primitiveSetProperty(this.getOrLoadNeoStoreRecord(), propertyKey, value, this.recordChangeSet.getPropertyRecords());
    }

    void graphChangeProperty(int propertyKey, Value value) {
        this.propertyCreator.primitiveSetProperty(this.getOrLoadNeoStoreRecord(), propertyKey, value, this.recordChangeSet.getPropertyRecords());
    }

    void graphRemoveProperty(int propertyKey) {
        RecordProxy<NeoStoreRecord, Void> recordChange = this.getOrLoadNeoStoreRecord();
        this.propertyDeleter.removeProperty(recordChange, propertyKey, this.recordChangeSet.getPropertyRecords());
    }

    void createSchemaRule(SchemaRule schemaRule) {
        Iterator var2 = ((SchemaRecord)this.recordChangeSet.getSchemaRuleChanges().create(schemaRule.getId(), schemaRule).forChangingData()).iterator();

        while(var2.hasNext()) {
            DynamicRecord change = (DynamicRecord)var2.next();
            change.setInUse(true);
            change.setCreated();
        }

    }

    void dropSchemaRule(SchemaRule rule) {
        RecordProxy<SchemaRecord, SchemaRule> change = this.recordChangeSet.getSchemaRuleChanges().getOrLoad(rule.getId(), rule);
        SchemaRecord records = (SchemaRecord)change.forChangingData();
        Iterator var4 = records.iterator();

        while(var4.hasNext()) {
            DynamicRecord record = (DynamicRecord)var4.next();
            record.setInUse(false);
        }

        records.setInUse(false);
    }

    private void changeSchemaRule(SchemaRule rule, SchemaRule updatedRule) {
        RecordProxy<SchemaRecord, SchemaRule> change = this.recordChangeSet.getSchemaRuleChanges().getOrLoad(rule.getId(), rule);
        SchemaRecord records = (SchemaRecord)change.forReadingData();
        RecordProxy<SchemaRecord, SchemaRule> recordChange = this.recordChangeSet.getSchemaRuleChanges().setRecord(rule.getId(), records, updatedRule);
        SchemaRecord dynamicRecords = (SchemaRecord)recordChange.forChangingData();
        dynamicRecords.setDynamicRecords(this.schemaStore.allocateFrom(updatedRule));
    }

    void addLabelToNode(long labelId, long nodeId) {
        NodeRecord nodeRecord = (NodeRecord)this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null).forChangingData();
        NodeLabelsField.parseLabelsField(nodeRecord).add(labelId, this.nodeStore, this.nodeStore.getDynamicLabelStore());
    }

    void removeLabelFromNode(long labelId, long nodeId) {
        NodeRecord nodeRecord = (NodeRecord)this.recordChangeSet.getNodeRecords().getOrLoad(nodeId, (Void)null).forChangingData();
        NodeLabelsField.parseLabelsField(nodeRecord).remove(labelId, this.nodeStore);
    }

    void setConstraintIndexOwner(StoreIndexDescriptor storeIndex, long constraintId) {
        StoreIndexDescriptor updatedStoreIndex = storeIndex.withOwningConstraint(constraintId);
        this.changeSchemaRule(storeIndex, updatedStoreIndex);
    }

    public interface PropertyReceiver<P extends StorageProperty> {
        void receive(P var1, long var2);
    }

    private static class CommandComparator implements Comparator<Command> {
        private CommandComparator() {
        }

        public int compare(Command o1, Command o2) {
            long id1 = o1.getKey();
            long id2 = o2.getKey();
            return Long.compare(id1, id2);
        }
    }
}
