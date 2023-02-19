//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.graphdb.facade.spi;

import java.net.URL;
import org.neo4j.function.ThrowingFunction;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.module.DataSourceModule;
import org.neo4j.graphdb.security.URLAccessValidationError;
import org.neo4j.internal.kernel.api.Kernel;
import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.exceptions.TransactionFailureException;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.internal.kernel.api.security.SecurityContext;
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
import org.neo4j.storageengine.api.StoreId;
import org.neo4j.values.virtual.MapValue;

public class ProcedureGDBFacadeSPI implements SPI {
    private final DatabaseLayout databaseLayout;
    private final DataSourceModule sourceModule;
    private final DependencyResolver resolver;
    private final CoreAPIAvailabilityGuard availability;
    private final ThrowingFunction<URL, URL, URLAccessValidationError> urlValidator;
    private final SecurityContext securityContext;
    private final ThreadToStatementContextBridge threadToTransactionBridge;

    public ProcedureGDBFacadeSPI(DataSourceModule sourceModule, DependencyResolver resolver, CoreAPIAvailabilityGuard availability, ThrowingFunction<URL, URL, URLAccessValidationError> urlValidator, SecurityContext securityContext, ThreadToStatementContextBridge threadToTransactionBridge) {
        this.databaseLayout = sourceModule.neoStoreDataSource.getDatabaseLayout();
        this.sourceModule = sourceModule;
        this.resolver = resolver;
        this.availability = availability;
        this.urlValidator = urlValidator;
        this.securityContext = securityContext;
        this.threadToTransactionBridge = threadToTransactionBridge;
    }

    public boolean databaseIsAvailable(long timeout) {
        return this.availability.isAvailable(timeout);
    }

    public DependencyResolver resolver() {
        return this.resolver;
    }

    public StoreId storeId() {
        return (StoreId)this.sourceModule.storeId.get();
    }

    public DatabaseLayout databaseLayout() {
        return this.databaseLayout;
    }

    public String name() {
        return "ProcedureGraphDatabaseService";
    }

    public Result executeQuery(String query, MapValue parameters, TransactionalContext tc) {
        try {
            this.availability.assertDatabaseAvailable();
            return this.sourceModule.neoStoreDataSource.getExecutionEngine().executeQuery(query, parameters, tc);
        } catch (QueryExecutionKernelException var5) {
            throw var5.asUserException();
        }
    }

    public AutoIndexing autoIndexing() {
        return this.sourceModule.neoStoreDataSource.getAutoIndexing();
    }

    public void registerKernelEventHandler(KernelEventHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void unregisterKernelEventHandler(KernelEventHandler handler) {
        throw new UnsupportedOperationException();
    }

    public <T> void registerTransactionEventHandler(TransactionEventHandler<T> handler) {
        throw new UnsupportedOperationException();
    }

    public <T> void unregisterTransactionEventHandler(TransactionEventHandler<T> handler) {
        throw new UnsupportedOperationException();
    }

    public URL validateURLAccess(URL url) throws URLAccessValidationError {
        return (URL)this.urlValidator.apply(url);
    }

    public GraphDatabaseQueryService queryService() {
        return (GraphDatabaseQueryService)this.resolver.resolveDependency(GraphDatabaseQueryService.class);
    }

    public Kernel kernel() {
        return (Kernel)this.resolver.resolveDependency(Kernel.class);
    }

    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    public KernelTransaction beginTransaction(Type type, LoginContext ignored, long timeout) {
        try {
            this.availability.assertDatabaseAvailable();
            KernelTransaction kernelTx = ((InwardKernel)this.sourceModule.kernelAPI.get()).beginTransaction(type, this.securityContext, timeout);
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
