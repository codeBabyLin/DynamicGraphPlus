//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v1;

import org.neo4j.driver.Value;
import org.neo4j.driver.internal.messaging.ValuePacker;
import org.neo4j.driver.internal.packstream.PackOutput;
import org.neo4j.driver.internal.packstream.PackStream.Packer;
import org.neo4j.driver.internal.packstream.PackStream.UnPackable;
import org.neo4j.driver.internal.value.InternalValue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ValuePackerV1 implements ValuePacker {
    protected final Packer packer;
    private final boolean byteArraySupportEnabled;

    public ValuePackerV1(PackOutput output, boolean byteArraySupportEnabled) {
        this.packer = new Packer(output);
        this.byteArraySupportEnabled = byteArraySupportEnabled;
    }

    public final void packStructHeader(int size, byte signature) throws IOException {
        this.packer.packStructHeader(size, signature);
    }

    public final void pack(String string) throws IOException {
        this.packer.pack(string);
    }

    public final void pack(Value value) throws IOException {
        if (value instanceof InternalValue) {
            this.packInternalValue((InternalValue)value);
        } else {
            throw new IllegalArgumentException("Unable to pack: " + value);
        }
    }

    public final void pack(Map<String, Value> map) throws IOException {
        if (map != null && map.size() != 0) {
            this.packer.packMapHeader(map.size());
            Iterator var2 = map.entrySet().iterator();

            while(var2.hasNext()) {
                Entry<String, Value> entry = (Entry)var2.next();
                this.packer.pack((String)entry.getKey());
                this.pack((Value)entry.getValue());
            }

        } else {
            this.packer.packMapHeader(0);
        }
    }

    protected void packInternalValue(InternalValue value) throws IOException {
        Iterator var2;
        switch(value.typeConstructor()) {
            case NULL:
                this.packer.packNull();
                break;
            case BYTES:
                if (!this.byteArraySupportEnabled) {
                    throw new UnPackable("Packing bytes is not supported as the current server this driver connected to does not support unpack bytes.");
                }

                this.packer.pack(value.asByteArray());
                break;
            case STRING:
                this.packer.pack(value.asString());
                break;
            case BOOLEAN:
                this.packer.pack(value.asBoolean());
                break;
            case INTEGER:
                this.packer.pack(value.asLong());
                break;
            case FLOAT:
                this.packer.pack(value.asDouble());
                break;
            case MAP:
                this.packer.packMapHeader(value.size());
                var2 = value.keys().iterator();

                while(var2.hasNext()) {
                    String s = (String)var2.next();
                    this.packer.pack(s);
                    this.pack(value.get(s));
                }

                return;
            case LIST:
                this.packer.packListHeader(value.size());
                var2 = value.values().iterator();

                while(var2.hasNext()) {
                    Value item = (Value)var2.next();
                    this.pack(item);
                }

                return;
            default:
                throw new IOException("Unknown type: " + value.type().name());
        }

    }
}
