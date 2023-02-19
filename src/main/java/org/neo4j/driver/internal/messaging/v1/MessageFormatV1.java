//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v1;

import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.packstream.PackOutput;

public class MessageFormatV1 implements MessageFormat {
    public static final byte NODE = 78;
    public static final byte RELATIONSHIP = 82;
    public static final byte UNBOUND_RELATIONSHIP = 114;
    public static final byte PATH = 80;
    public static final int NODE_FIELDS = 3;

    public MessageFormatV1() {
    }

    public Writer newWriter(PackOutput output, boolean byteArraySupportEnabled) {
        return new MessageWriterV1(output, byteArraySupportEnabled);
    }

    public Reader newReader(PackInput input) {
        return new MessageReaderV1(input);
    }
}
