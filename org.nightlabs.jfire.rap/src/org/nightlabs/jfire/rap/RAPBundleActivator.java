package org.nightlabs.jfire.rap;

import org.nightlabs.singleton.SingletonProviderFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RAPBundleActivator implements BundleActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		SingletonProviderFactory.setProviderClass(RAPSingletonProvider.class);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}
