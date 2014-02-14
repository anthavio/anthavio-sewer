package net.anthavio.sewer.spring;

import net.anthavio.sewer.ServerConfig;
import net.anthavio.sewer.ServerConfigs;
import net.anthavio.sewer.ServerPort;
import net.anthavio.sewer.ServerType;
import net.anthavio.sewer.test.SewerSpringListener;
import net.anthavio.spring.test.ContextRefLoader;

import org.fest.assertions.api.Assertions;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * 
 * @author martin.vanek
 *
 */
@ServerConfigs(//
{ @ServerConfig(type = ServerType.JETTY6, home = "src/test/jetty6", port = 0),
		@ServerConfig(type = ServerType.JETTY, home = "src/test/jetty8", port = 0) })
@TestExecutionListeners(SewerSpringListener.class)
@ContextConfiguration(loader = ContextRefLoader.class, locations = "test-spring-context")
public class SpringListenerTest extends AbstractTestNGSpringContextTests {

	@ServerPort(serverIndex = 0)
	private int port6;

	@ServerPort(serverIndex = 1)
	private int port8;

	@Test
	public void test() {
		Assertions.assertThat(port6).isGreaterThan(0);
		Assertions.assertThat(port8).isGreaterThan(0);
		Assertions.assertThat(port6).isNotEqualTo(port8);
	}
}
