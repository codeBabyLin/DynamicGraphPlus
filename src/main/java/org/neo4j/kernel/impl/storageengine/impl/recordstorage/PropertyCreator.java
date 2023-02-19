//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import cn.DynamicGraph.Common.DGVersion;
import org.neo4j.kernel.impl.store.DynamicRecordAllocator;
import org.neo4j.kernel.impl.store.PropertyStore;
import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.kernel.impl.store.id.IdSequence;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.PrimitiveRecord;
import org.neo4j.kernel.impl.store.record.PropertyBlock;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.Record;
import org.neo4j.kernel.impl.transaction.state.RecordAccess;
import org.neo4j.kernel.impl.transaction.state.RecordAccess.RecordProxy;
import org.neo4j.values.storable.Value;

public class PropertyCreator {
    private final DynamicRecordAllocator stringRecordAllocator;
    private final DynamicRecordAllocator arrayRecordAllocator;
    private final IdSequence propertyRecordIdGenerator;
    private final PropertyTraverser traverser;
    private final boolean allowStorePointsAndTemporal;

    public PropertyCreator(PropertyStore propertyStore, PropertyTraverser traverser) {
        this(propertyStore.getStringStore(), propertyStore.getArrayStore(), propertyStore, traverser, propertyStore.allowStorePointsAndTemporal());
    }

    PropertyCreator(DynamicRecordAllocator stringRecordAllocator, DynamicRecordAllocator arrayRecordAllocator, IdSequence propertyRecordIdGenerator, PropertyTraverser traverser, boolean allowStorePointsAndTemporal) {
        this.stringRecordAllocator = stringRecordAllocator;
        this.arrayRecordAllocator = arrayRecordAllocator;
        this.propertyRecordIdGenerator = propertyRecordIdGenerator;
        this.traverser = traverser;
        this.allowStorePointsAndTemporal = allowStorePointsAndTemporal;
    }
    //***************************************
    //DynamicGraph

    public <P extends PrimitiveRecord> void primitiveSetProperty(RecordProxy<P, Void> primitiveRecordChange, int propertyKey, Value value, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords,long version) {
        PropertyBlock block = this.encodePropertyValue(propertyKey, value);
        PrimitiveRecord primitive = (PrimitiveRecord)primitiveRecordChange.forReadingLinkage();

        assert this.traverser.assertPropertyChain(primitive, propertyRecords);

        int newBlockSizeInBytes = block.getSize();
        RecordProxy<PropertyRecord, PrimitiveRecord> freeHostProxy = null;
        RecordProxy<PropertyRecord, PrimitiveRecord> existingHostProxy = null;

        PropertyRecord prevProp;
        for(long prop = primitive.getNextProp(); prop != (long)Record.NO_NEXT_PROPERTY.intValue(); prop = prevProp.getNextProp()) {
            RecordProxy<PropertyRecord, PrimitiveRecord> proxy = propertyRecords.getOrLoad(prop, primitive);
            prevProp = (PropertyRecord)proxy.forReadingLinkage();
            if(!prevProp.inUse()) continue;

            assert prevProp.inUse() : prevProp;


            PropertyBlock existingBlock = prevProp.getPropertyBlock(propertyKey);
            if (existingBlock != null) {
                //existingHostProxy = proxy;
                PropertyRecord existingHost = proxy.forChangingData();
                //*******************************************************************
                //DynamicGraph
                PropertyRecord newHost;
                newHost = (PropertyRecord)propertyRecords.create(this.propertyRecordIdGenerator.nextId(), primitive).forChangingData();


                newHost.addPropertyBlock(block);
                newHost.setVersion(version);

                long oldversion = existingHost.getVersion();
                long endVersion = DGVersion.getStartVersion(version);
                long newVersion = DGVersion.setEndVersion(endVersion,oldversion);
                existingHost.setVersion(newVersion);

                long newId = newHost.getId();
                long oldId = existingHost.getId();
                long Prev = existingHost.getPrevProp();
                long Next = existingHost.getNextProp();
                if(Prev != (long)Record.NO_PREVIOUS_PROPERTY.intValue()){
                    PropertyRecord prev = (PropertyRecord) propertyRecords.getOrLoad(Prev, primitive).forChangingLinkage();
                    prev.setNextProp(newId);
                    newHost.setPrevProp(Prev);
                }
                else{
                    ((PrimitiveRecord)primitiveRecordChange.forChangingLinkage()).setNextProp(newId);
                }
                if(Next != (long)Record.NO_NEXT_PROPERTY.intValue()){
                    PropertyRecord next = (PropertyRecord) propertyRecords.getOrLoad(Next, primitive).forChangingLinkage();
                    next.setPrevProp(newId);
                    newHost.setNextProp(Next);
                }



                //newHost.setPrevProp(Prev);
               // newHost.setNextProp(Next);


                //newHost.setId(oldId);
                //existingHost.setId(newId);
                //newHost.setPrevHisProp(-1);
                //newHost.setNextHisProp(-1);
                newHost.setNextHisProp(existingHost.getId());
                existingHost.setPrevHisProp(newHost.getId());

                //existingHost.setInUse(false);

                //existingHost.setInUse(false);
                newHost.setInUse(true);
                //newHost.setChanged(primitive);
                //existingHost.setChanged(primitive);
                //existingHost.setNodeId(-1);
                assert this.traverser.assertPropertyChain(primitive, propertyRecords);
                long nextp = primitive.getNextProp();
                long nextHis = newHost.getId();
                List<PropertyRecord> toCheck1 = new LinkedList();
                List<PropertyRecord> toCheck2 = new LinkedList();
                while(nextp!= (long)Record.NO_NEXT_PROPERTY.intValue()){
                    PropertyRecord temp = (PropertyRecord) propertyRecords.getOrLoad(nextp, primitive).forReadingData();
                    toCheck1.add(temp);
                    nextp = temp.getNextProp();
                }


                while(nextHis!= (long)Record.NO_NEXT_PROPERTY.intValue()){
                    PropertyRecord temp = (PropertyRecord) propertyRecords.getOrLoad(nextHis, primitive).forReadingData();
                    toCheck2.add(temp);
                    nextHis = temp.getNextHisProp();
                }

                return;
                //long prevPropertyId = existingHost.getPrevProp();
                //long nextPropertyId = existingHost.getNextProp();
                //long prevPropertyId = existingHost.getPrevProp();
                //long nextPropertyId = existingHost.getNextProp();
            /*    if(prevPropertyId != (long)Record.NO_PREVIOUS_PROPERTY.intValue()){
                    RecordProxy<PropertyRecord, PrimitiveRecord> prevXy = propertyRecords.getOrLoad(prevPropertyId, primitive);
                    PropertyRecord prev= (PropertyRecord)proxy.forReadingLinkage();
                }*/


                //DynamicGraph
                //*******************************************************************



                //*****************************************

              /*  this.removeProperty(primitive, existingHost, existingBlock);
                if (newBlockSizeInBytes <= existingBlock.getSize() || this.propertyFitsInside(newBlockSizeInBytes, existingHost)) {
                    existingHost.addPropertyBlock(block);

                    assert this.traverser.assertPropertyChain(primitive, propertyRecords);

                    return;
                }

                if (freeHostProxy != null) {
                    PropertyRecord freeHost = (PropertyRecord)freeHostProxy.forChangingData();
                    freeHost.addPropertyBlock(block);
                    freeHost.setChanged(primitive);

                    assert this.traverser.assertPropertyChain(primitive, propertyRecords);

                    return;
                }*/

                //*****************************

            }
        }

        PropertyRecord freeHost;
        if (freeHostProxy == null) {
            freeHost = (PropertyRecord)propertyRecords.create(this.propertyRecordIdGenerator.nextId(), primitive).forChangingData();
            freeHost.setInUse(true);
            if (primitive.getNextProp() != (long)Record.NO_NEXT_PROPERTY.intValue()) {
                prevProp = (PropertyRecord)propertyRecords.getOrLoad(primitive.getNextProp(), primitive).forChangingLinkage();

                assert prevProp.getPrevProp() == (long)Record.NO_PREVIOUS_PROPERTY.intValue();

                prevProp.setPrevProp(freeHost.getId());
                freeHost.setNextProp(prevProp.getId());
                prevProp.setChanged(primitive);
            }

            ((PrimitiveRecord)primitiveRecordChange.forChangingLinkage()).setNextProp(freeHost.getId());
        } else {
            freeHost = (PropertyRecord)freeHostProxy.forChangingData();
        }
        freeHost.addPropertyBlock(block);
        freeHost.setVersion(version);

        assert this.traverser.assertPropertyChain(primitive, propertyRecords);

    }

        //dynamicGraph
    //**************************************


    public <P extends PrimitiveRecord> void primitiveSetProperty(RecordProxy<P, Void> primitiveRecordChange, int propertyKey, Value value, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords) {
        PropertyBlock block = this.encodePropertyValue(propertyKey, value);
        P primitive = (P) primitiveRecordChange.forReadingLinkage();

        assert this.traverser.assertPropertyChain(primitive, propertyRecords);

        int newBlockSizeInBytes = block.getSize();
        RecordProxy<PropertyRecord, PrimitiveRecord> freeHostProxy = null;
        RecordProxy<PropertyRecord, PrimitiveRecord> existingHostProxy = null;

        PropertyRecord prevProp;
        for(long prop = primitive.getNextProp(); prop != (long)Record.NO_NEXT_PROPERTY.intValue(); prop = prevProp.getNextProp()) {
            RecordProxy<PropertyRecord, PrimitiveRecord> proxy = propertyRecords.getOrLoad(prop, primitive);
            prevProp = (PropertyRecord)proxy.forReadingLinkage();

            assert prevProp.inUse() : prevProp;

            if (this.propertyFitsInside(newBlockSizeInBytes, prevProp)) {
                freeHostProxy = proxy;
                if (existingHostProxy != null) {
                    PropertyRecord freeHost = (PropertyRecord)proxy.forChangingData();
                    freeHost.addPropertyBlock(block);
                    freeHost.setChanged(primitive);

                    assert this.traverser.assertPropertyChain(primitive, propertyRecords);

                    return;
                }
            }

            PropertyBlock existingBlock = prevProp.getPropertyBlock(propertyKey);
            if (existingBlock != null) {
                existingHostProxy = proxy;
                PropertyRecord existingHost = (PropertyRecord)proxy.forChangingData();
                this.removeProperty(primitive, existingHost, existingBlock);
                if (newBlockSizeInBytes <= existingBlock.getSize() || this.propertyFitsInside(newBlockSizeInBytes, existingHost)) {
                    existingHost.addPropertyBlock(block);

                    assert this.traverser.assertPropertyChain(primitive, propertyRecords);

                    return;
                }

                if (freeHostProxy != null) {
                    PropertyRecord freeHost = (PropertyRecord)freeHostProxy.forChangingData();
                    freeHost.addPropertyBlock(block);
                    freeHost.setChanged(primitive);

                    assert this.traverser.assertPropertyChain(primitive, propertyRecords);

                    return;
                }
            }
        }

        PropertyRecord freeHost;
        if (freeHostProxy == null) {
            freeHost = (PropertyRecord)propertyRecords.create(this.propertyRecordIdGenerator.nextId(), primitive).forChangingData();
            freeHost.setInUse(true);
            if (primitive.getNextProp() != (long)Record.NO_NEXT_PROPERTY.intValue()) {
                prevProp = (PropertyRecord)propertyRecords.getOrLoad(primitive.getNextProp(), primitive).forChangingLinkage();

                assert prevProp.getPrevProp() == (long)Record.NO_PREVIOUS_PROPERTY.intValue();

                prevProp.setPrevProp(freeHost.getId());
                freeHost.setNextProp(prevProp.getId());
                prevProp.setChanged(primitive);
            }

            ((PrimitiveRecord)primitiveRecordChange.forChangingLinkage()).setNextProp(freeHost.getId());
        } else {
            freeHost = (PropertyRecord)freeHostProxy.forChangingData();
        }

        freeHost.addPropertyBlock(block);

        assert this.traverser.assertPropertyChain(primitive, propertyRecords);

    }

    private void removeProperty(PrimitiveRecord primitive, PropertyRecord host, PropertyBlock block) {
        host.removePropertyBlock(block.getKeyIndexId());
        host.setChanged(primitive);
        Iterator var4 = block.getValueRecords().iterator();

        while(var4.hasNext()) {
            DynamicRecord record = (DynamicRecord)var4.next();

            assert record.inUse();

            record.setInUse(false, block.getType().intValue());
            host.addDeletedRecord(record);
        }

    }

    private boolean propertyFitsInside(int newBlockSizeInBytes, PropertyRecord propRecord) {
        int propSize = propRecord.size();

        assert propSize >= 0 : propRecord;

        return propSize + newBlockSizeInBytes <= PropertyType.getPayloadSize();
    }

    public PropertyBlock encodePropertyValue(int propertyKey, Value value) {
        return this.encodeValue(new PropertyBlock(), propertyKey, value);
    }

    public PropertyBlock encodeValue(PropertyBlock block, int propertyKey, Value value) {
        PropertyStore.encodeValue(block, propertyKey, value, this.stringRecordAllocator, this.arrayRecordAllocator, this.allowStorePointsAndTemporal);
        return block;
    }

    public long createPropertyChain(PrimitiveRecord owner, Iterator<PropertyBlock> properties, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords) {
        return this.createPropertyChain(owner, properties, propertyRecords, (p) -> {
        });
    }

    private long createPropertyChain(PrimitiveRecord owner, Iterator<PropertyBlock> properties, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords, Consumer<PropertyRecord> createdPropertyRecords) {
        if (properties != null && properties.hasNext()) {
            PropertyRecord currentRecord = (PropertyRecord)propertyRecords.create(this.propertyRecordIdGenerator.nextId(), owner).forChangingData();
            createdPropertyRecords.accept(currentRecord);
            currentRecord.setInUse(true);
            currentRecord.setCreated();

            PropertyBlock block;
            for(; properties.hasNext(); currentRecord.addPropertyBlock(block)) {
                block = (PropertyBlock)properties.next();
                if (currentRecord.size() + block.getSize() > PropertyType.getPayloadSize()) {
                    PropertyRecord prevRecord = currentRecord;
                    long propertyId = this.propertyRecordIdGenerator.nextId();
                    currentRecord = (PropertyRecord)propertyRecords.create(propertyId, owner).forChangingData();
                    createdPropertyRecords.accept(currentRecord);
                    currentRecord.setInUse(true);
                    currentRecord.setCreated();
                    prevRecord.setNextProp(propertyId);
                    currentRecord.setPrevProp(prevRecord.getId());
                }
            }

            return currentRecord.getId();
        } else {
            return (long)Record.NO_NEXT_PROPERTY.intValue();
        }
    }
}
