package net.anthavio.sewer.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestSpringConfig {

	@Bean
	public String something() {
		return "SECRET!";
	}
}
