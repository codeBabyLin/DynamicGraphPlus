//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.runtime;

import org.neo4j.internal.kernel.api.exceptions.KernelException;

public interface BoltResultHandle {
    BoltResult start() throws KernelException;

    void close(boolean var1);

    void terminate();
}
