//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.io.File;
import java.nio.file.OpenOption;
import java.util.Arrays;
import java.util.Iterator;

import cn.DynamicGraph.Common.Serialization;
//import cn.DynamicGraph.kernel.impl.store.NodeVersionLabelsField;


import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.format.RecordFormats;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.RecordLoad;
import org.neo4j.kernel.impl.util.Bits;
import org.neo4j.logging.LogProvider;

public class NodeStore extends CommonAbstractStore<NodeRecord, NoStoreHeader> {
    public static final String TYPE_DESCRIPTOR = "NodeStore";
    private final DynamicArrayStore dynamicLabelStore;

    //DynamicGraph
    //**************************************************


    //DynamicGraph
    //**************************************************

    public static Long readOwnerFromDynamicLabelsRecord(DynamicRecord record) {
        byte[] data = record.getData();
        byte[] header = PropertyType.ARRAY.readDynamicRecordHeader(data);
        byte[] array = Arrays.copyOfRange(data, header.length, data.length);
        int requiredBits = header[2];
        if (requiredBits == 0) {
            return null;
        } else {
            Bits bits = Bits.bitsFromBytes(array);
            return bits.getLong(requiredBits);
        }
    }

    public NodeStore(File file, File idFile, Config config, IdGeneratorFactory idGeneratorFactory, PageCache pageCache, LogProvider logProvider, DynamicArrayStore dynamicLabelStore, RecordFormats recordFormats, OpenOption... openOptions) {
        super(file, idFile, config, IdType.NODE, idGeneratorFactory, pageCache, logProvider, "NodeStore", recordFormats.node(), NoStoreHeaderFormat.NO_STORE_HEADER_FORMAT, recordFormats.storeVersion(), openOptions);
        this.dynamicLabelStore = dynamicLabelStore;
        //this.dynamicVersionLabelStore = dynamicVersionLabelStore;
    }

    public <FAILURE extends Exception> void accept(Processor<FAILURE> processor, NodeRecord record) throws FAILURE {
        processor.processNode(this, record);
    }

    public void ensureHeavy(NodeRecord node) {
        if (NodeLabelsField.fieldPointsToDynamicRecordOfLabels(node.getLabelField())) {
            this.ensureHeavy(node, NodeLabelsField.firstDynamicLabelRecordId(node.getLabelField()));
        }

    }

    public void ensureHeavy(NodeRecord node, long firstDynamicLabelRecord) {
        if (node.isLight()) {
            node.setLabelField(node.getLabelField(), this.dynamicLabelStore.getRecords(firstDynamicLabelRecord, RecordLoad.NORMAL));

            //DynamicGraph
            //***********************************************************
            byte[] data = DynamicArrayStore.readFullByteArrayFromHeavyRecordsEx(node.getUsedDynamicLabelRecords(),PropertyType.ARRAY);
            LongLongHashMap longLongHashMap = Serialization.readMapFromByteArray(data);
            node.setVersionLabelField(longLongHashMap);
            //DynamicGraph
            //***********************************************************
        }
    }

    public void updateRecord(NodeRecord record) {
        super.updateRecord(record);
        this.updateDynamicLabelRecords(record.getDynamicLabelRecords());
    }

    public DynamicArrayStore getDynamicLabelStore() {
        return this.dynamicLabelStore;
    }

    public void updateDynamicLabelRecords(Iterable<DynamicRecord> dynamicLabelRecords) {
        Iterator var2 = dynamicLabelRecords.iterator();

        while(var2.hasNext()) {
            DynamicRecord record = (DynamicRecord)var2.next();
            this.dynamicLabelStore.updateRecord(record);
        }

    }

    //DynamicGraph
    //**************************************************


    public void ensureHeavyEx(NodeRecord node) {
        if (NodeLabelsField.fieldPointsToDynamicRecordOfLabels(node.getLabelField())) {
            this.ensureHeavyEx(node, NodeLabelsField.firstDynamicLabelRecordId(node.getLabelField()));
        }

    }

    public void ensureHeavyEx(NodeRecord node, long firstDynamicLabelRecord) {
        if (node.isLight()) {
            this.ensureHeavy(node,firstDynamicLabelRecord);
            //Collectionthis.dynamicLabelStore.getRecords(firstDynamicLabelRecord, RecordLoad.NORMAL);
            byte[] data = DynamicArrayStore.readFullByteArrayFromHeavyRecordsEx(node.getUsedDynamicLabelRecords(),PropertyType.ARRAY);
            LongLongHashMap longLongHashMap = Serialization.readMapFromByteArray(data);
            node.setVersionLabelField(longLongHashMap);
            //node.setVersionLabelField(node.getVersionLabelField(), this.dynamicVersionLabelStore.getRecords(firstDynamicLabelRecord, RecordLoad.NORMAL));
        }
    }



    public void updateRecordWithVersion(NodeRecord record) {
        super.updateRecord(record);
        //this.updateDynamicLabelRecords(record.getDynamicLabelRecords());
        //this.updateDynamicVersionLabelRecords(record.getDynamicVersionLabelRecords());
    }



}
