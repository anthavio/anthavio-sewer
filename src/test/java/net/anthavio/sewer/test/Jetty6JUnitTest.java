package net.anthavio.sewer.test;

import net.anthavio.sewer.ServerConfig;
import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerPort;
import net.anthavio.sewer.ServerType;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author martin.vanek
 *
 */
@RunWith(SewerClassRunner.class)
//@ServerConfig(type = ServerType.JETTY6, home = "src/test/jetty6", port = 0)
public class Jetty6JUnitTest {

	//@Rule
	//public MethodRule rule;

	@ServerPort
	private int port;

	@ServerConfig(type = ServerType.JETTY6, home = "src/test/jetty6", port = 0)
	private ServerInstance jetty6_auto;

	@ServerConfig(type = ServerType.JETTY6, home = "src/test/jetty6")
	private ServerInstance jetty6_8080;

	@ServerConfig(type = ServerType.JETTY6, home = "src/test/jetty6", port = 9876)
	private ServerInstance jetty6_9876;

	@Test
	public void test() {
		Assertions.assertThat(port).isGreaterThan(0);
		Assertions.assertThat(jetty6_auto).isNotNull();
		Assertions.assertThat(jetty6_auto.getLocalPorts()[0]).isEqualTo(port);

		Assertions.assertThat(jetty6_8080.getLocalPorts()[0]).isEqualTo(8080);
		Assertions.assertThat(jetty6_9876.getLocalPorts()[0]).isEqualTo(9876);
	}
}
