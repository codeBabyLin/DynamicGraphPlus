//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.encode;

import org.neo4j.driver.internal.messaging.Message;
import org.neo4j.driver.internal.messaging.MessageEncoder;
import org.neo4j.driver.internal.messaging.ValuePacker;
import org.neo4j.driver.internal.messaging.request.RunWithMetadataMessage;
import org.neo4j.driver.internal.util.Preconditions;
import org.neo4j.driver.internal.value.IntegerValue;

import java.io.IOException;

public class RunWithMetadataMessageEncoder implements MessageEncoder {
    public RunWithMetadataMessageEncoder() {
    }

    public void encode(Message message, ValuePacker packer) throws IOException {
        Preconditions.checkArgument(message, RunWithMetadataMessage.class);
        RunWithMetadataMessage runMessage = (RunWithMetadataMessage)message;

        if(runMessage.isWithVersion()){
            packer.packStructHeader(4, runMessage.signature());
            packer.pack(runMessage.statement());
            packer.pack(runMessage.parameters());
            packer.pack(runMessage.metadata());
            //DynamicGraph
            packer.pack(new IntegerValue(runMessage.getVersion()));
            //DynamicGraph
        }
        else{
            packer.packStructHeader(3, runMessage.signature());
            packer.pack(runMessage.statement());
            packer.pack(runMessage.parameters());
            packer.pack(runMessage.metadata());
        }


    }
}
