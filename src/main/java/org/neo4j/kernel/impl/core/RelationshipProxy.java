//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.core;

import java.util.*;

import cn.DynamicGraph.Common.DGVersion;
import cn.DynamicGraph.Common.Serialization;
import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.DatabaseShutdownException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.NotInTransactionException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.RelationshipScanCursor;
import org.neo4j.internal.kernel.api.TokenRead;
import org.neo4j.internal.kernel.api.exceptions.EntityNotFoundException;
import org.neo4j.internal.kernel.api.exceptions.InvalidTransactionTypeKernelException;
import org.neo4j.internal.kernel.api.exceptions.PropertyKeyIdNotFoundKernelException;
import org.neo4j.internal.kernel.api.exceptions.explicitindex.AutoIndexingKernelException;
import org.neo4j.internal.kernel.api.exceptions.schema.IllegalTokenNameException;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.Statement;
import org.neo4j.storageengine.api.EntityType;
import org.neo4j.storageengine.api.RelationshipVisitor;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

public class RelationshipProxy implements Relationship, RelationshipVisitor<RuntimeException> {
    private final EmbeddedProxySPI spi;
    private long id = -1L;
    private long startNode = -1L;
    private long endNode = -1L;
    private int type;

    public RelationshipProxy(EmbeddedProxySPI spi, long id, long startNode, int type, long endNode) {
        this.spi = spi;
        this.visit(id, type, startNode, endNode);
    }

    public RelationshipProxy(EmbeddedProxySPI spi, long id) {
        this.spi = spi;
        this.id = id;
    }

    public final void visit(long id, int type, long startNode, long endNode) throws RuntimeException {
        this.id = id;
        this.type = type;
        this.startNode = startNode;
        this.endNode = endNode;
    }

    public boolean initializeData() {
        if (this.startNode == -1L) {
            KernelTransaction transaction = this.spi.kernelTransaction();
            Statement ignore = transaction.acquireStatement();
            Throwable var3 = null;

            boolean var6;
            try {
                RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
                transaction.dataRead().singleRelationship(this.id, relationships);
                boolean wasPresent = relationships.next();
                this.type = relationships.type();
                this.startNode = relationships.sourceNodeReference();
                this.endNode = relationships.targetNodeReference();
                var6 = wasPresent;
            } catch (Throwable var15) {
                var3 = var15;
                throw var15;
            } finally {
                if (ignore != null) {
                    if (var3 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var14) {
                            var3.addSuppressed(var14);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var6;
        } else {
            return true;
        }
    }

    @Override
    public long getRelVersion() {
        KernelTransaction transaction = this.spi.kernelTransaction();
        Statement ignore = transaction.acquireStatement();
        RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
        this.singleRelationship(transaction,relationships);
        return relationships.relVersion();
    }

    public long getId() {
        return this.id;
    }

    private int typeId() {
        this.initializeData();
        return this.type;
    }

    private long sourceId() {
        this.initializeData();
        return this.startNode;
    }

    private long targetId() {
        this.initializeData();
        return this.endNode;
    }

    public GraphDatabaseService getGraphDatabase() {
        return this.spi.getGraphDatabase();
    }

    public void delete() {
        KernelTransaction transaction = this.spi.kernelTransaction();

        try {
            boolean deleted = transaction.dataWrite().relationshipDelete(this.id);
            if (!deleted) {
                throw new NotFoundException("Unable to delete relationship[" + this.getId() + "] since it is already deleted.");
            }
        } catch (InvalidTransactionTypeKernelException var3) {
            throw new ConstraintViolationException(var3.getMessage(), var3);
        } catch (AutoIndexingKernelException var4) {
            throw new IllegalStateException("Auto indexing encountered a failure while deleting the relationship: " + var4.getMessage(), var4);
        }
    }

    public Node[] getNodes() {
        this.spi.assertInUnterminatedTransaction();
        return new Node[]{this.spi.newNodeProxy(this.sourceId()), this.spi.newNodeProxy(this.targetId())};
    }

    public Node getOtherNode(Node node) {
        this.spi.assertInUnterminatedTransaction();
        return this.spi.newNodeProxy(this.getOtherNodeId(node.getId()));
    }

    public Node getStartNode() {
        this.spi.assertInUnterminatedTransaction();
        return this.spi.newNodeProxy(this.sourceId());
    }

    public Node getEndNode() {
        this.spi.assertInUnterminatedTransaction();
        return this.spi.newNodeProxy(this.targetId());
    }

    public long getStartNodeId() {
        return this.sourceId();
    }

    public long getEndNodeId() {
        return this.targetId();
    }

    public long getOtherNodeId(long id) {
        long start = this.sourceId();
        long end = this.targetId();
        if (start == id) {
            return end;
        } else if (end == id) {
            return start;
        } else {
            throw new NotFoundException("Node[" + id + "] not connected to this relationship[" + this.getId() + "]");
        }
    }

    public RelationshipType getType() {
        this.spi.assertInUnterminatedTransaction();
        return this.spi.getRelationshipTypeById(this.typeId());
    }

    public Iterable<String> getPropertyKeys() {
        KernelTransaction transaction = this.spi.kernelTransaction();
        ArrayList keys = new ArrayList();

        try {
            RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
            PropertyCursor properties = transaction.ambientPropertyCursor();
            this.singleRelationship(transaction, relationships);
            TokenRead token = transaction.tokenRead();
            relationships.properties(properties);

            while(properties.next()) {
                keys.add(token.propertyKeyName(properties.propertyKey()));
            }

            return keys;
        } catch (PropertyKeyIdNotFoundKernelException var6) {
            throw new IllegalStateException("Property key retrieved through kernel API should exist.", var6);
        }
    }

    public Map<String, Object> getProperties(String... keys) {
        Objects.requireNonNull(keys, "Properties keys should be not null array.");
        if (keys.length == 0) {
            return Collections.emptyMap();
        } else {
            KernelTransaction transaction = this.spi.kernelTransaction();
            int itemsToReturn = keys.length;
            TokenRead token = transaction.tokenRead();
            int[] propertyIds = new int[itemsToReturn];

            for(int i = 0; i < itemsToReturn; ++i) {
                String key = keys[i];
                if (key == null) {
                    throw new NullPointerException(String.format("Key %d was null", i));
                }

                propertyIds[i] = token.propertyKey(key);
            }

            Map<String, Object> properties = new HashMap(itemsToReturn);
            RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
            PropertyCursor propertyCursor = transaction.ambientPropertyCursor();
            this.singleRelationship(transaction, relationships);
            relationships.properties(propertyCursor);
            int propertiesToFind = itemsToReturn;

            while(propertiesToFind > 0 && propertyCursor.next()) {
                int currentKey = propertyCursor.propertyKey();

                for(int i = 0; i < itemsToReturn; ++i) {
                    if (propertyIds[i] == currentKey) {
                        properties.put(keys[i], propertyCursor.propertyValue().asObjectCopy());
                        --propertiesToFind;
                        break;
                    }
                }
            }

            return properties;
        }
    }

    public Map<String, Object> getAllProperties() {
        KernelTransaction transaction = this.spi.kernelTransaction();
        HashMap properties = new HashMap();

        try {
            RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
            PropertyCursor propertyCursor = transaction.ambientPropertyCursor();
            TokenRead token = transaction.tokenRead();
            this.singleRelationship(transaction, relationships);
            relationships.properties(propertyCursor);

            while(propertyCursor.next()) {
                properties.put(token.propertyKeyName(propertyCursor.propertyKey()), propertyCursor.propertyValue().asObjectCopy());
            }

            return properties;
        } catch (PropertyKeyIdNotFoundKernelException var6) {
            throw new IllegalStateException("Property key retrieved through kernel API should exist.", var6);
        }
    }

    public Object getProperty(String key) {
        if (null == key) {
            throw new IllegalArgumentException("(null) property key is not allowed");
        } else {
            KernelTransaction transaction = this.spi.kernelTransaction();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                throw new NotFoundException(String.format("No such property, '%s'.", key));
            } else {
                RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
                PropertyCursor properties = transaction.ambientPropertyCursor();
                this.singleRelationship(transaction, relationships);
                relationships.properties(properties);

                do {
                    if (!properties.next()) {
                        throw new NotFoundException(String.format("No such property, '%s'.", key));
                    }
                } while(propertyKey != properties.propertyKey());

                Value value = properties.propertyValue();
                if (value == Values.NO_VALUE) {
                    throw new NotFoundException(String.format("No such property, '%s'.", key));
                } else {
                    //DynamicGraph
                   // Map<Integer,Object> data = Serialization.readJMapFromObject(value.asObjectCopy());
                   // Object v = getCurrentValue(data);
                    //DynamicGraph
                   // return v;
                    return value.asObjectCopy();
                }
            }
        }
    }

    private Object getVersionValue(Map<Integer,Object> data,long version){
        int keyMax = -1;
        Iterator<Integer> it = data.keySet().iterator();
        while(it.hasNext()){
            int key = it.next();
            if(DGVersion.getStartVersion(key) == version) return data.get(key);
        }
        return null;
    }
    private Object getCurrentValue(Map<Integer,Object> data){
        int keyMax = -1;
        Iterator<Integer> it = data.keySet().iterator();
        while(it.hasNext()){
            int key = it.next();
            if(!DGVersion.hasEndVersion(key)) return data.get(key);
        }
        return null;
    }
    @Override
    public Object getProperty(String key, long version) {
        if (null == key) {
            throw new IllegalArgumentException("(null) property key is not allowed");
        } else {
            KernelTransaction transaction = this.spi.kernelTransaction();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                throw new NotFoundException(String.format("No such property, '%s'.", key));
            } else {
                RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
                PropertyCursor properties = transaction.ambientPropertyCursor();
                this.singleRelationship(transaction, relationships);
                relationships.properties(properties);

                do {
                    if (!properties.next()) {
                        throw new NotFoundException(String.format("No such property, '%s'.", key));
                    }
                } while(propertyKey != properties.propertyKey());

                Value value = properties.propertyValue(version);
                if (value == Values.NO_VALUE) {
                    throw new NotFoundException(String.format("No such property, '%s'.", key));
                } else {
                    //DynamicGraph
                    //Map<Integer,Object> data = Serialization.readJMapFromObject(value.asObjectCopy());
                    //Object v = getVersionValue(data,version);
                    //DynamicGraph
                    //return v;
                    return value.asObjectCopy();
                }
            }
        }
    }

    public Object getProperty(String key, Object defaultValue) {
        if (null == key) {
            throw new IllegalArgumentException("(null) property key is not allowed");
        } else {
            KernelTransaction transaction = this.spi.kernelTransaction();
            RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
            PropertyCursor properties = transaction.ambientPropertyCursor();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                return defaultValue;
            } else {
                this.singleRelationship(transaction, relationships);
                relationships.properties(properties);

                do {
                    if (!properties.next()) {
                        return defaultValue;
                    }
                } while(propertyKey != properties.propertyKey());

                Value value = properties.propertyValue();
                return value == Values.NO_VALUE ? defaultValue : value.asObjectCopy();
            }
        }
    }

    public boolean hasProperty(String key) {
        if (null == key) {
            return false;
        } else {
            KernelTransaction transaction = this.spi.kernelTransaction();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                return false;
            } else {
                RelationshipScanCursor relationships = transaction.ambientRelationshipCursor();
                PropertyCursor properties = transaction.ambientPropertyCursor();
                this.singleRelationship(transaction, relationships);
                relationships.properties(properties);

                do {
                    if (!properties.next()) {
                        return false;
                    }
                } while(propertyKey != properties.propertyKey());

                return true;
            }
        }
    }

    public void setProperty(String key, Object value) {
        KernelTransaction transaction = this.spi.kernelTransaction();

        int propertyKeyId;
        try {
            propertyKeyId = transaction.tokenWrite().propertyKeyGetOrCreateForName(key);
        } catch (IllegalTokenNameException var21) {
            throw new IllegalArgumentException(String.format("Invalid property key '%s'.", key), var21);
        }

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var6 = null;

            try {
                transaction.dataWrite().relationshipSetProperty(this.id, propertyKeyId, Values.of(value, false));
            } catch (Throwable var20) {
                var6 = var20;
                throw var20;
            } finally {
                if (ignore != null) {
                    if (var6 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var19) {
                            var6.addSuppressed(var19);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

        } catch (IllegalArgumentException var23) {
            this.spi.failTransaction();
            throw var23;
        } catch (EntityNotFoundException var24) {
            throw new NotFoundException(var24);
        } catch (InvalidTransactionTypeKernelException var25) {
            throw new ConstraintViolationException(var25.getMessage(), var25);
        } catch (AutoIndexingKernelException var26) {
            throw new IllegalStateException("Auto indexing encountered a failure while setting property: " + var26.getMessage(), var26);
        }
    }

    public Object removeProperty(String key) {
        KernelTransaction transaction = this.spi.kernelTransaction();

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var4 = null;

            Object var6;
            try {
                int propertyKeyId = transaction.tokenWrite().propertyKeyGetOrCreateForName(key);
                var6 = transaction.dataWrite().relationshipRemoveProperty(this.id, propertyKeyId).asObjectCopy();
            } catch (Throwable var19) {
                var4 = var19;
                throw var19;
            } finally {
                if (ignore != null) {
                    if (var4 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var18) {
                            var4.addSuppressed(var18);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var6;
        } catch (EntityNotFoundException var21) {
            throw new NotFoundException(var21);
        } catch (IllegalTokenNameException var22) {
            throw new IllegalArgumentException(String.format("Invalid property key '%s'.", key), var22);
        } catch (InvalidTransactionTypeKernelException var23) {
            throw new ConstraintViolationException(var23.getMessage(), var23);
        } catch (AutoIndexingKernelException var24) {
            throw new IllegalStateException("Auto indexing encountered a failure while removing property: " + var24.getMessage(), var24);
        }
    }

    public boolean isType(RelationshipType type) {
        this.spi.assertInUnterminatedTransaction();
        return this.spi.getRelationshipTypeById(this.typeId()).name().equals(type.name());
    }

    public int compareTo(Object rel) {
        Relationship r = (Relationship)rel;
        return Long.compare(this.getId(), r.getId());
    }

    public boolean equals(Object o) {
        return o instanceof Relationship && this.getId() == ((Relationship)o).getId();
    }

    public int hashCode() {
        return (int)(this.getId() >>> 32 ^ this.getId());
    }

    public String toString() {
        String relType;
        try {
            relType = this.spi.getRelationshipTypeById(this.typeId()).name();
            return String.format("(%d)-[%s,%d]->(%d)", this.sourceId(), relType, this.getId(), this.targetId());
        } catch (DatabaseShutdownException | NotInTransactionException var3) {
            relType = "RELTYPE(" + this.type + ")";
            return String.format("(?)-[%s,%d]->(?)", relType, this.getId());
        }
    }

    private void singleRelationship(KernelTransaction transaction, RelationshipScanCursor relationships) {
        transaction.dataRead().singleRelationship(this.id, relationships);
        if (!relationships.next()) {
            throw new NotFoundException(new EntityNotFoundException(EntityType.RELATIONSHIP, this.id));
        }
    }
}
