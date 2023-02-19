//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging;

import org.neo4j.driver.Statement;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.internal.Bookmarks;
import org.neo4j.driver.internal.BookmarksHolder;
import org.neo4j.driver.internal.async.ExplicitTransaction;
import org.neo4j.driver.internal.async.connection.ChannelAttributes;
import org.neo4j.driver.internal.cursor.StatementResultCursorFactory;
import org.neo4j.driver.internal.messaging.v1.BoltProtocolV1;
import org.neo4j.driver.internal.messaging.v2.BoltProtocolV2;
import org.neo4j.driver.internal.messaging.v3.BoltProtocolV3;
import org.neo4j.driver.internal.messaging.v4.BoltProtocolV4;
import org.neo4j.driver.internal.shaded.io.netty.channel.Channel;
import org.neo4j.driver.internal.shaded.io.netty.channel.ChannelPromise;
import org.neo4j.driver.internal.spi.Connection;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface BoltProtocol {
    MessageFormat createMessageFormat();

    void initializeChannel(String var1, Map<String, Value> var2, ChannelPromise var3);

    void prepareToCloseChannel(Channel var1);

    CompletionStage<Void> beginTransaction(Connection var1, Bookmarks var2, TransactionConfig var3);

    CompletionStage<Bookmarks> commitTransaction(Connection var1);

    CompletionStage<Void> rollbackTransaction(Connection var1);

    StatementResultCursorFactory runInAutoCommitTransaction(Connection var1, Statement var2, BookmarksHolder var3, TransactionConfig var4, boolean var5);

    StatementResultCursorFactory runInExplicitTransaction(Connection var1, Statement var2, ExplicitTransaction var3, boolean var4);

    int version();

    static BoltProtocol forChannel(Channel channel) {
        return forVersion(ChannelAttributes.protocolVersion(channel));
    }

    static BoltProtocol forVersion(int version) {
        switch(version) {
            case 1:
                return BoltProtocolV1.INSTANCE;
            case 2:
                return BoltProtocolV2.INSTANCE;
            case 3:
                return BoltProtocolV3.INSTANCE;
            case 4:
                return BoltProtocolV4.INSTANCE;
            default:
                throw new ClientException("Unknown protocol version: " + version);
        }
    }
}
