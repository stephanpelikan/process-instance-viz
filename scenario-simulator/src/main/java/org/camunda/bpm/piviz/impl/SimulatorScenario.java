package org.camunda.bpm.piviz.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.event.EventHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.CoverageTestRunState;
import org.camunda.bpm.extension.process_test_coverage.listeners.CompensationEventCoverageHandler;
import org.camunda.bpm.extension.process_test_coverage.listeners.FlowNodeHistoryEventHandler;
import org.camunda.bpm.extension.process_test_coverage.listeners.PathCoverageParseListener;
import org.camunda.bpm.extension.process_test_coverage.model.MethodCoverage;
import org.camunda.bpm.extension.process_test_coverage.model.ProcessCoverage;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractBaseElementBuilder;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
import org.camunda.bpm.model.xml.impl.util.ModelTypeException;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.piviz.SimulatorProvider;
import org.camunda.bpm.piviz.SimulatorProvider.ActivityState;
import org.camunda.bpm.piviz.result.Element;
import org.camunda.bpm.piviz.result.ElementStatus;
import org.camunda.bpm.piviz.result.Report;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.act.BusinessRuleTaskAction;
import org.camunda.bpm.scenario.act.ConditionalIntermediateEventAction;
import org.camunda.bpm.scenario.act.EventBasedGatewayAction;
import org.camunda.bpm.scenario.act.MessageEndEventAction;
import org.camunda.bpm.scenario.act.MessageIntermediateCatchEventAction;
import org.camunda.bpm.scenario.act.MessageIntermediateThrowEventAction;
import org.camunda.bpm.scenario.act.ReceiveTaskAction;
import org.camunda.bpm.scenario.act.SendTaskAction;
import org.camunda.bpm.scenario.act.ServiceTaskAction;
import org.camunda.bpm.scenario.act.SignalIntermediateCatchEventAction;
import org.camunda.bpm.scenario.act.TimerIntermediateEventAction;
import org.camunda.bpm.scenario.act.UserTaskAction;
import org.camunda.bpm.scenario.run.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorScenario implements ProcessScenario {

	private static final String VARIABLE_EXECUTION_LISTENER = "variableExecutionListener";
	
	private static final String VARIABLE_EXECUTION_LISTENER_EXPRESSION = "${" + VARIABLE_EXECUTION_LISTENER + "}";
	
	private Logger logger = LoggerFactory.getLogger(SimulatorScenario.class);
	
	private SimulatorProvider provider;
	
	private ProcessEngine simulatorEngine;
	
	private String simulatedProcessDefinitionId;
	
	private String simulatedDeployementId;
	
	private String simulatedProcessDefinitionKey;
	
	private CoverageTestRunState coverageTestRunState;
	
	public SimulatorScenario(final SimulatorProvider provider, final ProcessEngine simulatorEngine) throws Exception {
		this.provider = provider;
		this.simulatorEngine = simulatorEngine;

        coverageTestRunState = new CoverageTestRunState();
        coverageTestRunState.setTestClassName(provider.getProcessDefinitionKey());
        coverageTestRunState.setCurrentTestMethodName(provider.getProcessDefinitionKey());
        initializeListenerRunState();

		final ProcessDefinition definition = loadDefinitionIntoSimulatorEngine();
		simulatedProcessDefinitionId = definition.getId();
		simulatedProcessDefinitionKey = definition.getKey();
		simulatedDeployementId = definition.getDeploymentId();
		
        initializeTestMethodCoverage(definition);
        
        final VariableExecutionListener variableExecutionListener = new VariableExecutionListener(this);
        Mocks.register(VARIABLE_EXECUTION_LISTENER, variableExecutionListener);
        
	}
	
	public void execute() {
		
		final Map<String, Object> variables = provider.getHistory()
				.stream()
				.filter(h -> h instanceof SimulatorProvider.VariableHistory)
				.map(h -> (SimulatorProvider.VariableHistory) h)
				.filter(h -> h.atProcessStart)
				.peek(h -> { h.consumed = true; })
				.collect(Collectors.toMap(h -> h.name, h -> h.value));
		logger.info("Setting start variables: {}", variables);
		
//		final var startActivity = activities.get(0);
//		if (startActivity.getActivityType().equals("startEvent")) {
		try {
			ClockUtil.setTrackStart();
			Scenario.run(this)
					.startByKey(simulatedProcessDefinitionKey, variables)
					.engine(simulatorEngine)
					.execute();
		} catch (NotYetCompletedException e) {
			// ignore this
		}
//		}
		
	}

	public Report createReport() {
		
		// return SimulatorBpmnJsReport.generateReport(simulatedProcessDefinitionKey, coverageTestRunState);
		final MethodCoverage coverage = coverageTestRunState.getCurrentTestMethodCoverage();
        
		final Report result = new Report();
		
		coverage.getCoveredFlowNodes(simulatedProcessDefinitionKey)
				.stream()
				.map(flowNode -> new Element(
						flowNode.getElementId(),
						flowNode.hasEnded() ? ElementStatus.COMPLETED : ElementStatus.RUNNING))
				.forEach(result::addElement);
        
        coverage.getCoveredSequenceFlowIds(simulatedProcessDefinitionKey)
				.stream()
				.map(sequenceFlow -> new Element(
						sequenceFlow,
						ElementStatus.COMPLETED))
				.forEach(result::addElement);
        
        return result;

	}
	
	public void cleanup() {
		
		simulatorEngine.getRepositoryService().deleteDeployment(
				simulatedDeployementId, true, true, true);
		final long hc = simulatorEngine.getHistoryService().createHistoricProcessInstanceQuery().count();
		final long rc = simulatorEngine.getRuntimeService().createProcessInstanceQuery().count();
		logger.warn("Counts: {}/{}", rc, hc);
		
	}
	
	private ProcessDefinition loadDefinitionIntoSimulatorEngine() throws Exception {
		
		// deployments cannot be reused because of test coverage implemenation
		
		// processDefinitionId is used as deploymentName in the simulator process engine
//		final var previousDeployment = simulatorRepositoryService().createDeploymentQuery()
//				.deploymentName(provider.getProcessDefinitionId())
//				.singleResult();
//		if (previousDeployment != null) {
//			return simulatorRepositoryService().createProcessDefinitionQuery()
//					.deploymentId(previousDeployment.getId())
//					.singleResult();
//		}
		
		BpmnModelInstance model = null;
		try (final InputStream processDefinitionIn = provider.getProcessDefinition()) {
			model = Bpmn.readModelFromStream(processDefinitionIn);
		}
		model = adoptModel(model);
		
		final DeploymentWithDefinitions deployResult = simulatorRepositoryService().createDeployment()
				.addModelInstance(provider.getProcessDefinitionId() + ".bpmn", model)
				.name(provider.getProcessDefinitionId())
				.deployWithResult();
		return deployResult.getDeployedProcessDefinitions()
				.get(0);
		
	}
	
	private BpmnModelInstance adoptModel(BpmnModelInstance model) {
		
		// remove all custom listeners
		model.getModelElementsByType(BaseElement.class)
				.forEach(node -> removeAllListeners(CamundaExecutionListener.class, node));
		model.getModelElementsByType(UserTask.class)
				.forEach(node -> removeAllListeners(CamundaTaskListener.class, node));
		
		// add simulator listeners
		model.getModelElementsByType(FlowNode.class)
				.forEach(node -> replaceCustomListenersByVariableListener(node));
		model.getModelElementsByType(SequenceFlow.class)
				.forEach(node -> replaceCustomListenersByVariableListener(node));
		
		return model;
		
	}
	
	private void removeAllListeners(final Class<?> listenerType, final BaseElement node) {
		
		final ExtensionElements extensionElements = node.getExtensionElements();
		if (extensionElements == null) {
			return;
		}
		final List<ModelElementInstance> toBeRemoved = extensionElements
				.getElements()
				.stream()
				.filter(e -> e.getClass().isInstance(listenerType))
				.collect(Collectors.toList());
		toBeRemoved.forEach(extensionElements::removeChildElement);
		
	}
	
	private void replaceCustomListenersByVariableListener(final BaseElement node) {
		
		AbstractBaseElementBuilder<?,?> builder = null;
		try {
			builder = node.builder();
		} catch (ModelTypeException e) {
			// node does not support builder
			return;
		}

		
		if (node instanceof SequenceFlow) {
			final CamundaExecutionListener takeListener = node.getModelInstance()
					.newInstance(CamundaExecutionListener.class);
			takeListener.setCamundaEvent("take");
			takeListener.setCamundaDelegateExpression(VARIABLE_EXECUTION_LISTENER_EXPRESSION);
			builder.addExtensionElement(takeListener);
		} else {
			final CamundaExecutionListener startListener = node.getModelInstance()
					.newInstance(CamundaExecutionListener.class);
			startListener.setCamundaEvent("start");
			startListener.setCamundaDelegateExpression(VARIABLE_EXECUTION_LISTENER_EXPRESSION);
			builder.addExtensionElement(startListener);
			final CamundaExecutionListener endListener = node.getModelInstance()
					.newInstance(CamundaExecutionListener.class);
			endListener.setCamundaEvent("end");
			endListener.setCamundaDelegateExpression(VARIABLE_EXECUTION_LISTENER_EXPRESSION);
			builder.addExtensionElement(endListener);
		}
		
	}
	
	void setVariablesIfNecessary(final String activityId, final String eventName, final String executionId) {
		
		final Map<String, Object> changedVariables = new HashMap<>();
		
		provider.getHistory()
				.stream()
				.filter(h -> !h.consumed)
				.peek(h -> {
					if (h instanceof SimulatorProvider.VariableHistory) {
						final SimulatorProvider.VariableHistory v
								= (SimulatorProvider.VariableHistory) h;
						v.consumed = true;
						changedVariables.put(v.name, v.value);
					}
				})
				.filter(h -> h instanceof SimulatorProvider.ActivityHistory)
				.map(h -> (SimulatorProvider.ActivityHistory) h)
				.filter(h -> h.activityId.equals(activityId))
				.findFirst();
		
		if (!changedVariables.isEmpty()) {
			logger.info("Setting variables for event '{}' at activity '{}': {}",
					eventName, activityId, changedVariables);
			simulatorEngine.getRuntimeService()
					.setVariables(executionId, changedVariables);
		}
		
	}
	
	public String getSimulatedProcessDefinitionKey() {
		return simulatedProcessDefinitionKey;
	};

	private RepositoryService simulatorRepositoryService() {
		return simulatorEngine.getRepositoryService();
	}

	@Override
	public void hasStarted(String activityId) {
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hasFinished(String activityId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hasCompleted(String activityId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hasCanceled(String activityId) {
		// TODO Auto-generated method stub
		
	}

	private SimulatorProvider.ActivityHistory getCurrentHistory(String activityId) {
		
		final SimulatorProvider.ActivityHistory result = provider.getHistory()
				.stream()
				.filter(h -> !h.consumed)
				.filter(h -> h instanceof SimulatorProvider.ActivityHistory)
				.map(h -> (SimulatorProvider.ActivityHistory) h)
				.filter(h -> h.activityId.equals(activityId))
				.findFirst()
				.orElseThrow(() -> new NotYetCompletedException());
		
		result.consumed = true;
		
		return result;
		
	}
	
	@Override
	public UserTaskAction waitsAtUserTask(String activityId) {
		
		final SimulatorProvider.ActivityHistory history = getCurrentHistory(activityId);
		final SimulatorProvider.ActivityState state = history.state;

		if (state == ActivityState.RUNNING) {
			return task -> { throw new NotYetCompletedException(); };
		}
		
		if (state == ActivityState.COMPLETED) {
			return task -> task.complete();
		}
		
		if (state == ActivityState.ERROR) {
			return task -> simulatorEngine.getRuntimeService()
					.createIncident("failedJob", task.getExecutionId(), null);
		}
		
		// CANCELLED: Should be forced by the engine. If not
		// it was forced by the user so, the process instance needs to be deleted
		return task -> simulatorEngine.getRuntimeService()
				.deleteProcessInstance(task.getProcessInstanceId(), null);
		
	}

	@Override
	public TimerIntermediateEventAction waitsAtTimerIntermediateEvent(String activityId) {

		final SimulatorProvider.ActivityHistory history = getCurrentHistory(activityId);
		final SimulatorProvider.ActivityState state = history.state;

		if (state == ActivityState.RUNNING) {
			return timer -> { throw new NotYetCompletedException(); };
		}
		
		if (state == ActivityState.COMPLETED) {
			return null;
		}
		
		if (state == ActivityState.ERROR) {
//			return timer-> simulatorEngine.getRuntimeService()
//					.createIncident("failedJob", timer.getExecutionId(), null);
		}
		
		// CANCELLED: Should be forced by the engine. If not
		// it was forced by the user so, the process instance needs to be deleted
		return timer -> simulatorEngine.getRuntimeService()
				.deleteProcessInstance(timer.getProcessInstanceId(), null);

	}

	@Override
	public MessageIntermediateCatchEventAction waitsAtMessageIntermediateCatchEvent(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReceiveTaskAction waitsAtReceiveTask(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SignalIntermediateCatchEventAction waitsAtSignalIntermediateCatchEvent(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Runner runsCallActivity(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventBasedGatewayAction waitsAtEventBasedGateway(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceTaskAction waitsAtServiceTask(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SendTaskAction waitsAtSendTask(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageIntermediateThrowEventAction waitsAtMessageIntermediateThrowEvent(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageEndEventAction waitsAtMessageEndEvent(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BusinessRuleTaskAction waitsAtBusinessRuleTask(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConditionalIntermediateEventAction waitsAtConditionalIntermediateEvent(String activityId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.camunda.bpm.extension.process_test_coverage.junit.rules#initializeListenerRunState()
	 */
    /**
     * Sets the test run state for the coverage listeners. logging.
     * {@see ProcessCoverageInMemProcessEngineConfiguration}
     */
    private void initializeListenerRunState() {

        final ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) simulatorEngine.getProcessEngineConfiguration();

        // Configure activities listener

        final FlowNodeHistoryEventHandler historyEventHandler = (FlowNodeHistoryEventHandler) processEngineConfiguration.getHistoryEventHandler();
        historyEventHandler.setCoverageTestRunState(coverageTestRunState);

        // Configure sequence flow listener

        final List<BpmnParseListener> bpmnParseListeners = processEngineConfiguration.getCustomPostBPMNParseListeners();

        for (BpmnParseListener parseListener : bpmnParseListeners) {

            if (parseListener instanceof PathCoverageParseListener) {

                final PathCoverageParseListener listener = (PathCoverageParseListener) parseListener;
                listener.setCoverageTestRunState(coverageTestRunState);
            }
        }

        // Compensation event handler

        final EventHandler compensationEventHandler = processEngineConfiguration.getEventHandler("compensate");
        if (compensationEventHandler != null && compensationEventHandler instanceof CompensationEventCoverageHandler) {

            final CompensationEventCoverageHandler compensationEventCoverageHandler = (CompensationEventCoverageHandler) compensationEventHandler;
            compensationEventCoverageHandler.setCoverageTestRunState(coverageTestRunState);

        } else {
            logger.warn("CompensationEventCoverageHandler not registered with process engine configuration!"
                    + " Compensation boundary events coverage will not be registered.");
        }

    }

    private void initializeTestMethodCoverage(final ProcessDefinition processDefinition) {
    	
        final MethodCoverage testCoverage = new MethodCoverage(processDefinition.getDeploymentId());

        // Construct the pristine coverage object

        // TODO decide on the builders fate
        final ProcessCoverage processCoverage = new ProcessCoverage(simulatorEngine, processDefinition);

        testCoverage.addProcessCoverage(processCoverage);

        coverageTestRunState.getClassCoverage().addTestMethodCoverage(provider.getProcessDefinitionKey(), testCoverage);
        
    }

}
