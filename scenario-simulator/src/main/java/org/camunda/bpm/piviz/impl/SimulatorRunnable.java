package org.camunda.bpm.piviz.impl;

import org.camunda.bpm.piviz.result.Report;

public interface SimulatorRunnable extends Runnable {

	Report getResult();
	
}
