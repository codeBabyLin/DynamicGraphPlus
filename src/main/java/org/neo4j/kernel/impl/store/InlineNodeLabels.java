//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import cn.DynamicGraph.Common.Serialization;
import org.neo4j.collection.PrimitiveLongCollections;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.util.Bits;
//import scala.collection.mutable.Map;

public class InlineNodeLabels implements NodeLabels {
    private static final int LABEL_BITS = 36;
    private final NodeRecord node;

    public InlineNodeLabels(NodeRecord node) {
        this.node = node;
    }


    //Dynamicgraph
    //**********************************************

    public long[] get(NodeStore nodeStore) {

        return get(this.node);
    }

    public static long[] get(NodeRecord node) {
        //************************
        if(!node.getVersionLabels().isEmpty()){
            return node.getVersionLabels().keySet().toArray();
        }
        //************************

        return parseInlined(node.getLabelField());
    }

    public long[] getIfLoaded() {
        //************************
        if(!node.getVersionLabels().isEmpty()){
            return node.getVersionLabels().keySet().toArray();
        }
        //************************

        return parseInlined(this.node.getLabelField());
    }

    //Dynamicgraph
    //**********************************************

    public Collection<DynamicRecord> put(long[] labelIds, NodeStore nodeStore, DynamicRecordAllocator allocator) {
        Arrays.sort(labelIds);
        return putSorted(this.node, labelIds, nodeStore, allocator);
    }

    public static Collection<DynamicRecord> putSorted(NodeRecord node, long[] labelIds, NodeStore nodeStore, DynamicRecordAllocator allocator) {
        return (Collection)(tryInlineInNodeRecord(node, labelIds, node.getDynamicLabelRecords()) ? Collections.emptyList() : DynamicNodeLabels.putSorted(node, labelIds, nodeStore, allocator));
    }

    public Collection<DynamicRecord> add(long labelId, NodeStore nodeStore, DynamicRecordAllocator allocator) {
        long[] augmentedLabelIds = labelCount(this.node.getLabelField()) == 0 ? new long[]{labelId} : LabelIdArray.concatAndSort(parseInlined(this.node.getLabelField()), labelId);
        return putSorted(this.node, augmentedLabelIds, nodeStore, allocator);
    }

    public Collection<DynamicRecord> remove(long labelId, NodeStore nodeStore) {
        long[] newLabelIds = LabelIdArray.filter(parseInlined(this.node.getLabelField()), labelId);
        boolean inlined = tryInlineInNodeRecord(this.node, newLabelIds, this.node.getDynamicLabelRecords());

        assert inlined;

        return Collections.emptyList();
    }

    //DynamicGraph
    //*************************************************************************


/*    @Override
    public Collection<DynamicRecord> put(long[] var1, NodeStore var2, DynamicRecordAllocator var3, Map<Long, Long> versionMap) {
        return null;
    }*/

    @Override
    public Collection<DynamicRecord> add(long labelId, NodeStore nodeStore, DynamicRecordAllocator allocator, long version) {


        long[] augmentedLabelIds = labelCount(this.node.getLabelField()) == 0 ? new long[]{labelId} : LabelIdArray.concatAndSort(parseInlined(this.node.getLabelField()), labelId);
        //putSorted(this.node, augmentedLabelIds, nodeStore, allocator);
        this.node.getVersionLabels().put(labelId, version);
        byte [] data = Serialization.writeMapToByteArray(this.node.getVersionLabels());
        Collection<DynamicRecord> changedDynamicRecords = new ArrayList();
        nodeStore.getDynamicLabelStore().allocateRecordsFromBytes(changedDynamicRecords,data);
        this.node.setLabelField(DynamicNodeLabels.dynamicPointer(changedDynamicRecords), changedDynamicRecords);

        //nodeStore.ensureHeavyEx(this.node, NodeLabelsField.firstDynamicLabelRecordId(this.node.getLabelField()));
        //this.node.getVersionLabels().put(labelId, version);
        //long[] existingLabelIds = getDynamicLabelsArray(this.node.getUsedDynamicLabelRecords(), nodeStore.getDynamicLabelStore());
        //long[] newLabelIds = LabelIdArray.concatAndSort(existingLabelIds, labelId);
        //Collection<DynamicRecord> existingRecords = this.node.getDynamicLabelRecords();
        //Collection<DynamicRecord> changedDynamicRecords = allocateRecordsForDynamicLabels(this.node.getId(), newLabelIds, (DynamicRecordAllocator)(new ReusableRecordsCompositeAllocator(existingRecords, allocator)));
        //Collection<DynamicRecord> changedDynamicRecords = new ArrayList();
       // byte [] data = Serialization.writeMapToByteArray(this.node.getVersionLabels());
       // nodeStore.getDynamicLabelStore().allocateRecordsFromBytes(changedDynamicRecords,data);
       // this.node.setLabelField(dynamicPointer(changedDynamicRecords), changedDynamicRecords);
        return changedDynamicRecords;
    }

    @Override
    public Collection<DynamicRecord> remove(long var1, NodeStore var3, long version) {
        return null;
    }


    //DynamicGraph
    //*************************************************************************


    static boolean tryInlineInNodeRecord(NodeRecord node, long[] ids, Collection<DynamicRecord> changedDynamicRecords) {
        if (ids.length > 7) {
            return false;
        } else {
            byte bitsPerLabel = (byte)(ids.length > 0 ? 36 / ids.length : 36);
            Bits bits = Bits.bits(5);
            if (!inlineValues(ids, bitsPerLabel, bits)) {
                return false;
            } else {
                node.setLabelField(combineLabelCountAndLabelStorage((byte)ids.length, bits.getLongs()[0]), changedDynamicRecords);
                return true;
            }
        }
    }

    private static boolean inlineValues(long[] values, int maxBitsPerLabel, Bits target) {
        long limit = 1L << maxBitsPerLabel;
        long[] var5 = values;
        int var6 = values.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            long value = var5[var7];
            if (Long.highestOneBit(value) >= limit) {
                return false;
            }

            target.put(value, maxBitsPerLabel);
        }

        return true;
    }

    public static long[] parseInlined(long labelField) {
        byte numberOfLabels = labelCount(labelField);
        if (numberOfLabels == 0) {
            return PrimitiveLongCollections.EMPTY_LONG_ARRAY;
        } else {
            long existingLabelsField = NodeLabelsField.parseLabelsBody(labelField);
            byte bitsPerLabel = (byte)(36 / numberOfLabels);
            Bits bits = Bits.bitsFromLongs(new long[]{existingLabelsField});
            long[] result = new long[numberOfLabels];

            for(int i = 0; i < result.length; ++i) {
                result[i] = bits.getLong(bitsPerLabel);
            }

            return result;
        }
    }

    private static long combineLabelCountAndLabelStorage(byte labelCount, long labelBits) {
        return (long)labelCount << 36 | labelBits;
    }

    private static byte labelCount(long labelField) {
        return (byte)((int)((labelField & 1030792151040L) >>> 36));
    }

    public boolean isInlined() {
        return true;
    }

    public String toString() {
        return String.format("Inline(0x%x:%s)", this.node.getLabelField(), Arrays.toString(this.getIfLoaded()));
    }
}
