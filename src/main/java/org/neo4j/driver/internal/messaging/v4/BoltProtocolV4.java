//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v4;

import org.neo4j.driver.Statement;
import org.neo4j.driver.internal.BookmarksHolder;
import org.neo4j.driver.internal.async.ExplicitTransaction;
import org.neo4j.driver.internal.cursor.InternalStatementResultCursorFactory;
import org.neo4j.driver.internal.cursor.StatementResultCursorFactory;
import org.neo4j.driver.internal.handlers.AbstractPullAllResponseHandler;
import org.neo4j.driver.internal.handlers.PullHandlers;
import org.neo4j.driver.internal.handlers.RunResponseHandler;
import org.neo4j.driver.internal.handlers.pulln.BasicPullResponseHandler;
import org.neo4j.driver.internal.messaging.BoltProtocol;
import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.request.RunWithMetadataMessage;
import org.neo4j.driver.internal.messaging.v3.BoltProtocolV3;
import org.neo4j.driver.internal.spi.Connection;

public class BoltProtocolV4 extends BoltProtocolV3 {
    public static final int VERSION = 4;
    public static final BoltProtocol INSTANCE = new BoltProtocolV4();

    public BoltProtocolV4() {
    }

    public MessageFormat createMessageFormat() {
        return new MessageFormatV4();
    }

    protected StatementResultCursorFactory buildResultCursorFactory(Connection connection, Statement statement, BookmarksHolder bookmarksHolder, ExplicitTransaction tx, RunWithMetadataMessage runMessage, boolean waitForRunResponse) {
        //DynamiGraph
        if(statement.isWithVersion()){
            runMessage.setVersion(statement.getVersion());
        }
        //DynamiGraph
        RunResponseHandler runHandler = new RunResponseHandler(METADATA_EXTRACTOR);
        AbstractPullAllResponseHandler pullAllHandler = PullHandlers.newBoltV3PullAllHandler(statement, runHandler, connection, bookmarksHolder, tx);
        BasicPullResponseHandler pullHandler = PullHandlers.newBoltV4PullHandler(statement, runHandler, connection, bookmarksHolder, tx);
        return new InternalStatementResultCursorFactory(connection, runMessage, runHandler, pullHandler, pullAllHandler, waitForRunResponse);
    }

    protected void verifyDatabaseNameBeforeTransaction(String databaseName) {
    }

    public int version() {
        return 4;
    }
}
