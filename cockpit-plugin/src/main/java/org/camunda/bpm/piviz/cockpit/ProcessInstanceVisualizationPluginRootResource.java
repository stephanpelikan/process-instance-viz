package org.camunda.bpm.piviz.cockpit;

import javax.ws.rs.Path;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;

@Path("plugin/" + ProcessInstanceVisualizationPlugin.ID)
public class ProcessInstanceVisualizationPluginRootResource extends AbstractCockpitPluginRootResource {

	public ProcessInstanceVisualizationPluginRootResource() {
		super(ProcessInstanceVisualizationPlugin.ID);
	}

}
