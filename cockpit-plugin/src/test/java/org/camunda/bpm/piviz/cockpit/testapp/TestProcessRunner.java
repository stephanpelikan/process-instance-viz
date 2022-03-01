package org.camunda.bpm.piviz.cockpit.testapp;

import java.util.HashMap;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TestProcessRunner {

	private static final Logger logger = LoggerFactory.getLogger(TestProcessRunner.class);
	
	@Autowired
	private RuntimeService runtimeService;
	
	@EventListener
	public void startTestProcess(final ProcessApplicationStartedEvent event) {

		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("IV", "ABC");
		final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testprocess", variables);
		logger.info("Startet testprocess: http://localhost:8080/camunda/app/cockpit/default/#/process-instance/{}/runtime", processInstance.getId());
		
	}
	
}
