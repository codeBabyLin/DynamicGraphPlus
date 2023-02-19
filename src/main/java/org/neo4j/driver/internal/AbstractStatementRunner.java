//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal;

import org.neo4j.driver.*;
import org.neo4j.driver.internal.util.Extract;
import org.neo4j.driver.internal.value.MapValue;

import java.util.Map;

public abstract class AbstractStatementRunner implements StatementRunner {
    public AbstractStatementRunner() {
    }

    public final StatementResult run(String statementTemplate, Value parameters) {
        return this.run(new Statement(statementTemplate, parameters));
    }

    public final StatementResult run(String statementTemplate, Map<String, Object> statementParameters) {
        return this.run(statementTemplate, parameters(statementParameters));
    }

    public final StatementResult run(String statementTemplate, Record statementParameters) {
        return this.run(statementTemplate, parameters(statementParameters));
    }

    public final StatementResult run(String statementText) {
        return this.run(statementText, Values.EmptyMap);
    }


    //DynamicGraph
    public final StatementResult run(String statementTemplate, Value parameters,long version) {
        return this.run(new Statement(statementTemplate, parameters,version));
    }

    public final StatementResult run(String statementTemplate, Map<String, Object> statementParameters,long version) {
        return this.run(statementTemplate, parameters(statementParameters),version);
    }

    public final StatementResult run(String statementTemplate, Record statementParameters,long version) {
        return this.run(statementTemplate, parameters(statementParameters),version);
    }

    public final StatementResult run(String statementText,long version) {
        return this.run(statementText, Values.EmptyMap,version);
    }

    //DynamicGraph

    public static Value parameters(Record record) {
        return record == null ? Values.EmptyMap : parameters(record.asMap());
    }

    public static Value parameters(Map<String, Object> map) {
        return (Value)(map != null && !map.isEmpty() ? new MapValue(Extract.mapOfValues(map)) : Values.EmptyMap);
    }
}
