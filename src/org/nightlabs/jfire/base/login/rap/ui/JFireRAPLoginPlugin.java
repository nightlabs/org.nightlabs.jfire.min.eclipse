package org.nightlabs.jfire.base.login.rap.ui;

import org.eclipse.core.runtime.Plugin;
import org.nightlabs.jfire.base.login.ui.ILoginHandler;
import org.osgi.framework.BundleContext;

public class JFireRAPLoginPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.nightlabs.jfire.base.login.rap.ui";

	private HttpServiceTracker httpServiceTracker;
	
	public JFireRAPLoginPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		context.registerService(ILoginHandler.class.getName(), new JFireRAPLoginHandler(), null);
		
		httpServiceTracker = new HttpServiceTracker(context);
		httpServiceTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		httpServiceTracker.close();
		
		super.stop(context);
	}
}
