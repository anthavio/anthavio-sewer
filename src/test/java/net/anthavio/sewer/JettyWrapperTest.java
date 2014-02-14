package net.anthavio.sewer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Assert;

import net.anthavio.sewer.jetty.Jetty6Wrapper;
import net.anthavio.sewer.jetty.JettyWrapper;

import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

/**
 * 
 * @author martin.vanek
 *
 */
public class JettyWrapperTest {

	@Test
	public void testJettyWrapper() throws Exception {
		JettyWrapper jetty = new JettyWrapper("src/test/jetty8", 0); //dynamic port allocation
		jetty.start();
		Assertions.assertThat(jetty.getPort()).isGreaterThan(0);

		URL url = new URL("http://localhost:" + jetty.getPort() + "/halleluyah.html");
		InputStream stream = (InputStream) url.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		Assert.assertEquals("Halleluyah!", line);
		jetty.stop();
	}

	@Test
	public void testJetty6Wrapper() throws Exception {
		Jetty6Wrapper jetty = new Jetty6Wrapper("src/test/jetty6", 0); //dynamic port allocation
		jetty.start();
		Assertions.assertThat(jetty.getPort()).isGreaterThan(0);

		URL url = new URL("http://localhost:" + jetty.getPort() + "/halleluyah.html");
		InputStream stream = (InputStream) url.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		Assert.assertEquals("Halleluyah!", line);
		jetty.stop();
	}

}
