package net.anthavio.sewer.test;

import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerInstanceManager;
import net.anthavio.sewer.ServerMetadata;
import net.anthavio.sewer.ServerMetadata.CacheInstance;
import net.anthavio.sewer.ServerType;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * 
 * public class MyCoolTests {
 * 
 *   @Rule
 *   private SewerRule sewer = new SewerRule(ServerType.JETTY, "src/test/jetty8", 0);
 *   
 *   @Test
 *   public void test() {
 *     int port  = sewer.getServer().getLocalPorts()[0];
 *   }
 *   
 * }
 * 
 * Can interact with method annotations - http://www.codeaffine.com/2012/09/24/junit-rules/
 * 
 * @author martin.vanek
 *
 */
public class SewerRule implements TestRule {

	private final ServerInstanceManager manager = ServerInstanceManager.INSTANCE;

	private final ServerMetadata metadata;

	private ServerInstance server;

	public SewerRule(ServerType type, String home) {
		this(type, home, -1, null, CacheInstance.ALLWAYS);
	}

	public SewerRule(ServerType type, String home, int port) {
		this(type, home, port, null, CacheInstance.ALLWAYS);
	}

	public SewerRule(ServerType type, String home, int port, CacheInstance cache) {
		this(type, home, port, null, cache);
	}

	public SewerRule(ServerType type, String home, int port, String[] configs, CacheInstance cache) {
		this.metadata = new ServerMetadata(type, home, port, configs, cache);
	}

	public ServerInstance getServer() {
		return server;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new SewerStatement(base);
	}

	public class SewerStatement extends Statement {

		private final Statement base;

		public SewerStatement(Statement base) {
			this.base = base;
		}

		@Override
		public void evaluate() throws Throwable {
			server = manager.borrowServer(metadata);
			try {
				base.evaluate();
			} finally {
				manager.returnServer(metadata);
			}

		}

	}

}
