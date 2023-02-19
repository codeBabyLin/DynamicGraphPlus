//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v1.messaging.request;

import java.util.Objects;
import org.neo4j.bolt.messaging.RequestMessage;
import org.neo4j.values.virtual.MapValue;
import org.neo4j.values.virtual.VirtualValues;

public class RunMessage implements RequestMessage {
    public static final byte SIGNATURE = 16;
    private final String statement;
    private final MapValue params;
    private long version;
    private boolean isWithVersion;

    public RunMessage(String statement) {
        this(statement, VirtualValues.EMPTY_MAP);
    }

    public RunMessage(String statement, MapValue params) {
        this.statement = (String)Objects.requireNonNull(statement);
        this.params = (MapValue)Objects.requireNonNull(params);
    }

    public RunMessage(String statement, MapValue params,long version) {
        this.statement = (String)Objects.requireNonNull(statement);
        this.params = (MapValue)Objects.requireNonNull(params);
        this.version = version;
    }

    public void setVersion(long version) {
        this.version = version;
        this.isWithVersion = true;
    }

    public long getVersion() {
        return version;
    }

    public boolean isWithVersion() {
        return isWithVersion;
    }

    public String statement() {
        return this.statement;
    }

    public MapValue params() {
        return this.params;
    }

    public boolean safeToProcessInAnyState() {
        return false;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            RunMessage that = (RunMessage)o;
            return Objects.equals(this.statement, that.statement) && Objects.equals(this.params, that.params);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.statement, this.params});
    }

    public String toString() {
        return "RUN " + this.statement + ' ' + this.params;
    }
}
