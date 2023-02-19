//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.bolt.v3.runtime;

import org.neo4j.bolt.messaging.RequestMessage;
import org.neo4j.bolt.runtime.BoltStateMachineState;
import org.neo4j.bolt.runtime.StateMachineContext;
import org.neo4j.bolt.runtime.StatementMetadata;
import org.neo4j.bolt.runtime.StatementProcessor;
import org.neo4j.bolt.v1.runtime.bookmarking.Bookmark;
import org.neo4j.bolt.v3.messaging.request.CommitMessage;
import org.neo4j.bolt.v3.messaging.request.RollbackMessage;
import org.neo4j.bolt.v3.messaging.request.RunMessage;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.util.Preconditions;
import org.neo4j.values.storable.Values;

public class TransactionReadyState extends FailSafeBoltStateMachineState {
    private BoltStateMachineState streamingState;
    private BoltStateMachineState readyState;

    public TransactionReadyState() {
    }

    public BoltStateMachineState processUnsafe(RequestMessage message, StateMachineContext context) throws Exception {
        if (message instanceof RunMessage) {
            return this.processRunMessage((RunMessage)message, context);
        } else if (message instanceof CommitMessage) {
            return this.processCommitMessage(context);
        } else {
            return message instanceof RollbackMessage ? this.processRollbackMessage(context) : null;
        }
    }

    public String name() {
        return "TX_READY";
    }

    public void setTransactionStreamingState(BoltStateMachineState streamingState) {
        this.streamingState = streamingState;
    }

    public void setReadyState(BoltStateMachineState readyState) {
        this.readyState = readyState;
    }

    private BoltStateMachineState processRunMessage(RunMessage message, StateMachineContext context) throws KernelException {
        long start = context.clock().millis();
        StatementProcessor statementProcessor = context.connectionState().getStatementProcessor();
        StatementMetadata statementMetadata = statementProcessor.run(message.statement(), message.params());
        long end = context.clock().millis();
        context.connectionState().onMetadata("fields", Values.stringArray(statementMetadata.fieldNames()));
        context.connectionState().onMetadata("t_first", Values.longValue(end - start));
        return this.streamingState;
    }

    private BoltStateMachineState processCommitMessage(StateMachineContext context) throws Exception {
        StatementProcessor statementProcessor = context.connectionState().getStatementProcessor();
        Bookmark bookmark = statementProcessor.commitTransaction();
        bookmark.attachTo(context.connectionState());
        return this.readyState;
    }

    private BoltStateMachineState processRollbackMessage(StateMachineContext context) throws Exception {
        StatementProcessor statementProcessor = context.connectionState().getStatementProcessor();
        statementProcessor.rollbackTransaction();
        return this.readyState;
    }

    protected void assertInitialized() {
        Preconditions.checkState(this.streamingState != null, "Streaming state not set");
        Preconditions.checkState(this.readyState != null, "Ready state not set");
        super.assertInitialized();
    }
}
