//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.newapi;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import cn.DynamicGraph.Common.DGVersion;
import cn.DynamicGraph.Common.Serialization;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.kernel.api.AssertOpen;
import org.neo4j.storageengine.api.StorageProperty;
import org.neo4j.storageengine.api.StoragePropertyCursor;
import org.neo4j.storageengine.api.txstate.PropertyContainerState;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueGroup;
import org.neo4j.values.storable.ValueWriter;
import org.neo4j.values.storable.Values;

public class DefaultPropertyCursor implements PropertyCursor {
    private Read read;
    private StoragePropertyCursor storeCursor;
    private PropertyContainerState propertiesState;
    private Iterator<StorageProperty> txStateChangedProperties;
    private StorageProperty txStateValue;
    private AssertOpen assertOpen;
    private final DefaultCursors pool;

    DefaultPropertyCursor(DefaultCursors pool, StoragePropertyCursor storeCursor) {
        this.pool = pool;
        this.storeCursor = storeCursor;
    }


    void initNode(long nodeReference, long reference, Read read, AssertOpen assertOpen) {
        assert nodeReference != -1L;

        this.init(reference, read, assertOpen);
        if (read.hasTxStateWithChanges()) {
            this.propertiesState = read.txState().getNodeState(nodeReference);
            this.txStateChangedProperties = this.propertiesState.addedAndChangedProperties();
        }

    }

    void initRelationship(long relationshipReference, long reference, Read read, AssertOpen assertOpen) {
        assert relationshipReference != -1L;

        this.init(reference, read, assertOpen);
        if (read.hasTxStateWithChanges()) {
            this.propertiesState = read.txState().getRelationshipState(relationshipReference);
            this.txStateChangedProperties = this.propertiesState.addedAndChangedProperties();
        }

    }

    void initGraph(long reference, Read read, AssertOpen assertOpen) {
        this.init(reference, read, assertOpen);
        if (read.hasTxStateWithChanges()) {
            this.propertiesState = read.txState().getGraphState();
            if (this.propertiesState != null) {
                this.txStateChangedProperties = this.propertiesState.addedAndChangedProperties();
            }
        }

    }

    private void init(long reference, Read read, AssertOpen assertOpen) {
        this.assertOpen = assertOpen;
        this.read = read;
        this.storeCursor.init(reference);
    }

    public boolean next() {
        boolean hasNext;
        do {
            hasNext = this.innerNext();
        } while(hasNext && !this.allowed(this.propertyKey()));

        return hasNext;
    }

    private boolean allowed(int propertyKey) {
        return this.read.ktx.securityContext().mode().allowsPropertyReads(propertyKey);
    }

    private boolean innerNextHistory() {
 /*       if (this.txStateChangedProperties != null) {
            if (this.txStateChangedProperties.hasNext()) {
                this.txStateValue = (StorageProperty)this.txStateChangedProperties.next();
                return true;
            }

            this.txStateChangedProperties = null;
            this.txStateValue = null;
        }*/

        boolean skip;
        do {
            if (!this.storeCursor.nextHistory()) {
                return false;
            }

            skip = this.propertiesState != null && this.propertiesState.isPropertyChangedOrRemoved(this.storeCursor.propertyKey());
        } while(skip);

        return true;
    }


    private boolean innerNext() {
        if (this.txStateChangedProperties != null) {
            if (this.txStateChangedProperties.hasNext()) {
                this.txStateValue = (StorageProperty)this.txStateChangedProperties.next();
                return true;
            }

            this.txStateChangedProperties = null;
            this.txStateValue = null;
        }

        boolean skip;
        do {
            if (!this.storeCursor.next()) {
                return false;
            }

            skip = this.propertiesState != null && this.propertiesState.isPropertyChangedOrRemoved(this.storeCursor.propertyKey());
        } while(skip);

        return true;
    }

    public void close() {
        if (!this.isClosed()) {
            this.propertiesState = null;
            this.txStateChangedProperties = null;
            this.txStateValue = null;
            this.read = null;
            this.storeCursor.reset();
            this.pool.accept(this);
        }

    }

    public int propertyKey() {
        return this.txStateValue != null ? this.txStateValue.propertyKeyId() : this.storeCursor.propertyKey();
    }

    public ValueGroup propertyType() {
        return this.txStateValue != null ? this.txStateValue.value().valueGroup() : this.storeCursor.propertyType();
    }

    @Override
    public boolean nextHistory() {
        boolean hasNext;
        do {
            hasNext = this.innerNextHistory();
        } while(hasNext && !this.allowed(this.propertyKey()));

        return hasNext;
    }



    //Dynamicgraph

    private Value getVersionValue(Map<Integer,Object> data,long version){
        int keyMax = -1;
        Iterator<Integer> it = data.keySet().iterator();
        while(it.hasNext()){
            int key = it.next();
            if(DGVersion.getStartVersion(key) == version) return Values.of(data.get(key));
        }
        return Values.NO_VALUE;
    }
    private Value getCurrentValue(Map<Integer,Object> data){
        int keyMax = -1;
        Iterator<Integer> it = data.keySet().iterator();
        while(it.hasNext()){
            int key = it.next();
            if(!DGVersion.hasEndVersion(key)) return Values.of(data.get(key));
        }
        return Values.NO_VALUE;
    }
    public Value propertyValue() {
        Value value = this.getValue();
        //Map<Integer,Object> data = Serialization.readJMapFromObject(value.asObjectCopy());
        return value;
    }
    private Value getValue(){
        if (this.txStateValue != null) {
            return this.txStateValue.value();
        } else {
            Value value = this.storeCursor.propertyValue();
            this.assertOpen.assertOpen();
            return value;
        }
    }

    @Override
    public Value propertyValue(long version) {
       Value value = this.getValue();
        Map<Integer,Object> data = Serialization.readJMapFromObject(value.asObjectCopy());
        return getVersionValue(data,version);
        //return null;
    }

    @Override
    public long propertyVersion() {
       return this.storeCursor.propertyVersion();
    }

    @Override
    public Value propertyValue(long version, boolean isReal) {
        if(isReal){
            return this.getValue();
        }
        else{
            return this.propertyValue(version);
        }
    }

    //DynamicGraph


    public <E extends Exception> void writeTo(ValueWriter<E> target) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean booleanValue() {
        throw new UnsupportedOperationException("not implemented");
    }

    public String stringValue() {
        throw new UnsupportedOperationException("not implemented");
    }

    public long longValue() {
        throw new UnsupportedOperationException("not implemented");
    }

    public double doubleValue() {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueEqualTo(long value) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueEqualTo(double value) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueEqualTo(String value) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueMatches(Pattern regex) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueGreaterThan(long number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueGreaterThan(double number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueLessThan(long number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueLessThan(double number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueGreaterThanOrEqualTo(long number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueGreaterThanOrEqualTo(double number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueLessThanOrEqualTo(long number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean valueLessThanOrEqualTo(double number) {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean isClosed() {
        return this.read == null;
    }

    public String toString() {
        return this.isClosed() ? "PropertyCursor[closed state]" : "PropertyCursor[id=" + this.propertyKey() + ", " + this.storeCursor.toString() + " ]";
    }

    public void release() {
        this.storeCursor.close();
    }
}
