//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.graphdb;

import java.util.Map;

public interface PropertyContainer {
    GraphDatabaseService getGraphDatabase();

    boolean hasProperty(String var1);

    Object getProperty(String var1);
    Object getProperty(String var1, long version);

    Object getProperty(String var1, Object var2);

    void setProperty(String var1, Object var2);

    Object removeProperty(String var1);

    Iterable<String> getPropertyKeys();

    Map<String, Object> getProperties(String... var1);

    Map<String, Object> getAllProperties();
}
