package net.anthavio.sewer.jetty;

import net.anthavio.sewer.ServerInstanceBuilder;
import net.anthavio.sewer.ServerMetadata;

/**
 * 
 * @author martin.vanek
 *
 */
public class Jetty6InstanceBuilder implements ServerInstanceBuilder {

	public static Jetty6InstanceBuilder INSTANCE = new Jetty6InstanceBuilder();

	@Override
	public Jetty6Wrapper build(ServerMetadata metadata) {
		return new Jetty6Wrapper(metadata);
	}

}
