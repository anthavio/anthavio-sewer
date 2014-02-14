package net.anthavio.sewer.test;

import net.anthavio.sewer.ServerLifeCycle;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * 
 * spring-test TestExecutionListener implementation
 * 
 * Use like this...
 * 
 * @TestExecutionListeners(SewerSpringListener.class)
 * @ContextConfiguration(loader = ContextRefLoader.class, locations = "some-context-name")
 * public class MyFunkyTest extends AbstractTestNGSpringContextTests {
 * 
 * 	@ServerConfig(type = ServerType.JETTY, home = "src/test/jetty8", port = 0)
 * 	public ServerInstance server;
 *
 * 	@Test
 * 	public void test() {
 *    server.getLocalPorts();
 * 		//Assertions.assertThat(port).isGreaterThan(0);
 * 	}
 * }
 * 
 * 
 * @author martin.vanek
 *
 */
public class SewerSpringListener extends ServerLifeCycle implements TestExecutionListener {

	private boolean prepared = false;

	private boolean started = false;

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		super.beforeTestClass(testContext.getTestClass());
		prepared = true;
	}

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		if (prepared) {
			super.beforeTestMethod(testContext.getTestInstance(), testContext.getTestMethod());
		}
		started = true;
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		if (started) {
			super.afterTestMethod();
		}
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		super.afterTestClass();
	}

}
