//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.graphdb;

import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface GraphDatabaseService{




    //DynamicGraph
    //*******************************

    long [] listAllVersions();
    long getCurrentVersion();
    void seekVersion(long version);
    long getNextVersion();
    long []getNextVersions(long versionCount);
    boolean commitVersions(long[] versions);
    boolean commitVersion(long version, boolean isInuse);
    Transaction beginTx(long version);



    //DynamicGraph
    //*******************************


    ResourceIterable<Node> getAllNodesInSingleVersion(long version);

    ResourceIterable<Relationship> getAllRelationshipsInSingleVersion(long version);



    ResourceIterable<Node> getAllNodesInVersionDelta(long startVersion, long endVersion);

    ResourceIterable<Relationship> getAllRelationshipsInVersionDelta(long startVersion, long endVersion);


    ResourceIterable<Node> getAllNodesInVersions(long startVersion, long endVersion);

    ResourceIterable<Relationship> getAllRelationshipsInVersions(long startVersion, long endVersion);





    Node createNode();

    /** @deprecated */
    //@Deprecated
    Long createNodeId();

    Node createNode(Label... var1);

    Node getNodeById(long var1);

    Relationship getRelationshipById(long var1);

    ResourceIterable<Node> getAllNodes();

    ResourceIterable<Relationship> getAllRelationships();

    ResourceIterator<Node> findNodes(Label var1, String var2, Object var3);

    default ResourceIterator<Node> findNodes(Label label, String key1, Object value1, String key2, Object value2) {
        throw new UnsupportedOperationException("findNodes by multiple property names and values is not supported.");
    }

    default ResourceIterator<Node> findNodes(Label label, String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        throw new UnsupportedOperationException("findNodes by multiple property names and values is not supported.");
    }

    default ResourceIterator<Node> findNodes(Label label, Map<String, Object> propertyValues) {
        throw new UnsupportedOperationException("findNodes by multiple property names and values is not supported.");
    }

    default ResourceIterator<Node> findNodes(Label label, String key, String template, StringSearchMode searchMode) {
        throw new UnsupportedOperationException("Specialized string queries are not supported");
    }

    Node findNode(Label var1, String var2, Object var3);

    ResourceIterator<Node> findNodes(Label var1);

    ResourceIterable<Label> getAllLabelsInUse();

    ResourceIterable<RelationshipType> getAllRelationshipTypesInUse();

    ResourceIterable<Label> getAllLabels();

    ResourceIterable<RelationshipType> getAllRelationshipTypes();

    ResourceIterable<String> getAllPropertyKeys();

    boolean isAvailable(long var1);

    void shutdown();

    Transaction beginTx();

    Transaction beginTx(long var1, TimeUnit var3);

    Result execute(String var1) throws QueryExecutionException;

    Result execute(String var1, long var2, TimeUnit var4) throws QueryExecutionException;

    Result execute(String var1, Map<String, Object> var2) throws QueryExecutionException;

    Result execute(String var1, Map<String, Object> var2, long var3, TimeUnit var5) throws QueryExecutionException;

    <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> var1);

    <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> var1);

    KernelEventHandler registerKernelEventHandler(KernelEventHandler var1);

    KernelEventHandler unregisterKernelEventHandler(KernelEventHandler var1);

    Schema schema();

    /** @deprecated */
    //@Deprecated
    IndexManager index();

    TraversalDescription traversalDescription();

    BidirectionalTraversalDescription bidirectionalTraversalDescription();
}
