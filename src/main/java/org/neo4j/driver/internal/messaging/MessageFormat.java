//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging;

import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.packstream.PackOutput;

import java.io.IOException;

public interface MessageFormat {
    MessageFormat.Writer newWriter(PackOutput var1, boolean var2);

    MessageFormat.Reader newReader(PackInput var1);

    public interface Reader {
        void read(ResponseMessageHandler var1) throws IOException;
    }

    public interface Writer {
        void write(Message var1) throws IOException;
    }
}
