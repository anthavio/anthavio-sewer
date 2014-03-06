package net.anthavio.sewer.test;

import net.anthavio.sewer.ServerMetadata.CacheScope;
import net.anthavio.sewer.ServerType;

import org.fest.assertions.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Managed servet test using junit 4.9 introduced @Rule TestRule framework
 * 
 * @author martin.vanek
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//this will ensure correct order of test execution
public class JettyJUnitRuleTest {

	@Rule
	public SewerRule rule1 = new SewerRule(ServerType.JETTY, "src/test/jetty8", 0);

	@Rule
	public SewerRule rule2 = new SewerRule(ServerType.JETTY, "src/test/jetty8", 0);

	@Rule
	public SewerRule rule3 = new SewerRule(ServerType.JETTY6, "src/test/jetty6", 0, CacheScope.METHOD);

	//JUnit is creating new test class instance for every test method invocation (contrary to TestNG)
	//static fields are needed to store value between test
	private static int test1rule1port;

	private static int test1rule3port;

	private static boolean test1passed = false;

	@Test
	public void testFirst() {
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
	public void testSecond() {
		Assertions.assertThat(test1passed).isTrue();
		Assertions.assertThat(test1rule1port).isEqualTo(rule1.getServer().getLocalPorts()[0]); //Cache JVM => same port
		Assertions.assertThat(test1rule1port).isEqualTo(rule2.getServer().getLocalPorts()[0]);

		Assertions.assertThat(test1rule3port).isNotEqualTo(rule3.getServer().getLocalPorts()[0]); //Cache METHOD => different port
	}

}