package net.anthavio.sewer.jetty;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerMetadata;
import net.anthavio.sewer.ServerMetadata.CacheScope;
import net.anthavio.sewer.ServerType;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlConfiguration;

/**
 * Works with Jetty version 6
 * 
 * @author martin.vanek
 *
 */
public class Jetty6Wrapper implements ServerInstance {

	private static final String DEFAULT_JETTY_HOME = System.getProperty("user.dir");

	private static final String DEFAULT_JETTY_CFG = "/etc/jetty.xml";

	private final String jettyHome;

	private final String jettyLogs;

	private final String[] configs;

	private final Properties properties = new Properties();

	private final Server server;

	private final ServerMetadata metadata;

	public Jetty6Wrapper() {
		this(System.getProperty("jetty.home", DEFAULT_JETTY_HOME));
	}

	public Jetty6Wrapper(String jettyHome) {
		this(jettyHome, -1, jettyHome + DEFAULT_JETTY_CFG);
	}

	public Jetty6Wrapper(String jettyHome, String... configs) {
		this(jettyHome, -1, configs);
	}

	public Jetty6Wrapper(String jettyHome, int port) {
		this(jettyHome, port, jettyHome + DEFAULT_JETTY_CFG);
	}

	/**
	 * @param port < 0 - jetty.xml, port 0 - dynamic, port > 0 static
	 */
	public Jetty6Wrapper(String jettyHome, int port, String... configs) {
		if (jettyHome == null) {
			throw new IllegalArgumentException("Null jettyHome");
		}
		File fJettyHome = new File(jettyHome);
		if (fJettyHome.exists() == false) {
			throw new IllegalArgumentException("jetty.home directory does not exist " + fJettyHome.getAbsolutePath());
		}
		this.jettyHome = jettyHome;

		properties.put("jetty.home", jettyHome);
		//System.setProperty("jetty.home", jettyHome);

		this.jettyLogs = jettyHome + "/logs";
		File fJettyLogs = new File(jettyLogs);
		if (fJettyLogs.exists() == false) {
			if (fJettyLogs.mkdir() == false) {
				throw new IllegalArgumentException("jetty.logs directory does not exist and cannot be created: "
						+ fJettyLogs.getAbsolutePath());
			}
		}
		properties.put("jetty.logs", jettyHome);
		//System.setProperty("jetty.logs", jettyLogs);

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
						throw new IllegalArgumentException("Config file " + configs[i] + " cannot be located inside " + jettyHome);
					}

					//throw new IllegalArgumentException("Config file " + configs[i] + " cannot be located relatively to "
					//		+ jettyHome);
				}
			}
		}

		if (port >= 0) {
			//override jetty.xml connector port
			//System.setProperty("jetty.port", String.valueOf(port));
			properties.put("jetty.port", port);
		}
		this.metadata = new ServerMetadata(ServerType.JETTY6, jettyHome, port, configs, CacheScope.JVM);
		this.server = configure(this.configs);
	}

	public Jetty6Wrapper(ServerMetadata metadata) {
		this(metadata.getServerHome(), metadata.getPort(), metadata.getConfigs());
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

	public int getPort() {
		int[] ports = getLocalPorts(this.server);
		if (ports.length > 1) {
			Log.warn("Multiple ports found " + Arrays.asList(ports));
		}
		return ports[0];
	}

	@Override
	public int[] getLocalPorts() {
		if (!isStarted()) {
			throw new IllegalStateException("Start Server first");
		}
		int[] ports = getLocalPorts(server);
		if (ports == null || ports.length == 0) {
			throw new IllegalStateException("Cannot find port. No Connector is configured for Server");
		}
		return ports;
	}

	@Override
	public boolean isStarted() {
		return server != null && this.server.isStarted();
	}

	public boolean isStopped() {
		return server == null || this.server.isStopped();
	}

	@Override
	public void start() {
		if (isStopped()) {
			try {
				Log.info("Start Jetty " + this.toString());
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
					Log.warn("Exception occured while stopping Jetty with failed webapp", e);
				}
			}
		} else {
			throw new IllegalStateException("Jetty is already running");
		}
	}

	@Override
	public void stop() {
		if (isStarted()) {
			try {
				Log.info("Stop Jetty " + this.toString());
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
		// Refactored org.mortbay.jetty.XmlConfiguration.main(new String[] { "src/test/jetty/etc/jetty.xml" });
		Server server = null;
		XmlConfiguration last = null;
		try {
			for (int i = 0; i < configs.length; i++) {
				if (configs[i].toLowerCase().endsWith(".properties")) {
					properties.load(Resource.newResource(configs[i]).getInputStream());
				} else {
					XmlConfiguration configuration = new XmlConfiguration(Resource.newResource(configs[i]).getURL());
					if (last != null) {
						configuration.getIdMap().putAll(last.getIdMap());
					}
					if (properties.size() > 0) {
						configuration.setProperties(properties);
					}
					Object object = configuration.configure();
					last = configuration;
					if (object instanceof Server) {
						server = (Server) object;
					}
				}
			}
		} catch (Exception x) {
			if (x instanceof RuntimeException) {
				throw (RuntimeException) x;
			}
			throw new IllegalStateException("Jetty configuration failed", x);
		}
		if (server == null) {
			throw new IllegalStateException("Jetty instance not found in configuration");
		}
		return server;
	}

	private int[] getLocalPorts(Server server) {
		Connector[] connectors = server.getConnectors();
		if (connectors == null || connectors.length == 0) {
			throw new IllegalStateException("Cannot find port. No connector is configured for server");
		}

		int[] ports = new int[connectors.length];
		for (int i = 0; i < connectors.length; ++i) {
			Connector connector = connectors[i];
			ports[i] = connector.getLocalPort();
		}
		return ports;
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
					Log.warn("Failed WebAppContext found " + wac);
					return true;
				}
				//explore servlets as well - Jersey servet for example don't invalidate context/webapp on it's initialisation exception
				ServletHandler servletHandler = wac.getServletHandler();
				ServletHolder[] servlets = servletHandler.getServlets();
				for (ServletHolder servletHolder : servlets) {
					if (servletHolder.isFailed() || servletHolder.getUnavailableException() != null) {
						Log.warn("Failed " + servletHolder + " found in WebAppContext " + wac);
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
		Jetty6Wrapper jetty;
		if (args.length == 0) {
			jetty = new Jetty6Wrapper();
		} else {
			File fFirst = new File(args[0]);
			if (fFirst.exists() == false) {
				throw new IllegalArgumentException("Nonexisting jetty.home or config file");
			}
			if (fFirst.isDirectory()) {
				String[] args2 = new String[args.length - 1];
				System.arraycopy(args, 1, args2, 0, args2.length);
				jetty = new Jetty6Wrapper(args[0], args2);
			} else {
				// first arg is a file
				jetty = new Jetty6Wrapper(DEFAULT_JETTY_HOME, args);
			}
		}
		jetty.start();
	}

}
