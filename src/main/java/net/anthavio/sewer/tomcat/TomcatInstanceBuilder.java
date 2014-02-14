package net.anthavio.sewer.tomcat;

import net.anthavio.sewer.ServerInstanceBuilder;
import net.anthavio.sewer.ServerMetadata;

/**
 * 
 * @author martin.vanek
 *
 */
public class TomcatInstanceBuilder implements ServerInstanceBuilder {

	public static TomcatInstanceBuilder INSTANCE = new TomcatInstanceBuilder();

	@Override
	public TomcatWrapper build(ServerMetadata metadata) {
		return new TomcatWrapper(metadata);
	}
}
