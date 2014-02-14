package net.anthavio.sewer;

import java.util.Arrays;

/**
 * 
 * @author martin.vanek
 *
 */
public class ServerMetadata {

	public static enum CacheInstance {
		ALLWAYS, //cache forever
		CLASS, //ceche between methods (tests) of same class
		NEVER, //don't cache even between methods (tests)
		CHANGE; //cache until different instance is requested
	}

	private final ServerType type;

	private final String serverHome;

	private final int port;

	private final String[] configs;

	private final CacheInstance cache;

	public ServerMetadata(ServerConfig annotation) {
		this(annotation.type(), annotation.home(), annotation.port(), annotation.configs(), annotation.cache());
	}

	public ServerMetadata(ServerType type, String serverHome, int port, String[] configs, CacheInstance cache) {
		if (type == null) {
			throw new IllegalArgumentException("Null ServerType");
		}
		this.type = type;

		if (serverHome == null) {
			throw new IllegalArgumentException("Null serverHome");
		}
		this.serverHome = serverHome;

		this.port = port;
		this.configs = configs;

		if (cache == null) {
			throw new IllegalArgumentException("Null cache");
		}
		this.cache = cache;
	}

	public ServerInstance newServerInstance() {
		return type.getBuilder().build(this);
	}

	public String getServerHome() {
		return serverHome;
	}

	public int getPort() {
		return port;
	}

	public String[] getConfigs() {
		return configs;
	}

	public CacheInstance getCache() {
		return cache;
	}

	@Override
	public String toString() {
		return type + " " + serverHome + " " + port + " " + Arrays.asList(configs) + " " + cache;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cache == null) ? 0 : cache.hashCode());
		result = prime * result + Arrays.hashCode(configs);
		result = prime * result + port;
		result = prime * result + ((serverHome == null) ? 0 : serverHome.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerMetadata other = (ServerMetadata) obj;
		if (cache != other.cache)
			return false;
		if (!Arrays.equals(configs, other.configs))
			return false;
		if (port != other.port)
			return false;
		if (serverHome == null) {
			if (other.serverHome != null)
				return false;
		} else if (!serverHome.equals(other.serverHome))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
