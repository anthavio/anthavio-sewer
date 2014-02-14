package net.anthavio.sewer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injection marker annotation
 * 
 * @author martin.vanek
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerPort {

	/**
	 * Multiple instances can be started. This allow to pick the right one.
	 */
	int serverIndex() default 0;

	/**
	 * Multiple ports can be configured (http, https, websocket). This allow to pick the right one.
	 */
	int portIndex() default 0;
}
