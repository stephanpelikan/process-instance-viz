package org.camunda.bpm.piviz;

import org.camunda.bpm.piviz.impl.SimulatorClassLoader;
import org.camunda.bpm.piviz.impl.SimulatorRunnable;
import org.camunda.bpm.piviz.result.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate a certain business process (workflow) by using the
 * history of a real life process instance (which might still be running).
 * 
 * @author stephanpelikan
 */
public class Simulator {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);
	
	private static volatile long counter = 0;
	
	private SimulatorClassLoader classLoader;

	/**
	 * The Simulator uses an isolated {@link ClassLoader} to avoid mutual interference of
	 * existing Camunda BPM engines (potentially of a different version). The parent
	 * {@link ClassLoader} will be used to load common classes and classes shared
	 * different simulations (e.g. org.h2.* and org.slf4j.*).
	 *  
	 * @param parentClassLoader The parent {@link ClassLoader}
	 */
	public Simulator(final ClassLoader parentClassLoader) {
		classLoader = new SimulatorClassLoader(parentClassLoader);
	}
	
	/**
	 * Uses <i>Thread.currentThread().getContextClassLoader()</i>.
	 * 
	 * @see Simulator#Simulator(ClassLoader)
	 */
	public Simulator() {
		this(Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * @see Simulator#Simulator()
	 * @return An non interfering Simulator instance
	 */
	public static Simulator newInstance() {
		return new Simulator();
	}
	
	/**
	 * Simulate a process instance
	 * 
	 * @param provider A provider of details of the real life process instance to be simulated
	 * @return The details of the simulation
	 * @throws Exception Something went wrong
	 */
	public Report simulate(final SimulatorProvider provider) throws Exception {

		// get instance of SimulatorRunnableImpl
		
		final SimulatorRunnable runnable = (SimulatorRunnable)
				classLoader.loadClass(
					SimulatorRunnable.class.getName()
					+ "Impl")
				.getConstructor(SimulatorProvider.class)
				.newInstance(provider);
		
		// start thread
		
		final Thread simulatorThread = new Thread(runnable);
		simulatorThread.setName("PiVizSimulator#" + counter++);
		simulatorThread.setContextClassLoader(classLoader);
		
		synchronized (runnable) {
			simulatorThread.start();
			// wait to be woken up - thread will continue doing cleanup
			runnable.wait();
		}
	
		// provide results
		
		return runnable.getResult();

	}

}
