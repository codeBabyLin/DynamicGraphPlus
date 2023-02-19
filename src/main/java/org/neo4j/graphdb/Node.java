//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.graphdb;

import java.util.Map;

public interface Node extends Entity {
    long getId();

    void delete();

    Iterable<Relationship> getRelationships();

    boolean hasRelationship();

    Iterable<Relationship> getRelationships(RelationshipType... var1);

    Iterable<Relationship> getRelationships(Direction var1, RelationshipType... var2);

    boolean hasRelationship(RelationshipType... var1);

    boolean hasRelationship(Direction var1, RelationshipType... var2);

    Iterable<Relationship> getRelationships(Direction var1);

    boolean hasRelationship(Direction var1);

    Iterable<Relationship> getRelationships(RelationshipType var1, Direction var2);

    boolean hasRelationship(RelationshipType var1, Direction var2);

    Relationship getSingleRelationship(RelationshipType var1, Direction var2);

    Relationship createRelationshipTo(Node var1, RelationshipType var2);

    Iterable<RelationshipType> getRelationshipTypes();

    int getDegree();

    int getDegree(RelationshipType var1);

    int getDegree(Direction var1);

    int getDegree(RelationshipType var1, Direction var2);

    void addLabel(Label var1);

    void removeLabel(Label var1);

    boolean hasLabel(Label var1);

    Iterable<Label> getLabels();
    //DynamicGraph

    long getNodeVersion();
    Map<Label,Long> getVersionLabel();
    //Object getProperty(String var1,long version);

    //DynamicGraph

}
