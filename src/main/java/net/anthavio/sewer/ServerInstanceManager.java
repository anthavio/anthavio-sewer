package net.anthavio.sewer;

import java.util.HashMap;
import java.util.Map;

import net.anthavio.sewer.ServerMetadata.CacheScope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ServerInstanceManager life spans across all tests, so server instance can be reused whithin test cases.
 * 
 * @author martin.vanek
 *
 */
public class ServerInstanceManager {

	public static final ServerInstanceManager INSTANCE = new ServerInstanceManager();

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<ServerMetadata, ServerInstance> cache = new HashMap<ServerMetadata, ServerInstance>();

	/**
	 * Build concrete server instance according ServerInstanceData is contract for concrete ServerInstanceManager
	 */
	//protected abstract ServerInstance newServerInstance(ServerMetadata setup);
	/*
		private void housekeeping(ServerMetadata newSetup) {
			Set<ServerMetadata> keySet = cache.keySet();
			for (ServerMetadata oldSetup : keySet) {
				ServerInstance server = null;
				switch (oldSetup.getCache()) {
				case NEVER:
					//remove abandoned entries
					server = cache.remove(oldSetup);
					break;
				case CHANGE:
					//remove change driven entries
					if (!newSetup.equals(oldSetup)) {
						//XXX this will never work...
						server = cache.remove(oldSetup);
					}
					break;
				}
				if (server != null) {
					logger.debug("Cache remove " + oldSetup);
					try {
						if (server.isStarted()) {
							server.stop();
						}
					} catch (Exception x) {
						logger.warn("Exception while stopping server " + x);
					}
				}
			}
		}
	*/
	public ServerInstance borrowServer(ServerMetadata setup) {
		logger.debug("server borrow " + setup);
		//houskeeping first...
		//housekeeping(setup);

		ServerInstance server = cache.get(setup);
		if (server == null) {
			logger.debug("Cache miss " + setup);
			server = setup.newServerInstance();
			server.start();
			cache.put(setup, server);
		} else {
			logger.debug("Cache hit " + setup);
		}

		return server;
	}

	/**
	 * For a good citizents that are cleaning after themselves
	 * @return 
	 */
	public ServerInstance returnServer(ServerMetadata setup) {
		logger.debug("server return " + setup);
		ServerInstance server = cache.get(setup);
		if (server == null) {
			throw new IllegalArgumentException("Server not found in cache " + setup);
		}
		if (setup.getCache() == CacheScope.METHOD) {
			cache.remove(setup);
			try {
				logger.debug("Cache remove " + setup);
				if (server.isStarted()) {
					server.stop();
				}
			} catch (Exception x) {
				logger.warn("Exception while stopping server " + x);
			}
		}
		return server;
	}

	/**
	 * Return instance
	 */
	public ServerInstance getServer(ServerMetadata setup) {
		if (setup == null) {
			throw new IllegalArgumentException("Null setup data!!!");
		}
		return cache.get(setup);
	}

}
