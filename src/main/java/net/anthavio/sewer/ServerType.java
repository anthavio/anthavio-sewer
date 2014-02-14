package net.anthavio.sewer;

import net.anthavio.sewer.jetty.Jetty6InstanceBuilder;
import net.anthavio.sewer.jetty.JettyInstanceBuilder;
import net.anthavio.sewer.tomcat.TomcatInstanceBuilder;

/**
 * List of supported servers
 * 
 * @author martin.vanek
 *
 */
public enum ServerType {

	//XXX putting classes here might require to ALL possible server classes to be on classpath. Then only String classnames will be here... 

	JETTY(JettyInstanceBuilder.class), JETTY6(Jetty6InstanceBuilder.class), TOMCAT(TomcatInstanceBuilder.class);

	private final Class<? extends ServerInstanceBuilder> builderClass;

	private ServerInstanceBuilder builder;

	private ServerType(Class<? extends ServerInstanceBuilder> builderClass) {
		this.builderClass = builderClass;
	}

	public ServerInstanceBuilder getBuilder() {
		if (builder == null) {
			try {
				builder = builderClass.newInstance();
			} catch (Exception x) {
				throw new IllegalStateException("ServerInstanceBuilder instance creation failed", x);
			}
		}
		return builder;

	}
}
