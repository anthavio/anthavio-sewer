package net.anthavio.sewer.test;

import java.lang.reflect.Method;
import java.util.List;

import net.anthavio.sewer.ServerLifeCycle;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * Standard TestNG Listener
 * 
 * Use it...
 * 
 * @Listeners(SewerTestNgListener.class)
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
 * @author martin.vanek
 *
 */
public class SewerTestNgListener extends ServerLifeCycle implements IMethodInterceptor, IInvokedMethodListener {

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
		if (methods.size() != 0) {
			IMethodInstance imethod = methods.get(0);
			super.beforeTestClass(imethod.getMethod().getTestClass().getRealClass());
			/*
			//Object instance = imethod.getInstance();
			Class<?> testClass = imethod.getMethod().getTestClass().getRealClass();
			ServerMetadata[] serverSetups = JettyLoaderBase.getServerMetaData(testClass);
			for (int i = 0; i < serverSetups.length; ++i) {
				jim.checkoutServer(serverSetups[i]);
			}
			*/
		}
		return methods;
	}

	@Override
	public void beforeInvocation(IInvokedMethod imethod, ITestResult testResult) {
		Object instance = imethod.getTestMethod().getInstance();
		Method method = imethod.getTestMethod().getConstructorOrMethod().getMethod();
		super.beforeTestMethod(instance, method);

	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		super.afterTestMethod();
	}

}
