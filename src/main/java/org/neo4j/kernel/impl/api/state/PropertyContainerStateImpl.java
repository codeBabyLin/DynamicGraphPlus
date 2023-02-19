//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.api.state;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.kernel.api.properties.PropertyKeyValue;
import org.neo4j.kernel.impl.util.collection.CollectionsFactory;
import org.neo4j.storageengine.api.StorageProperty;
import org.neo4j.storageengine.api.txstate.PropertyContainerState;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

class PropertyContainerStateImpl implements PropertyContainerState {
    private final long id;
    private MutableLongObjectMap<Value> addedProperties;
    private MutableLongObjectMap<Long> addedPropertiesWithVersions;
    private MutableLongObjectMap<Value> changedProperties;
    private MutableLongSet removedProperties;
    protected final CollectionsFactory collectionsFactory;

    PropertyContainerStateImpl(long id, CollectionsFactory collectionsFactory) {
        this.id = id;
        this.collectionsFactory = (CollectionsFactory)Objects.requireNonNull(collectionsFactory);
    }

    public long getId() {
        return this.id;
    }

    void clear() {
        if (this.changedProperties != null) {
            this.changedProperties.clear();
        }

        if (this.addedProperties != null) {
            this.addedProperties.clear();
        }

        if (this.removedProperties != null) {
            this.removedProperties.clear();
        }

    }


    //Dynamicgraph
    void changeProperty(int propertyKeyId, Value value,long version) {
        if(this.addedPropertiesWithVersions == null){
            this.addedPropertiesWithVersions = new LongObjectHashMap<Long>();
        }
        if (this.addedProperties != null && this.addedProperties.containsKey((long)propertyKeyId)) {
            this.addedProperties.put((long)propertyKeyId, value);
            this.addedPropertiesWithVersions.put((long)propertyKeyId, version);
        } else {
            if (this.changedProperties == null) {
                this.changedProperties = this.collectionsFactory.newValuesMap();
            }

            this.changedProperties.put((long)propertyKeyId, value);
            this.addedPropertiesWithVersions.put((long)propertyKeyId,version);
            if (this.removedProperties != null) {
                this.removedProperties.remove((long)propertyKeyId);
            }

        }
    }

    void addProperty(int propertyKeyId, Value value,long version) {
        if(this.addedPropertiesWithVersions == null){
            this.addedPropertiesWithVersions = new LongObjectHashMap<Long>();
        }
        if (this.removedProperties != null && this.removedProperties.remove((long)propertyKeyId)) {
            this.changeProperty(propertyKeyId, value);
        } else {
            if (this.addedProperties == null) {
                this.addedProperties = this.collectionsFactory.newValuesMap();
            }

            this.addedProperties.put((long)propertyKeyId, value);
            this.addedPropertiesWithVersions.put((long)propertyKeyId,version);
        }
    }

    void removeProperty(int propertyKeyId,long version) {
        if(this.addedPropertiesWithVersions == null){
            this.addedPropertiesWithVersions = new LongObjectHashMap<Long>();
        }
        this.addedPropertiesWithVersions.put((long)propertyKeyId,version);
        if (this.addedProperties == null || this.addedProperties.remove((long)propertyKeyId) == null) {
            if (this.removedProperties == null) {
                this.removedProperties = this.collectionsFactory.newLongSet();
            }

            this.removedProperties.add((long)propertyKeyId);
            if (this.changedProperties != null) {
                this.changedProperties.remove((long)propertyKeyId);
            }

        }
    }

    //Dynamicgraph





    void changeProperty(int propertyKeyId, Value value) {
        if (this.addedProperties != null && this.addedProperties.containsKey((long)propertyKeyId)) {
            this.addedProperties.put((long)propertyKeyId, value);
        } else {
            if (this.changedProperties == null) {
                this.changedProperties = this.collectionsFactory.newValuesMap();
            }

            this.changedProperties.put((long)propertyKeyId, value);
            if (this.removedProperties != null) {
                this.removedProperties.remove((long)propertyKeyId);
            }

        }
    }

    void addProperty(int propertyKeyId, Value value) {
        if (this.removedProperties != null && this.removedProperties.remove((long)propertyKeyId)) {
            this.changeProperty(propertyKeyId, value);
        } else {
            if (this.addedProperties == null) {
                this.addedProperties = this.collectionsFactory.newValuesMap();
            }

            this.addedProperties.put((long)propertyKeyId, value);
        }
    }

    void removeProperty(int propertyKeyId) {
        if (this.addedProperties == null || this.addedProperties.remove((long)propertyKeyId) == null) {
            if (this.removedProperties == null) {
                this.removedProperties = this.collectionsFactory.newLongSet();
            }

            this.removedProperties.add((long)propertyKeyId);
            if (this.changedProperties != null) {
                this.changedProperties.remove((long)propertyKeyId);
            }

        }
    }

    public Iterator<StorageProperty> addedProperties() {
        return this.toPropertyIterator(this.addedProperties);
    }

    public Iterator<StorageProperty> changedProperties() {
        return this.toPropertyIterator(this.changedProperties);
    }
    public MutableLongObjectMap<Long> PropertiesWithVersions() {
       return this.addedPropertiesWithVersions;
    }

    public IntIterable removedProperties() {
        return (IntIterable)(this.removedProperties == null ? IntSets.immutable.empty() : this.removedProperties.asLazy().collectInt(Math::toIntExact));
    }

    public Iterator<StorageProperty> addedAndChangedProperties() {
        if (this.addedProperties == null) {
            return this.toPropertyIterator(this.changedProperties);
        } else {
            return this.changedProperties == null ? this.toPropertyIterator(this.addedProperties) : Iterators.concat(new Iterator[]{this.toPropertyIterator(this.addedProperties), this.toPropertyIterator(this.changedProperties)});
        }
    }

    public boolean hasPropertyChanges() {
        return this.addedProperties != null || this.removedProperties != null || this.changedProperties != null;
    }

    public boolean isPropertyChangedOrRemoved(int propertyKey) {
        return this.removedProperties != null && this.removedProperties.contains((long)propertyKey) || this.changedProperties != null && this.changedProperties.containsKey((long)propertyKey);
    }

    public Value propertyValue(int propertyKey) {
        if (this.removedProperties != null && this.removedProperties.contains((long)propertyKey)) {
            return Values.NO_VALUE;
        } else {
            if (this.addedProperties != null) {
                Value addedValue = (Value)this.addedProperties.get((long)propertyKey);
                if (addedValue != null) {
                    return addedValue;
                }
            }

            return this.changedProperties != null ? (Value)this.changedProperties.get((long)propertyKey) : null;
        }
    }

    private Iterator<StorageProperty> toPropertyIterator(LongObjectMap<Value> propertyMap) {
        return propertyMap == null ? Collections.emptyIterator() : propertyMap.keyValuesView().collect((e) -> {
            return (StorageProperty)new PropertyKeyValue(Math.toIntExact(e.getOne()), (Value)e.getTwo());
        }).iterator();
    }
}
