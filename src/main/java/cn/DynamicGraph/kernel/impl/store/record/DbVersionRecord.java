package cn.DynamicGraph.kernel.impl.store.record;

import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;

public class DbVersionRecord extends AbstractBaseRecord {
    private long value;
    private long nodeCounts = -1;
    private long relCounts = -1;

    public DbVersionRecord(long id) {
        super(id);
    }
    public DbVersionRecord initialize(boolean inUse, long value) {
        super.initialize(inUse);
        this.value = value;
        return this;
    }
    public DbVersionRecord initialize(boolean inUse, long value, long nodeCounts, long relCounts) {
        super.initialize(inUse);
        this.value = value;
        this.nodeCounts = nodeCounts;
        this.relCounts = relCounts;
        return this;
    }

    @Override
    public void setInUse(boolean inUse) {
        super.setInUse(inUse);
    }

    public void clear() {
        this.initialize(false, -1L);
    }

    public long getNodeCounts() {
        return this.nodeCounts;
    }
    public long getRelCounts() {
        return this.relCounts;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void setNodeCounts(long nodeCounts) {
        this.nodeCounts = nodeCounts;
    }

    public void setRelCounts(long relCounts) {
        this.relCounts = relCounts;
    }
}
