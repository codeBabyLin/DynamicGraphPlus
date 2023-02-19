//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v2;

import org.neo4j.driver.internal.messaging.v1.MessageWriterV1;
import org.neo4j.driver.internal.packstream.PackOutput;

public class MessageWriterV2 extends MessageWriterV1 {
    public MessageWriterV2(PackOutput output) {
        super(new ValuePackerV2(output));
    }
}
