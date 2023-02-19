//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v3.runtime;

import java.time.Clock;
import java.time.Duration;
import org.neo4j.bolt.BoltChannel;
import org.neo4j.bolt.runtime.BoltResult;
import org.neo4j.bolt.runtime.BoltResultHandle;
import org.neo4j.bolt.v1.runtime.TransactionStateMachineV1SPI;
import org.neo4j.bolt.v1.runtime.TransactionStateMachineV1SPI.BoltResultHandleV1;
import org.neo4j.cypher.internal.javacompat.QueryResultProvider;
import org.neo4j.kernel.impl.query.TransactionalContext;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.values.virtual.MapValue;

public class TransactionStateMachineV3SPI extends TransactionStateMachineV1SPI {
    public TransactionStateMachineV3SPI(GraphDatabaseAPI db, BoltChannel boltChannel, Duration txAwaitDuration, Clock clock) {
        super(db, boltChannel, txAwaitDuration, clock);
    }

    protected BoltResultHandle newBoltResultHandle(String statement, MapValue params, TransactionalContext transactionalContext) {
        return new BoltResultHandleV3(statement, params, transactionalContext);
    }

    private class BoltResultHandleV3 extends BoltResultHandleV1 {
        BoltResultHandleV3(String statement, MapValue params, TransactionalContext transactionalContext) {
            //super(TransactionStateMachineV3SPI.this, statement, params, transactionalContext);
            super( statement, params, transactionalContext);
        }

        protected BoltResult newBoltResult(QueryResultProvider result, Clock clock) {
            return new CypherAdapterStreamV3(result.queryResult(), clock);
        }
    }
}
