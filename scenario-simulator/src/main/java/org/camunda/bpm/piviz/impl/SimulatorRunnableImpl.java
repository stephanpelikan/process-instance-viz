package org.camunda.bpm.piviz.impl;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.ProcessCoverageInMemProcessEngineConfiguration;
import org.camunda.bpm.piviz.SimulatorProvider;
import org.camunda.bpm.piviz.result.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorRunnableImpl implements SimulatorRunnable {

	private static final Logger logger = LoggerFactory.getLogger(SimulatorRunnableImpl.class);
	
	private static ProcessEngine simulatorEngine;
	
	private SimulatorProvider provider;

	private Report result;

	public SimulatorRunnableImpl(final SimulatorProvider provider) {
		this.provider = provider;
	}

	@Override
	public void run() {

		SimulatorScenario scenario = null;
		
		synchronized (this) {

			try {

				final ProcessEngine usedProcessEngine = getSimulatorEngine();

				scenario = new SimulatorScenario(provider, usedProcessEngine);
				scenario.execute();

				result = scenario.createReport();

			} catch (Throwable e) {
				logger.info("Failed", e);
			} finally {
				this.notify();
			}

		}
		
		if (scenario != null) {
			scenario.cleanup();
		}

	}

	private static ProcessEngine getSimulatorEngine() {
		
		if (simulatorEngine == null) {
			final ProcessCoverageInMemProcessEngineConfiguration simulatorConfiguration
					= new ProcessCoverageInMemProcessEngineConfiguration();
			
			simulatorConfiguration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
			simulatorConfiguration.setDeploymentSynchronized(true);
			simulatorConfiguration.setDatabaseSchemaUpdate("true");

			simulatorEngine = simulatorConfiguration.buildProcessEngine();
		}
		return simulatorEngine;
		
	}

	@Override
	public Report getResult() {
		return result;
	}

}
