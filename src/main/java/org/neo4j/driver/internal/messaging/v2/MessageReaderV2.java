//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v2;

import org.neo4j.driver.internal.messaging.v1.MessageReaderV1;
import org.neo4j.driver.internal.packstream.PackInput;

public class MessageReaderV2 extends MessageReaderV1 {
    public MessageReaderV2(PackInput input) {
        super(new ValueUnpackerV2(input));
    }
}
