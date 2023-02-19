//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.storageengine.api;

public interface StorageRelationshipCursor extends RelationshipVisitor<RuntimeException>, StorageEntityCursor {
    int type();

    long sourceNodeReference();

    long targetNodeReference();

    long relVersion();

    void visit(long var1, int var3, long var4, long var6);
}
