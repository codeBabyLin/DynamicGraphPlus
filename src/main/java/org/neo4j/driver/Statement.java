//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver;

import org.neo4j.driver.internal.util.Iterables;
import org.neo4j.driver.internal.util.Preconditions;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.util.Immutable;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Immutable
public class Statement {
    private final String text;
    private final Value parameters;

    private long version;
    private boolean isWithVersion;

    public Statement(String text, Value parameters) {
        this.text = validateQuery(text);
        if (parameters == null) {
            this.parameters = Values.EmptyMap;
        } else {
            if (!(parameters instanceof MapValue)) {
                throw new IllegalArgumentException("The parameters should be provided as Map type. Unsupported parameters type: " + parameters.type().name());
            }

            this.parameters = parameters;
        }

    }
    public Statement(String text, Value parameters,long version) {
        this.text = validateQuery(text);
        if (parameters == null) {
            this.parameters = Values.EmptyMap;
        } else {
            if (!(parameters instanceof MapValue)) {
                throw new IllegalArgumentException("The parameters should be provided as Map type. Unsupported parameters type: " + parameters.type().name());
            }

            this.parameters = parameters;
        }
        //this.version = version;
        this.setVersion(version);

    }

    public Statement(String text, Map<String, Object> parameters) {
        this(text, Values.value(parameters));
    }
    public Statement(String text, Map<String, Object> parameters,long version) {
        this(text, Values.value(parameters),version);
    }

    public Statement(String text) {
        this(text, Values.EmptyMap);
    }
    public Statement(String text,long version) {
        this(text, Values.EmptyMap,version);
    }

    public String text() {
        return this.text;
    }

    public Value parameters() {
        return this.parameters;
    }

    public long getVersion() {
        return version;
    }

    public boolean isWithVersion() {
        return isWithVersion;
    }

    public void setVersion(long version) {
        this.version = version;
        this.isWithVersion = true;
    }

    public Statement withText(String newText) {
        return new Statement(newText, this.parameters);
    }

    public Statement withParameters(Value newParameters) {
        return new Statement(this.text, newParameters);
    }

    public Statement withParameters(Map<String, Object> newParameters) {
        return new Statement(this.text, newParameters);
    }

    public Statement withUpdatedParameters(Value updates) {
        if (updates != null && !updates.isEmpty()) {
            Map<String, Value> newParameters = Iterables.newHashMapWithSize(Math.max(this.parameters.size(), updates.size()));
            newParameters.putAll(this.parameters.asMap(Values.ofValue()));
            Iterator var3 = updates.asMap(Values.ofValue()).entrySet().iterator();

            while(var3.hasNext()) {
                Entry<String, Value> entry = (Entry)var3.next();
                Value value = (Value)entry.getValue();
                if (value.isNull()) {
                    newParameters.remove(entry.getKey());
                } else {
                    newParameters.put(entry.getKey(), value);
                }
            }

            return this.withParameters(Values.value(newParameters));
        } else {
            return this;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Statement statement = (Statement)o;
            return this.text.equals(statement.text) && this.parameters.equals(statement.parameters);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.text.hashCode();
        result = 31 * result + this.parameters.hashCode();
        return result;
    }

    public String toString() {
        return String.format("Statement{text='%s', parameters=%s}", this.text, this.parameters);
    }

    private static String validateQuery(String query) {
        Preconditions.checkArgument(query != null, "Cypher query should not be null");
        Preconditions.checkArgument(!query.isEmpty(), "Cypher query should not be an empty string");
        return query;
    }
}
