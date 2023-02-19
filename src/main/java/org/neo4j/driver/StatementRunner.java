//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver;

import java.util.Map;

public interface StatementRunner {
    StatementResult run(String var1, Value var2);

    StatementResult run(String var1, Map<String, Object> var2);

    StatementResult run(String var1, Record var2);

    StatementResult run(String var1);

    StatementResult run(Statement var1);


    StatementResult run(String var1, Value var2, long version);

    StatementResult run(String var1, Map<String, Object> var2, long version);

    StatementResult run(String var1, Record var2, long version);

    StatementResult run(String var1, long version);

    StatementResult run(Statement var1, long version);

}
