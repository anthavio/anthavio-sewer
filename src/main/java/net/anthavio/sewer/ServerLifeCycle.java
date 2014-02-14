package net.anthavio.sewer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Every framework has it's own callback methods.
 * 
 * @author martin.vanek
 *
 */
public class ServerLifeCycle {

	protected ServerInstanceManager manager = ServerInstanceManager.INSTANCE;

	//To prevent nullpointer exception when initialization fails, but other lifecycle methods are still called
	//SewerSpringListener does this... 
	protected ServerMetadata[] metadatas;

	private boolean prepared = false;

	private boolean started = false;

	public void beforeTestClass(Class<?> testClass) {
		this.metadatas = getServerMetaData(testClass);
		prepared = true;
	}

	public void beforeTestMethod(Object testInstance, Method method) {
		if (prepared) {
			for (int i = 0; i < metadatas.length; ++i) {
				manager.borrowServer(metadatas[i]);
			}
			started = true;
			injectResources(testInstance, metadatas);
		}
	}

	public void afterTestMethod() {
		if (started) {
			for (int i = 0; i < metadatas.length; ++i) {
				manager.returnServer(metadatas[i]);
			}
		}
	}

	public void afterTestClass() {

	}

	public static void injectResources(Object testInstance, ServerMetadata[] metadata) {
		ServerInstanceManager manager = ServerInstanceManager.INSTANCE;
		Field[] fields = testInstance.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(ServerPort.class)) {
				if (field.getType() != int.class) {
					throw new IllegalStateException("@ServerPort annotated field " + field.getType().getName() + " "
							+ field.getName() + " on " + testInstance.getClass() + " must be of int type");
				}
				ServerPort annotation = field.getAnnotation(ServerPort.class);
				ServerInstance jetty = manager.getServer(metadata[annotation.serverIndex()]);
				int[] ports = jetty.getLocalPorts();
				int port = ports[annotation.portIndex()];
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					field.setInt(testInstance, port);
				} catch (Exception x) {
					throw new IllegalStateException("Failed to inject field " + field.getName() + " on "
							+ testInstance.getClass(), x);
				}

			} else if (field.isAnnotationPresent(ServerConfig.class)) {
				if (!ServerInstance.class.isAssignableFrom(field.getType())) {
					throw new IllegalStateException("@ServerConfig annotated field " + field.getType().getName() + " "
							+ field.getName() + " on " + testInstance.getClass() + " must be of " + ServerInstance.class);
				}
				ServerConfig annotation = field.getAnnotation(ServerConfig.class);
				ServerMetadata setup = new ServerMetadata(annotation);
				ServerInstance jetty = manager.getServer(setup);
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					field.set(testInstance, jetty);
				} catch (Exception x) {
					throw new IllegalStateException("Failed to inject field " + field.getName() + " on "
							+ testInstance.getClass(), x);
				}
			}
		}
	}

	public static ServerMetadata[] getServerMetaData(Class<?> testClass) {
		ServerConfig[] jettyConfigs = getServerConfigs(testClass);
		ServerMetadata[] serverSetups = new ServerMetadata[jettyConfigs.length];
		for (int i = 0; i < jettyConfigs.length; ++i) {
			ServerConfig config = jettyConfigs[i];
			serverSetups[i] = new ServerMetadata(config);
		}
		return serverSetups;
	}

	/**
	 * TODO - it sould probably traverse class hierarchy in case of inheritance
	 * 
	 * Gather all @ServerConfig annotation from test class and it's fields.
	 * Involves a lot of reflection so it is not fastest... 
	 */
	public static ServerConfig[] getServerConfigs(Class<?> testClass) {
		if (testClass == null) {
			throw new IllegalArgumentException("Null passed instead of test class");
		}
		ServerConfigs multiConfig = testClass.getAnnotation(ServerConfigs.class);
		ServerConfig singleConfig = testClass.getAnnotation(ServerConfig.class);
		/*
		if ((multiConfig == null || multiConfig.value().length == 0) && singleConfig == null) {
			throw new IllegalArgumentException("Annotation @ServerConfig(s) not present on " + testClass.getName());
		}
		*/
		if (multiConfig != null && multiConfig.value().length != 0 && singleConfig != null) {
			throw new IllegalArgumentException("Both Annotations @ServerConfig(s) are present on " + testClass.getName());
		}
		List<ServerConfig> list = new LinkedList<ServerConfig>();
		if (singleConfig != null) {
			list.add(singleConfig);
		}
		if (multiConfig != null) {
			ServerConfig[] configs = multiConfig.value();
			for (ServerConfig config : configs) {
				list.add(config);
			}
		}
		//@ServerConfig annotated field - injection of ServerWrapper
		Field[] fields = testClass.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(ServerConfig.class)) {
				list.add(field.getAnnotation(ServerConfig.class));
			}
		}
		//Quite controvensial to throw this exception. we will see...
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Annotation @ServerConfig not found anywhere on " + testClass);
		}
		return list.toArray(new ServerConfig[list.size()]);
	}
}
