//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v1;

import org.neo4j.driver.Statement;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.internal.Bookmarks;
import org.neo4j.driver.internal.BookmarksHolder;
import org.neo4j.driver.internal.async.ExplicitTransaction;
import org.neo4j.driver.internal.async.connection.ChannelAttributes;
import org.neo4j.driver.internal.cursor.AsyncResultCursorOnlyFactory;
import org.neo4j.driver.internal.cursor.StatementResultCursorFactory;
import org.neo4j.driver.internal.handlers.*;
import org.neo4j.driver.internal.messaging.BoltProtocol;
import org.neo4j.driver.internal.messaging.Message;
import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.request.InitMessage;
import org.neo4j.driver.internal.messaging.request.MultiDatabaseUtil;
import org.neo4j.driver.internal.messaging.request.PullAllMessage;
import org.neo4j.driver.internal.messaging.request.RunMessage;
import org.neo4j.driver.internal.shaded.io.netty.channel.Channel;
import org.neo4j.driver.internal.shaded.io.netty.channel.ChannelPromise;
import org.neo4j.driver.internal.spi.Connection;
import org.neo4j.driver.internal.spi.ResponseHandler;
import org.neo4j.driver.internal.util.Futures;
import org.neo4j.driver.internal.util.MetadataExtractor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class BoltProtocolV1 implements BoltProtocol {
    public static final int VERSION = 1;
    public static final BoltProtocol INSTANCE = new BoltProtocolV1();
    public static final MetadataExtractor METADATA_EXTRACTOR = new MetadataExtractor("result_available_after", "result_consumed_after");
    private static final String BEGIN_QUERY = "BEGIN";
    private static final Message BEGIN_MESSAGE = new RunMessage("BEGIN");
    private static final Message COMMIT_MESSAGE = new RunMessage("COMMIT");
    private static final Message ROLLBACK_MESSAGE = new RunMessage("ROLLBACK");

    public BoltProtocolV1() {
    }

    public MessageFormat createMessageFormat() {
        return new MessageFormatV1();
    }

    public void initializeChannel(String userAgent, Map<String, Value> authToken, ChannelPromise channelInitializedPromise) {
        Channel channel = channelInitializedPromise.channel();
        InitMessage message = new InitMessage(userAgent, authToken);
        InitResponseHandler handler = new InitResponseHandler(channelInitializedPromise);
        ChannelAttributes.messageDispatcher(channel).enqueue(handler);
        channel.writeAndFlush(message, channel.voidPromise());
    }

    public void prepareToCloseChannel(Channel channel) {
    }

    public CompletionStage<Void> beginTransaction(Connection connection, Bookmarks bookmarks, TransactionConfig config) {
        try {
            this.verifyBeforeTransaction(config, connection.databaseName());
        } catch (Exception var5) {
            return Futures.failedFuture(var5);
        }

        if (bookmarks.isEmpty()) {
            connection.write(BEGIN_MESSAGE, NoOpResponseHandler.INSTANCE, PullAllMessage.PULL_ALL, NoOpResponseHandler.INSTANCE);
            return Futures.completedWithNull();
        } else {
            CompletableFuture<Void> beginTxFuture = new CompletableFuture();
            connection.writeAndFlush(new RunMessage("BEGIN", bookmarks.asBeginTransactionParameters()), NoOpResponseHandler.INSTANCE, PullAllMessage.PULL_ALL, new BeginTxResponseHandler(beginTxFuture));
            return beginTxFuture;
        }
    }

    public CompletionStage<Bookmarks> commitTransaction(Connection connection) {
        CompletableFuture<Bookmarks> commitFuture = new CompletableFuture();
        ResponseHandler pullAllHandler = new CommitTxResponseHandler(commitFuture);
        connection.writeAndFlush(COMMIT_MESSAGE, NoOpResponseHandler.INSTANCE, PullAllMessage.PULL_ALL, pullAllHandler);
        return commitFuture;
    }

    public CompletionStage<Void> rollbackTransaction(Connection connection) {
        CompletableFuture<Void> rollbackFuture = new CompletableFuture();
        ResponseHandler pullAllHandler = new RollbackTxResponseHandler(rollbackFuture);
        connection.writeAndFlush(ROLLBACK_MESSAGE, NoOpResponseHandler.INSTANCE, PullAllMessage.PULL_ALL, pullAllHandler);
        return rollbackFuture;
    }

    public StatementResultCursorFactory runInAutoCommitTransaction(Connection connection, Statement statement, BookmarksHolder bookmarksHolder, TransactionConfig config, boolean waitForRunResponse) {
        this.verifyBeforeTransaction(config, connection.databaseName());
        return buildResultCursorFactory(connection, statement, (ExplicitTransaction)null, waitForRunResponse);
    }

    public StatementResultCursorFactory runInExplicitTransaction(Connection connection, Statement statement, ExplicitTransaction tx, boolean waitForRunResponse) {
        return buildResultCursorFactory(connection, statement, tx, waitForRunResponse);
    }

    public int version() {
        return 1;
    }

    private static StatementResultCursorFactory buildResultCursorFactory(Connection connection, Statement statement, ExplicitTransaction tx, boolean waitForRunResponse) {
        String query = statement.text();
        Map<String, Value> params = statement.parameters().asMap(Values.ofValue());
        RunMessage runMessage = new RunMessage(query, params);;
        //DynamicGraph
        //long version = statement.getVersion();
        if(statement.isWithVersion()) {
           runMessage.setVersion(statement.getVersion());
        }
        //DynamicGraph
        RunResponseHandler runHandler = new RunResponseHandler(METADATA_EXTRACTOR);
        AbstractPullAllResponseHandler pullAllHandler = PullHandlers.newBoltV1PullAllHandler(statement, runHandler, connection, tx);
        return new AsyncResultCursorOnlyFactory(connection, runMessage, runHandler, pullAllHandler, waitForRunResponse);
    }

    private void verifyBeforeTransaction(TransactionConfig config, String databaseName) {
        if (config != null && !config.isEmpty()) {
            throw txConfigNotSupported();
        } else {
            MultiDatabaseUtil.assertEmptyDatabaseName(databaseName, this.version());
        }
    }

    private static ClientException txConfigNotSupported() {
        return new ClientException("Driver is connected to the database that does not support transaction configuration. Please upgrade to neo4j 3.5.0 or later in order to use this functionality");
    }
}
