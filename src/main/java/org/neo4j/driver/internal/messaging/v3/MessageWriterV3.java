//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v3;

import org.neo4j.driver.internal.messaging.AbstractMessageWriter;
import org.neo4j.driver.internal.messaging.MessageEncoder;
import org.neo4j.driver.internal.messaging.encode.*;
import org.neo4j.driver.internal.messaging.v2.ValuePackerV2;
import org.neo4j.driver.internal.packstream.PackOutput;
import org.neo4j.driver.internal.util.Iterables;

import java.util.Map;

public class MessageWriterV3 extends AbstractMessageWriter {
    public MessageWriterV3(PackOutput output) {
        super(new ValuePackerV2(output), buildEncoders());
    }

    private static Map<Byte, MessageEncoder> buildEncoders() {
        Map<Byte, MessageEncoder> result = Iterables.newHashMapWithSize(9);
        result.put((byte)1, new HelloMessageEncoder());
        result.put((byte)2, new GoodbyeMessageEncoder());
        result.put((byte)16, new RunWithMetadataMessageEncoder());
        result.put((byte)47, new DiscardAllMessageEncoder());
        result.put((byte)63, new PullAllMessageEncoder());
        result.put((byte)17, new BeginMessageEncoder());
        result.put((byte)18, new CommitMessageEncoder());
        result.put((byte)19, new RollbackMessageEncoder());
        result.put((byte)15, new ResetMessageEncoder());
        return result;
    }
}
