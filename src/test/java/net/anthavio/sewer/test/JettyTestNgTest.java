package net.anthavio.sewer.test;

import net.anthavio.sewer.ServerConfig;
import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerPort;
import net.anthavio.sewer.ServerType;

import org.fest.assertions.api.Assertions;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * 
 * @author martin.vanek
 *
 */
@Listeners(SewerTestNgListener.class)
public class JettyTestNgTest {

	@ServerPort
	private int port8;

	@ServerConfig(type = ServerType.JETTY, home = "src/test/jetty8", port = 0)
	private ServerInstance jetty8;

	@ServerPort(serverIndex = 1)
	private int port6;

	@ServerConfig(type = ServerType.JETTY6, home = "src/test/jetty6", port = 0)
	private ServerInstance jetty6;

	@Test
	public void test() {
		Assertions.assertThat(port8).isGreaterThan(0);
		Assertions.assertThat(jetty8).isNotNull();
		Assertions.assertThat(jetty8.getLocalPorts()[0]).isEqualTo(port8);

		Assertions.assertThat(port6).isGreaterThan(0);
		Assertions.assertThat(port6).isNotEqualTo(port8);
		Assertions.assertThat(jetty6).isNotNull();
		Assertions.assertThat(jetty6.getLocalPorts()[0]).isNotEqualTo(port8);
	}
}
