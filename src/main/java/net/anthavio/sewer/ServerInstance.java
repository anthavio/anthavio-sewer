package net.anthavio.sewer;

/**
 * Common interface for Servers
 * 
 * @author martin.vanek
 *
 */
public interface ServerInstance {

	public ServerMetadata getMetadata();

	public void start();

	public void stop();

	public boolean isStarted();

	public int[] getLocalPorts();

}
