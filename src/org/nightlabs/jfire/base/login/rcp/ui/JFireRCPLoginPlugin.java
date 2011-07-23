package org.nightlabs.jfire.base.login.rcp.ui;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JFireRCPLoginPlugin
extends Plugin
//implements IStartup
{
	private static JFireRCPLoginPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// This is now done via the extension-point managed by LoginHandlerRegistry.
//		context.registerService(ILoginHandler.class.getName(), new JFireRCPLoginHandler(), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static JFireRCPLoginPlugin getDefault() {
		return plugin;
	}

//	@Override
//	public void earlyStartup() {
//		// do nothing, only necessary to trigger start of this plug-in
//	}

}
