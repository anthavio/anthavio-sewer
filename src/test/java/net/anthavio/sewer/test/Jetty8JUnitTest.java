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
//@ServerConfig(type = ServerType.JETTY, home = "src/test/jetty8", port = 0)
public class Jetty8JUnitTest {

	//@Rule
	//public MethodRule rule;

	@ServerPort
	private int port;

	@ServerConfig(type = ServerType.JETTY, home = "src/test/jetty8", port = 0)
	private ServerInstance server;

	@Test
	public void test() {
		Assertions.assertThat(port).isGreaterThan(0);
		Assertions.assertThat(server).isNotNull();
		Assertions.assertThat(server.getLocalPorts()[0]).isEqualTo(port);
	}
}
