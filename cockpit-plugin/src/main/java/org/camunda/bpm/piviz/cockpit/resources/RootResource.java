package org.camunda.bpm.piviz.cockpit.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import org.camunda.bpm.piviz.cockpit.ProcessInstanceVisualizationPlugin;

@Path("plugin/" + ProcessInstanceVisualizationPlugin.ID)
public class RootResource extends AbstractCockpitPluginRootResource {

	public RootResource() {
		super(ProcessInstanceVisualizationPlugin.ID);
	}

	@Path("{engineName}/process-instance")
	public ProcessInstanceResource getProcessInstanceResource(@PathParam("engineName") String engineName) {
		return subResource(new ProcessInstanceResource(engineName), engineName);
	}

}
