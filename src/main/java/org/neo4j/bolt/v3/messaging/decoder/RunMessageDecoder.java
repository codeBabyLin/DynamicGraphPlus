//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v3.messaging.decoder;

import java.io.IOException;
import org.neo4j.bolt.messaging.RequestMessage;
import org.neo4j.bolt.messaging.RequestMessageDecoder;
import org.neo4j.bolt.messaging.Neo4jPack.Unpacker;
import org.neo4j.bolt.runtime.BoltResponseHandler;
import org.neo4j.bolt.v3.messaging.request.RunMessage;
import org.neo4j.values.AnyValue;
import org.neo4j.values.storable.LongValue;
import org.neo4j.values.virtual.MapValue;

public class RunMessageDecoder implements RequestMessageDecoder {
    private final BoltResponseHandler responseHandler;

    public RunMessageDecoder(BoltResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public int signature() {
        return 16;
    }

    public BoltResponseHandler responseHandler() {
        return this.responseHandler;
    }

    public RequestMessage decode(Unpacker unpacker) throws IOException {
        String statement = unpacker.unpackString();
        MapValue params = unpacker.unpackMap();
        MapValue meta = unpacker.unpackMap();
        RunMessage runMessage= new RunMessage(statement, params, meta);
        return runMessage;
   /*     try {
            AnyValue v = unpacker.unpack();
            LongValue lv = (LongValue) v;
            long version = lv.asObjectCopy();
            runMessage.setVersion(version);
            return runMessage;
        }
        catch(IOException e){
            return runMessage;
        }*/

    }
}
