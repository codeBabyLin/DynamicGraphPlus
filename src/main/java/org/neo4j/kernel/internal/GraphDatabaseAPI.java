//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.internal;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.security.URLAccessValidationError;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.storageengine.api.StoreId;

public interface GraphDatabaseAPI extends GraphDatabaseService {
    DependencyResolver getDependencyResolver();

    StoreId storeId();

    URL validateURLAccess(URL var1) throws URLAccessValidationError;

    DatabaseLayout databaseLayout();

    InternalTransaction beginTransaction(Type var1, LoginContext var2);

    InternalTransaction beginTransaction(Type var1, LoginContext var2, long var3, TimeUnit var5);

    InternalTransaction beginTransaction(Type var1, LoginContext var2, long version);

    InternalTransaction beginTransaction(Type var1, LoginContext var2, long var3, TimeUnit var5, long version);
}
