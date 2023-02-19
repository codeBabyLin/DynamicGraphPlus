//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.request;

import org.neo4j.driver.Value;
import org.neo4j.driver.internal.messaging.Message;

import java.util.Collections;
import java.util.Map;

public class RunMessage implements Message {
    public static final byte SIGNATURE = 16;
    private final String statement;
    private final Map<String, Value> parameters;
    private long version;
    private boolean isWithVersion;
    public RunMessage(String statement) {
        this(statement, Collections.emptyMap());
    }

    public RunMessage(String statement, Map<String, Value> parameters) {
        this.statement = statement;
        this.parameters = parameters;
    }
    public RunMessage(String statement, Map<String, Value> parameters,long version) {
        this.statement = statement;
        this.parameters = parameters;
        this.setVersion(version);
        //this.version = version;
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

    public Map<String, Value> parameters() {
        return this.parameters;
    }

    public byte signature() {
        return 16;
    }


    public String toString() {
        return String.format("RUN \"%s\" %s", this.statement, this.parameters);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            boolean var10000;
            label44: {
                label30: {
                    RunMessage that = (RunMessage)o;
                    if (this.parameters != null) {
                        if (!this.parameters.equals(that.parameters)) {
                            break label30;
                        }
                    } else if (that.parameters != null) {
                        break label30;
                    }

                    if (this.statement != null) {
                        if (this.statement.equals(that.statement)) {
                            break label44;
                        }
                    } else if (that.statement == null) {
                        break label44;
                    }
                }

                var10000 = false;
                return var10000;
            }

            var10000 = true;
            return var10000;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.statement != null ? this.statement.hashCode() : 0;
        result = 31 * result + (this.parameters != null ? this.parameters.hashCode() : 0);
        return result;
    }
}
