package net.anthavio.sewer;

import net.anthavio.sewer.jetty.Jetty6Wrapper;

import org.fest.assertions.api.Assertions;

/**
 * Jetty 6 main class
 * 
 * @author martin.vanek
 *
 */
public class TestJetty6Starter {

	public static void main(String[] args) {
		Jetty6Wrapper wrapper = new Jetty6Wrapper("src/test/jetty6");
		wrapper.start();
		Assertions.assertThat(wrapper.getPort()).isEqualTo(8080);
		wrapper.stop();
	}
}
