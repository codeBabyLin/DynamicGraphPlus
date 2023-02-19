//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.storageengine.api;

import org.neo4j.values.storable.Value;
import org.neo4j.values.storable.ValueGroup;

public interface StoragePropertyCursor extends StorageCursor {
    void init(long var1);

    int propertyKey();

    boolean nextHistory();

    long propertyVersion();

    ValueGroup propertyType();

    Value propertyValue();
}
