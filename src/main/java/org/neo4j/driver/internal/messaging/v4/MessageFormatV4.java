//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v4;

import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.v2.MessageReaderV2;
import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.packstream.PackOutput;

public class MessageFormatV4 implements MessageFormat {
    public MessageFormatV4() {
    }

    public Writer newWriter(PackOutput output, boolean byteArraySupportEnabled) {
        return new MessageWriterV4(output);
    }

    public Reader newReader(PackInput input) {
        return new MessageReaderV2(input);
    }
}
