//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.format.standard;

//import DynamicGraph.store.Node.NodeVersionRecordFormat;
import org.neo4j.kernel.impl.store.format.BaseRecordFormats;
import org.neo4j.kernel.impl.store.format.Capability;
import org.neo4j.kernel.impl.store.format.FormatFamily;
import org.neo4j.kernel.impl.store.format.RecordFormat;
import org.neo4j.kernel.impl.store.format.RecordFormats;
import org.neo4j.kernel.impl.store.format.StoreVersion;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.LabelTokenRecord;
import org.neo4j.kernel.impl.store.record.NodeRecord;
import org.neo4j.kernel.impl.store.record.PropertyKeyTokenRecord;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.kernel.impl.store.record.RelationshipGroupRecord;
import org.neo4j.kernel.impl.store.record.RelationshipRecord;
import org.neo4j.kernel.impl.store.record.RelationshipTypeTokenRecord;

public class StandardV3_4 extends BaseRecordFormats {
    public static final String STORE_VERSION;
    public static final RecordFormats RECORD_FORMATS;
    public static final String NAME = "standard";

    public StandardV3_4() {
        super(STORE_VERSION, StoreVersion.STANDARD_V3_4.introductionVersion(), 8, new Capability[]{Capability.SCHEMA, Capability.DENSE_NODES, Capability.LUCENE_5, Capability.POINT_PROPERTIES, Capability.TEMPORAL_PROPERTIES});
    }
    //nodeRecordFormat-> DynamicNodeRecordFormat

    public RecordFormat<NodeRecord> node() {
        return new NodeRecordFormat();
    }

    public RecordFormat<RelationshipGroupRecord> relationshipGroup() {
        return new RelationshipGroupRecordFormat();
    }

    public RecordFormat<RelationshipRecord> relationship() {
        return new RelationshipRecordFormat();
    }

    public RecordFormat<PropertyRecord> property() {
        return new PropertyRecordFormat();
    }

    public RecordFormat<LabelTokenRecord> labelToken() {
        return new LabelTokenRecordFormat();
    }

    public RecordFormat<PropertyKeyTokenRecord> propertyKeyToken() {
        return new PropertyKeyTokenRecordFormat();
    }

    public RecordFormat<RelationshipTypeTokenRecord> relationshipTypeToken() {
        return new RelationshipTypeTokenRecordFormat();
    }

    public RecordFormat<DynamicRecord> dynamic() {
        return new DynamicRecordFormat();
    }

    public FormatFamily getFormatFamily() {
        return StandardFormatFamily.INSTANCE;
    }

    public String name() {
        return "standard";
    }

    static {
        STORE_VERSION = StoreVersion.STANDARD_V3_4.versionString();
        RECORD_FORMATS = new StandardV3_4();
    }
}
