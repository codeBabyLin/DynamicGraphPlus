//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v1;

import org.neo4j.driver.Value;
import org.neo4j.driver.internal.messaging.MessageFormat.Reader;
import org.neo4j.driver.internal.messaging.ResponseMessageHandler;
import org.neo4j.driver.internal.messaging.ValueUnpacker;
import org.neo4j.driver.internal.packstream.PackInput;

import java.io.IOException;
import java.util.Map;

public class MessageReaderV1 implements Reader {
    private final ValueUnpacker unpacker;

    public MessageReaderV1(PackInput input) {
        this((ValueUnpacker)(new ValueUnpackerV1(input)));
    }

    protected MessageReaderV1(ValueUnpacker unpacker) {
        this.unpacker = unpacker;
    }

    public void read(ResponseMessageHandler handler) throws IOException {
        this.unpacker.unpackStructHeader();
        int type = this.unpacker.unpackStructSignature();
        switch(type) {
            case 112:
                this.unpackSuccessMessage(handler);
                break;
            case 113:
                this.unpackRecordMessage(handler);
                break;
            case 126:
                this.unpackIgnoredMessage(handler);
                break;
            case 127:
                this.unpackFailureMessage(handler);
                break;
            default:
                throw new IOException("Unknown message type: " + type);
        }

    }

    private void unpackSuccessMessage(ResponseMessageHandler output) throws IOException {
        Map<String, Value> map = this.unpacker.unpackMap();
        output.handleSuccessMessage(map);
    }

    private void unpackFailureMessage(ResponseMessageHandler output) throws IOException {
        Map<String, Value> params = this.unpacker.unpackMap();
        String code = ((Value)params.get("code")).asString();
        String message = ((Value)params.get("message")).asString();
        output.handleFailureMessage(code, message);
    }

    private void unpackIgnoredMessage(ResponseMessageHandler output) throws IOException {
        output.handleIgnoredMessage();
    }

    private void unpackRecordMessage(ResponseMessageHandler output) throws IOException {
        Value[] fields = this.unpacker.unpackArray();
        output.handleRecordMessage(fields);
    }
}
