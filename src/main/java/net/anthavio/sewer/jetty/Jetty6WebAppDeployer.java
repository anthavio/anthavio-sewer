package net.anthavio.sewer.jetty;

import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;

/**
 * 
 * Deploys single web application. Jetty 6 version
 * 
 * Only reason to 'extends WebAppContex' is that I'm lazy to copy all WebAppContext configuration fields/setters
 * 
 * For usage example see src/test/jetty6/etc/jetty.xml
 * 
 * @author martin.vanek
 *
 */
public class Jetty6WebAppDeployer extends WebAppContext {

	private HandlerContainer _contexts;

	private WebAppContext wac;

	public HandlerContainer getContexts() {
		return _contexts;
	}

	public void setContexts(HandlerContainer _contexts) {
		this._contexts = _contexts;
	}

	public void doStart() throws Exception {
		if (_contexts == null)
			throw new IllegalArgumentException("No HandlerContainer");
		wac = new WebAppContext();

		wac.setContextPath(getContextPath());

		if (getConfigurationClasses() != null) {
			wac.setConfigurationClasses(getConfigurationClasses());
		}
		if (getDefaultsDescriptor() != null) {
			wac.setDefaultsDescriptor(getDefaultsDescriptor());
		}
		if (getWar() != null) {
			Resource r = Resource.newResource(getWar());
			if (!r.exists()) {
				throw new IllegalArgumentException("War does not exist " + getWar());
			}
			wac.setWar(getWar());
		}
		if (getResourceBase() != null) {
			Resource r = Resource.newResource(getResourceBase());
			if (!r.exists()) {
				throw new IllegalArgumentException("ResourceBase does not exist " + getResourceBase());
			}
			wac.setResourceBase(getResourceBase());
		}

		wac.setExtractWAR(isExtractWAR());
		wac.setCopyWebDir(isCopyWebDir());

		wac.setExtraClasspath(getExtraClasspath());
		wac.setParentLoaderPriority(isParentLoaderPriority());

		_contexts.addHandler(wac);
		wac.start();
	}

	@Override
	protected void doStop() throws Exception {
		wac.stop();
	}
}
