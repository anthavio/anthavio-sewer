package net.anthavio.sewer.grizzly;

import java.io.IOException;

import javax.servlet.ServletContainerInitializer;

import net.anthavio.sewer.ServerInstance;
import net.anthavio.sewer.ServerMetadata;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.WebappContext;

/**
 * Grizzly does not have external configuration or webapp deployment.
 * Also integration with JSP is missing....
 * 
 * @author vanek
 *
 */
public class GrizzlyWrapper implements ServerInstance {

	private HttpServer server;

	private GrizzlyWrapper(int port, ServletContainerInitializer initalizer) {
		server = HttpServer.createSimpleServer();
		WebappContext webappContext = new WebappContext("GrizzlyWrapper WebappContext", "", "");
		//webappContext.s
		/*
		FilterRegistration testFilterReg = webappContext.addFilter("TestFilter", TestFilter.class);
		testFilterReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/*");

		ServletRegistration servletRegistration = webappContext.addServlet("Jersey",
				org.glassfish.jersey.servlet.ServletContainer.class);
		servletRegistration.addMapping("/myapp/*");
		servletRegistration.setInitParameter("jersey.config.server.provider.packages", "com.example");
		*/
		webappContext.deploy(server);

	}

	@Override
	public ServerMetadata getMetadata() {
		return null;
	}

	@Override
	public void start() {
		if (server != null && server.isStarted()) {
			try {
				server.start();
			} catch (IOException iox) {
				throw new IllegalStateException("Jetty failed to start", iox);
			}
		}
	}

	@Override
	public void stop() {
		if (server != null && !server.isStarted()) {
			try {
				server.shutdownNow();
			} catch (Exception x) {
				throw new IllegalStateException("Jetty failed to stop", x);
			}
		}
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getLocalPorts() {
		// TODO Auto-generated method stub
		return null;
	}

}
