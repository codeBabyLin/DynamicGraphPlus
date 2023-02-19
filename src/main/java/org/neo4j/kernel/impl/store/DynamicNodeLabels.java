//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.util.*;

import cn.DynamicGraph.Common.Serialization;

import org.eclipse.collections.api.block.procedure.primitive.LongLongProcedure;

import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.kernel.impl.store.allocator.ReusableRecordsCompositeAllocator;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.NodeRecord;
//import scala.collection.mutable.Map;

public class DynamicNodeLabels implements NodeLabels {
    private final NodeRecord node;

    public DynamicNodeLabels(NodeRecord node) {
        this.node = node;
    }

    //DynamicGraph
    //*******************************
    //添加判断条件

    public long[] get(NodeStore nodeStore) {
        return get(this.node, nodeStore);
    }


    public static long[] get(NodeRecord node, NodeStore nodeStore, long version) {
        //************************
        if(node.isLight()){
            nodeStore.ensureHeavyEx(node);
        }
        LongLongHashMap newMaps = new LongLongHashMap();
        Map<Long,Long> versionLabels = new HashMap<Long, Long>();
        if(!node.getVersionLabels().isEmpty()){
            LongLongProcedure longLongProcedure = new LongLongProcedure() {
                @Override
                public void value(long l, long l1) {
                    if(l1 == version) {
                        newMaps.put(l,l1);
                        versionLabels.put(l,l1);
                    }
                }
            };
            node.getVersionLabels().forEachKeyValue(longLongProcedure);
            return newMaps.keySet().toArray();
        }
        else return newMaps.keySet().toArray();
        //************************
     /*   if (node.isLight()) {
            nodeStore.ensureHeavy(node, NodeLabelsField.firstDynamicLabelRecordId(node.getLabelField()));
        }

        return getDynamicLabelsArray(node.getUsedDynamicLabelRecords(), nodeStore.getDynamicLabelStore());*/
    }
    public static Map<Long,Long> getVersionLabels(NodeRecord node, NodeStore nodeStore) {
        //************************
        if(node.isLight()){
            nodeStore.ensureHeavyEx(node);
        }
        Map<Long,Long> versionLabels = new HashMap<Long, Long>();
        if(!node.getVersionLabels().isEmpty()){
            LongLongProcedure longLongProcedure = new LongLongProcedure() {
                @Override
                public void value(long l, long l1) {
                    versionLabels.put(l,l1);
                }
            };
            node.getVersionLabels().forEachKeyValue(longLongProcedure);
            return versionLabels;
        }
        else return versionLabels;
        //************************
     /*   if (node.isLight()) {
            nodeStore.ensureHeavy(node, NodeLabelsField.firstDynamicLabelRecordId(node.getLabelField()));
        }

        return getDynamicLabelsArray(node.getUsedDynamicLabelRecords(), nodeStore.getDynamicLabelStore());*/
    }
    public static long[] get(NodeRecord node, NodeStore nodeStore) {
        //************************
        if(node.isLight()){
            nodeStore.ensureHeavyEx(node);
        }
        if(!node.getVersionLabels().isEmpty()){
            return node.getVersionLabels().keySet().toArray();
        }
        else return node.getVersionLabels().keySet().toArray();
        //************************
     /*   if (node.isLight()) {
            nodeStore.ensureHeavy(node, NodeLabelsField.firstDynamicLabelRecordId(node.getLabelField()));
        }

        return getDynamicLabelsArray(node.getUsedDynamicLabelRecords(), nodeStore.getDynamicLabelStore());*/
    }

    public long[] getIfLoaded() {
        //************************

        if(!node.getVersionLabels().isEmpty()){
            return node.getVersionLabels().keySet().toArray();
        }
        else return node.getVersionLabels().keySet().toArray();
        //************************
        //return this.node.isLight() ? null : LabelIdArray.stripNodeId((long[])((long[])DynamicArrayStore.getRightArray(AbstractDynamicStore.readFullByteArrayFromHeavyRecords(this.node.getUsedDynamicLabelRecords(), PropertyType.ARRAY)).asObject()));
    }

    //DynamicGraph
    //*******************************

    public Collection<DynamicRecord> put(long[] labelIds, NodeStore nodeStore, DynamicRecordAllocator allocator) {
        Arrays.sort(labelIds);
        return putSorted(this.node, labelIds, nodeStore, allocator);
    }

    static Collection<DynamicRecord> putSorted(NodeRecord node, long[] labelIds, NodeStore nodeStore, DynamicRecordAllocator allocator) {
        long existingLabelsField = node.getLabelField();
        long existingLabelsBits = NodeLabelsField.parseLabelsBody(existingLabelsField);
        Collection<DynamicRecord> changedDynamicRecords = node.getDynamicLabelRecords();
        long labelField = node.getLabelField();
        if (NodeLabelsField.fieldPointsToDynamicRecordOfLabels(labelField)) {
            nodeStore.ensureHeavy(node, existingLabelsBits);
            changedDynamicRecords = node.getDynamicLabelRecords();
            setNotInUse(changedDynamicRecords);
        }

        if (!InlineNodeLabels.tryInlineInNodeRecord(node, labelIds, changedDynamicRecords)) {
            Iterator<DynamicRecord> recycledRecords = changedDynamicRecords.iterator();
            Collection allocatedRecords = allocateRecordsForDynamicLabels(node.getId(), labelIds, (DynamicRecordAllocator)(new ReusableRecordsCompositeAllocator(recycledRecords, allocator)));

            while(recycledRecords.hasNext()) {
                DynamicRecord removedRecord = (DynamicRecord)recycledRecords.next();
                removedRecord.setInUse(false);
                allocatedRecords.add(removedRecord);
            }

            node.setLabelField(dynamicPointer(allocatedRecords), allocatedRecords);
            changedDynamicRecords = allocatedRecords;
        }

        return changedDynamicRecords;
    }

    public Collection<DynamicRecord> add(long labelId, NodeStore nodeStore, DynamicRecordAllocator allocator) {
        nodeStore.ensureHeavy(this.node, NodeLabelsField.firstDynamicLabelRecordId(this.node.getLabelField()));
        long[] existingLabelIds = getDynamicLabelsArray(this.node.getUsedDynamicLabelRecords(), nodeStore.getDynamicLabelStore());
        long[] newLabelIds = LabelIdArray.concatAndSort(existingLabelIds, labelId);
        Collection<DynamicRecord> existingRecords = this.node.getDynamicLabelRecords();
        Collection<DynamicRecord> changedDynamicRecords = allocateRecordsForDynamicLabels(this.node.getId(), newLabelIds, (DynamicRecordAllocator)(new ReusableRecordsCompositeAllocator(existingRecords, allocator)));
        this.node.setLabelField(dynamicPointer(changedDynamicRecords), changedDynamicRecords);
        return changedDynamicRecords;
    }

    public Collection<DynamicRecord> remove(long labelId, NodeStore nodeStore) {
        nodeStore.ensureHeavy(this.node, NodeLabelsField.firstDynamicLabelRecordId(this.node.getLabelField()));
        long[] existingLabelIds = getDynamicLabelsArray(this.node.getUsedDynamicLabelRecords(), nodeStore.getDynamicLabelStore());
        long[] newLabelIds = LabelIdArray.filter(existingLabelIds, labelId);
        Collection<DynamicRecord> existingRecords = this.node.getDynamicLabelRecords();
        if (InlineNodeLabels.tryInlineInNodeRecord(this.node, newLabelIds, existingRecords)) {
            setNotInUse(existingRecords);
        } else {
            Collection<DynamicRecord> newRecords = allocateRecordsForDynamicLabels(this.node.getId(), newLabelIds, (DynamicRecordAllocator)(new ReusableRecordsCompositeAllocator(existingRecords, nodeStore.getDynamicLabelStore())));
            this.node.setLabelField(dynamicPointer(newRecords), existingRecords);
            if (!newRecords.equals(existingRecords)) {
                Iterator var8 = existingRecords.iterator();

                while(var8.hasNext()) {
                    DynamicRecord record = (DynamicRecord)var8.next();
                    if (!newRecords.contains(record)) {
                        record.setInUse(false);
                    }
                }
            }
        }

        return existingRecords;
    }

    //DynamicGraph
    //******************************************************************************

/*    @Override
    public Collection<DynamicRecord> put(long[] var1, NodeStore var2, DynamicRecordAllocator var3, Map<Long, Long> versionMap) {
        return null;
    }*/

    @Override
    public Collection<DynamicRecord> add(long labelId, NodeStore nodeStore, DynamicRecordAllocator allocator, long version) {
        nodeStore.ensureHeavy(this.node);
        this.node.getVersionLabels().put(labelId, version);
        //long[] existingLabelIds = getDynamicLabelsArray(this.node.getUsedDynamicLabelRecords(), nodeStore.getDynamicLabelStore());
        //long[] newLabelIds = LabelIdArray.concatAndSort(existingLabelIds, labelId);
        //Collection<DynamicRecord> existingRecords = this.node.getDynamicLabelRecords();
        //Collection<DynamicRecord> changedDynamicRecords = allocateRecordsForDynamicLabels(this.node.getId(), newLabelIds, (DynamicRecordAllocator)(new ReusableRecordsCompositeAllocator(existingRecords, allocator)));
        Collection<DynamicRecord> changedDynamicRecords = new ArrayList();
        byte [] data = Serialization.writeMapToByteArray(this.node.getVersionLabels());
        nodeStore.getDynamicLabelStore().allocateRecordsFromBytes(changedDynamicRecords,data);
        this.node.setLabelField(dynamicPointer(changedDynamicRecords), changedDynamicRecords);
        return changedDynamicRecords;

        //return null;
    }

    @Override
    public Collection<DynamicRecord> remove(long var1, NodeStore var3, long version) {
        return null;
    }

    //DynamicGraph
    //******************************************************************************


    public long getFirstDynamicRecordId() {
        return NodeLabelsField.firstDynamicLabelRecordId(this.node.getLabelField());
    }

    public static long dynamicPointer(Collection<DynamicRecord> newRecords) {
        return 549755813888L | ((DynamicRecord)Iterables.first(newRecords)).getId();
    }

    private static void setNotInUse(Collection<DynamicRecord> changedDynamicRecords) {
        Iterator var1 = changedDynamicRecords.iterator();

        while(var1.hasNext()) {
            DynamicRecord record = (DynamicRecord)var1.next();
            record.setInUse(false);
        }

    }

    public boolean isInlined() {
        return false;
    }
    public  String toString(){
        if(!NodeLabelsField.fieldPointsToDynamicRecordOfLabels(this.node.getLabelField())){
            return new InlineNodeLabels(this.node).toString();
        }
        else return toString2();
    }

    private String toString2() {
        return this.node.isLight() ? String.format("Dynamic(id:%d)", NodeLabelsField.firstDynamicLabelRecordId(this.node.getLabelField())) : String.format("Dynamic(id:%d,[%s])", NodeLabelsField.firstDynamicLabelRecordId(this.node.getLabelField()), Arrays.toString(getDynamicLabelsArrayFromHeavyRecords(this.node.getUsedDynamicLabelRecords())));
    }

    public static Collection<DynamicRecord> allocateRecordsForDynamicLabels(long nodeId, long[] labels, AbstractDynamicStore dynamicLabelStore) {
        return allocateRecordsForDynamicLabels(nodeId, labels, (DynamicRecordAllocator)dynamicLabelStore);
    }

    public static Collection<DynamicRecord> allocateRecordsForDynamicLabels(long nodeId, long[] labels, DynamicRecordAllocator allocator) {
        long[] storedLongs = LabelIdArray.prependNodeId(nodeId, labels);
        Collection<DynamicRecord> records = new ArrayList();
        DynamicArrayStore.allocateRecords(records, storedLongs, allocator, false);
        return records;
    }

    public static long[] getDynamicLabelsArray(Iterable<DynamicRecord> records, AbstractDynamicStore dynamicLabelStore) {
        long[] storedLongs = (long[])((long[])DynamicArrayStore.getRightArray(dynamicLabelStore.readFullByteArray(records, PropertyType.ARRAY)).asObject());
        return LabelIdArray.stripNodeId(storedLongs);
    }

    public static long[] getDynamicLabelsArrayFromHeavyRecords(Iterable<DynamicRecord> records) {
        long[] storedLongs = (long[])((long[])DynamicArrayStore.getRightArray(AbstractDynamicStore.readFullByteArrayFromHeavyRecords(records, PropertyType.ARRAY)).asObject());
        return LabelIdArray.stripNodeId(storedLongs);
    }

    public static Pair<Long, long[]> getDynamicLabelsArrayAndOwner(Iterable<DynamicRecord> records, AbstractDynamicStore dynamicLabelStore) {
        long[] storedLongs = (long[])((long[])DynamicArrayStore.getRightArray(dynamicLabelStore.readFullByteArray(records, PropertyType.ARRAY)).asObject());
        return Pair.of(storedLongs[0], LabelIdArray.stripNodeId(storedLongs));
    }
}
