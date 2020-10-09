package org.camunda.bpm.piviz.impl;

import org.camunda.bpm.engine.delegate.ExecutionListener;

public class VariableExecutionListener implements ExecutionListener {

	private SimulatorScenario owner;

	public VariableExecutionListener(SimulatorScenario owner) {
		this.owner = owner;
	}

	@Override
	public void notify(org.camunda.bpm.engine.delegate.DelegateExecution execution) throws Exception {
		owner.setVariablesIfNecessary(execution.getCurrentActivityId(), execution.getEventName(),
				execution.getProcessInstanceId());
	};
	
}
