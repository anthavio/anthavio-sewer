package net.anthavio.sewer.test;

import net.anthavio.sewer.ServerMetadata.CacheInstance;
import net.anthavio.sewer.ServerType;

import org.fest.assertions.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

/**
 * Managed servet test using junit 4.9 introduced @Rule TestRule framework
 * 
 * @author martin.vanek
 *
 */
public class Jetty8JUnitRuleTest {

	@Rule
	public SewerRule rule1 = new SewerRule(ServerType.JETTY, "src/test/jetty8", 0);

	@Rule
	public SewerRule rule2 = new SewerRule(ServerType.JETTY, "src/test/jetty8", 0);

	@Rule
	public SewerRule rule3 = new SewerRule(ServerType.JETTY6, "src/test/jetty6", 0, CacheInstance.NEVER);

	private int test1rule1port;

	private int test1rule3port;

	private boolean test1passed = false;

	@Test
	public void test1() {
		Assertions.assertThat(rule1.getServer()).isNotNull();
		test1rule1port = rule1.getServer().getLocalPorts()[0];
		Assertions.assertThat(test1rule1port).isGreaterThan(0);

		Assertions.assertThat(rule1.getServer()).isEqualTo(rule2.getServer()); //same config means same server

		Assertions.assertThat(rule1.getServer()).isNotEqualTo(rule3.getServer());
		test1rule3port = rule3.getServer().getLocalPorts()[0];
		Assertions.assertThat(test1rule1port).isNotEqualTo(test1rule3port);

		test1passed = true;
	}

	@Test
	public void test2() {

		Assertions.assertThat(test1rule1port).isSameAs(rule1.getServer().getLocalPorts()[0]); //Cache allways => same port
		Assertions.assertThat(test1rule1port).isSameAs(rule2.getServer().getLocalPorts()[0]);

		Assertions.assertThat(test1rule3port).isNotEqualTo(rule3.getServer().getLocalPorts()[0]); //Cache never => different port

	}
}
