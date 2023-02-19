//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v1;

import org.neo4j.driver.internal.messaging.AbstractMessageWriter;
import org.neo4j.driver.internal.messaging.MessageEncoder;
import org.neo4j.driver.internal.messaging.ValuePacker;
import org.neo4j.driver.internal.messaging.encode.*;
import org.neo4j.driver.internal.packstream.PackOutput;
import org.neo4j.driver.internal.util.Iterables;

import java.util.Map;

public class MessageWriterV1 extends AbstractMessageWriter {
    public MessageWriterV1(PackOutput output, boolean byteArraySupportEnabled) {
        this(new ValuePackerV1(output, byteArraySupportEnabled));
    }

    protected MessageWriterV1(ValuePacker packer) {
        super(packer, buildEncoders());
    }

    private static Map<Byte, MessageEncoder> buildEncoders() {
        Map<Byte, MessageEncoder> result = Iterables.newHashMapWithSize(6);
        result.put((byte)47, new DiscardAllMessageEncoder());
        result.put((byte)1, new InitMessageEncoder());
        result.put((byte)63, new PullAllMessageEncoder());
        result.put((byte)15, new ResetMessageEncoder());
        result.put((byte)16, new RunMessageEncoder());
        return result;
    }
}
