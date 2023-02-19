//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.store.record;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import cn.DynamicGraph.Common.DGVersion;
import org.neo4j.kernel.impl.store.PropertyType;

public class PropertyRecord extends AbstractBaseRecord implements Iterable<PropertyBlock> {
    private static final byte TYPE_NODE = 1;
    private static final byte TYPE_REL = 2;
    private long nextProp;
    private long prevProp;
    //DynamicGraph
    private long version;
    private long nextHisProp = -1;
    private long prevHisProp = -1;
    //DynamicGraph
    private final long[] blocks = new long[PropertyType.getPayloadSizeLongs()];
    private int blocksCursor;
    private final PropertyBlock[] blockRecords = new PropertyBlock[PropertyType.getPayloadSizeLongs()];
    private boolean blocksLoaded;
    private int blockRecordsCursor;
    private long entityId;
    private byte entityType;
    private List<DynamicRecord> deletedRecords;

    public PropertyRecord(long id) {
        super(id);
    }

    public PropertyRecord(long id, PrimitiveRecord primitive) {
        super(id);
        primitive.setIdTo(this);
    }

    public PropertyRecord initialize(boolean inUse, long prevProp, long nextProp) {
        super.initialize(inUse);
        this.prevProp = prevProp;
        this.nextProp = nextProp;
        this.nextHisProp = Record.NO_NEXT_PROPERTY.intValue();
        this.prevHisProp = Record.NO_PREVIOUS_PROPERTY.intValue();
        this.deletedRecords = null;
        this.blockRecordsCursor = this.blocksCursor = 0;
        this.blocksLoaded = false;
        return this;
    }

    //DynamicGraph
    public PropertyRecord initialize(boolean inUse, long prevProp, long nextProp,long version, long prevHisProp, long nextHisProp) {
        super.initialize(inUse);
        this.prevProp = prevProp;
        this.nextProp = nextProp;
        this.deletedRecords = null;
        this.blockRecordsCursor = this.blocksCursor = 0;
        this.blocksLoaded = false;
        this.version = version;
        this.prevHisProp = prevHisProp;
        this.nextHisProp = nextHisProp;
        return this;
    }

    //DynamicGraph
    public void clear() {
        super.initialize(false);
        this.entityId = -1L;
        this.entityType = 0;
        this.prevProp = (long)Record.NO_PREVIOUS_PROPERTY.intValue();
        this.nextProp = (long)Record.NO_NEXT_PROPERTY.intValue();
        this.deletedRecords = null;
        this.blockRecordsCursor = this.blocksCursor = 0;
        this.blocksLoaded = false;
    }

    //DynamicGraph
    public void setVersion(long version){
        this.version = version;
    }
    public void setNextHisProp(long nextHisProp){
        this.nextHisProp = nextHisProp;
    }
    public void setPrevHisProp(long prevHisProp){
        this.prevHisProp = prevHisProp;
    }

    public long getVersion() {
        return this.version;
    }

    public long getNextHisProp() {
        return this.nextHisProp;
    }

    public long getPrevHisProp() {
        return this.prevHisProp;
    }

    //DynamicGraph

    public void setNodeId(long nodeId) {
        this.entityType = 1;
        this.entityId = nodeId;
    }

    public void setRelId(long relId) {
        this.entityType = 2;
        this.entityId = relId;
    }

    public boolean isNodeSet() {
        return this.entityType == 1;
    }

    public boolean isRelSet() {
        return this.entityType == 2;
    }

    public long getNodeId() {
        return this.isNodeSet() ? this.entityId : -1L;
    }

    public long getRelId() {
        return this.isRelSet() ? this.entityId : -1L;
    }

    public long getEntityId() {
        return this.entityId;
    }

    public int size() {
        this.ensureBlocksLoaded();
        int result = 0;

        for(int i = 0; i < this.blockRecordsCursor; ++i) {
            result += this.blockRecords[i].getSize();
        }

        return result;
    }

    public int numberOfProperties() {
        this.ensureBlocksLoaded();
        return this.blockRecordsCursor;
    }

    public Iterator<PropertyBlock> iterator() {
        this.ensureBlocksLoaded();
        return new Iterator<PropertyBlock>() {
            private int blockRecordsIteratorCursor;
            private boolean canRemoveFromIterator;

            public boolean hasNext() {
                return this.blockRecordsIteratorCursor < PropertyRecord.this.blockRecordsCursor;
            }

            public PropertyBlock next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    this.canRemoveFromIterator = true;
                    return PropertyRecord.this.blockRecords[this.blockRecordsIteratorCursor++];
                }
            }

            public void remove() {
                if (!this.canRemoveFromIterator) {
                    throw new IllegalStateException("cursor:" + this.blockRecordsIteratorCursor + " canRemove:" + this.canRemoveFromIterator);
                } else {
                    if (--PropertyRecord.this.blockRecordsCursor > --this.blockRecordsIteratorCursor) {
                        PropertyRecord.this.blockRecords[this.blockRecordsIteratorCursor] = PropertyRecord.this.blockRecords[PropertyRecord.this.blockRecordsCursor];
                    }

                    this.canRemoveFromIterator = false;
                }
            }
        };
    }

    public List<DynamicRecord> getDeletedRecords() {
        return this.deletedRecords != null ? this.deletedRecords : Collections.emptyList();
    }

    public void addDeletedRecord(DynamicRecord record) {
        assert !record.inUse();

        if (this.deletedRecords == null) {
            this.deletedRecords = new LinkedList();
        }

        this.deletedRecords.add(record);
    }

    public void addPropertyBlock(PropertyBlock block) {
        this.ensureBlocksLoaded();

        assert this.size() + block.getSize() <= PropertyType.getPayloadSize() : "Exceeded capacity of property record " + this + ". My current size is reported as " + this.size() + "The added block was " + block + " (note that size is " + block.getSize() + ")";

        this.blockRecords[this.blockRecordsCursor++] = block;
    }

    private void ensureBlocksLoaded() {
        if (!this.blocksLoaded) {
            assert this.blockRecordsCursor == 0;

            int length;
            for(int index = 0; index < this.blocksCursor; index += length) {
                PropertyType type = PropertyType.getPropertyTypeOrThrow(this.blocks[index]);
                PropertyBlock block = new PropertyBlock();
                length = type.calculateNumberOfBlocksUsed(this.blocks[index]);
                block.setValueBlocks(Arrays.copyOfRange(this.blocks, index, index + length));
                this.blockRecords[this.blockRecordsCursor++] = block;
            }

            this.blocksLoaded = true;
        }

    }

    public void setPropertyBlock(PropertyBlock block) {
        this.removePropertyBlock(block.getKeyIndexId());
        this.addPropertyBlock(block);
    }

    public PropertyBlock getPropertyBlock(int keyIndex) {
        this.ensureBlocksLoaded();

        for(int i = 0; i < this.blockRecordsCursor; ++i) {
            PropertyBlock block = this.blockRecords[i];
            if (block.getKeyIndexId() == keyIndex) {
                return block;
            }
        }

        return null;
    }

    public PropertyBlock removePropertyBlock(int keyIndex) {
        this.ensureBlocksLoaded();

        for(int i = 0; i < this.blockRecordsCursor; ++i) {
            if (this.blockRecords[i].getKeyIndexId() == keyIndex) {
                PropertyBlock block = this.blockRecords[i];
                if (--this.blockRecordsCursor > i) {
                    this.blockRecords[i] = this.blockRecords[this.blockRecordsCursor];
                }

                return block;
            }
        }

        return null;
    }

    public void clearPropertyBlocks() {
        this.blockRecordsCursor = 0;
    }

    public long getNextProp() {
        return this.nextProp;
    }

    public void setNextProp(long nextProp) {
        this.nextProp = nextProp;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Property[").append(this.getId()).append(",used=").append(this.inUse()).append(",prev=").append(this.prevProp).append(",next=").append(this.nextProp);
        buf.append(",prev=").append(this.prevHisProp).append(",nextHis=").append(this.nextHisProp);
        buf.append(",startversion=").append(DGVersion.getStartVersion(this.version)).append(",endVersion=").append(DGVersion.getEndVersion(this.version));
        if (this.entityId != -1L) {
            buf.append(this.entityType == 1 ? ",node=" : ",rel=").append(this.entityId);
        }

        if (this.blocksLoaded) {
            for(int i = 0; i < this.blockRecordsCursor; ++i) {
                buf.append(',').append(this.blockRecords[i]);
            }
        } else {
            buf.append(", (blocks not loaded)");
        }

        if (this.deletedRecords != null) {
            Iterator var4 = this.deletedRecords.iterator();

            while(var4.hasNext()) {
                DynamicRecord dyn = (DynamicRecord)var4.next();
                buf.append(", del:").append(dyn);
            }
        }

        buf.append("]");
        return buf.toString();
    }

    public void setChanged(PrimitiveRecord primitive) {
        primitive.setIdTo(this);
    }

    public long getPrevProp() {
        return this.prevProp;
    }

    public void setPrevProp(long prev) {
        this.prevProp = prev;
    }

    public PropertyRecord clone() {
        PropertyRecord result = (PropertyRecord)(new PropertyRecord(this.getId())).initialize(this.inUse());
        result.nextProp = this.nextProp;
        result.prevProp = this.prevProp;
        result.entityId = this.entityId;
        result.entityType = this.entityType;
        System.arraycopy(this.blocks, 0, result.blocks, 0, this.blocks.length);
        result.blocksCursor = this.blocksCursor;

        for(int i = 0; i < this.blockRecordsCursor; ++i) {
            result.blockRecords[i] = this.blockRecords[i].clone();
        }

        result.blockRecordsCursor = this.blockRecordsCursor;
        result.blocksLoaded = this.blocksLoaded;
        if (this.deletedRecords != null) {
            Iterator var4 = this.deletedRecords.iterator();

            while(var4.hasNext()) {
                DynamicRecord deletedRecord = (DynamicRecord)var4.next();
                result.addDeletedRecord(deletedRecord.clone());
            }
        }

        return result;
    }

    public long[] getBlocks() {
        return this.blocks;
    }

    public void addLoadedBlock(long block) {
        assert this.blocksCursor + 1 <= this.blocks.length : "Capacity of " + this.blocks.length + " exceeded";

        this.blocks[this.blocksCursor++] = block;
    }

    public int getBlockCapacity() {
        return this.blocks.length;
    }

    public int getNumberOfBlocks() {
        return this.blocksCursor;
    }
}
