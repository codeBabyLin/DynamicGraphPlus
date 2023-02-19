//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.record;

public abstract class PrimitiveRecord extends AbstractBaseRecord {
    protected long nextProp;

    public PrimitiveRecord(long id) {
        super(id);
    }

    /** @deprecated */
    @Deprecated
    public PrimitiveRecord(long id, long nextProp) {
        super(id);
        this.nextProp = nextProp;
    }

    public void clear() {
        super.clear();
        this.nextProp = (long)Record.NO_NEXT_PROPERTY.intValue();
    }

    protected PrimitiveRecord initialize(boolean inUse, long nextProp) {
        super.initialize(inUse);
        this.nextProp = nextProp;
        return this;
    }

    public long getNextProp() {
        return this.nextProp;
    }

    public void setNextProp(long nextProp) {
        this.nextProp = nextProp;
    }

    public abstract void setIdTo(PropertyRecord var1);
}
