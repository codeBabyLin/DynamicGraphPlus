package org.neo4j.graphdb.facade;

import org.neo4j.bolt.BoltServer;
import org.neo4j.dbms.database.DatabaseManager;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.factory.module.PlatformModule;
import org.neo4j.graphdb.factory.module.edition.AbstractEditionModule;
import org.neo4j.graphdb.security.URLAccessRule;
import org.neo4j.graphdb.spatial.Geometry;
import org.neo4j.graphdb.spatial.Point;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.internal.DataCollectorManager;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.procs.Neo4jTypes;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.proc.Context;
import org.neo4j.kernel.api.security.provider.SecurityProvider;
import org.neo4j.kernel.availability.AvailabilityGuardInstaller;
import org.neo4j.kernel.availability.StartupWaiter;
import org.neo4j.kernel.builtinprocs.SpecialBuiltInProcedures;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.api.dbms.NonTransactionalDbmsOperations;
import org.neo4j.kernel.impl.cache.VmPauseMonitorComponent;
import org.neo4j.kernel.impl.factory.DatabaseInfo;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.pagecache.PublishPageCacheTracerMetricsAfterStart;
import org.neo4j.kernel.impl.proc.ProcedureConfig;
import org.neo4j.kernel.impl.proc.ProcedureTransactionProvider;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.impl.proc.TerminationGuardProvider;
import org.neo4j.kernel.impl.query.QueryEngineProvider;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.kernel.internal.Version;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.Logger;
import org.neo4j.procedure.ProcedureTransaction;
import org.neo4j.procedure.TerminationGuard;
import org.neo4j.scheduler.DeferredExecutor;
import org.neo4j.scheduler.Group;

import java.io.File;
import java.util.Map;
import java.util.function.Function;

public class GraphDatabaseFacadeFactory {
    protected final DatabaseInfo databaseInfo;
    private final Function<PlatformModule, AbstractEditionModule> editionFactory;

    public GraphDatabaseFacadeFactory(DatabaseInfo databaseInfo, Function<PlatformModule, AbstractEditionModule> editionFactory) {
        this.databaseInfo = databaseInfo;
        this.editionFactory = editionFactory;
    }

    //Dynamicgraph method
    //*******************
    public GraphDatabaseFacade newFacade(File storeDir, Config config, Dependencies dependencies) {
        //return this.initFacade(storeDir, config, dependencies, new DynamicGraphDatabaseFacade());
        return this.initFacade(storeDir, config, dependencies, new GraphDatabaseFacade());
    }

    //Dynamicgraph method
    //*******************

    public GraphDatabaseFacade initFacade(File storeDir, Map<String, String> params, Dependencies dependencies, GraphDatabaseFacade graphDatabaseFacade) {
        return this.initFacade(storeDir, Config.defaults(params), dependencies, graphDatabaseFacade);
    }

    public GraphDatabaseFacade initFacade(File storeDir, Config config, Dependencies dependencies, GraphDatabaseFacade graphDatabaseFacade) {
        PlatformModule platform = this.createPlatform(storeDir, config, dependencies);
        AbstractEditionModule edition = (AbstractEditionModule)this.editionFactory.apply(platform);
        dependencies.availabilityGuardInstaller().install(edition.getGlobalAvailabilityGuard(platform.clock, platform.logging, platform.config));
        platform.life.add(new VmPauseMonitorComponent(config, platform.logging.getInternalLog(VmPauseMonitorComponent.class), platform.jobScheduler));
        Procedures procedures = setupProcedures(platform, edition, graphDatabaseFacade);
        platform.dependencies.satisfyDependency(new NonTransactionalDbmsOperations(procedures));
        Logger msgLog = platform.logging.getInternalLog(this.getClass()).infoLogger();
        DatabaseManager databaseManager = edition.createDatabaseManager(graphDatabaseFacade, platform, edition, procedures, msgLog);
        platform.life.add(databaseManager);
        platform.dependencies.satisfyDependency(databaseManager);
        DataCollectorManager dataCollectorManager = new DataCollectorManager(platform.dataSourceManager, platform.jobScheduler, procedures, platform.monitors, platform.config);
        platform.life.add(dataCollectorManager);
        edition.createSecurityModule(platform, procedures);
        SecurityProvider securityProvider = edition.getSecurityProvider();
        platform.dependencies.satisfyDependencies(new Object[]{securityProvider.authManager()});
        platform.dependencies.satisfyDependencies(new Object[]{securityProvider.userManagerSupplier()});
        platform.life.add(platform.globalKernelExtensions);
        platform.life.add(createBoltServer(platform, edition, databaseManager));
        platform.dependencies.satisfyDependency(edition.globalTransactionCounter());
        platform.life.add(new PublishPageCacheTracerMetricsAfterStart(platform.tracers.pageCursorTracerSupplier));
        platform.life.add(new StartupWaiter(edition.getGlobalAvailabilityGuard(platform.clock, platform.logging, platform.config), edition.getTransactionStartTimeout()));
        platform.dependencies.satisfyDependency(edition.getSchemaWriteGuard());
        platform.life.setLast(platform.eventHandlers);
        edition.createDatabases(databaseManager, config);
        String activeDatabase = (String)config.get(GraphDatabaseSettings.active_database);
        GraphDatabaseFacade databaseFacade = (GraphDatabaseFacade)databaseManager.getDatabaseFacade(activeDatabase).orElseThrow(() -> {
            return new IllegalStateException(String.format("Database %s not found. Please check the logs for startup errors.", activeDatabase));
        });
        RuntimeException error = null;

        try {
            platform.life.start();
        } catch (Throwable var24) {
            error = new RuntimeException("Error starting " + this.getClass().getName() + ", " + platform.storeLayout.storeDirectory(), var24);
        } finally {
            if (error != null) {
                try {
                    graphDatabaseFacade.shutdown();
                } catch (Throwable var23) {
                    error.addSuppressed(var23);
                }
            }

        }

        if (error != null) {
            msgLog.log("Failed to start database", error);
            throw error;
        } else {
            return databaseFacade;
        }
    }

    protected PlatformModule createPlatform(File storeDir, Config config, Dependencies dependencies) {
        return new PlatformModule(storeDir, config, this.databaseInfo, dependencies);
    }

    private static Procedures setupProcedures(PlatformModule platform, AbstractEditionModule editionModule, GraphDatabaseFacade facade) {
        File pluginDir = (File)platform.config.get(GraphDatabaseSettings.plugin_dir);
        Log internalLog = platform.logging.getInternalLog(Procedures.class);
        ProcedureConfig procedureConfig = new ProcedureConfig(platform.config);
        Procedures procedures = new Procedures(facade, new SpecialBuiltInProcedures(Version.getNeo4jVersion(), platform.databaseInfo.edition.toString()), pluginDir, internalLog, procedureConfig);
        platform.life.add(procedures);
        platform.dependencies.satisfyDependency(procedures);
        procedures.registerType(Node.class, Neo4jTypes.NTNode);
        procedures.registerType(Relationship.class, Neo4jTypes.NTRelationship);
        procedures.registerType(Path.class, Neo4jTypes.NTPath);
        procedures.registerType(Geometry.class, Neo4jTypes.NTGeometry);
        procedures.registerType(Point.class, Neo4jTypes.NTPoint);
        Log proceduresLog = platform.logging.getUserLog(Procedures.class);
        procedures.registerComponent(Log.class, (ctx) -> {
            return proceduresLog;
        }, true);
        procedures.registerComponent(ProcedureTransaction.class, new ProcedureTransactionProvider(), true);
        procedures.registerComponent(TerminationGuard.class, new TerminationGuardProvider(), true);
        procedures.registerComponent(DependencyResolver.class, (ctx) -> {
            return (DependencyResolver)ctx.get(Context.DEPENDENCY_RESOLVER);
        }, false);
        procedures.registerComponent(KernelTransaction.class, (ctx) -> {
            return (KernelTransaction)ctx.get(Context.KERNEL_TRANSACTION);
        }, false);
        procedures.registerComponent(GraphDatabaseAPI.class, (ctx) -> {
            return (GraphDatabaseAPI)ctx.get(Context.DATABASE_API);
        }, false);
        procedures.registerComponent(SecurityContext.class, (ctx) -> {
            return (SecurityContext)ctx.get(Context.SECURITY_CONTEXT);
        }, true);

        try {
            editionModule.registerProcedures(procedures, procedureConfig);
        } catch (KernelException var9) {
            internalLog.error("Failed to register built-in edition procedures at start up: " + var9.getMessage());
        }

        return procedures;
    }

    private static BoltServer createBoltServer(PlatformModule platform, AbstractEditionModule edition, DatabaseManager databaseManager) {
        return new BoltServer(databaseManager, platform.jobScheduler, platform.connectorPortRegister, edition.getConnectionTracker(), platform.usageData, platform.config, platform.clock, platform.monitors, platform.logging, platform.dependencies);
    }

    public interface Dependencies {
        Monitors monitors();

        LogProvider userLogProvider();

        Iterable<Class<?>> settingsClasses();

        Iterable<KernelExtensionFactory<?>> kernelExtensions();

        Map<String, URLAccessRule> urlAccessRules();

        Iterable<QueryEngineProvider> executionEngines();

        Iterable<Pair<DeferredExecutor, Group>> deferredExecutors();

        default AvailabilityGuardInstaller availabilityGuardInstaller() {
            return (availabilityGuard) -> {
            };
        }
    }
}
