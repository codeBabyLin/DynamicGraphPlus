//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v2;

import org.neo4j.driver.internal.messaging.v1.MessageFormatV1;
import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.packstream.PackOutput;

public class MessageFormatV2 extends MessageFormatV1 {
    public static final byte DATE = 68;
    public static final int DATE_STRUCT_SIZE = 1;
    public static final byte TIME = 84;
    public static final int TIME_STRUCT_SIZE = 2;
    public static final byte LOCAL_TIME = 116;
    public static final int LOCAL_TIME_STRUCT_SIZE = 1;
    public static final byte LOCAL_DATE_TIME = 100;
    public static final int LOCAL_DATE_TIME_STRUCT_SIZE = 2;
    public static final byte DATE_TIME_WITH_ZONE_OFFSET = 70;
    public static final byte DATE_TIME_WITH_ZONE_ID = 102;
    public static final int DATE_TIME_STRUCT_SIZE = 3;
    public static final byte DURATION = 69;
    public static final int DURATION_TIME_STRUCT_SIZE = 4;
    public static final byte POINT_2D_STRUCT_TYPE = 88;
    public static final int POINT_2D_STRUCT_SIZE = 3;
    public static final byte POINT_3D_STRUCT_TYPE = 89;
    public static final int POINT_3D_STRUCT_SIZE = 4;

    public MessageFormatV2() {
    }

    public Writer newWriter(PackOutput output, boolean byteArraySupportEnabled) {
        if (!byteArraySupportEnabled) {
            throw new IllegalArgumentException("Bolt V2 should support byte arrays");
        } else {
            return new MessageWriterV2(output);
        }
    }

    public Reader newReader(PackInput input) {
        return new MessageReaderV2(input);
    }
}
