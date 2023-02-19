//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.record;

import java.util.Objects;

public class RelationshipRecord extends PrimitiveRecord {
    private long firstNode;
    private long secondNode;
    private int type;
    private long firstPrevRel;
    private long firstNextRel;
    private long secondPrevRel;
    private long secondNextRel;
    private boolean firstInFirstChain;
    private boolean firstInSecondChain;
    private long version;

    /** @deprecated */
    @Deprecated
    public RelationshipRecord(long id, long firstNode, long secondNode, int type) {
        this(id);
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.type = type;
    }

    /** @deprecated */
    @Deprecated
    public RelationshipRecord(long id, boolean inUse, long firstNode, long secondNode, int type, long firstPrevRel, long firstNextRel, long secondPrevRel, long secondNextRel, boolean firstInFirstChain, boolean firstInSecondChain) {
        this(id, firstNode, secondNode, type);
        this.setInUse(inUse);
        this.firstPrevRel = firstPrevRel;
        this.firstNextRel = firstNextRel;
        this.secondPrevRel = secondPrevRel;
        this.secondNextRel = secondNextRel;
        this.firstInFirstChain = firstInFirstChain;
        this.firstInSecondChain = firstInSecondChain;
    }

    public RelationshipRecord(long id) {
        super(id);
    }

    public RelationshipRecord initialize(boolean inUse, long nextProp, long firstNode, long secondNode, int type, long firstPrevRel, long firstNextRel, long secondPrevRel, long secondNextRel, boolean firstInFirstChain, boolean firstInSecondChain) {
        super.initialize(inUse, nextProp);
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.type = type;
        this.firstPrevRel = firstPrevRel;
        this.firstNextRel = firstNextRel;
        this.secondPrevRel = secondPrevRel;
        this.secondNextRel = secondNextRel;
        this.firstInFirstChain = firstInFirstChain;
        this.firstInSecondChain = firstInSecondChain;
        return this;
    }
    public RelationshipRecord initialize(boolean inUse, long nextProp, long firstNode, long secondNode, int type, long firstPrevRel, long firstNextRel, long secondPrevRel, long secondNextRel, boolean firstInFirstChain, boolean firstInSecondChain,long version) {
        super.initialize(inUse, nextProp);
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.type = type;
        this.firstPrevRel = firstPrevRel;
        this.firstNextRel = firstNextRel;
        this.secondPrevRel = secondPrevRel;
        this.secondNextRel = secondNextRel;
        this.firstInFirstChain = firstInFirstChain;
        this.firstInSecondChain = firstInSecondChain;
        this.version = version;
        return this;
    }



    public void clear() {
        this.initialize(false, (long)Record.NO_NEXT_PROPERTY.intValue(), -1L, -1L, -1, 1L, (long)Record.NO_NEXT_RELATIONSHIP.intValue(), 1L, (long)Record.NO_NEXT_RELATIONSHIP.intValue(), true, true);
    }

    public void setLinks(long firstNode, long secondNode, int type) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.type = type;
    }

    public long getFirstNode() {
        return this.firstNode;
    }

    public void setFirstNode(long firstNode) {
        this.firstNode = firstNode;
    }


    public long getVersion() {
        return this.version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getSecondNode() {
        return this.secondNode;
    }

    public void setSecondNode(long secondNode) {
        this.secondNode = secondNode;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getFirstPrevRel() {
        return this.firstPrevRel;
    }

    public void setFirstPrevRel(long firstPrevRel) {
        this.firstPrevRel = firstPrevRel;
    }

    public long getFirstNextRel() {
        return this.firstNextRel;
    }

    public void setFirstNextRel(long firstNextRel) {
        this.firstNextRel = firstNextRel;
    }

    public long getSecondPrevRel() {
        return this.secondPrevRel;
    }

    public void setSecondPrevRel(long secondPrevRel) {
        this.secondPrevRel = secondPrevRel;
    }

    public long getSecondNextRel() {
        return this.secondNextRel;
    }

    public void setSecondNextRel(long secondNextRel) {
        this.secondNextRel = secondNextRel;
    }

    public boolean isFirstInFirstChain() {
        return this.firstInFirstChain;
    }

    public void setFirstInFirstChain(boolean firstInFirstChain) {
        this.firstInFirstChain = firstInFirstChain;
    }

    public boolean isFirstInSecondChain() {
        return this.firstInSecondChain;
    }

    public void setFirstInSecondChain(boolean firstInSecondChain) {
        this.firstInSecondChain = firstInSecondChain;
    }

    public String toString() {
        return "Relationship[" + this.getId() + ",used=" + this.inUse() + ",source=" + this.firstNode + ",target=" + this.secondNode + ",type=" + this.type + (this.firstInFirstChain ? ",sCount=" : ",sPrev=") + this.firstPrevRel + ",sNext=" + this.firstNextRel + (this.firstInSecondChain ? ",tCount=" : ",tPrev=") + this.secondPrevRel + ",tNext=" + this.secondNextRel + ",prop=" + this.getNextProp() + ",secondaryUnitId=" + this.getSecondaryUnitId() + (this.firstInFirstChain ? ", sFirst" : ",!sFirst") + (this.firstInSecondChain ? ", tFirst" : ",!tFirst") + "]";
    }

    public RelationshipRecord clone() {
        RelationshipRecord record = (new RelationshipRecord(this.getId())).initialize(this.inUse(), this.nextProp, this.firstNode, this.secondNode, this.type, this.firstPrevRel, this.firstNextRel, this.secondPrevRel, this.secondNextRel, this.firstInFirstChain, this.firstInSecondChain);
        record.setSecondaryUnitId(this.getSecondaryUnitId());
        return record;
    }

    public void setIdTo(PropertyRecord property) {
        property.setRelId(this.getId());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            if (!super.equals(o)) {
                return false;
            } else {
                RelationshipRecord that = (RelationshipRecord)o;
                return this.firstNode == that.firstNode && this.secondNode == that.secondNode && this.type == that.type && this.firstPrevRel == that.firstPrevRel && this.firstNextRel == that.firstNextRel && this.secondPrevRel == that.secondPrevRel && this.secondNextRel == that.secondNextRel && this.firstInFirstChain == that.firstInFirstChain && this.firstInSecondChain == that.firstInSecondChain;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{super.hashCode(), this.firstNode, this.secondNode, this.type, this.firstPrevRel, this.firstNextRel, this.secondPrevRel, this.secondNextRel, this.firstInFirstChain, this.firstInSecondChain});
    }
}
