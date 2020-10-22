package org.camunda.bpm.piviz.cockpit.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.piviz.Simulator;
import org.camunda.bpm.piviz.SimulatorProvider;
import org.camunda.bpm.piviz.SimulatorProviderImpl;
import org.camunda.bpm.piviz.result.Report;

public class ProcessInstanceResource extends AbstractCockpitPluginResource {

	private static Simulator simulator = Simulator.newInstance();
	
	public ProcessInstanceResource(final String engineName) {
		super(engineName);
	}
	
	@GET
	@Path("{processInstanceId}")
	public Report getHighlightedElements(@PathParam("processInstanceId") String processInstanceId)
			throws Exception {
		
		final SimulatorProvider provider = new SimulatorProviderImpl(getProcessEngine(), processInstanceId);
		final Report report = simulator.simulate(provider);
		
		return report;
		
	}
	
}
