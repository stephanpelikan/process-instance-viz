package org.camunda.bpm.piviz.impl;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.piviz.SimulatorProvider;

public class SimulatorClassLoader extends URLClassLoader {

	public SimulatorClassLoader(final ClassLoader parent) {
		super(new URL[0], parent);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (name.startsWith(SimulatorProvider.class.getName())
				|| (name.startsWith(SimulatorRunnable.class.getName())
						&& !name.startsWith(SimulatorRunnable.class.getName() + "Impl"))) {
			return getParent().loadClass(name);
		}
		return super.findClass(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		
		// First check whether it's already been loaded, if so use it
		final Class<?> alreadyLoadedClass = findLoadedClass(name);
		if (alreadyLoadedClass != null) {
			return alreadyLoadedClass;
		}

		if (ClockUtil.class.getName().equals(name)) {
			return ClockUtil.class;
		}
		
		Class<?> loadedClass = null;
		try {
			// Ignore parent delegation and just try to load locally
			loadedClass = findClass(name);
		} catch (ClassNotFoundException e) {
			// Swallow exception - does not exist locally
		}
		
		if (loadedClass == null) {
			final Class<?> parentClass = super.loadClass(name);
			
			if (name.startsWith("org.slf4j")
					|| name.startsWith("org.h2")) {
				return parentClass;
			}

			final CodeSource codeSource = parentClass.getProtectionDomain().getCodeSource();
			if (codeSource == null) {
				return parentClass;
			}
			
			final URL location = codeSource.getLocation();
			if (!location.getProtocol().equals("file")
					&& !location.getProtocol().equals("jar")) {
				return parentClass;
			}
			
			super.addURL(location);
			loadedClass = findClass(name);
		}
		
		return loadedClass;
		
	}

}