package org.camunda.bpm.piviz.cockpit.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "org.camunda.bpm.piviz.cockpit" })
public class CockpitTestApp {
	public static void main(String... args) {
		SpringApplication.run(CockpitTestApp.class, args);
	}
	
//	@Bean
//	@Qualifier("JUHU")
//	public Object test() throws Exception {
//		LoggerFactory.getLogger(this.getClass()).warn("info: {}", new ClassPathResource("plugin-webapp/piviz-plugin/app/plugin.js").getURL());
//		LoggerFactory.getLogger(this.getClass()).warn("info: {}", new ClassPathResource("plugin-webapp/piviz-plugin/info.txt").getURL());
//		return new Object();
//	}
}
