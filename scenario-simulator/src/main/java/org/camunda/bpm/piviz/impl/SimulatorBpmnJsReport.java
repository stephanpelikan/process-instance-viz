package org.camunda.bpm.piviz.impl;

import java.util.Set;

import org.camunda.bpm.extension.process_test_coverage.junit.rules.CoverageTestRunState;
import org.camunda.bpm.extension.process_test_coverage.model.CoveredFlowNode;
import org.camunda.bpm.extension.process_test_coverage.model.MethodCoverage;
import org.camunda.bpm.extension.process_test_coverage.util.BpmnJsReport;

public class SimulatorBpmnJsReport extends BpmnJsReport {

	public static String generateReport(final String processDefinitionKey, final CoverageTestRunState coverageTestRunState) {
		
		final MethodCoverage coverage = coverageTestRunState.getCurrentTestMethodCoverage();
        final Set<CoveredFlowNode> coveredFlowNodes = coverage.getCoveredFlowNodes(processDefinitionKey);
        final Set<String> coveredSequenceFlowIds = coverage.getCoveredSequenceFlowIds(processDefinitionKey);
        final String flowNodeMarkers = generateJavaScriptFlowNodeAnnotations(coveredFlowNodes);
        final String sequenceFlowMarkers = generateJavaScriptSequenceFlowAnnotations(coveredSequenceFlowIds);
        final String markers = flowNodeMarkers + sequenceFlowMarkers;

        return markers;
        
	}
	
}
