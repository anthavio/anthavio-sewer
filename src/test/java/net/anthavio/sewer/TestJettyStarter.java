package net.anthavio.sewer;

import net.anthavio.sewer.jetty.JettyWrapper;

import org.fest.assertions.api.Assertions;

/**
 * Jetty 8 main class
 * 
 * @author martin.vanek
 *
 */
public class TestJettyStarter {

	public static void main(String[] args) {
		JettyWrapper wrapper = new JettyWrapper("src/test/jetty9", 0);
		wrapper.start();
		Assertions.assertThat(wrapper.getPort()).isNotEqualTo(8080);
		wrapper.stop();
	}
}
