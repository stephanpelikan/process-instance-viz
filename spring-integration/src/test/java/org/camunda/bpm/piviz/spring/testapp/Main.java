package org.camunda.bpm.piviz.spring.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "org.camunda.bpm.piviz.spring" })
public class Main {
	public static void main(String... args) {
		SpringApplication.run(Main.class, args);
	}
}
