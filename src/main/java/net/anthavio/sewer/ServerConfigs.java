/**
 * 
 */
package net.anthavio.sewer;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author vanek
 *
 */
@Documented
@Inherited
@Target(TYPE)
@Retention(RUNTIME)
public @interface ServerConfigs {

	ServerConfig[] value();
}
