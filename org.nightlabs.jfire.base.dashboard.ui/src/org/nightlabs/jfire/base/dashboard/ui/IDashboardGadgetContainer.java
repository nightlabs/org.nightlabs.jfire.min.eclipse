package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.jface.action.IToolBarManager;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * A {@link IDashboardGadgetContainer} gives access to the config of a gadget {@link #getLayoutEntry()}.
 * 
 * @author abieber
 */
public interface IDashboardGadgetContainer {
	
	/**
	 * @return The config of the gadget.
	 */
	DashboardGadgetLayoutEntry<?> getLayoutEntry();

	/**
	 * Set the title of the gadget-container.
	 */
	void setTitle(String title);

	/**
	 * @return The toolbar of the gadget
	 */
	IToolBarManager getToolBarManager();
}