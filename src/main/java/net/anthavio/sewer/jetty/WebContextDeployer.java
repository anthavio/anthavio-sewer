package net.anthavio.sewer.jetty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.mortbay.jetty.deployer.ContextDeployer;

/**
 * 
 * @author vanek
 *
 * Jetty LifeCycle pro deployment jedineho WebContextu.
 * Vychazi z @see {@link ContextDeployer} a pouziva @see {@link WebAppContext}
 * 
 * Mozna by stalo zato dedit primo z WebAppContext
 * 
 * @deprecated use JettyWebAppDeployer instead
 */
@Deprecated
public class WebContextDeployer extends AbstractLifeCycle {

	private static final Logger LOG = Log.getLogger(WebContextDeployer.class);

	private ContextHandlerCollection _contexts;

	private final WebAppContext webContext;

	private String classLoaderName;

	private String tempDirectory;

	private static Map<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();

	public void setContexts(ContextHandlerCollection contexts) {
		if (isStarted() || isStarting()) {
			throw new IllegalStateException("Cannot set Contexts after deployer start");
		}
		_contexts = contexts;
	}

	public WebContextDeployer() throws Exception {
		super();
		webContext = new WebAppContext();
	}

	@Override
	protected void doStart() throws Exception {
		if (_contexts == null) {
			throw new IllegalStateException("No context handler collection specified for deployer");
		}

		if (getWar() == null && getBaseResource() == null) {
			throw new IllegalStateException("No war directory specified for deployer");
		}

		deploy();
	}

	@Override
	protected void doStop() throws Exception {
		undeploy();
	}

	private void undeploy() throws Exception {
		LOG.info("Undeploy " + webContext.getWar() + " -> " + webContext);
		if (webContext == null) {
			return;
		}
		_contexts.removeHandler(webContext);
		webContext.stop();
	}

	private void deploy() throws Exception {

		//Pri pouziti JNDI (jetty-plus) musi mit kazda aplikace vlastni ClassLoader
		//jinak to padne na javax.naming.NamingException: This context is immutable
		ClassLoader classLoader;
		if (classLoaderName != null) {
			classLoader = classLoaders.get(classLoaderName);
			if (classLoader == null) {
				classLoader = new WebAppClassLoader(webContext);
				classLoaders.put(classLoaderName, classLoader);
			}
		} else {
			classLoader = new WebAppClassLoader(webContext);
			//ClassLoader classLoader = WebContextDeployer.class.getClassLoader();
			//ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		}
		webContext.setClassLoader(classLoader);

		webContext.setParentLoaderPriority(true);

		webContext.setServer(_contexts.getServer());

		webContext.setExtractWAR(false);
		webContext.setCopyWebDir(false);

		//nastaveni tempDirectory
		if (tempDirectory != null) {
			File dir = new File(tempDirectory);
			if (dir.isDirectory() && dir.canWrite()) {
				webContext.setTempDirectory(dir);
			} else {
				LOG.warn("Illegal temDirectory path: " + tempDirectory + " for context: " + getContextPath());
			}
		}

		_contexts.addHandler(webContext);

		// kvuli logovani
		if ((getWar() == null || getWar().length() == 0) && getBaseResource() != null) {
			setWar(getBaseResource().toString());
		}

		LOG.info("Deploy " + getWar() + " -> " + getContextPath());
		if (_contexts.isStarted()) {
			webContext.start();
		}

	}

	public void setWar(String war) {
		this.webContext.setWar(war);
	}

	public String getWar() {
		return this.webContext.getWar();
	}

	public void setContextPath(String contextPath) {
		this.webContext.setContextPath(contextPath);
	}

	public String getContextPath() {
		return this.webContext.getContextPath();
	}

	public void setDefaultsDescriptor(String defaultsDescriptor) {
		this.webContext.setDefaultsDescriptor(defaultsDescriptor);
	}

	public void setConfigurationClasses(String[] configurationClasses) {
		this.webContext.setConfigurationClasses(configurationClasses);
	}

	public void setExtraClassPath(String extraClassPath) {
		this.webContext.setExtraClasspath(extraClassPath);
	}

	public void setClassLoaderName(String classLoaderName) {
		this.classLoaderName = classLoaderName;
	}

	public Resource getBaseResource() {
		return this.webContext.getBaseResource();
	}

	public void setBaseResource(ResourceCollection baseResource) {
		this.webContext.setBaseResource(baseResource);
	}

	public String getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public SecurityHandler getSecurityHandler() {
		return webContext.getSecurityHandler();
	}

	public void setSecurityHandler(SecurityHandler securityHandler) {
		this.webContext.setSecurityHandler(securityHandler);
	}
}
