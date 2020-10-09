package org.camunda.bpm.piviz;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;

public class SimulatorProviderImpl implements SimulatorProvider {

	private String processInstanceId;

	private String processDefinitionId;

	private String processDefinitionKey;

	private ProcessEngine processEngine;

	private List<HistoryBase> history;

	@SuppressWarnings("unchecked")
	public SimulatorProviderImpl(final ProcessEngine processEngine, final String processInstanceId)
			throws Exception {

		this.processInstanceId = processInstanceId;
		this.processEngine = processEngine;

		final HistoricProcessInstance processInstance = processEngine.getHistoryService()
				.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId)
				.singleResult();

		this.processDefinitionId = processInstance.getProcessDefinitionId();
		this.processDefinitionKey = processInstance.getProcessDefinitionKey();

		final HistoricIncidentQuery incidentsQuery = processEngine.getHistoryService()
				.createHistoricIncidentQuery();
		List<HistoricIncident> processIncidents;
		try {
			final Method unlimitedListMethod = incidentsQuery.getClass().getMethod("unlimitedList");
			processIncidents = (List<HistoricIncident>) unlimitedListMethod.invoke(incidentsQuery);
		} catch (NoSuchMethodException e) {
			processIncidents = incidentsQuery.list();
		}
		List<HistoricIncident> incidents = processIncidents;
		
		final HistoricActivityInstanceQuery activitiesQuery = processEngine.getHistoryService()
				.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId)
				.orderByHistoricActivityInstanceStartTime()
				.asc();
		List<HistoricActivityInstance> activities;
		try {
			final Method unlimitedListMethod = activitiesQuery.getClass().getMethod("unlimitedList");
			activities = (List<HistoricActivityInstance>) unlimitedListMethod.invoke(activitiesQuery);
		} catch (NoSuchMethodException e) {
			activities = activitiesQuery.list();
		}
		
		history = activities.stream()
				.map(activity -> {
					final ActivityHistory historyItem = new ActivityHistory();
					historyItem.activityInstanceId = activity.getId();
					historyItem.activityId = activity.getActivityId();
					historyItem.taskId = activity.getTaskId();
					historyItem.state = SimulatorProviderImpl.mapState(activity, incidents);
					historyItem.timeSinceProcessStart = activity.getStartTime().getTime() - processInstance.getStartTime().getTime();
					historyItem.endTimeSinceProcessStart = activity.getEndTime() != null ? activity.getEndTime().getTime() - processInstance.getStartTime().getTime() : -1;
					historyItem.executionId = activity.getExecutionId();
					return historyItem;
				})
				.collect(Collectors.toList());
		
	}

	private static ActivityState mapState(final HistoricActivityInstance activity, final List<HistoricIncident> incidents) {
		
		if (activity.getEndTime() == null) {
			if (incidents.stream()
					.filter(incident -> incident.getActivityId().equals(activity.getActivityId()))
					.filter(incident -> incident.isOpen())
					.findAny()
					.isPresent()) {
				return ActivityState.ERROR;
			}
			return ActivityState.RUNNING;
		}
		if (activity.isCanceled()) {
			return ActivityState.CANCELLED;
		}
		return ActivityState.COMPLETED;
		
	}

	@Override
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	@Override
	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	@Override
	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	@Override
	public InputStream getProcessDefinition() {
		return processEngine.getRepositoryService().getProcessModel(processDefinitionId);
	}

	@Override
	public List<HistoryBase> getHistory() {
		return history;
	}
	
}
