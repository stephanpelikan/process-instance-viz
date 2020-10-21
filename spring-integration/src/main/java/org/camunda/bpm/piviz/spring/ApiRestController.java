package org.camunda.bpm.piviz.spring;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.piviz.Simulator;
import org.camunda.bpm.piviz.SimulatorProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiRestController {

	private static Simulator simulator = Simulator.newInstance();
	
	@Autowired
	private ProcessEngine processEngine;
	
	@RequestMapping(value = "/sim/{processInstanceId}", produces = "application/json")
	public Object simulate(@PathVariable("processInstanceId") final String processInstanceId) throws Exception {
		
		final SimulatorProviderImpl provider = new SimulatorProviderImpl(processEngine, processInstanceId);
		
		return simulator.simulate(provider);
		
	}
	
}
