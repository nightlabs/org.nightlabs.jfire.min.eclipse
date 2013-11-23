package org.nightlabs.jfire.base.ui.app.rcp;

import org.eclipse.core.runtime.Plugin;
import org.nightlabs.singleton.SingletonProviderFactory;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JFireRCPAppPlugin extends Plugin {


	/**
	 * The constructor
	 */
	public JFireRCPAppPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		SingletonProviderFactory.setProviderClass(RCPSingletonProvider.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

}
