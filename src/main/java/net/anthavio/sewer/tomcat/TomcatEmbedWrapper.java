package net.anthavio.sewer.tomcat;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerMetadata;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.UnhandledException;

/**
 * @author vanek
 * 
 *         Embeded Tomcat 7 wrapper implementation
 * 
 *         {@link org.apache.catalina.startup.Tomcat}
 * 
 *         {@link StandardServer} {@link org.apache.catalina.Server}
 * 
 *         {@link StandardService} {@link org.apache.catalina.Service}
 * 
 *         {@link StandardEngine} {@link org.apache.catalina.Engine}
 *         {@link org.apache.catalina.Container} ${catalina.base}
 * 
 *         {@link StandardHost} {@link org.apache.catalina.Host}
 *         {@link org.apache.catalina.Container} ${catalina.base}/webapps
 * 
 *         {@link StandardContext#startInternal}
 *         {@link org.apache.catalina.Context}
 *         {@link org.apache.catalina.Container}
 * 
 *         {@link WebappLoader#startInternal} {@link org.apache.catalina.Loader}
 * 
 *         {@link ContextConfig}
 * 
 *         {@link StandardWrapper}
 */
public class TomcatEmbedWrapper implements ServerInstance {

	private final Tomcat tomcat;

	private final Map<String, StandardContext> webAppContexts = new HashMap<String, StandardContext>();

	public TomcatEmbedWrapper(int port, String catalinaBase, String appBase) {
		tomcat = new Tomcat();
		tomcat.setPort(port);
		System.setProperty("catalina.home", catalinaBase);
		System.setProperty("catalina.base", catalinaBase);
		tomcat.setBaseDir(catalinaBase);
		tomcat.getHost().setAppBase(appBase);
	}

	public TomcatEmbedWrapper(int port) {
		this(port, System.getProperty("user.dir"), System.getProperty("user.dir"));
	}

	@Override
	public ServerMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addWebApp(String context, String webAppDir) {
		try {
			StandardContext tomcatContext = (StandardContext) tomcat.addWebapp(context, webAppDir);
			webAppContexts.put(context, tomcatContext);
		} catch (ServletException sx) {
			throw new UnhandledException(sx);
		}
		/*
		 * //Inicializace primo kontextu StandardContext ctx = new
		 * StandardContext(); ctx.setPath("/example");
		 * ctx.setDocBase("../../webapp"); ctx.addLifecycleListener(new
		 * DefaultWebXmlListener());
		 * 
		 * ContextConfig ctxCfg = new ContextConfig(); //bez tohohle to inicializuje
		 * dvakrat jsp servlet
		 * ctxCfg.setDefaultWebXml("org/apache/catalin/startup/NO_DEFAULT_XML");
		 * ctx.addLifecycleListener(ctxCfg); host.addChild(ctx);
		 */
	}

	@Override
	public int[] getLocalPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isStarted() {
		return tomcat != null && tomcat.getServer().getState().isAvailable();
	}

	public void start() {
		try {
			tomcat.start();
		} catch (LifecycleException lx) {
			throw new UnhandledException(lx);
		}
		tomcat.getServer().await();
	}

	public void stop() {
		try {
			tomcat.getServer().stop();
		} catch (LifecycleException lx) {
			throw new UnhandledException(lx);
		}
	}
	/*
		public Map<String, ApplicationContext> getSpringContexts() {
			Map<String, ApplicationContext> springContexts = new HashMap<String, ApplicationContext>();
			for (StandardContext webAppCtx : webAppContexts.values()) {
				WebApplicationContext springCtx = WebApplicationContextUtils.getWebApplicationContext(webAppCtx
						.getServletContext());
				springContexts.put(webAppCtx.getDisplayName(), springCtx);
			}
			return springContexts;
		}
	*/

}
