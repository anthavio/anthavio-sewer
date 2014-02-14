package net.anthavio.sewer.jetty;

import net.anthavio.sewer.ServerInstanceBuilder;
import net.anthavio.sewer.ServerMetadata;

/**
 * 
 * @author martin.vanek
 *
 */
public class JettyInstanceBuilder implements ServerInstanceBuilder {

	public static JettyInstanceBuilder INSTANCE = new JettyInstanceBuilder();

	@Override
	public JettyWrapper build(ServerMetadata metadata) {
		return new JettyWrapper(metadata);
	}

}
