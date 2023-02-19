//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v3;

import org.neo4j.driver.Statement;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.Bookmarks;
import org.neo4j.driver.internal.BookmarksHolder;
import org.neo4j.driver.internal.async.ExplicitTransaction;
import org.neo4j.driver.internal.async.connection.ChannelAttributes;
import org.neo4j.driver.internal.cursor.AsyncResultCursorOnlyFactory;
import org.neo4j.driver.internal.cursor.StatementResultCursorFactory;
import org.neo4j.driver.internal.handlers.*;
import org.neo4j.driver.internal.messaging.BoltProtocol;
import org.neo4j.driver.internal.messaging.MessageFormat;
import org.neo4j.driver.internal.messaging.request.*;
import org.neo4j.driver.internal.shaded.io.netty.channel.Channel;
import org.neo4j.driver.internal.shaded.io.netty.channel.ChannelPromise;
import org.neo4j.driver.internal.spi.Connection;
import org.neo4j.driver.internal.util.Futures;
import org.neo4j.driver.internal.util.MetadataExtractor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class BoltProtocolV3 implements BoltProtocol {
    public static final int VERSION = 3;
    public static final BoltProtocol INSTANCE = new BoltProtocolV3();
    public static final MetadataExtractor METADATA_EXTRACTOR = new MetadataExtractor("t_first", "t_last");

    public BoltProtocolV3() {
    }

    public MessageFormat createMessageFormat() {
        return new MessageFormatV3();
    }

    public void initializeChannel(String userAgent, Map<String, Value> authToken, ChannelPromise channelInitializedPromise) {
        Channel channel = channelInitializedPromise.channel();
        HelloMessage message = new HelloMessage(userAgent, authToken);
        HelloResponseHandler handler = new HelloResponseHandler(channelInitializedPromise);
        ChannelAttributes.messageDispatcher(channel).enqueue(handler);
        channel.writeAndFlush(message, channel.voidPromise());
    }

    public void prepareToCloseChannel(Channel channel) {
        GoodbyeMessage message = GoodbyeMessage.GOODBYE;
        ChannelAttributes.messageDispatcher(channel).enqueue(NoOpResponseHandler.INSTANCE);
        channel.writeAndFlush(message, channel.voidPromise());
    }

    public CompletionStage<Void> beginTransaction(Connection connection, Bookmarks bookmarks, TransactionConfig config) {
        try {
            this.verifyDatabaseNameBeforeTransaction(connection.databaseName());
        } catch (Exception var6) {
            return Futures.failedFuture(var6);
        }

        BeginMessage beginMessage = new BeginMessage(bookmarks, config, connection.databaseName(), connection.mode());
        if (bookmarks.isEmpty()) {
            connection.write(beginMessage, NoOpResponseHandler.INSTANCE);
            return Futures.completedWithNull();
        } else {
            CompletableFuture<Void> beginTxFuture = new CompletableFuture();
            connection.writeAndFlush(beginMessage, new BeginTxResponseHandler(beginTxFuture));
            return beginTxFuture;
        }
    }

    public CompletionStage<Bookmarks> commitTransaction(Connection connection) {
        CompletableFuture<Bookmarks> commitFuture = new CompletableFuture();
        connection.writeAndFlush(CommitMessage.COMMIT, new CommitTxResponseHandler(commitFuture));
        return commitFuture;
    }

    public CompletionStage<Void> rollbackTransaction(Connection connection) {
        CompletableFuture<Void> rollbackFuture = new CompletableFuture();
        connection.writeAndFlush(RollbackMessage.ROLLBACK, new RollbackTxResponseHandler(rollbackFuture));
        return rollbackFuture;
    }

    public StatementResultCursorFactory runInAutoCommitTransaction(Connection connection, Statement statement, BookmarksHolder bookmarksHolder, TransactionConfig config, boolean waitForRunResponse) {
        this.verifyDatabaseNameBeforeTransaction(connection.databaseName());
        RunWithMetadataMessage runMessage = RunWithMetadataMessage.autoCommitTxRunMessage(statement, config, connection.databaseName(), connection.mode(), bookmarksHolder.getBookmarks());
        return this.buildResultCursorFactory(connection, statement, bookmarksHolder, (ExplicitTransaction)null, runMessage, waitForRunResponse);
    }

    public StatementResultCursorFactory runInExplicitTransaction(Connection connection, Statement statement, ExplicitTransaction tx, boolean waitForRunResponse) {
        RunWithMetadataMessage runMessage = RunWithMetadataMessage.explicitTxRunMessage(statement);
        return this.buildResultCursorFactory(connection, statement, BookmarksHolder.NO_OP, tx, runMessage, waitForRunResponse);
    }

    protected StatementResultCursorFactory buildResultCursorFactory(Connection connection, Statement statement, BookmarksHolder bookmarksHolder, ExplicitTransaction tx, RunWithMetadataMessage runMessage, boolean waitForRunResponse) {
        //DynamiGraph
        if(statement.isWithVersion()){
            runMessage.setVersion(statement.getVersion());
        }
        //DynamiGraph
        RunResponseHandler runHandler = new RunResponseHandler(METADATA_EXTRACTOR);
        AbstractPullAllResponseHandler pullHandler = PullHandlers.newBoltV3PullAllHandler(statement, runHandler, connection, bookmarksHolder, tx);
        return new AsyncResultCursorOnlyFactory(connection, runMessage, runHandler, pullHandler, waitForRunResponse);
    }

    protected void verifyDatabaseNameBeforeTransaction(String databaseName) {
        MultiDatabaseUtil.assertEmptyDatabaseName(databaseName, this.version());
    }

    public int version() {
        return 3;
    }
}
