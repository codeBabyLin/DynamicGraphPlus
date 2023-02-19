//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.messaging;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.neo4j.bolt.messaging.Neo4jPack.Unpacker;
import org.neo4j.bolt.runtime.BoltConnection;
import org.neo4j.bolt.runtime.BoltResponseHandler;
import org.neo4j.bolt.runtime.Neo4jError;
import org.neo4j.bolt.v1.packstream.PackStream.PackStreamException;
import org.neo4j.kernel.api.exceptions.Status.Request;

public abstract class BoltRequestMessageReader {
    private final BoltConnection connection;
    private final BoltResponseHandler externalErrorResponseHandler;
    private final Map<Integer, RequestMessageDecoder> decoders;

    protected BoltRequestMessageReader(BoltConnection connection, BoltResponseHandler externalErrorResponseHandler, List<RequestMessageDecoder> decoders) {
        this.connection = connection;
        this.externalErrorResponseHandler = externalErrorResponseHandler;
        this.decoders = (Map)decoders.stream().collect(Collectors.toMap(RequestMessageDecoder::signature, Function.identity()));
    }

    public void read(Unpacker unpacker) throws IOException {
        try {
            this.doRead(unpacker);
        } catch (BoltIOException var4) {
            if (!var4.causesFailureMessage()) {
                throw var4;
            }

            Neo4jError error = Neo4jError.from(var4);
            this.connection.enqueue((stateMachine) -> {
                stateMachine.handleExternalFailure(error, this.externalErrorResponseHandler);
            });
        }

    }

    private void doRead(Unpacker unpacker) throws IOException {
        try {
            unpacker.unpackStructHeader();
            int signature = unpacker.unpackStructSignature();
            RequestMessageDecoder decoder = (RequestMessageDecoder)this.decoders.get(Integer.valueOf(signature));
            if (decoder == null) {
                throw new BoltIOException(Request.InvalidFormat, String.format("Message 0x%s is not a valid message signature.", Integer.toHexString(signature)));
            } else {
                RequestMessage message = decoder.decode(unpacker);
                BoltResponseHandler responseHandler = decoder.responseHandler();
                this.connection.enqueue((stateMachine) -> {
                    stateMachine.process(message, responseHandler);
                });
            }
        } catch (PackStreamException var6) {
            throw new BoltIOException(Request.InvalidFormat, String.format("Unable to read message type. Error was: %s.", var6.getMessage()), var6);
        }
    }
}
