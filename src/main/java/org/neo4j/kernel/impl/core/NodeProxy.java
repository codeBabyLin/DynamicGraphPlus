//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.core;

import java.util.*;

import cn.DynamicGraph.Common.DGVersion;
import cn.DynamicGraph.Common.Serialization;
import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.internal.kernel.api.LabelSet;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.RelationshipGroupCursor;
import org.neo4j.internal.kernel.api.TokenRead;
import org.neo4j.internal.kernel.api.exceptions.EntityNotFoundException;
import org.neo4j.internal.kernel.api.exceptions.InvalidTransactionTypeKernelException;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.LabelNotFoundKernelException;
import org.neo4j.internal.kernel.api.exceptions.PropertyKeyIdNotFoundKernelException;
import org.neo4j.internal.kernel.api.exceptions.explicitindex.AutoIndexingKernelException;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.internal.kernel.api.exceptions.schema.IllegalTokenNameException;
import org.neo4j.internal.kernel.api.exceptions.schema.TooManyLabelsException;
import org.neo4j.internal.kernel.api.helpers.Nodes;
import org.neo4j.internal.kernel.api.helpers.RelationshipFactory;
import org.neo4j.internal.kernel.api.helpers.RelationshipSelections;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.SilentTokenNameLookup;
import org.neo4j.kernel.api.Statement;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.exceptions.Status.Transaction;
import org.neo4j.storageengine.api.EntityType;
import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.Values;

public class NodeProxy implements Node, RelationshipFactory<Relationship> {
    private final EmbeddedProxySPI spi;
    private final long nodeId;

    public NodeProxy(EmbeddedProxySPI spi, long nodeId) {
        this.nodeId = nodeId;
        this.spi = spi;
    }

    public long getId() {
        return this.nodeId;
    }

    public GraphDatabaseService getGraphDatabase() {
        return this.spi.getGraphDatabase();
    }

    public void delete() {
        KernelTransaction transaction = this.safeAcquireTransaction();

        try {
            boolean deleted = transaction.dataWrite().nodeDelete(this.getId());
            if (!deleted) {
                throw new NotFoundException("Unable to delete Node[" + this.nodeId + "] since it has already been deleted.");
            }
        } catch (InvalidTransactionTypeKernelException var3) {
            throw new ConstraintViolationException(var3.getMessage(), var3);
        } catch (AutoIndexingKernelException var4) {
            throw new IllegalStateException("Auto indexing encountered a failure while deleting the node: " + var4.getMessage(), var4);
        }
    }

    public ResourceIterable<Relationship> getRelationships() {
        return this.getRelationships(Direction.BOTH);
    }

    public ResourceIterable<Relationship> getRelationships(Direction direction) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        return this.innerGetRelationships(transaction, direction, (int[])null);
    }

    public ResourceIterable<Relationship> getRelationships(RelationshipType... types) {
        return this.getRelationships(Direction.BOTH, types);
    }

    public ResourceIterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
        return this.getRelationships(dir, type);
    }

    public ResourceIterable<Relationship> getRelationships(Direction direction, RelationshipType... types) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        int[] typeIds = this.relTypeIds(types, transaction.tokenRead());
        return this.innerGetRelationships(transaction, direction, typeIds);
    }

    private ResourceIterable<Relationship> innerGetRelationships(KernelTransaction transaction, Direction direction, int[] typeIds) {
        return () -> {
            return this.getRelationshipSelectionIterator(transaction, direction, typeIds);
        };
    }

    public boolean hasRelationship() {
        return this.hasRelationship(Direction.BOTH);
    }

    public boolean hasRelationship(Direction direction) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        return this.innerHasRelationships(transaction, direction, (int[])null);
    }

    public boolean hasRelationship(RelationshipType... types) {
        return this.hasRelationship(Direction.BOTH, types);
    }

    public boolean hasRelationship(Direction direction, RelationshipType... types) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        int[] typeIds = this.relTypeIds(types, transaction.tokenRead());
        return this.innerHasRelationships(transaction, direction, typeIds);
    }

    public boolean hasRelationship(RelationshipType type, Direction dir) {
        return this.hasRelationship(dir, type);
    }

    private boolean innerHasRelationships(KernelTransaction transaction, Direction direction, int[] typeIds) {
        ResourceIterator<Relationship> iterator = this.getRelationshipSelectionIterator(transaction, direction, typeIds);
        Throwable var5 = null;

        boolean var6;
        try {
            var6 = iterator.hasNext();
        } catch (Throwable var15) {
            var5 = var15;
            throw var15;
        } finally {
            if (iterator != null) {
                if (var5 != null) {
                    try {
                        iterator.close();
                    } catch (Throwable var14) {
                        var5.addSuppressed(var14);
                    }
                } else {
                    iterator.close();
                }
            }

        }

        return var6;
    }

    public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
        ResourceIterator<Relationship> rels = this.getRelationships(dir, type).iterator();
        Throwable var4 = null;

        Relationship rel;
        try {
            if (rels.hasNext()) {
                rel = (Relationship)rels.next();

                Relationship other;
                while(rels.hasNext()) {
                    other = (Relationship)rels.next();
                    if (!other.equals(rel)) {
                        throw new NotFoundException("More than one relationship[" + type + ", " + dir + "] found for " + this);
                    }
                }

                other = rel;
                return other;
            }

            rel = null;
        } catch (Throwable var16) {
            var4 = var16;
            throw var16;
        } finally {
            if (rels != null) {
                if (var4 != null) {
                    try {
                        rels.close();
                    } catch (Throwable var15) {
                        var4.addSuppressed(var15);
                    }
                } else {
                    rels.close();
                }
            }

        }

        return rel;
    }

    public void setProperty(String key, Object value) {
        KernelTransaction transaction = this.spi.kernelTransaction();

        int propertyKeyId;
        try {
            propertyKeyId = transaction.tokenWrite().propertyKeyGetOrCreateForName(key);
        } catch (IllegalTokenNameException var23) {
            throw new IllegalArgumentException(String.format("Invalid property key '%s'.", key), var23);
        }

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var6 = null;

            try {
                transaction.dataWrite().nodeSetProperty(this.nodeId, propertyKeyId, Values.of(value, false));
            } catch (Throwable var22) {
                var6 = var22;
                throw var22;
            } finally {
                if (ignore != null) {
                    if (var6 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var21) {
                            var6.addSuppressed(var21);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

        } catch (ConstraintValidationException var25) {
            throw new ConstraintViolationException(var25.getUserMessage(new SilentTokenNameLookup(transaction.tokenRead())), var25);
        } catch (IllegalArgumentException var26) {
            this.spi.failTransaction();
            throw var26;
        } catch (EntityNotFoundException var27) {
            throw new NotFoundException(var27);
        } catch (InvalidTransactionTypeKernelException var28) {
            throw new ConstraintViolationException(var28.getMessage(), var28);
        } catch (AutoIndexingKernelException var29) {
            throw new IllegalStateException("Auto indexing encountered a failure while setting property: " + var29.getMessage(), var29);
        } catch (KernelException var30) {
            throw new ConstraintViolationException(var30.getMessage(), var30);
        }
    }

    public Object removeProperty(String key) throws NotFoundException {
        KernelTransaction transaction = this.spi.kernelTransaction();

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var4 = null;

            Object var6;
            try {
                int propertyKeyId = transaction.tokenWrite().propertyKeyGetOrCreateForName(key);
                var6 = transaction.dataWrite().nodeRemoveProperty(this.nodeId, propertyKeyId).asObjectCopy();
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

    public Object getProperty(String key, Object defaultValue) {
        if (null == key) {
            throw new IllegalArgumentException("(null) property key is not allowed");
        } else {
            KernelTransaction transaction = this.safeAcquireTransaction();
            NodeCursor nodes = transaction.ambientNodeCursor();
            PropertyCursor properties = transaction.ambientPropertyCursor();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                return defaultValue;
            } else {
                this.singleNode(transaction, nodes);
                nodes.properties(properties);

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

    public Iterable<String> getPropertyKeys() {
        KernelTransaction transaction = this.safeAcquireTransaction();
        ArrayList keys = new ArrayList();

        try {
            NodeCursor nodes = transaction.ambientNodeCursor();
            PropertyCursor properties = transaction.ambientPropertyCursor();
            this.singleNode(transaction, nodes);
            TokenRead token = transaction.tokenRead();
            nodes.properties(properties);

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
            KernelTransaction transaction = this.safeAcquireTransaction();
            int itemsToReturn = keys.length;
            Map<String, Object> properties = new HashMap(itemsToReturn);
            TokenRead token = transaction.tokenRead();
            int[] propertyIds = new int[itemsToReturn];

            for(int i = 0; i < itemsToReturn; ++i) {
                String key = keys[i];
                if (key == null) {
                    throw new NullPointerException(String.format("Key %d was null", i));
                }

                propertyIds[i] = token.propertyKey(key);
            }

            NodeCursor nodes = transaction.ambientNodeCursor();
            PropertyCursor propertyCursor = transaction.ambientPropertyCursor();
            this.singleNode(transaction, nodes);
            nodes.properties(propertyCursor);
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
        KernelTransaction transaction = this.safeAcquireTransaction();
        HashMap properties = new HashMap();

        try {
            NodeCursor nodes = transaction.ambientNodeCursor();
            PropertyCursor propertyCursor = transaction.ambientPropertyCursor();
            TokenRead token = transaction.tokenRead();
            this.singleNode(transaction, nodes);
            nodes.properties(propertyCursor);

            while(propertyCursor.next()) {
                if(DGVersion.hasEndVersion(propertyCursor.propertyVersion())) continue;
                properties.put(token.propertyKeyName(propertyCursor.propertyKey()), propertyCursor.propertyValue().asObjectCopy());
            }

            return properties;
        } catch (PropertyKeyIdNotFoundKernelException var6) {
            throw new IllegalStateException("Property key retrieved through kernel API should exist.", var6);
        }
    }

    public Object getProperty(String key) throws NotFoundException {
        if (null == key) {
            throw new IllegalArgumentException("(null) property key is not allowed");
        } else {
            KernelTransaction transaction = this.safeAcquireTransaction();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                throw new NotFoundException(String.format("No such property, '%s'.", key));
            } else {
                NodeCursor nodes = transaction.ambientNodeCursor();
                PropertyCursor properties = transaction.ambientPropertyCursor();
                this.singleNode(transaction, nodes);
                nodes.properties(properties);

                do {
                    if (!properties.next()) {
                        throw new NotFoundException(String.format("No such property, '%s'.", key));
                    }
                } while(propertyKey != properties.propertyKey());

                Value value = properties.propertyValue(1,true);
                //DynamicGraph


                //DynamicGraph


                if (value == Values.NO_VALUE) {
                    throw new NotFoundException(String.format("No such property, '%s'.", key));
                } else {
                    //DynamicGraph
                    //Map<Integer,Object> data = Serialization.readJMapFromObject(value.asObjectCopy());
                    //Object v = getCurrentValue(data);
                    //DynamicGraph
                    //return v;
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
    public Object getProperty(String key,long startVersion) throws NotFoundException {
        if (null == key) {
            throw new IllegalArgumentException("(null) property key is not allowed");
        } else {
            KernelTransaction transaction = this.safeAcquireTransaction();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                throw new NotFoundException(String.format("No such property, '%s'.", key));
            } else {
                NodeCursor nodes = transaction.ambientNodeCursor();
                PropertyCursor properties = transaction.ambientPropertyCursor();
                this.singleNode(transaction, nodes);
                nodes.properties(properties);

                do {
                    if (!properties.next()) {
                        throw new NotFoundException(String.format("No such property, '%s'.", key));
                    }
                } while(propertyKey != properties.propertyKey());
                long propertyVersion = properties.propertyVersion();
                if(DGVersion.getStartVersion(propertyVersion) != startVersion){
                    do {
                        if (!properties.nextHistory()) {
                            throw new NotFoundException(String.format("No such property, '%s'.", key));
                        }
                        propertyVersion = properties.propertyVersion();
                    } while(DGVersion.getStartVersion(propertyVersion) != startVersion);
                }

                //while()
                Value value = properties.propertyValue();

                if (value == Values.NO_VALUE) {
                    throw new NotFoundException(String.format("No such property, '%s'.", key));
                } else {
                    return value.asObjectCopy();
                }
            }
        }
    }




    public boolean hasProperty(String key) {
        if (null == key) {
            return false;
        } else {
            KernelTransaction transaction = this.safeAcquireTransaction();
            int propertyKey = transaction.tokenRead().propertyKey(key);
            if (propertyKey == -1) {
                return false;
            } else {
                NodeCursor nodes = transaction.ambientNodeCursor();
                PropertyCursor properties = transaction.ambientPropertyCursor();
                this.singleNode(transaction, nodes);
                nodes.properties(properties);

                do {
                    if (!properties.next()) {
                        return false;
                    }
                } while(propertyKey != properties.propertyKey());

                return true;
            }
        }
    }

    private KernelTransaction safeAcquireTransaction() {
        KernelTransaction transaction = this.spi.kernelTransaction();
        if (transaction.isTerminated()) {
            Status terminationReason = (Status)transaction.getReasonIfTerminated().orElse(Transaction.Terminated);
            throw new TransactionTerminatedException(terminationReason);
        } else {
            return transaction;
        }
    }

    public int compareTo(Object node) {
        Node n = (Node)node;
        return Long.compare(this.getId(), n.getId());
    }

    public boolean equals(Object o) {
        return o instanceof Node && this.getId() == ((Node)o).getId();
    }

    public int hashCode() {
        return (int)(this.nodeId >>> 32 ^ this.nodeId);
    }

    public String toString() {
        return "Node[" + this.getId() + "]";
    }

    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        if (otherNode == null) {
            throw new IllegalArgumentException("Other node is null.");
        } else {
            KernelTransaction transaction = this.safeAcquireTransaction();

            try {
                Statement ignore = transaction.acquireStatement();
                Throwable var5 = null;

                RelationshipProxy var9;
                try {
                    int relationshipTypeId = transaction.tokenWrite().relationshipTypeGetOrCreateForName(type.name());
                    long relationshipId = transaction.dataWrite().relationshipCreate(this.nodeId, relationshipTypeId, otherNode.getId());
                    var9 = this.spi.newRelationshipProxy(relationshipId, this.nodeId, relationshipTypeId, otherNode.getId());
                } catch (Throwable var21) {
                    var5 = var21;
                    throw var21;
                } finally {
                    if (ignore != null) {
                        if (var5 != null) {
                            try {
                                ignore.close();
                            } catch (Throwable var20) {
                                var5.addSuppressed(var20);
                            }
                        } else {
                            ignore.close();
                        }
                    }

                }

                return var9;
            } catch (IllegalTokenNameException var23) {
                throw new IllegalArgumentException(var23);
            } catch (EntityNotFoundException var24) {
                throw new NotFoundException("Node[" + var24.entityId() + "] is deleted and cannot be used to create a relationship");
            } catch (InvalidTransactionTypeKernelException var25) {
                throw new ConstraintViolationException(var25.getMessage(), var25);
            }
        }
    }

    public void addLabel(Label label) {
        KernelTransaction transaction = this.spi.kernelTransaction();

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var4 = null;

            try {
                transaction.dataWrite().nodeAddLabel(this.getId(), transaction.tokenWrite().labelGetOrCreateForName(label.name()));
            } catch (Throwable var18) {
                var4 = var18;
                throw var18;
            } finally {
                if (ignore != null) {
                    if (var4 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var17) {
                            var4.addSuppressed(var17);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

        } catch (ConstraintValidationException var20) {
            throw new ConstraintViolationException(var20.getUserMessage(new SilentTokenNameLookup(transaction.tokenRead())), var20);
        } catch (IllegalTokenNameException var21) {
            throw new ConstraintViolationException(String.format("Invalid label name '%s'.", label.name()), var21);
        } catch (TooManyLabelsException var22) {
            throw new ConstraintViolationException("Unable to add label.", var22);
        } catch (EntityNotFoundException var23) {
            throw new NotFoundException("No node with id " + this.getId() + " found.", var23);
        } catch (KernelException var24) {
            throw new ConstraintViolationException(var24.getMessage(), var24);
        }
    }

    public void removeLabel(Label label) {
        KernelTransaction transaction = this.spi.kernelTransaction();

        try {
            Statement ignore = transaction.acquireStatement();
            Throwable var4 = null;

            try {
                int labelId = transaction.tokenRead().nodeLabel(label.name());
                if (labelId != -1) {
                    transaction.dataWrite().nodeRemoveLabel(this.getId(), labelId);
                }
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (ignore != null) {
                    if (var4 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

        } catch (EntityNotFoundException var17) {
            throw new NotFoundException("No node with id " + this.getId() + " found.", var17);
        } catch (KernelException var18) {
            throw new ConstraintViolationException(var18.getMessage(), var18);
        }
    }

    public boolean hasLabel(Label label) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        NodeCursor nodes = transaction.ambientNodeCursor();
        Statement ignore = transaction.acquireStatement();
        Throwable var5 = null;

        boolean var7;
        try {
            int labelId = transaction.tokenRead().nodeLabel(label.name());
            if (labelId == -1) {
                var7 = false;
                return var7;
            }

            transaction.dataRead().singleNode(this.nodeId, nodes);
            var7 = nodes.next() && nodes.hasLabel(labelId);
        } catch (Throwable var17) {
            var5 = var17;
            throw var17;
        } finally {
            if (ignore != null) {
                if (var5 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var16) {
                        var5.addSuppressed(var16);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return var7;
    }

    public Iterable<Label> getLabels() {
        KernelTransaction transaction = this.safeAcquireTransaction();
        NodeCursor nodes = transaction.ambientNodeCursor();

        try {
            Statement ignore = this.spi.statement();
            Throwable var4 = null;

            try {
                this.singleNode(transaction, nodes);
                LabelSet labelSet = nodes.labels();
                TokenRead tokenRead = transaction.tokenRead();
                ArrayList<Label> list = new ArrayList(labelSet.numberOfLabels());

                for(int i = 0; i < labelSet.numberOfLabels(); ++i) {
                    list.add(Label.label(tokenRead.nodeLabelName(labelSet.label(i))));
                }

                ArrayList var21 = list;
                return var21;
            } catch (Throwable var18) {
                var4 = var18;
                throw var18;
            } finally {
                if (ignore != null) {
                    if (var4 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var17) {
                            var4.addSuppressed(var17);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }
        } catch (LabelNotFoundKernelException var20) {
            throw new IllegalStateException("Label retrieved through kernel API should exist.", var20);
        }
    }

    public int getDegree() {
        KernelTransaction transaction = this.safeAcquireTransaction();
        Statement ignore = transaction.acquireStatement();
        Throwable var3 = null;

        int var5;
        try {
            NodeCursor nodes = transaction.ambientNodeCursor();
            this.singleNode(transaction, nodes);
            var5 = Nodes.countAll(nodes, transaction.cursors());
        } catch (Throwable var14) {
            var3 = var14;
            throw var14;
        } finally {
            if (ignore != null) {
                if (var3 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                    }
                } else {
                    ignore.close();
                }
            }

        }

        return var5;
    }

    public int getDegree(RelationshipType type) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        int typeId = transaction.tokenRead().relationshipType(type.name());
        if (typeId == -1) {
            return 0;
        } else {
            Statement ignore = transaction.acquireStatement();
            Throwable var5 = null;

            int var7;
            try {
                NodeCursor nodes = transaction.ambientNodeCursor();
                this.singleNode(transaction, nodes);
                var7 = Nodes.countAll(nodes, transaction.cursors(), typeId);
            } catch (Throwable var16) {
                var5 = var16;
                throw var16;
            } finally {
                if (ignore != null) {
                    if (var5 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var15) {
                            var5.addSuppressed(var15);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }

            return var7;
        }
    }

    public int getDegree(Direction direction) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        Statement ignore = transaction.acquireStatement();
        Throwable var4 = null;

        try {
            NodeCursor nodes = transaction.ambientNodeCursor();
            this.singleNode(transaction, nodes);
            int var6;
            switch(direction) {
                case OUTGOING:
                    var6 = Nodes.countOutgoing(nodes, transaction.cursors());
                    return var6;
                case INCOMING:
                    var6 = Nodes.countIncoming(nodes, transaction.cursors());
                    return var6;
                case BOTH:
                    var6 = Nodes.countAll(nodes, transaction.cursors());
                    return var6;
                default:
                    throw new IllegalStateException("Unknown direction " + direction);
            }
        } catch (Throwable var17) {
            var4 = var17;
            throw var17;
        } finally {
            if (ignore != null) {
                if (var4 != null) {
                    try {
                        ignore.close();
                    } catch (Throwable var16) {
                        var4.addSuppressed(var16);
                    }
                } else {
                    ignore.close();
                }
            }

        }
    }

    public int getDegree(RelationshipType type, Direction direction) {
        KernelTransaction transaction = this.safeAcquireTransaction();
        int typeId = transaction.tokenRead().relationshipType(type.name());
        if (typeId == -1) {
            return 0;
        } else {
            Statement ignore = transaction.acquireStatement();
            Throwable var6 = null;

            try {
                NodeCursor nodes = transaction.ambientNodeCursor();
                this.singleNode(transaction, nodes);
                int var8;
                switch(direction) {
                    case OUTGOING:
                        var8 = Nodes.countOutgoing(nodes, transaction.cursors(), typeId);
                        return var8;
                    case INCOMING:
                        var8 = Nodes.countIncoming(nodes, transaction.cursors(), typeId);
                        return var8;
                    case BOTH:
                        var8 = Nodes.countAll(nodes, transaction.cursors(), typeId);
                        return var8;
                    default:
                        throw new IllegalStateException("Unknown direction " + direction);
                }
            } catch (Throwable var19) {
                var6 = var19;
                throw var19;
            } finally {
                if (ignore != null) {
                    if (var6 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var18) {
                            var6.addSuppressed(var18);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }
        }
    }

    public Iterable<RelationshipType> getRelationshipTypes() {
        KernelTransaction transaction = this.safeAcquireTransaction();

        try {
            RelationshipGroupCursor relationships = transaction.cursors().allocateRelationshipGroupCursor();
            Throwable var3 = null;

            ArrayList var39;
            try {
                Statement ignore = transaction.acquireStatement();
                Throwable var5 = null;

                try {
                    NodeCursor nodes = transaction.ambientNodeCursor();
                    TokenRead tokenRead = transaction.tokenRead();
                    this.singleNode(transaction, nodes);
                    nodes.relationships(relationships);
                    ArrayList types = new ArrayList();

                    while(relationships.next()) {
                        int type = relationships.type();
                        if (relationships.totalCount() > 0) {
                            types.add(RelationshipType.withName(tokenRead.relationshipTypeName(relationships.type())));
                        }
                    }

                    var39 = types;
                } catch (Throwable var34) {
                    var5 = var34;
                    throw var34;
                } finally {
                    if (ignore != null) {
                        if (var5 != null) {
                            try {
                                ignore.close();
                            } catch (Throwable var33) {
                                var5.addSuppressed(var33);
                            }
                        } else {
                            ignore.close();
                        }
                    }

                }
            } catch (Throwable var36) {
                var3 = var36;
                throw var36;
            } finally {
                if (relationships != null) {
                    if (var3 != null) {
                        try {
                            relationships.close();
                        } catch (Throwable var32) {
                            var3.addSuppressed(var32);
                        }
                    } else {
                        relationships.close();
                    }
                }

            }

            return var39;
        } catch (KernelException var38) {
            throw new NotFoundException("Relationship name not found.", var38);
        }
    }

    private ResourceIterator<Relationship> getRelationshipSelectionIterator(KernelTransaction transaction, Direction direction, int[] typeIds) {
        NodeCursor node = transaction.ambientNodeCursor();
        transaction.dataRead().singleNode(this.getId(), node);
        if (!node.next()) {
            throw new NotFoundException(String.format("Node %d not found", this.nodeId));
        } else {
            switch(direction) {
                case OUTGOING:
                    return RelationshipSelections.outgoingIterator(transaction.cursors(), node, typeIds, this);
                case INCOMING:
                    return RelationshipSelections.incomingIterator(transaction.cursors(), node, typeIds, this);
                case BOTH:
                    return RelationshipSelections.allIterator(transaction.cursors(), node, typeIds, this);
                default:
                    throw new IllegalStateException("Unknown direction " + direction);
            }
        }
    }

    private int[] relTypeIds(RelationshipType[] types, TokenRead token) {
        int[] ids = new int[types.length];
        int outIndex = 0;
        RelationshipType[] var5 = types;
        int var6 = types.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            RelationshipType type = var5[var7];
            int id = token.relationshipType(type.name());
            if (id != -1) {
                ids[outIndex++] = id;
            }
        }

        if (outIndex != ids.length) {
            ids = Arrays.copyOf(ids, outIndex);
        }

        return ids;
    }

    private void singleNode(KernelTransaction transaction, NodeCursor nodes) {
        transaction.dataRead().singleNode(this.nodeId, nodes);
        if (!nodes.next()) {
            throw new NotFoundException(new EntityNotFoundException(EntityType.NODE, this.nodeId));
        }
    }

    public Relationship relationship(long id, long startNodeId, int typeId, long endNodeId) {
        return this.spi.newRelationshipProxy(id, startNodeId, typeId, endNodeId);
    }

    //DynamicGraph
    public long getNodeVersion(){
        KernelTransaction transaction = this.safeAcquireTransaction();
        Statement ignore = transaction.acquireStatement();
        NodeCursor nodes = transaction.ambientNodeCursor();
        this.singleNode(transaction,nodes);
        //transaction.dataRead().singleNode(nodeId,nodes);
        //if (!nodes.next()) throw new NotFoundException(new EntityNotFoundException(EntityType.NODE, nodeId));
        return nodes.nodeVersion();

    }
    public Map<Label,Long> getVersionLabel() {
       /* KernelTransaction transaction = this.safeAcquireTransaction();
        Statement ignore = transaction.acquireStatement();
        NodeCursor nodes = transaction.ambientNodeCursor();
        this.singleNode(transaction,nodes);
        //this.singleNode(transaction, nodes);
        Map<Long,Long> labels = nodes.versionLabels();
        TokenRead tokenRead = transaction.tokenRead();
        Map<Label,Long> newLabels = new HashMap<>();

        for(Map.Entry<Long,Long> entry: labels.entrySet()) {
            newLabels.put(tokenRead.nodeLabelName(Math.toIntExact(entry.getKey())), entry.getValue());
        }

        ArrayList var21 = list;
        return var21;*/

        KernelTransaction transaction = this.safeAcquireTransaction();
        NodeCursor nodes = transaction.ambientNodeCursor();

        try {
            Statement ignore = this.spi.statement();
            Throwable var4 = null;

            try {
                this.singleNode(transaction, nodes);
                Map<Long,Long> labelMap = nodes.versionLabels();
                //LabelSet labelSet = nodes.labels();
                TokenRead tokenRead = transaction.tokenRead();
                Map<Label,Long> newLabels = new HashMap<>();
                //ArrayList<Label> list = new ArrayList(labelSet.numberOfLabels());

                for(Map.Entry<Long,Long> entry: labelMap.entrySet()) {
                    newLabels.put(Label.label(tokenRead.nodeLabelName(Math.toIntExact(entry.getKey()))), entry.getValue());
                }

                //ArrayList var21 = list;
                return newLabels;
            } catch (Throwable var18) {
                var4 = var18;
                throw var18;
            } finally {
                if (ignore != null) {
                    if (var4 != null) {
                        try {
                            ignore.close();
                        } catch (Throwable var17) {
                            var4.addSuppressed(var17);
                        }
                    } else {
                        ignore.close();
                    }
                }

            }
        } catch (LabelNotFoundKernelException var20) {
            throw new IllegalStateException("Label retrieved through kernel API should exist.", var20);
        }


    }

    //DynamicGraph

}
