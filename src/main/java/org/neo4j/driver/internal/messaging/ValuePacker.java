//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging;

import org.neo4j.driver.Value;

import java.io.IOException;
import java.util.Map;

public interface ValuePacker {
    void packStructHeader(int var1, byte var2) throws IOException;

    void pack(String var1) throws IOException;

    void pack(Value var1) throws IOException;

    void pack(Map<String, Value> var1) throws IOException;
}
