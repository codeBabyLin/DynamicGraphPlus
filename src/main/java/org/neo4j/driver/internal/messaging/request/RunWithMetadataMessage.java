//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.request;

import org.neo4j.driver.*;
import org.neo4j.driver.internal.Bookmarks;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class RunWithMetadataMessage extends MessageWithMetadata {
    public static final byte SIGNATURE = 16;
    private final String statement;
    private  long version ;
    private boolean isWithVersion;
    private final Map<String, Value> parameters;

    public static RunWithMetadataMessage autoCommitTxRunMessage(Statement statement, TransactionConfig config, String databaseName, AccessMode mode, Bookmarks bookmarks) {
        return autoCommitTxRunMessage(statement, config.timeout(), config.metadata(), databaseName, mode, bookmarks);
    }

    public static RunWithMetadataMessage autoCommitTxRunMessage(Statement statement, Duration txTimeout, Map<String, Value> txMetadata, String databaseName, AccessMode mode, Bookmarks bookmarks) {
        Map<String, Value> metadata = TransactionMetadataBuilder.buildMetadata(txTimeout, txMetadata, databaseName, mode, bookmarks);
        return new RunWithMetadataMessage(statement.text(), statement.parameters().asMap(Values.ofValue()), metadata,statement.getVersion());
    }

    public static RunWithMetadataMessage explicitTxRunMessage(Statement statement) {
        return new RunWithMetadataMessage(statement.text(), statement.parameters().asMap(Values.ofValue()), Collections.emptyMap(),statement.getVersion());
    }

    public static RunWithMetadataMessage autoCommitTxRunMessage(Statement statement, Duration txTimeout, Map<String, Value> txMetadata, String databaseName, AccessMode mode, Bookmarks bookmarks, long version) {
        Map<String, Value> metadata = TransactionMetadataBuilder.buildMetadata(txTimeout, txMetadata, databaseName, mode, bookmarks);
        return new RunWithMetadataMessage(statement.text(), statement.parameters().asMap(Values.ofValue()), metadata,version);
    }

    public static RunWithMetadataMessage explicitTxRunMessage(Statement statement, long version) {
        return new RunWithMetadataMessage(statement.text(), statement.parameters().asMap(Values.ofValue()), Collections.emptyMap(),version);
    }


    private RunWithMetadataMessage(String statement, Map<String, Value> parameters, Map<String, Value> metadata, long version) {
        super(metadata);
        this.statement = statement;
        this.parameters = parameters;
        //this.version = version;
        this.setVersion(version);
    }
    private RunWithMetadataMessage(String statement, Map<String, Value> parameters, Map<String, Value> metadata) {
        super(metadata);
        this.statement = statement;
        this.parameters = parameters;
    }
    public long getVersion(){
        return this.version;
    }

    public boolean isWithVersion() {
        return isWithVersion;
    }

    public void setVersion(long version) {

        this.version = version;
        this.isWithVersion = true;
    }

    public String statement() {
        return this.statement;
    }

    public Map<String, Value> parameters() {
        return this.parameters;
    }

    public byte signature() {
        return 16;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            RunWithMetadataMessage that = (RunWithMetadataMessage)o;
            return Objects.equals(this.statement, that.statement) && Objects.equals(this.parameters, that.parameters) && Objects.equals(this.metadata(), that.metadata());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.statement, this.parameters, this.metadata()});
    }

    public String toString() {
        return "RUN \"" + this.statement + "\" " + this.parameters + " " + this.metadata();
    }
}
