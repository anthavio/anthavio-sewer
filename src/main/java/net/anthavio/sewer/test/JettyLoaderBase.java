/**
 * 
 */
package net.anthavio.sewer.test;

import net.anthavio.spring.test.ContextRefLoader;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 * This class was a stupid idea. It makes little sense to do jetty starting and spring context loading in single place
 * 
 * Cannot shutdown these instances though, because there is no stop/shutdown callback/lifecycle
 * 
 * @author vanek
 *
 */
@Deprecated
public abstract class JettyLoaderBase extends ContextRefLoader {
	/*
		@Override
		public String[] processLocations(Class<?> testClass, String... locations) {

			ServerConfig[] configs = getServerConfigs(testClass);
			ServerMetadata[] serverSetups = new ServerMetadata[configs.length];
			for (int i = 0; i < configs.length; ++i) {
				ServerConfig config = configs[i];
				serverSetups[i] = new ServerMetadata(config.home(), config.port(), config.configs(), config.cache());
				getInstanceManager().checkoutServer(serverSetups[i]);
			}
			if (locations.length == 0) {
				//no spring context locations -> jetty start only
				return locations;
			} else {
				return super.processLocations(testClass, locations);
			}
		}
	*/

	/**
	 * We must to override this because jetty shutdown brings down Web contexts with Spring contexts 
	 * and our own ContextRefLoader shutdown hooks will do mess 
	 */
	@Override
	public AbstractApplicationContext loadContext(String... locations) throws Exception {
		if (locations.length == 0) {
			//no spring context locations -> jetty start only
			logger.info("No location are specified. Returning empty context");
			return new StaticApplicationContext();
		} else {
			logger.info("Loading " + locations[0] + " context with selector " + locations[1]);
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locations[1]);
			BeanFactoryReference reference = locator.useBeanFactory(locations[0]);
			AbstractApplicationContext context = (AbstractApplicationContext) reference.getFactory();
			return context;
		}
	}

}
