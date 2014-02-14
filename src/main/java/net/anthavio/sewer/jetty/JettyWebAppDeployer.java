/**
 * 
 */
package net.anthavio.sewer.jetty;

import java.io.IOException;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Deploys single web application. Jetty 7/8 version
 * 
 * For usage example see src/test/jetty8/etc/jetty.xml
 * 
 * @author vanek
 *
 */
public class JettyWebAppDeployer extends org.eclipse.jetty.webapp.WebAppContext {

	public JettyWebAppDeployer(ContextHandlerCollection parent) {
		//Crucial part! Without this, we will not be started/stopped
		parent.addHandler(this);
	}

	/**
	 * Default version is too benevolent. It allows non existing directories and then just ignores them
	 */
	public void setResourceBase(String resourceBase) {
		Resource resource;
		try {
			resource = Resource.newResource(resourceBase);
			if (!resource.exists() || !resource.isDirectory()) {
				throw new IllegalArgumentException(resource + " is not an existing directory.");
			}
			super.setBaseResource(resource);
		} catch (IOException iox) {
			throw new RuntimeException(iox);
		}

	}

	/**
	 * Better named alternative for setResourceBase/setBaseResource duo... but does the same
	 */
	public void setWebAppDir(String path) {
		setResourceBase(path);
	}

}
