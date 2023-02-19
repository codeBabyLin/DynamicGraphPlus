//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v1;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalPath;
import org.neo4j.driver.internal.InternalPath.SelfContainedSegment;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.internal.messaging.ValueUnpacker;
import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.packstream.PackStream.Unpacker;
import org.neo4j.driver.internal.packstream.PackType;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.neo4j.driver.internal.util.Iterables;
import org.neo4j.driver.internal.value.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path.Segment;
import org.neo4j.driver.types.Relationship;

import java.io.IOException;
import java.util.*;

public class ValueUnpackerV1 implements ValueUnpacker {
    protected final Unpacker unpacker;

    public ValueUnpackerV1(PackInput input) {
        this.unpacker = new Unpacker(input);
    }

    public long unpackStructHeader() throws IOException {
        return this.unpacker.unpackStructHeader();
    }

    public int unpackStructSignature() throws IOException {
        return this.unpacker.unpackStructSignature();
    }

    public Map<String, Value> unpackMap() throws IOException {
        int size = (int)this.unpacker.unpackMapHeader();
        if (size == 0) {
            return Collections.emptyMap();
        } else {
            Map<String, Value> map = Iterables.newHashMapWithSize(size);

            for(int i = 0; i < size; ++i) {
                String key = this.unpacker.unpackString();
                map.put(key, this.unpack());
            }

            return map;
        }
    }

    public Value[] unpackArray() throws IOException {
        int size = (int)this.unpacker.unpackListHeader();
        Value[] values = new Value[size];

        for(int i = 0; i < size; ++i) {
            values[i] = this.unpack();
        }

        return values;
    }

    private Value unpack() throws IOException {
        PackType type = this.unpacker.peekNextType();
        switch(type) {
            case NULL:
                return Values.value(this.unpacker.unpackNull());
            case BOOLEAN:
                return Values.value(this.unpacker.unpackBoolean());
            case INTEGER:
                return Values.value(this.unpacker.unpackLong());
            case FLOAT:
                return Values.value(this.unpacker.unpackDouble());
            case BYTES:
                return Values.value(this.unpacker.unpackBytes());
            case STRING:
                return Values.value(this.unpacker.unpackString());
            case MAP:
                return new MapValue(this.unpackMap());
            case LIST:
                int size = (int)this.unpacker.unpackListHeader();
                Value[] vals = new Value[size];

                for(int j = 0; j < size; ++j) {
                    vals[j] = this.unpack();
                }

                return new ListValue(vals);
            case STRUCT:
                long size1 = this.unpacker.unpackStructHeader();
                byte structType = this.unpacker.unpackStructSignature();
                return this.unpackStruct(size1, structType);
            default:
                throw new IOException("Unknown value type: " + type);
        }
    }

    protected Value unpackStruct(long size, byte type) throws IOException {
        switch(type) {
            case 78:
                this.ensureCorrectStructSize(TypeConstructor.NODE, 3, size);
                InternalNode adapted = this.unpackNode();
                return new NodeValue(adapted);
            case 79:
            case 81:
            default:
                throw new IOException("Unknown struct type: " + type);
            case 80:
                this.ensureCorrectStructSize(TypeConstructor.PATH, 3, size);
                return this.unpackPath();
            case 82:
                this.ensureCorrectStructSize(TypeConstructor.RELATIONSHIP, 5, size);
                return this.unpackRelationship();
        }
    }

    private Value unpackRelationship() throws IOException {
        long urn = this.unpacker.unpackLong();
        long startUrn = this.unpacker.unpackLong();
        long endUrn = this.unpacker.unpackLong();
        String relType = this.unpacker.unpackString();
        Map<String, Value> props = this.unpackMap();
        InternalRelationship adapted = new InternalRelationship(urn, startUrn, endUrn, relType, props);
        return new RelationshipValue(adapted);
    }

    private InternalNode unpackNode() throws IOException {
        long urn = this.unpacker.unpackLong();
        int numLabels = (int)this.unpacker.unpackListHeader();
        List<String> labels = new ArrayList(numLabels);

        int numProps;
        for(numProps = 0; numProps < numLabels; ++numProps) {
            labels.add(this.unpacker.unpackString());
        }

        numProps = (int)this.unpacker.unpackMapHeader();
        Map<String, Value> props = Iterables.newHashMapWithSize(numProps);

        for(int j = 0; j < numProps; ++j) {
            String key = this.unpacker.unpackString();
            props.put(key, this.unpack());
        }

        return new InternalNode(urn, labels, props);
    }

    private Value unpackPath() throws IOException {
        Node[] uniqNodes = new Node[(int)this.unpacker.unpackListHeader()];

        for(int i = 0; i < uniqNodes.length; ++i) {
            this.ensureCorrectStructSize(TypeConstructor.NODE, 3, this.unpacker.unpackStructHeader());
            this.ensureCorrectStructSignature("NODE", (byte)78, this.unpacker.unpackStructSignature());
            uniqNodes[i] = this.unpackNode();
        }

        InternalRelationship[] uniqRels = new InternalRelationship[(int)this.unpacker.unpackListHeader()];

        int length;
        for(length = 0; length < uniqRels.length; ++length) {
            this.ensureCorrectStructSize(TypeConstructor.RELATIONSHIP, 3, this.unpacker.unpackStructHeader());
            this.ensureCorrectStructSignature("UNBOUND_RELATIONSHIP", (byte)114, this.unpacker.unpackStructSignature());
            long id = this.unpacker.unpackLong();
            String relType = this.unpacker.unpackString();
            Map<String, Value> props = this.unpackMap();
            uniqRels[length] = new InternalRelationship(id, -1L, -1L, relType, props);
        }

        length = (int)this.unpacker.unpackListHeader();
        Segment[] segments = new Segment[length / 2];
        Node[] nodes = new Node[segments.length + 1];
        Relationship[] rels = new Relationship[segments.length];
        Node prevNode = uniqNodes[0];
        nodes[0] = prevNode;

        for(int i = 0; i < segments.length; ++i) {
            int relIdx = (int)this.unpacker.unpackLong();
            Node nextNode = uniqNodes[(int)this.unpacker.unpackLong()];
            InternalRelationship rel;
            if (relIdx < 0) {
                rel = uniqRels[-relIdx - 1];
                rel.setStartAndEnd(nextNode.id(), prevNode.id());
            } else {
                rel = uniqRels[relIdx - 1];
                rel.setStartAndEnd(prevNode.id(), nextNode.id());
            }

            nodes[i + 1] = nextNode;
            rels[i] = rel;
            segments[i] = new SelfContainedSegment(prevNode, rel, nextNode);
            prevNode = nextNode;
        }

        return new PathValue(new InternalPath(Arrays.asList(segments), Arrays.asList(nodes), Arrays.asList(rels)));
    }

    protected final void ensureCorrectStructSize(TypeConstructor typeConstructor, int expected, long actual) {
        if ((long)expected != actual) {
            String structName = typeConstructor.toString();
            throw new ClientException(String.format("Invalid message received, serialized %s structures should have %d fields, received %s structure has %d fields.", structName, expected, structName, actual));
        }
    }

    private void ensureCorrectStructSignature(String structName, byte expected, byte actual) {
        if (expected != actual) {
            throw new ClientException(String.format("Invalid message received, expected a `%s`, signature 0x%s. Received signature was 0x%s.", structName, Integer.toHexString(expected), Integer.toHexString(actual)));
        }
    }
}
