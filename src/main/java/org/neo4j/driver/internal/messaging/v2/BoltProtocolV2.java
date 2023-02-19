//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v2;

import org.neo4j.driver.internal.messaging.BoltProtocol;
import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.v1.BoltProtocolV1;

public class BoltProtocolV2 extends BoltProtocolV1 {
    public static final int VERSION = 2;
    public static final BoltProtocol INSTANCE = new BoltProtocolV2();

    public BoltProtocolV2() {
    }

    public MessageFormat createMessageFormat() {
        return new MessageFormatV2();
    }

    public int version() {
        return 2;
    }
}
