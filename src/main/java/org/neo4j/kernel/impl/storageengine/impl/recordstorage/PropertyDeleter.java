//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.storageengine.impl.recordstorage;

import java.util.Iterator;

import cn.DynamicGraph.Common.DGVersion;
import org.neo4j.kernel.impl.store.record.*;
import org.neo4j.kernel.impl.transaction.state.RecordAccess;
import org.neo4j.kernel.impl.transaction.state.RecordAccess.RecordProxy;

public class PropertyDeleter {
    private final PropertyTraverser traverser;

    public PropertyDeleter(PropertyTraverser traverser) {
        this.traverser = traverser;
    }

    public void deletePropertyChain(PrimitiveRecord primitive, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords) {
        long nextProp = primitive.getNextProp();

        while(nextProp != (long)Record.NO_NEXT_PROPERTY.intValue()) {
            RecordProxy<PropertyRecord, PrimitiveRecord> propertyChange = propertyRecords.getOrLoad(nextProp, primitive);
            PropertyRecord propRecord = (PropertyRecord)propertyChange.forChangingData();
            deletePropertyRecordIncludingValueRecords(propRecord);
            nextProp = propRecord.getNextProp();
            propRecord.setChanged(primitive);
        }

        primitive.setNextProp((long)Record.NO_NEXT_PROPERTY.intValue());
    }

    public static void deletePropertyRecordIncludingValueRecords(PropertyRecord record) {
        Iterator var1 = record.iterator();

        while(var1.hasNext()) {
            PropertyBlock block = (PropertyBlock)var1.next();
            Iterator var3 = block.getValueRecords().iterator();

            while(var3.hasNext()) {
                DynamicRecord valueRecord = (DynamicRecord)var3.next();

                assert valueRecord.inUse();

                valueRecord.setInUse(false);
                record.addDeletedRecord(valueRecord);
            }
        }

        record.clearPropertyBlocks();
        record.setInUse(false);
    }

    public <P extends PrimitiveRecord> boolean removePropertyIfExists(RecordProxy<P, Void> primitiveProxy, int propertyKey, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords) {
        PrimitiveRecord primitive = (PrimitiveRecord)primitiveProxy.forReadingData();
        long propertyId = this.traverser.findPropertyRecordContaining(primitive, propertyKey, propertyRecords, false);
        if (!Record.NO_NEXT_PROPERTY.is(propertyId)) {
            this.removeProperty(primitiveProxy, propertyKey, propertyRecords, primitive, propertyId);
            return true;
        } else {
            return false;
        }
    }

    //DynamicGraph
    public <P extends PrimitiveRecord> void removeProperty(RecordProxy<P, Void> primitiveProxy, int propertyKey, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords,long version) {
        PrimitiveRecord primitive = (PrimitiveRecord)primitiveProxy.forReadingData();
        long propertyId = this.traverser.findPropertyRecordContaining(primitive, propertyKey, propertyRecords, true);
        RecordProxy<PropertyRecord, PrimitiveRecord> recordChange = propertyRecords.getOrLoad(propertyId, primitive);
        PropertyRecord propRecord = (PropertyRecord)recordChange.forChangingData();
        //propRecord.setInUse(false);
        long oldVersion = propRecord.getVersion();
        long endVersion = DGVersion.getStartVersion(version);
        long newVersion = DGVersion.setEndVersion(endVersion,oldVersion);
        propRecord.setVersion(newVersion);

        //this.removeProperty(primitiveProxy, propertyKey, propertyRecords, primitive, propertyId);
    }

    //DynamicGraph

    public <P extends PrimitiveRecord> void removeProperty(RecordProxy<P, Void> primitiveProxy, int propertyKey, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords) {
        PrimitiveRecord primitive = (PrimitiveRecord)primitiveProxy.forReadingData();
        long propertyId = this.traverser.findPropertyRecordContaining(primitive, propertyKey, propertyRecords, true);
        this.removeProperty(primitiveProxy, propertyKey, propertyRecords, primitive, propertyId);
    }

    private <P extends PrimitiveRecord> void removeProperty(RecordProxy<P, Void> primitiveProxy, int propertyKey, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords, PrimitiveRecord primitive, long propertyId) {
        RecordProxy<PropertyRecord, PrimitiveRecord> recordChange = propertyRecords.getOrLoad(propertyId, primitive);
        PropertyRecord propRecord = (PropertyRecord)recordChange.forChangingData();
        if (!propRecord.inUse()) {
            throw new IllegalStateException("Unable to delete property[" + propertyId + "] since it is already deleted.");
        } else {
            PropertyBlock block = propRecord.removePropertyBlock(propertyKey);
            if (block == null) {
                throw new IllegalStateException("Property with index[" + propertyKey + "] is not present in property[" + propertyId + "]");
            } else {
                Iterator var10 = block.getValueRecords().iterator();

                while(var10.hasNext()) {
                    DynamicRecord valueRecord = (DynamicRecord)var10.next();

                    assert valueRecord.inUse();

                    valueRecord.setInUse(false, block.getType().intValue());
                    propRecord.addDeletedRecord(valueRecord);
                }

                if (propRecord.size() > 0) {
                    propRecord.setChanged(primitive);

                    assert this.traverser.assertPropertyChain(primitive, propertyRecords);
                } else {
                    this.unlinkPropertyRecord(propRecord, propertyRecords, primitiveProxy);
                }

            }
        }
    }

    private <P extends PrimitiveRecord> void unlinkPropertyRecord(PropertyRecord propRecord, RecordAccess<PropertyRecord, PrimitiveRecord> propertyRecords, RecordProxy<P, Void> primitiveRecordChange) {
        P primitive = (P) primitiveRecordChange.forReadingLinkage();

        assert this.traverser.assertPropertyChain(primitive, propertyRecords);

        assert propRecord.size() == 0;

        long prevProp = propRecord.getPrevProp();
        long nextProp = propRecord.getNextProp();
        if (primitive.getNextProp() == propRecord.getId()) {
            assert propRecord.getPrevProp() == (long)Record.NO_PREVIOUS_PROPERTY.intValue() : propRecord + " for " + primitive;

            ((PrimitiveRecord)primitiveRecordChange.forChangingLinkage()).setNextProp(nextProp);
        }

        PropertyRecord nextPropRecord;
        if (prevProp != (long)Record.NO_PREVIOUS_PROPERTY.intValue()) {
            nextPropRecord = (PropertyRecord)propertyRecords.getOrLoad(prevProp, primitive).forChangingLinkage();

            assert nextPropRecord.inUse() : nextPropRecord + "->" + propRecord + " for " + primitive;

            nextPropRecord.setNextProp(nextProp);
            nextPropRecord.setChanged(primitive);
        }

        if (nextProp != (long)Record.NO_NEXT_PROPERTY.intValue()) {
            nextPropRecord = (PropertyRecord)propertyRecords.getOrLoad(nextProp, primitive).forChangingLinkage();

            assert nextPropRecord.inUse() : propRecord + "->" + nextPropRecord + " for " + primitive;

            nextPropRecord.setPrevProp(prevProp);
            nextPropRecord.setChanged(primitive);
        }

        propRecord.setInUse(false);
        propRecord.setPrevProp((long)Record.NO_PREVIOUS_PROPERTY.intValue());
        propRecord.setNextProp((long)Record.NO_NEXT_PROPERTY.intValue());
        propRecord.setChanged(primitive);

        assert this.traverser.assertPropertyChain(primitive, propertyRecords);

    }


}
