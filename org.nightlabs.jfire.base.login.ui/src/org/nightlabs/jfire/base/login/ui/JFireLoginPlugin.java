package org.nightlabs.jfire.base.login.ui;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class JFireLoginPlugin 
extends AbstractUIPlugin
implements IStartup
{
	private static JFireLoginPlugin plugin;

	public JFireLoginPlugin() {
		plugin = this;
	}
	
	public static JFireLoginPlugin getDefault() {
		if(plugin == null)
			throw new AssertionError();
		
		return plugin;
	}
	
	@Override
	public void earlyStartup() {
		// Do nothing only ensure that all classes of this plug-in is started very early
	}
}
