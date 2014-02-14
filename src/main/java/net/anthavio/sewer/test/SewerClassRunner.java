package net.anthavio.sewer.test;

import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerInstanceManager;
import net.anthavio.sewer.ServerLifeCycle;
import net.anthavio.sewer.ServerMetadata;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit ClassRunner managein ServerInstances
 * 
 * @RunWith(SewerClassRunner.class)
 * public class MyFunkyTest {
 * 
 *   @ServerConfig(type = ServerType.JETTY, home="src/test/jetty8" port = 0)
 *   private ServerInstance server;
 *   
 *   @Test
 *   public void test() {
 *     int port = server.getLocalPorts()[0];
 *   }
 * 
 * }
 * 
 * Similar can be done with @BeforeClass and @AfterClass annotations, but this is more fun
 * 
 * TODO JUnit withBefores and withAfters are marked to be removed, new Rules should be used instead
 * 
 * @author martin.vanek
 *
 */
public class SewerClassRunner extends BlockJUnit4ClassRunner {

	private ServerMetadata[] metadata;

	private ServerInstanceManager manager = ServerInstanceManager.INSTANCE;

	public SewerClassRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
		//XXX what happends if there is multiple test classes?!?!? serverSetups will get overwritten????
		metadata = ServerLifeCycle.getServerMetaData(testClass);
	}

	/*
		protected Object createTest() throws Exception {
			Object testInstance = super.createTest();
			return testInstance;
		}
	*/
	/*
	@Override
	protected Statement withBeforeClasses(Statement statement) {
		//JUnit requires @BeforeClasses annotated method to be static - they can only access static fields... 
		Statement superResponse = super.withBeforeClasses(statement);
		return superResponse;
	}
	*/
	/**
	 * Runs all @BeforeClass and @AfterClass
	 * 
	 * see http://stackoverflow.com/questions/15141593/custom-blockjunit4classrunner-which-runs-test-suite-set-number-of-times
	@Override
	protected Statement classBlock(RunNotifier notifier) {
		return super.classBlock(notifier);
	}
	 */

	/**
	 * Runs all @Before (method)
	 */
	@Override
	protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
		for (int i = 0; i < metadata.length; ++i) {
			ServerInstance server = manager.borrowServer(metadata[i]);
		}
		ServerLifeCycle.injectResources(target, metadata);
		return super.withBefores(method, target, statement);
	}

	@Override
	protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
		for (int i = 0; i < metadata.length; ++i) {
			ServerInstance server = manager.returnServer(metadata[i]);
		}
		return super.withAfters(method, target, statement);
	}
	/*
		@Override
		protected Statement withAfterClasses(Statement statement) {
			Statement after = super.withAfterClasses(statement);
		}
	*/
}
