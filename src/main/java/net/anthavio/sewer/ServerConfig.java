/**
 * 
 */
package net.anthavio.sewer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.anthavio.sewer.ServerMetadata.CacheScope;

/**
 * Managed server configuration annotation
 * 
 * @author vanek
 *
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ServerConfig {

	/**
	 * 
	 */
	ServerType type();

	/**
	 * @return server home directory
	 */
	String home() default ".";

	/**
	 * @return configuration files (if apliable)
	 */
	String[] configs() default {};

	/**
	 * @return how to cache instance between tests
	 */
	CacheScope cache() default CacheScope.JVM;

	/**
	 * <ul>
	 * <li>0 - dynamic port allocation</li>
	 * <li>-1 - server configuration (xml) selects port</li>
	 * <li>positive number - will become port</li> 
	 * </ul>
	 */
	int port() default -1;

}
