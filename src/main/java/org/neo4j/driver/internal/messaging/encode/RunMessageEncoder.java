//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.encode;

import org.neo4j.driver.internal.messaging.Message;
import org.neo4j.driver.internal.messaging.MessageEncoder;
import org.neo4j.driver.internal.messaging.ValuePacker;
import org.neo4j.driver.internal.messaging.request.RunMessage;
import org.neo4j.driver.internal.util.Preconditions;
import org.neo4j.driver.internal.value.IntegerValue;

import java.io.IOException;

public class RunMessageEncoder implements MessageEncoder {
    public RunMessageEncoder() {
    }

    public void encode(Message message, ValuePacker packer) throws IOException {
        Preconditions.checkArgument(message, RunMessage.class);
        RunMessage runMessage = (RunMessage)message;
        if(runMessage.isWithVersion()){
            packer.packStructHeader(3, runMessage.signature());
            packer.pack(runMessage.statement());
            packer.pack(runMessage.parameters());
            //DynamicGraph
            packer.pack(new IntegerValue(runMessage.getVersion()));
            //DynamicGraph
        }
        else {
            packer.packStructHeader(2, runMessage.signature());
            packer.pack(runMessage.statement());
            packer.pack(runMessage.parameters());
            //DynamicGraph
           // packer.pack(new IntegerValue(runMessage.getVersion()));
            //DynamicGraph
        }

    }
}
