//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging;

import org.neo4j.driver.internal.messaging.MessageFormat.Writer;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractMessageWriter implements Writer {
    private final ValuePacker packer;
    private final Map<Byte, MessageEncoder> encodersByMessageSignature;

    protected AbstractMessageWriter(ValuePacker packer, Map<Byte, MessageEncoder> encodersByMessageSignature) {
        this.packer = (ValuePacker)Objects.requireNonNull(packer);
        this.encodersByMessageSignature = (Map)Objects.requireNonNull(encodersByMessageSignature);
    }

    public final void write(Message msg) throws IOException {
        byte signature = msg.signature();
        MessageEncoder encoder = (MessageEncoder)this.encodersByMessageSignature.get(signature);
        if (encoder == null) {
            throw new IOException("No encoder found for message " + msg + " with signature " + signature);
        } else {
            encoder.encode(msg, this.packer);
        }
    }
}
