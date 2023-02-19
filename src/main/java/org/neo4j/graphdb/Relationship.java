//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.graphdb;

public interface Relationship extends Entity {

    //DynamicGraph

    long getRelVersion();


    //DynamicGraph

    long getId();

    void delete();

    Node getStartNode();

    Node getEndNode();

    Node getOtherNode(Node var1);

    Node[] getNodes();

    RelationshipType getType();

    boolean isType(RelationshipType var1);

    default long getStartNodeId() {
        return this.getStartNode().getId();
    }

    default long getEndNodeId() {
        return this.getEndNode().getId();
    }

    default long getOtherNodeId(long id) {
        long start = this.getStartNodeId();
        long end = this.getEndNodeId();
        if (id == start) {
            return end;
        } else if (id == end) {
            return start;
        } else {
            throw new NotFoundException("Node[" + id + "] not connected to this relationship[" + this.getId() + "]");
        }
    }
}
