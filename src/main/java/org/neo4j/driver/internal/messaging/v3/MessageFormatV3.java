//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v3;

import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.v2.MessageReaderV2;
import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.packstream.PackOutput;

public class MessageFormatV3 implements MessageFormat {
    public MessageFormatV3() {
    }

    public Writer newWriter(PackOutput output, boolean byteArraySupportEnabled) {
        return new MessageWriterV3(output);
    }

    public Reader newReader(PackInput input) {
        return new MessageReaderV2(input);
    }
}
