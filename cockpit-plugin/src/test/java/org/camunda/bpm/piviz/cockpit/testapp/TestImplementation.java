package org.camunda.bpm.piviz.cockpit.testapp;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("TestImplementation")
public class TestImplementation {

	public void doIt() {
		LoggerFactory.getLogger(TestImplementation.class).info("doIt");
	}
	
}
