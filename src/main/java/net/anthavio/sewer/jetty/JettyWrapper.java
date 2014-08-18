package net.anthavio.sewer.jetty;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerMetadata;
import net.anthavio.sewer.ServerMetadata.CacheScope;
import net.anthavio.sewer.ServerType;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 * Works with Jetty version 7, 8, 9
 * 
 * Be aware that those Jetty versions do NOT use System properties (jetty.home, jetty.port, ...) anymore.
 * 
 * @author vanek
 * 
 */
public class JettyWrapper implements ServerInstance {

	protected static final Logger log = Log.getLogger(JettyWrapper.class);

	public static final String DEFAULT_JETTY_HOME = System.getProperty("user.dir");

	public static final String DEFAULT_JETTY_CFG = "/etc/jetty.xml";

	private final String jettyHome;

	private final String jettyLogs;

	private final String[] configs;

	private final Map<String, String> properties = new HashMap<String, String>();

	private final Server server;

	private final ServerMetadata metadata;

	public JettyWrapper() {
		this(System.getProperty("jetty.home", DEFAULT_JETTY_HOME));
	}

	public JettyWrapper(String jettyHome) {
		this(jettyHome, -1, jettyHome + DEFAULT_JETTY_CFG);
	}

	public JettyWrapper(String jettyHome, String... configs) {
		this(jettyHome, -1, configs);
	}

	public JettyWrapper(String jettyHome, int port) {
		this(jettyHome, port, jettyHome + DEFAULT_JETTY_CFG);
	}

	/**
	 * @param port < 0 - jetty.xml, port 0 - dynamic, port > 0 static
	 */
	public JettyWrapper(String jettyHome, int port, String... configs) {
		if (jettyHome == null) {
			throw new IllegalArgumentException("Null jettyHome");
		}
		File fJettyHome = new File(jettyHome);
		if (fJettyHome.exists() == false) {
			throw new IllegalArgumentException("jetty.home directory does not exist " + fJettyHome.getAbsolutePath());
		}
		this.jettyHome = jettyHome;
		//System.setProperty("jetty.home", jettyHome);
		properties.put("jetty.home", this.jettyHome);

		this.jettyLogs = jettyHome + "/logs";
		File fJettyLogs = new File(jettyLogs);
		if (fJettyLogs.exists() == false) {
			if (fJettyLogs.mkdir() == false) {
				throw new IllegalArgumentException("jetty.logs directory does not exist and cannot be created: "
						+ fJettyLogs.getAbsolutePath());
			}
		}
		//System.setProperty("jetty.logs", jettyLogs);
		properties.put("jetty.logs", this.jettyLogs);

		if (configs == null || configs.length == 0) {
			configs = new String[] { "jetty.xml" };
			//throw new IllegalArgumentException("At least one jetty config file must be specified");
		}
		this.configs = new String[configs.length];
		for (int i = 0; i < configs.length; ++i) {
			String config = configs[i];
			File fConfig = new File(config);
			if (fConfig.exists()) {
				this.configs[i] = config;
			} else {
				if (fConfig.isAbsolute()) {
					throw new IllegalArgumentException("Config file " + configs[i] + " does not exist");
				} else {
					if (exists(jettyHome + "/" + config)) {
						this.configs[i] = jettyHome + "/" + config;
					} else if (exists(jettyHome + "/etc/" + config)) {
						this.configs[i] = jettyHome + "/etc/" + config;
					} else {
						throw new IllegalArgumentException("Config file " + configs[i] + " not found in " + jettyHome);
					}
				}
			}
		}

		if (port >= 0) {
			//override jetty.xml connector port
			//System.setProperty("jetty.port", String.valueOf(port));
			properties.put("jetty.port", String.valueOf(port));
		}
		this.metadata = new ServerMetadata(ServerType.JETTY, jettyHome, port, configs, CacheScope.JVM);
		this.server = configure(this.configs);

	}

	public JettyWrapper(ServerMetadata metadata) {
		this(metadata.getServerHome(), metadata.getPort(), metadata.getConfigs());
	}

	/**
	 * Single servlet Jetty with dynamically allocated port
	 */
	public JettyWrapper(HttpServlet servlet) {
		this(0, servlet);
	}

	public JettyWrapper(int port, HttpServlet servlet) {
		this(port, "/", "/*", servlet);
	}

	/**
	 * @param port - 0 means dynamic allocated port
	 * @param rootContextPath - "/" default
	 * @param servletPathSpec - "/*" default
	 * @param servlet
	 */
	public JettyWrapper(int port, String rootContextPath, String servletPathSpec, HttpServlet servlet) {
		server = new Server(port);
		server.setStopAtShutdown(true);

		ServletContextHandler rootContext = new ServletContextHandler(server, rootContextPath);
		ServletHolder servletHolder = new ServletHolder(servlet);
		rootContext.addServlet(servletHolder, servletPathSpec);
		server.setHandler(rootContext);

		configs = new String[0];
		jettyHome = null;
		jettyLogs = null;
		metadata = null;
	}

	@Override
	public ServerMetadata getMetadata() {
		return metadata;
	}

	private boolean exists(String path) {
		return new File(path).exists();
	}

	public String getJettyHome() {
		return jettyHome;
	}

	public String getJettyLogs() {
		return jettyLogs;
	}

	public String[] getConfigs() {
		return configs;
	}

	public Server getServer() {
		return server;
	}

	public int[] getLocalPorts() {
		if (!isStarted()) {
			throw new IllegalStateException("Start Server first");
		}
		int[] ports = getLocalPorts(this.server);
		if (ports == null || ports.length == 0) {
			throw new IllegalStateException("Cannot find port. No Connector is configured for Server");
		}
		return ports;
	}

	public int getPort() {
		int[] ports = getLocalPorts();
		if (ports.length > 1) {
			log.warn("Multiple ports found " + Arrays.asList(ports));
		}
		return ports[0];
	}

	public boolean isStarted() {
		return server != null && server.isRunning();
	}

	public boolean isStopped() {
		return server == null || server.isStopped();
	}

	public void start() {
		if (isStopped()) {
			try {
				log.info("Start Jetty " + this.toString());
				server.start();
			} catch (Exception x) {
				try {
					server.stop();
				} catch (Exception x2) {
					// ignore
				}
				if (x instanceof RuntimeException) {
					throw (RuntimeException) x;
				} else {
					throw new IllegalStateException("Jetty failed to start", x);
				}
			}
			// We don't want running server with failed apps
			if (isSomethingRotten()) {
				try {
					server.stop();
				} catch (Exception e) {
					//ignore
				}
			}
		} else {
			throw new IllegalStateException("Jetty is already running");
		}
	}

	public void stop() {
		if (isStarted()) {
			try {
				log.info("Stop Jetty " + this.toString());
				server.stop();
			} catch (Exception x) {
				if (x instanceof RuntimeException) {
					throw (RuntimeException) x;
				} else {
					throw new IllegalStateException("Jetty failed to stop", x);
				}
			}
		} else {
			throw new IllegalStateException("Jetty is not running");
		}
	}

	private Server configure(String[] configs) {
		Map<String, Object> id_map = new HashMap<String, Object>();
		Server server = new Server();
		id_map.put("Server", server);
		for (String jettyXml : configs) {
			File fJettyXml = new File(jettyXml);
			if (fJettyXml.exists() == false) {
				throw new IllegalStateException("Jetty config does not exist " + fJettyXml.getAbsolutePath());
			}
			try {
				XmlConfiguration config = new XmlConfiguration(fJettyXml.toURI().toURL());
				config.getIdMap().putAll(id_map);
				config.getProperties().putAll(properties);
				config.configure(server);
				id_map = config.getIdMap();
			} catch (Exception x) {
				if (x instanceof RuntimeException) {
					throw (RuntimeException) x;
				}
				throw new IllegalStateException("Jetty failed to configure", x);
			}
		}
		return server;
	}

	/**
	 * Issue here is getLocalPort() method was moved in Jetty 9 
	 * 
	 * Jetty 7 an 8 has getLocalPort() on org.eclipse.jetty.server.Connector interface
	 * 
	 * Jetty 9 has redesigned Connector hierarchy and getLocalPort() method 
	 * is now on new org.eclipse.jetty.server.NetworkConnector interface (which extends original Connector)
	 */
	protected int[] getLocalPorts(Server server) {
		Class<?> clazz;
		Method mGetLocalPort;
		boolean jetty9 = false;
		try {
			clazz = Class.forName("org.eclipse.jetty.server.NetworkConnector");
			jetty9 = true;
		} catch (ClassNotFoundException cnx) {
			clazz = Connector.class; //jetty 8
		}
		//method getLocalPort() muste exist on connector class
		try {
			mGetLocalPort = clazz.getMethod("getLocalPort");
		} catch (Exception x) {
			throw new RuntimeException("Cannot find getLocalPort() method on " + clazz);
		}

		Connector[] connectors;
		if (jetty9) {
			connectors = getJetty9Connectors(server, clazz);
		} else {
			connectors = server.getConnectors();
		}

		int[] ports = new int[connectors.length];
		for (int i = 0; i < connectors.length; ++i) {
			Connector connector = connectors[i];
			try {
				ports[i] = (Integer) mGetLocalPort.invoke(connector);
			} catch (Exception x) {
				throw new RuntimeException("getLocalPort() invocation failed on " + connector, x);
			}
		}
		return ports;
	}

	/**
	 * Jetty9 NetworkConnector hierarchy 
	 * http://download.eclipse.org/jetty/stable-9/apidocs/org/eclipse/jetty/server/NetworkConnector.html 
	 */
	private Connector[] getJetty9Connectors(Server server, Class<?> connectorClass) {
		Connector[] connectors = server.getConnectors();
		if (connectors == null || connectors.length == 0) {
			throw new IllegalStateException("Cannot find port. No Connector is configured for server");
		}
		List<Connector> nconnectors = new ArrayList<Connector>();
		for (Connector connector : connectors) {
			//if(connector instanceof NetworkConnector)
			if (connectorClass.isAssignableFrom(connector.getClass())) {
				nconnectors.add(connector);
			}
		}
		return nconnectors.toArray(new Connector[nconnectors.size()]);
	}

	/**
	 * WebAppContext initialization exceptions are swallowed.
	 * 
	 * Only way to detect failed deployment is private field _unavailable
	 */
	private boolean isSomethingRotten() {
		Handler[] handlers = server.getChildHandlers();
		for (Handler handler : handlers) {
			if (handler instanceof WebAppContext) {
				WebAppContext wac = (WebAppContext) handler;
				if (wac.isFailed() || wac.getUnavailableException() != null) {
					log.warn("Failed WebAppContext found " + wac);
					return true;
				}
				//explore servlets as well - Jersey servet for example don't invalidate context/webapp on it's initialisation exception
				ServletHandler servletHandler = wac.getServletHandler();
				ServletHolder[] servlets = servletHandler.getServlets();
				for (ServletHolder servletHolder : servlets) {
					if (servletHolder.isFailed() || servletHolder.getUnavailableException() != null) {
						log.warn("Failed " + servletHolder + " found in WebAppContext " + wac);
						return true;
					}
				}

			}
		}
		return false;
	}

	/*
		@Override
		public Map<String, ApplicationContext> getSpringContexts() {
			HandlerCollection handlers = (HandlerCollection) server.getHandler();
			Handler[] webAppContexts = handlers.getChildHandlersByClass(WebAppContext.class);
			HashMap<String, ApplicationContext> springContexts = new HashMap<String, ApplicationContext>();
			for (Handler handler : webAppContexts) {
				WebAppContext webAppCtx = (WebAppContext) handler;
				WebApplicationContext springCtx = WebApplicationContextUtils.getWebApplicationContext(webAppCtx
						.getServletContext());
				springContexts.put(webAppCtx.getDisplayName(), springCtx);
			}
			return springContexts;
		}
	*/
	@Override
	public String toString() {
		return jettyHome + " " + Arrays.asList(configs);
	}

	public static void main(String[] args) {
		JettyWrapper jetty;
		if (args.length == 0) {
			jetty = new JettyWrapper();
		} else {
			File fFirst = new File(args[0]);
			if (fFirst.exists() == false) {
				throw new IllegalArgumentException("Nonexisting jetty.home or config file");
			}
			if (fFirst.isDirectory()) {
				String[] args2 = new String[args.length - 1];
				System.arraycopy(args, 1, args2, 0, args2.length);
				jetty = new JettyWrapper(args[0], args2);
			} else {
				// first arg is a file
				jetty = new JettyWrapper(DEFAULT_JETTY_HOME, args);
			}
		}
		jetty.start();
	}

}
