//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging;

import org.neo4j.driver.Value;

import java.io.IOException;
import java.util.Map;

public interface ValueUnpacker {
    long unpackStructHeader() throws IOException;

    int unpackStructSignature() throws IOException;

    Map<String, Value> unpackMap() throws IOException;

    Value[] unpackArray() throws IOException;
}
