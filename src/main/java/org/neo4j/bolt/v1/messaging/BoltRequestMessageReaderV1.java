//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v1.messaging;

import java.util.Arrays;
import java.util.List;
import org.neo4j.bolt.messaging.BoltRequestMessageReader;
import org.neo4j.bolt.messaging.BoltResponseMessageWriter;
import org.neo4j.bolt.messaging.RequestMessageDecoder;
import org.neo4j.bolt.runtime.BoltConnection;
import org.neo4j.bolt.runtime.BoltResponseHandler;
import org.neo4j.bolt.v1.messaging.decoder.AckFailureMessageDecoder;
import org.neo4j.bolt.v1.messaging.decoder.DiscardAllMessageDecoder;
import org.neo4j.bolt.v1.messaging.decoder.InitMessageDecoder;
import org.neo4j.bolt.v1.messaging.decoder.PullAllMessageDecoder;
import org.neo4j.bolt.v1.messaging.decoder.ResetMessageDecoder;
import org.neo4j.bolt.v1.messaging.decoder.RunMessageDecoder;
import org.neo4j.logging.Log;
import org.neo4j.logging.internal.LogService;

public class BoltRequestMessageReaderV1 extends BoltRequestMessageReader {
    public BoltRequestMessageReaderV1(BoltConnection connection, BoltResponseMessageWriter responseMessageWriter, LogService logService) {
        super(connection, newSimpleResponseHandler(connection, responseMessageWriter, logService), buildDecoders(connection, responseMessageWriter, logService));
    }

    private static List<RequestMessageDecoder> buildDecoders(BoltConnection connection, BoltResponseMessageWriter responseMessageWriter, LogService logService) {
        BoltResponseHandler initHandler = newSimpleResponseHandler(connection, responseMessageWriter, logService);
        BoltResponseHandler runHandler = newSimpleResponseHandler(connection, responseMessageWriter, logService);
        BoltResponseHandler resultHandler = new ResultHandler(responseMessageWriter, connection, internalLog(logService));
        BoltResponseHandler defaultHandler = newSimpleResponseHandler(connection, responseMessageWriter, logService);
        return Arrays.asList(new InitMessageDecoder(initHandler), new AckFailureMessageDecoder(defaultHandler), new ResetMessageDecoder(connection, defaultHandler), new RunMessageDecoder(runHandler), new DiscardAllMessageDecoder(resultHandler), new PullAllMessageDecoder(resultHandler));
    }

    private static BoltResponseHandler newSimpleResponseHandler(BoltConnection connection, BoltResponseMessageWriter responseMessageWriter, LogService logService) {
        return new MessageProcessingHandler(responseMessageWriter, connection, internalLog(logService));
    }

    private static Log internalLog(LogService logService) {
        return logService.getInternalLog(BoltRequestMessageReaderV1.class);
    }
}
