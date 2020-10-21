package org.camunda.bpm.piviz.cockpit;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;

public class ProcessInstanceVisualizationPlugin extends AbstractCockpitPlugin {
	
	public static final String ID = "piviz-plugin";
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public Set<Class<?>> getResourceClasses() {
		final HashSet<Class<?>> result = new HashSet<>();
		result.add(ProcessInstanceVisualizationPluginRootResource.class);
		return result;
	}
	
}
