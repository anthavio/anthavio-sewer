package net.anthavio.sewer.tomcat;

import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerMetadata;
import net.anthavio.sewer.ServerMetadata.CacheScope;
import net.anthavio.sewer.ServerType;

import org.apache.catalina.startup.Catalina;

/**
 * 
 * @author vanek
 * 
 * Tomcat 7 embedder
 */
public class TomcatWrapper implements ServerInstance {

	private final Catalina catalina;

	private final ServerMetadata metadata;

	public TomcatWrapper() {
		this(System.getProperty("catalina.base", System.getProperty("user.dir")));
	}

	public TomcatWrapper(String catalinaBase) {
		System.setProperty("catalina.home", catalinaBase);
		System.setProperty("catalina.base", catalinaBase);
		catalina = new Catalina();
		catalina.load();
		this.metadata = new ServerMetadata(ServerType.TOMCAT, catalinaBase, -1, null, CacheScope.JVM);
	}

	public TomcatWrapper(ServerMetadata metadata) {
		this(metadata.getServerHome());
	}

	@Override
	public ServerMetadata getMetadata() {
		return metadata;
	}

	public boolean isStarted() {
		return catalina != null && catalina.getServer().getState().isAvailable();
	}

	@Override
	public void start() {
		catalina.start();
	}

	@Override
	public void stop() {
		catalina.stop();
	}

	@Override
	public int[] getLocalPorts() {
		// TODO Auto-generated method stub
		return null;
	}
	/*
		@Override
		public Map<String, ApplicationContext> getSpringContexts() {
			StandardServer server = (StandardServer) catalina.getServer();
			StandardService service = (StandardService) server.findService("Catalina");
			Container engine = service.getContainer();
			Container host = engine.findChild("localhost");
			Container[] children = host.findChildren();

			Map<String, ApplicationContext> springContexts = new HashMap<String, ApplicationContext>();
			for (Container container : children) {
				if (container instanceof StandardContext) {
					StandardContext webAppCtx = (StandardContext) container;
					WebApplicationContext springCtx = WebApplicationContextUtils.getWebApplicationContext(webAppCtx
							.getServletContext());
					springContexts.put(webAppCtx.getDisplayName(), springCtx);
				}
			}
			return springContexts;
		}
	*/
}
