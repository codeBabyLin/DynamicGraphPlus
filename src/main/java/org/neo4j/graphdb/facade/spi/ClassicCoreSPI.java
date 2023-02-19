//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.graphdb.facade.spi;

import java.net.URL;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.module.DataSourceModule;
import org.neo4j.graphdb.factory.module.PlatformModule;
import org.neo4j.graphdb.security.URLAccessValidationError;
import org.neo4j.internal.kernel.api.Kernel;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.kernel.GraphDatabaseQueryService;
import org.neo4j.kernel.api.InwardKernel;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.explicitindex.AutoIndexing;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.impl.coreapi.CoreAPIAvailabilityGuard;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade.SPI;
import org.neo4j.kernel.impl.query.QueryExecutionKernelException;
import org.neo4j.kernel.impl.query.TransactionalContext;
import org.neo4j.kernel.lifecycle.LifecycleException;
import org.neo4j.logging.Logger;
import org.neo4j.storageengine.api.StoreId;
import org.neo4j.values.virtual.MapValue;

public class ClassicCoreSPI implements SPI {
    private final PlatformModule platform;
    private final DataSourceModule dataSource;
    private final Logger msgLog;
    private final CoreAPIAvailabilityGuard availability;
    private final ThreadToStatementContextBridge threadToTransactionBridge;

    public ClassicCoreSPI(PlatformModule platform, DataSourceModule dataSource, Logger msgLog, CoreAPIAvailabilityGuard availability, ThreadToStatementContextBridge threadToTransactionBridge) {
        this.platform = platform;
        this.dataSource = dataSource;
        this.msgLog = msgLog;
        this.availability = availability;
        this.threadToTransactionBridge = threadToTransactionBridge;
    }

    public boolean databaseIsAvailable(long timeout) {
        return this.dataSource.neoStoreDataSource.getDatabaseAvailabilityGuard().isAvailable(timeout);
    }

    public Result executeQuery(String query, MapValue parameters, TransactionalContext transactionalContext) {
        try {
            this.availability.assertDatabaseAvailable();
            return this.dataSource.neoStoreDataSource.getExecutionEngine().executeQuery(query, parameters, transactionalContext);
        } catch (QueryExecutionKernelException var5) {
            throw var5.asUserException();
        }
    }

    public AutoIndexing autoIndexing() {
        return this.dataSource.neoStoreDataSource.getAutoIndexing();
    }

    public DependencyResolver resolver() {
        return this.dataSource.neoStoreDataSource.getDependencyResolver();
    }

    public void registerKernelEventHandler(KernelEventHandler handler) {
        this.platform.eventHandlers.registerKernelEventHandler(handler);
    }

    public void unregisterKernelEventHandler(KernelEventHandler handler) {
        this.platform.eventHandlers.unregisterKernelEventHandler(handler);
    }

    public <T> void registerTransactionEventHandler(TransactionEventHandler<T> handler) {
        this.dataSource.neoStoreDataSource.getTransactionEventHandlers().registerTransactionEventHandler(handler);
    }

    public <T> void unregisterTransactionEventHandler(TransactionEventHandler<T> handler) {
        this.dataSource.neoStoreDataSource.getTransactionEventHandlers().unregisterTransactionEventHandler(handler);
    }

    public StoreId storeId() {
        return (StoreId)this.dataSource.storeId.get();
    }

    public DatabaseLayout databaseLayout() {
        return this.dataSource.neoStoreDataSource.getDatabaseLayout();
    }

    public URL validateURLAccess(URL url) throws URLAccessValidationError {
        return this.platform.urlAccessRule.validate(this.platform.config, url);
    }

    public GraphDatabaseQueryService queryService() {
        return (GraphDatabaseQueryService)this.resolver().resolveDependency(GraphDatabaseQueryService.class);
    }

    public Kernel kernel() {
        return (Kernel)this.resolver().resolveDependency(Kernel.class);
    }

    public String name() {
        return this.platform.databaseInfo.toString();
    }

    public void shutdown() {
        try {
            this.msgLog.log("Shutdown started");
            this.dataSource.neoStoreDataSource.getDatabaseAvailabilityGuard().shutdown();
            this.platform.life.shutdown();
        } catch (LifecycleException var2) {
            this.msgLog.log("Shutdown failed", var2);
            throw var2;
        }
    }

    public KernelTransaction beginTransaction(Type type, LoginContext loginContext, long timeout) {
        try {
            this.availability.assertDatabaseAvailable();
            KernelTransaction kernelTx = ((InwardKernel)this.dataSource.kernelAPI.get()).beginTransaction(type, loginContext, timeout);
            kernelTx.registerCloseListener((txId) -> {
                this.threadToTransactionBridge.unbindTransactionFromCurrentThread();
            });
            this.threadToTransactionBridge.bindTransactionToCurrentThread(kernelTx);
            return kernelTx;
        } catch (TransactionFailureException var6) {
            throw new org.neo4j.graphdb.TransactionFailureException(var6.getMessage(), var6);
        }
    }
}
