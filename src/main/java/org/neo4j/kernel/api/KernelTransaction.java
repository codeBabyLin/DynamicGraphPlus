//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.api;

import java.util.Map;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.PropertyCursor;
import org.neo4j.internal.kernel.api.RelationshipScanCursor;
import org.neo4j.internal.kernel.api.Transaction;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.exceptions.schema.SchemaKernelException;
import org.neo4j.internal.kernel.api.schema.SchemaDescriptor;
import org.neo4j.internal.kernel.api.security.AuthSubject;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.kernel.impl.api.ClockContext;
import org.neo4j.storageengine.api.schema.IndexDescriptor;

public interface KernelTransaction extends Transaction, AssertOpen {
    //DynamicGraph
    //***********************************************
    //void setStartEndVersion(long startVersion,long endVersion);

    void setVersion(long version);
    long getVersion();

    //DynamicGraph
    //***********************************************

    Statement acquireStatement();

    IndexDescriptor indexUniqueCreate(SchemaDescriptor var1, String var2) throws SchemaKernelException;

    SecurityContext securityContext();

    AuthSubject subjectOrAnonymous();

    long lastTransactionTimestampWhenStarted();

    long lastTransactionIdWhenStarted();

    long startTime();

    long timeout();

    void registerCloseListener(CloseListener var1);

    Type transactionType();

    long getTransactionId();

    long getCommitTime();

    Revertable overrideWith(SecurityContext var1);

    ClockContext clocks();

    NodeCursor ambientNodeCursor();

    RelationshipScanCursor ambientRelationshipCursor();

    PropertyCursor ambientPropertyCursor();

    void setMetaData(Map<String, Object> var1);

    Map<String, Object> getMetaData();

    boolean isSchemaTransaction();

    @FunctionalInterface
    public interface Revertable extends AutoCloseable {
        void close();
    }

    public interface CloseListener {
        void notify(long var1);
    }
}
