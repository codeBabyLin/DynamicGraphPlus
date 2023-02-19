//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.server;

//import cn.DynamicGraph.DataLoadAndRead;
import org.neo4j.commandline.Util;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.facade.GraphDatabaseDependencies;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.ListenSocketAddress;
import org.neo4j.io.IOUtils;
import org.neo4j.io.file.Files;
import org.neo4j.io.fs.DefaultFileSystemAbstraction;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.configuration.ConfigurationValidator;
import org.neo4j.kernel.configuration.HttpConnector.Encryption;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.scheduler.BufferingExecutor;
import org.neo4j.kernel.info.JvmChecker;
import org.neo4j.kernel.info.JvmMetadataRepository;
import org.neo4j.logging.*;
import org.neo4j.logging.FormattedLogProvider.Builder;
import org.neo4j.scheduler.Group;
import org.neo4j.server.database.GraphFactory;
import org.neo4j.server.logging.JULBridge;
import org.neo4j.server.logging.JettyLogBridge;
import sun.misc.Signal;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class ServerBootstrapper implements Bootstrapper {
    public static final int OK = 0;
    private static final int WEB_SERVER_STARTUP_ERROR_CODE = 1;
    private static final int GRAPH_DATABASE_STARTUP_ERROR_CODE = 2;
    private static final String SIGTERM = "TERM";
    private static final String SIGINT = "INT";
    private volatile NeoServer server;
    private volatile Closeable userLogFileStream;
    private Thread shutdownHook;
    private GraphDatabaseDependencies dependencies = GraphDatabaseDependencies.newDependencies();
    private Log log;
    private String serverAddress;

    public ServerBootstrapper() {
        this.log = FormattedLogProvider.toOutputStream(System.out).getLog(this.getClass());
        this.serverAddress = "unknown address";
    }

    public static int start(Bootstrapper boot, String... argv) {
        ServerCommandLineArgs args = ServerCommandLineArgs.parse(argv);
        if (args.version()) {
            System.out.println("neo4j " + Util.neo4jVersion());
            return 0;
        } else if (args.homeDir() == null) {
            throw new ServerStartupException("Argument --home-dir is required and was not provided.");
        } else {
            return boot.start(args.homeDir(), args.configFile(), args.configOverrides());
        }
    }

    public final int start(File homeDir, Optional<File> configFile, Map<String, String> configOverrides) {
        this.addShutdownHook();
        this.installSignalHandlers();

        try {
            Config config = Config.builder().withFile(configFile).withSettings(configOverrides).withHome(homeDir).withValidators(this.configurationValidators()).withNoThrowOnFileLoadFailure().withServerDefaults().build();
            LogProvider userLogProvider = this.setupLogging(config);
            this.dependencies = this.dependencies.userLogProvider(userLogProvider);
            this.log = userLogProvider.getLog(this.getClass());
            config.setLogger(this.log);
            this.serverAddress = (String)config.httpConnectors().stream().filter((c) -> {
                return Encryption.NONE.equals(c.encryptionLevel());
            }).findFirst().map((connector) -> {
                return ((ListenSocketAddress)config.get(connector.listen_address)).toString();
            }).orElse(this.serverAddress);
            this.checkCompatibility();
            this.server = this.createNeoServer(config, this.dependencies);
            //System.out.println("hi I'm here");
            this.server.start();
/*            //DynamicGraph
            GraphDatabaseFacade gdb = this.server.getDatabase().getGraph();
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            File p = new File(path).getParentFile().getParentFile();
            //String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("WebPage")).getFile();
            if(!new File(path).exists()) System.out.println("Directory 12 not exist!!!"+ p.getAbsolutePath());
            else System.out.println("Directory 12 exist!!!" + p.getAbsolutePath());
            //String path1 = Objects.requireNonNull(this.getClass().getClassLoader().getResource("WebPage/Data/provincetr.txt")).getPath();
            String path1 = new File(p,"conf/provincetr.txt").getAbsolutePath();
            //String path2 = Objects.requireNonNull(this.getClass().getClassLoader().getResource("WebPage/Data/newOral.txt")).getPath();
            String path2 = new File(p,"conf/newOral.txt").getAbsolutePath();
            DataLoadAndRead.prepareData2(gdb,path1,path2);
            //DynamicGraph*/
            return 0;
        } catch (ServerStartupException var6) {
            var6.describeTo(this.log);
            return 1;
        } catch (TransactionFailureException var7) {
            String locationMsg = this.server == null ? "" : " Another process may be using database location " + this.server.getDatabase().getLocation();
            this.log.error(String.format("Failed to start Neo4j on %s.", this.serverAddress) + locationMsg, var7);
            return 2;
        } catch (Exception var8) {
            this.log.error(String.format("Failed to start Neo4j on %s.", this.serverAddress), var8);
            return 1;
        }
    }

    public int stop() {
        String location = "unknown location";

        try {
            this.doShutdown();
            this.removeShutdownHook();
            return 0;
        } catch (Exception var3) {
            this.log.error("Failed to cleanly shutdown Neo Server on port [%s], database [%s]. Reason [%s] ", new Object[]{this.serverAddress, location, var3.getMessage(), var3});
            return 1;
        }
    }

    public boolean isRunning() {
        return this.server != null && this.server.getDatabase() != null && this.server.getDatabase().isRunning();
    }

    public NeoServer getServer() {
        return this.server;
    }

    public Log getLog() {
        return this.log;
    }

    private NeoServer createNeoServer(Config config, GraphDatabaseDependencies dependencies) {
        GraphFactory graphFactory = this.createGraphFactory(config);
        boolean httpAndHttpsDisabled = config.enabledHttpConnectors().isEmpty();
        return (NeoServer)(httpAndHttpsDisabled ? new DisabledNeoServer(graphFactory, dependencies, config) : this.createNeoServer(graphFactory, config, dependencies));
    }

    protected abstract GraphFactory createGraphFactory(Config var1);

    protected abstract NeoServer createNeoServer(GraphFactory var1, Config var2, GraphDatabaseDependencies var3);

    protected Collection<ConfigurationValidator> configurationValidators() {
        return Collections.emptyList();
    }

    private LogProvider setupLogging(Config config) {
        Builder builder = FormattedLogProvider.withoutRenderingContext().withZoneId(((LogTimeZone)config.get(GraphDatabaseSettings.db_timezone)).getZoneId()).withDefaultLogLevel((Level)config.get(GraphDatabaseSettings.store_internal_log_level));
        LogProvider userLogProvider = (Boolean)config.get(GraphDatabaseSettings.store_user_log_to_stdout) ? builder.toOutputStream(System.out) : this.createFileSystemUserLogProvider(config, builder);
        JULBridge.resetJUL();
        Logger.getLogger("").setLevel(java.util.logging.Level.WARNING);
        JULBridge.forwardTo((LogProvider)userLogProvider);
        JettyLogBridge.setLogProvider((LogProvider)userLogProvider);
        return (LogProvider)userLogProvider;
    }

    private void installSignalHandlers() {
        this.installSignalHandler("TERM", false);
        this.installSignalHandler("INT", true);
    }

    private void installSignalHandler(String sig, boolean tolerateErrors) {
        try {
            Signal.handle(new Signal(sig), (signal) -> {
                System.exit(0);
            });
        } catch (Throwable var4) {
            if (!tolerateErrors) {
                throw var4;
            }
        }

    }

    private void doShutdown() {
        if (this.server != null) {
            this.server.stop();
        }

        if (this.userLogFileStream != null) {
            this.closeUserLogFileStream();
        }

    }

    private void closeUserLogFileStream() {
        IOUtils.closeAllUnchecked(new Closeable[]{this.userLogFileStream});
    }

    private void addShutdownHook() {
        this.shutdownHook = new Thread(() -> {
            this.log.info("Neo4j Server shutdown initiated by request");
            this.doShutdown();
        });
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    private void removeShutdownHook() {
        if (this.shutdownHook != null && !Runtime.getRuntime().removeShutdownHook(this.shutdownHook)) {
            this.log.warn("Unable to remove shutdown hook");
        }

    }

    private LogProvider createFileSystemUserLogProvider(Config config, Builder builder) {
        BufferingExecutor deferredExecutor = new BufferingExecutor();
        this.dependencies = this.dependencies.withDeferredExecutor(deferredExecutor, Group.LOG_ROTATION);
        FileSystemAbstraction fs = new DefaultFileSystemAbstraction();
        File destination = (File)config.get(GraphDatabaseSettings.store_user_log_path);
        Long rotationThreshold = (Long)config.get(GraphDatabaseSettings.store_user_log_rotation_threshold);

        try {
            if (rotationThreshold == 0L) {
                OutputStream userLog = Files.createOrOpenAsOutputStream(fs, destination, true);
                this.userLogFileStream = userLog;
                return builder.toOutputStream(userLog);
            } else {
                RotatingFileOutputStreamSupplier rotatingUserLogSupplier = new RotatingFileOutputStreamSupplier(fs, destination, rotationThreshold, ((Duration)config.get(GraphDatabaseSettings.store_user_log_rotation_delay)).toMillis(), (Integer)config.get(GraphDatabaseSettings.store_user_log_max_archives), deferredExecutor);
                this.userLogFileStream = rotatingUserLogSupplier;
                return builder.toOutputStream(rotatingUserLogSupplier);
            }
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        }
    }

    private void checkCompatibility() {
        (new JvmChecker(this.log, new JvmMetadataRepository())).checkJvmCompatibilityAndIssueWarning();
    }
}
