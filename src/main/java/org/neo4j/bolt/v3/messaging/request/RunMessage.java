//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v3.messaging.request;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import org.neo4j.bolt.messaging.BoltIOException;
import org.neo4j.bolt.messaging.RequestMessage;
import org.neo4j.bolt.v1.runtime.bookmarking.Bookmark;
import org.neo4j.values.virtual.MapValue;
import org.neo4j.values.virtual.VirtualValues;

public class RunMessage implements RequestMessage {
    public static final byte SIGNATURE = 16;
    private final String statement;
    private final MapValue params;
    private final MapValue meta;
    private final Bookmark bookmark;
    private final Duration txTimeout;
    private final Map<String, Object> txMetadata;
    private long version;
    private boolean isWithVeriosn;

    public RunMessage(String statement) throws BoltIOException {
        this(statement, VirtualValues.EMPTY_MAP);
    }

    public RunMessage(String statement, MapValue params) throws BoltIOException {
        this(statement, params, VirtualValues.EMPTY_MAP);
    }

    public RunMessage(String statement, MapValue params, MapValue meta) throws BoltIOException {
        this.statement = (String)Objects.requireNonNull(statement);
        this.params = (MapValue)Objects.requireNonNull(params);
        this.meta = (MapValue)Objects.requireNonNull(meta);
        this.bookmark = Bookmark.fromParamsOrNull(meta);
        this.txTimeout = MessageMetadataParser.parseTransactionTimeout(meta);
        this.txMetadata = MessageMetadataParser.parseTransactionMetadata(meta);
    }

    public RunMessage(String statement,long version) throws BoltIOException {
        this(statement, VirtualValues.EMPTY_MAP,version);
    }

    public RunMessage(String statement, MapValue params,long version) throws BoltIOException {
        this(statement, params, VirtualValues.EMPTY_MAP,version);
    }

    public RunMessage(String statement, MapValue params, MapValue meta,long version) throws BoltIOException {
        this.statement = (String)Objects.requireNonNull(statement);
        this.params = (MapValue)Objects.requireNonNull(params);
        this.meta = (MapValue)Objects.requireNonNull(meta);
        this.bookmark = Bookmark.fromParamsOrNull(meta);
        this.txTimeout = MessageMetadataParser.parseTransactionTimeout(meta);
        this.txMetadata = MessageMetadataParser.parseTransactionMetadata(meta);
        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
        this.isWithVeriosn = true;
    }

    public boolean isWithVeriosn() {
        return isWithVeriosn;
    }

    public String statement() {
        return this.statement;
    }

    public MapValue params() {
        return this.params;
    }

    public MapValue meta() {
        return this.meta;
    }

    public boolean safeToProcessInAnyState() {
        return false;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            RunMessage that = (RunMessage)o;
            return Objects.equals(this.statement, that.statement) && Objects.equals(this.params, that.params) && Objects.equals(this.meta, that.meta);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.statement, this.params, this.meta});
    }

    public String toString() {
        return "RUN " + this.statement + ' ' + this.params + ' ' + this.meta;
    }

    public Bookmark bookmark() {
        return this.bookmark;
    }

    public Duration transactionTimeout() {
        return this.txTimeout;
    }

    public Map<String, Object> transactionMetadata() {
        return this.txMetadata;
    }
}
