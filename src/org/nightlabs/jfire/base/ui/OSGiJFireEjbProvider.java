package org.nightlabs.jfire.base.ui;

import java.util.Hashtable;

import org.nightlabs.jfire.base.GlobalJFireEjb3Provider;

public class OSGiJFireEjbProvider extends GlobalJFireEjb3Provider {

	private final ClassLoader initialContextLoader;

	public OSGiJFireEjbProvider(ClassLoader loader) {
		initialContextLoader = loader;
	}

	@Override
	public <T> T getLocalBean(Class<T> ejbLocalInterface) {
		return super.getLocalBean(ejbLocalInterface);
	}

	@Override
	public <T> T getRemoteBean(Class<T> ejbRemoteInterface, Hashtable<?, ?> environment) {
		ClassLoader original = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(initialContextLoader);

			return super.getRemoteBean(ejbRemoteInterface, environment);
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}
}
