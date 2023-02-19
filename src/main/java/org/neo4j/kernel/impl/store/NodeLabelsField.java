//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store;

import org.neo4j.kernel.impl.store.record.NodeRecord;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
public class NodeLabelsField {
    private NodeLabelsField() {
    }


    //DynamicGraph
    //*******************************************

    //去掉内联label的处理
    public static NodeLabels parseLabelsField(NodeRecord node) {
        long labelField = node.getLabelField();
        return new DynamicNodeLabels(node);
        //return (NodeLabels)(fieldPointsToDynamicRecordOfLabels(labelField) ? new DynamicNodeLabels(node) : new InlineNodeLabels(node));
    }


    public static long[] get(NodeRecord node, NodeStore nodeStore) {
        return DynamicNodeLabels.get(node,nodeStore);
        //return fieldPointsToDynamicRecordOfLabels(node.getLabelField()) ? DynamicNodeLabels.get(node, nodeStore) : InlineNodeLabels.get(node);
    }

    public static long[] get(NodeRecord node, NodeStore nodeStore,long version) {

        //Map<Long,Long> newSets = (Map<Long, Long>) getVersionLabels(node,nodeStore).entrySet().stream().filter(p -> p.getValue() == version).collect();
        //return getVersionLabels(node,nodeStore).entrySet().stream().filter(p -> p.getValue() == version).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
        //return ;
        return fieldPointsToDynamicRecordOfLabels(node.getLabelField()) ? DynamicNodeLabels.get(node, nodeStore) : InlineNodeLabels.get(node);
    }
    public static Map<Long,Long> getVersionLabels(NodeRecord node, NodeStore nodeStore) {
        return DynamicNodeLabels.getVersionLabels(node,nodeStore);
        //return fieldPointsToDynamicRecordOfLabels(node.getLabelField()) ? DynamicNodeLabels.get(node, nodeStore) : InlineNodeLabels.get(node);
    }
    //DynamicGraph
    //*******************************************


    public static boolean fieldPointsToDynamicRecordOfLabels(long labelField) {
        return (labelField & 549755813888L) != 0L;
        //return true;
    }

    public static long parseLabelsBody(long labelField) {
        return labelField & 68719476735L;
    }

    public static long firstDynamicLabelRecordId(long labelField) {
        if(!fieldPointsToDynamicRecordOfLabels(labelField)) return 0;
        assert fieldPointsToDynamicRecordOfLabels(labelField);

        return parseLabelsBody(labelField);
    }

    public static boolean isSane(long[] labelIds) {
        long prev = -1L;
        long[] var3 = labelIds;
        int var4 = labelIds.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            long labelId = var3[var5];
            if (labelId <= prev) {
                return false;
            }

            prev = labelId;
        }

        return true;
    }
}
