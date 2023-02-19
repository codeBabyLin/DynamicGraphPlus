//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.server.web;

import cn.DynamicGraph.server.web.HandlerFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPoolBudget;
import org.eclipse.jetty.webapp.WebAppContext;
import org.neo4j.helpers.ListenSocketAddress;
import org.neo4j.helpers.PortBindException;
import org.neo4j.kernel.api.net.NetworkConnectionTracker;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.database.DatabaseProvider;
import org.neo4j.server.database.InjectableProvider;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.security.ssl.SslSocketConnectorFactory;
import org.neo4j.server.web.JaxRsServletHolderFactory.Classes;
import org.neo4j.server.web.JaxRsServletHolderFactory.Packages;
import org.neo4j.ssl.SslPolicy;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Jetty9WebServer implements WebServer {
    private static final int JETTY_THREAD_POOL_IDLE_TIMEOUT = 60000;
    public static final ListenSocketAddress DEFAULT_ADDRESS = new ListenSocketAddress("0.0.0.0", 80);
    private boolean wadlEnabled;
    private Collection<InjectableProvider<?>> defaultInjectables;
    private Consumer<Server> jettyCreatedCallback;
    private RequestLog requestLog;
    private Server jetty;
    private HandlerCollection handlers;
    private ListenSocketAddress httpAddress;
    private ListenSocketAddress httpsAddress;
    private ServerConnector httpConnector;
    private ServerConnector httpsConnector;
    private final HashMap<String, String> staticContent;
    private final Map<String, JaxRsServletHolderFactory> jaxRSPackages;
    private final Map<String, JaxRsServletHolderFactory> jaxRSClasses;
    private final List<FilterDefinition> filters;
    private int jettyMaxThreads;
    private SslPolicy sslPolicy;
    private final SslSocketConnectorFactory sslSocketFactory;
    private final HttpConnectorFactory connectorFactory;
    private final Log log;

    public Jetty9WebServer(LogProvider logProvider, Config config, NetworkConnectionTracker connectionTracker) {
        this.httpAddress = DEFAULT_ADDRESS;
        this.staticContent = new HashMap();
        this.jaxRSPackages = new HashMap();
        this.jaxRSClasses = new HashMap();
        this.filters = new ArrayList();
        this.jettyMaxThreads = 1;
        this.log = logProvider.getLog(this.getClass());
        this.sslSocketFactory = new SslSocketConnectorFactory(connectionTracker, config);
        this.connectorFactory = new HttpConnectorFactory(connectionTracker, config);
    }

    public void start() throws Exception {
        if (this.jetty == null) {
            this.verifyAddressConfiguration();
            JettyThreadCalculator jettyThreadCalculator = new JettyThreadCalculator(this.jettyMaxThreads);
            this.jetty = new Server(createQueuedThreadPool(jettyThreadCalculator));
            if (this.httpAddress != null) {
                this.httpConnector = this.connectorFactory.createConnector(this.jetty, this.httpAddress, jettyThreadCalculator);
                this.jetty.addConnector(this.httpConnector);
            }

            if (this.httpsAddress != null) {
                if (this.sslPolicy == null) {
                    throw new RuntimeException("HTTPS set to enabled, but no SSL policy provided");
                }

                this.httpsConnector = this.sslSocketFactory.createConnector(this.jetty, this.sslPolicy, this.httpsAddress, jettyThreadCalculator);
                this.jetty.addConnector(this.httpsConnector);
            }

            if (this.jettyCreatedCallback != null) {
                this.jettyCreatedCallback.accept(this.jetty);
            }
        }

        this.handlers = new HandlerList();
        this.jetty.setHandler(this.handlers);
        this.handlers.addHandler(new MovedContextHandler());
        //DynamicGraph
       /* Iterator it= this.defaultInjectables.iterator();
        ContextHandler chd = null;
        while(it.hasNext()){
            Object ob = it.next();
            if(ob instanceof DatabaseProvider){
                chd = new HandlerFactory().getContextHandlerWithDate(((DatabaseProvider) ob).db.getGraph());
                break;
            }
        }
        ContextHandler ch = new HandlerFactory().getContextHandler();

        this.handlers.addHandler(ch);
        if(chd !=null) {
            this.handlers.addHandler(chd);
        }*/
        //DynamicGraph
        this.loadAllMounts();
        if (this.requestLog != null) {
            this.loadRequestLogging();
        }

        this.startJetty();
    }

    private static QueuedThreadPool createQueuedThreadPool(JettyThreadCalculator jtc) {
        BlockingQueue<Runnable> queue = new BlockingArrayQueue(jtc.getMinThreads(), jtc.getMinThreads(), jtc.getMaxCapacity());
        QueuedThreadPool threadPool = new QueuedThreadPool(jtc.getMaxThreads(), jtc.getMinThreads(), 60000, queue);
        threadPool.setThreadPoolBudget((ThreadPoolBudget)null);
        return threadPool;
    }

    public void stop() {
        if (this.jetty != null) {
            try {
                this.jetty.stop();
            } catch (Exception var3) {
                throw new RuntimeException(var3);
            }

            try {
                this.jetty.join();
            } catch (InterruptedException var2) {
                this.log.warn("Interrupted while waiting for Jetty to stop");
            }

            this.jetty = null;
        }

    }

    public void setHttpAddress(ListenSocketAddress address) {
        this.httpAddress = address;
    }

    public void setHttpsAddress(ListenSocketAddress address) {
        this.httpsAddress = address;
    }

    public void setSslPolicy(SslPolicy policy) {
        this.sslPolicy = policy;
    }

    public void setMaxThreads(int maxThreads) {
        this.jettyMaxThreads = maxThreads;
    }

    public void addJAXRSPackages(List<String> packageNames, String mountPoint, Collection<Injectable<?>> injectables) {
        mountPoint = this.ensureRelativeUri(mountPoint);
        mountPoint = this.trimTrailingSlashToKeepJettyHappy(mountPoint);
        JaxRsServletHolderFactory factory = (JaxRsServletHolderFactory)this.jaxRSPackages.computeIfAbsent(mountPoint, (k) -> {
            return new Packages();
        });
        factory.add(packageNames, injectables);
        this.log.debug("Adding JAXRS packages %s at [%s]", new Object[]{packageNames, mountPoint});
    }

    public void addJAXRSClasses(List<String> classNames, String mountPoint, Collection<Injectable<?>> injectables) {
        mountPoint = this.ensureRelativeUri(mountPoint);
        mountPoint = this.trimTrailingSlashToKeepJettyHappy(mountPoint);
        JaxRsServletHolderFactory factory = (JaxRsServletHolderFactory)this.jaxRSClasses.computeIfAbsent(mountPoint, (k) -> {
            return new Classes();
        });
        factory.add(classNames, injectables);
        this.log.debug("Adding JAXRS classes %s at [%s]", new Object[]{classNames, mountPoint});
    }

    public void setWadlEnabled(boolean wadlEnabled) {
        this.wadlEnabled = wadlEnabled;
    }

    public void setDefaultInjectables(Collection<InjectableProvider<?>> defaultInjectables) {
        this.defaultInjectables = defaultInjectables;
    }

    public void setJettyCreatedCallback(Consumer<Server> callback) {
        this.jettyCreatedCallback = callback;
    }

    public void removeJAXRSPackages(List<String> packageNames, String serverMountPoint) {
        JaxRsServletHolderFactory factory = (JaxRsServletHolderFactory)this.jaxRSPackages.get(serverMountPoint);
        if (factory != null) {
            factory.remove(packageNames);
        }

    }

    public void removeJAXRSClasses(List<String> classNames, String serverMountPoint) {
        JaxRsServletHolderFactory factory = (JaxRsServletHolderFactory)this.jaxRSClasses.get(serverMountPoint);
        if (factory != null) {
            factory.remove(classNames);
        }

    }

    public void addFilter(Filter filter, String pathSpec) {
        this.filters.add(new FilterDefinition(filter, pathSpec));
    }

    public void removeFilter(Filter filter, String pathSpec) {
        this.filters.removeIf((current) -> {
            return current.matches(filter, pathSpec);
        });
    }

    public void addStaticContent(String contentLocation, String serverMountPoint) {
        this.staticContent.put(serverMountPoint, contentLocation);
    }

    public void removeStaticContent(String contentLocation, String serverMountPoint) {
        this.staticContent.remove(serverMountPoint);
    }

    public void invokeDirectly(String targetPath, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.jetty.handle(targetPath, (Request)request, request, response);
    }

    public void setRequestLog(RequestLog requestLog) {
        this.requestLog = requestLog;
    }

    public Server getJetty() {
        return this.jetty;
    }

    private void startJetty() throws Exception {
        try {
            this.jetty.start();
        } catch (IOException var2) {
            throw new PortBindException(this.httpAddress, this.httpsAddress, var2);
        }
    }

    public InetSocketAddress getLocalHttpAddress() {
        return getAddress("HTTP", this.httpConnector);
    }

    public InetSocketAddress getLocalHttpsAddress() {
        return getAddress("HTTPS", this.httpsConnector);
    }

    private void loadAllMounts() {
        SortedSet<String> mountpoints = new TreeSet(Comparator.reverseOrder());
        mountpoints.addAll(this.staticContent.keySet());
        mountpoints.addAll(this.jaxRSPackages.keySet());
        mountpoints.addAll(this.jaxRSClasses.keySet());
        Iterator var2 = mountpoints.iterator();

        while(var2.hasNext()) {
            String contentKey = (String)var2.next();
            boolean isStatic = this.staticContent.containsKey(contentKey);
            boolean isJaxrsPackage = this.jaxRSPackages.containsKey(contentKey);
            boolean isJaxrsClass = this.jaxRSClasses.containsKey(contentKey);
            if (this.countSet(isStatic, isJaxrsPackage, isJaxrsClass) > 1) {
                throw new RuntimeException(String.format("content-key '%s' is mapped more than once", contentKey));
            }

            if (isStatic) {
                this.loadStaticContent(contentKey);
            } else if (isJaxrsPackage) {
                this.loadJAXRSPackage(contentKey);
            } else {
                if (!isJaxrsClass) {
                    throw new RuntimeException(String.format("content-key '%s' is not mapped", contentKey));
                }

                this.loadJAXRSClasses(contentKey);
            }
        }

    }

    private int countSet(boolean... booleans) {
        int count = 0;
        boolean[] var3 = booleans;
        int var4 = booleans.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            boolean bool = var3[var5];
            if (bool) {
                ++count;
            }
        }

        return count;
    }

    private void loadRequestLogging() {
        RequestLogHandler requestLogHandler = new HttpChannelOptionalRequestLogHandler();
        requestLogHandler.setRequestLog(this.requestLog);
        requestLogHandler.setServer(this.jetty);
        requestLogHandler.setHandler(this.jetty.getHandler());
        this.jetty.setHandler(requestLogHandler);
    }

    private String trimTrailingSlashToKeepJettyHappy(String mountPoint) {
        if (mountPoint.equals("/")) {
            return mountPoint;
        } else {
            if (mountPoint.endsWith("/")) {
                mountPoint = mountPoint.substring(0, mountPoint.length() - 1);
            }

            return mountPoint;
        }
    }

    private String ensureRelativeUri(String mountPoint) {
        try {
            URI result = new URI(mountPoint);
            return result.isAbsolute() ? result.getPath() : result.toString();
        } catch (URISyntaxException var3) {
            this.log.debug("Unable to translate [%s] to a relative URI in ensureRelativeUri(String mountPoint)", new Object[]{mountPoint});
            return mountPoint;
        }
    }

    private void loadStaticContent(String mountPoint) {
        String contentLocation = (String)this.staticContent.get(mountPoint);

        try {
            SessionHandler sessionHandler = new SessionHandler();
            sessionHandler.setServer(this.getJetty());
            WebAppContext staticContext = new WebAppContext();
            staticContext.setServer(this.getJetty());
            staticContext.setContextPath(mountPoint);
            staticContext.setSessionHandler(sessionHandler);
            staticContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
            URL resourceLoc = this.getClass().getClassLoader().getResource(contentLocation);
            if (resourceLoc != null) {
                URL url = resourceLoc.toURI().toURL();
                Resource resource = Resource.newResource(url);
                staticContext.setBaseResource(resource);
                this.addFiltersTo(staticContext);
                staticContext.addFilter(new FilterHolder(new StaticContentFilter()), "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
                this.handlers.addHandler(staticContext);
            } else {
                this.log.warn("No static content available for Neo4j Server at %s. management console may not be available.", new Object[]{this.addressConfigurationDescription()});
            }

        } catch (Exception var8) {
            this.log.error("Unknown error loading static content", var8);
            var8.printStackTrace();
            throw new RuntimeException(var8);
        }
    }

    private void loadJAXRSPackage(String mountPoint) {
        this.loadJAXRSResource(mountPoint, (JaxRsServletHolderFactory)this.jaxRSPackages.get(mountPoint));
    }

    private void loadJAXRSClasses(String mountPoint) {
        this.loadJAXRSResource(mountPoint, (JaxRsServletHolderFactory)this.jaxRSClasses.get(mountPoint));
    }

    private void loadJAXRSResource(String mountPoint, JaxRsServletHolderFactory jaxRsServletHolderFactory) {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setServer(this.getJetty());
        this.log.debug("Mounting servlet at [%s]", new Object[]{mountPoint});
        ServletContextHandler jerseyContext = new ServletContextHandler();
        jerseyContext.setServer(this.getJetty());
        jerseyContext.setErrorHandler(new NeoJettyErrorHandler());
        jerseyContext.setContextPath(mountPoint);
        jerseyContext.setSessionHandler(sessionHandler);
        jerseyContext.addServlet(jaxRsServletHolderFactory.create(this.defaultInjectables, this.wadlEnabled), "/*");
        this.addFiltersTo(jerseyContext);
        this.handlers.addHandler(jerseyContext);
    }

    private void addFiltersTo(ServletContextHandler context) {
        Iterator var2 = this.filters.iterator();

        while(var2.hasNext()) {
            FilterDefinition filterDef = (FilterDefinition)var2.next();
            context.addFilter(new FilterHolder(filterDef.getFilter()), filterDef.getPathSpec(), EnumSet.allOf(DispatcherType.class));
        }

    }

    private static InetSocketAddress getAddress(String name, ServerConnector connector) {
        if (connector == null) {
            throw new IllegalStateException(name + " connector is not configured");
        } else {
            return new InetSocketAddress(connector.getHost(), connector.getLocalPort());
        }
    }

    private void verifyAddressConfiguration() {
        if (this.httpAddress == null && this.httpsAddress == null) {
            throw new IllegalStateException("Either HTTP or HTTPS address must be configured to run the server");
        }
    }

    private String addressConfigurationDescription() {
        return (String)Stream.of(this.httpAddress, this.httpsAddress).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(", "));
    }

    private static class FilterDefinition {
        private final Filter filter;
        private final String pathSpec;

        FilterDefinition(Filter filter, String pathSpec) {
            this.filter = filter;
            this.pathSpec = pathSpec;
        }

        public boolean matches(Filter filter, String pathSpec) {
            return filter == this.filter && pathSpec.equals(this.pathSpec);
        }

        public Filter getFilter() {
            return this.filter;
        }

        String getPathSpec() {
            return this.pathSpec;
        }
    }
}
