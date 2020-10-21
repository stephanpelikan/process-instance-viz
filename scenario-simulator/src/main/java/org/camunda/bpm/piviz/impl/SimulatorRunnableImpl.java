package org.camunda.bpm.piviz.impl;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.ProcessCoverageInMemProcessEngineConfiguration;
import org.camunda.bpm.piviz.SimulatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorRunnableImpl implements SimulatorRunnable {

	private static final Logger logger = LoggerFactory.getLogger(SimulatorRunnableImpl.class);
	
	private static ProcessEngine simulatorEngine;
	
	private SimulatorProvider provider;

	private String result;

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
			// final var processEngineConfiguration = (ProcessEngineConfigurationImpl)
			// processEngine
			// .getProcessEngineConfiguration();
			final ProcessCoverageInMemProcessEngineConfiguration simulatorConfiguration
					= new ProcessCoverageInMemProcessEngineConfiguration();
			// simulatorConfiguration.setProcessEnginePlugins(processEngineConfiguration.getProcessEnginePlugins());
			simulatorConfiguration.setDeploymentSynchronized(true);
			simulatorConfiguration.setDatabaseSchemaUpdate("true");

			simulatorEngine = simulatorConfiguration.buildProcessEngine();
		}
		return simulatorEngine;
		
	}

	@Override
	public String getResult() {
		return result;
	}

}
