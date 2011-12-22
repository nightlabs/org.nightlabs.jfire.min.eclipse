package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.core.runtime.IExecutableExtension;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * Instances of this class are registered as extension to the point
 * <code>org.nightlabs.jfire.dashboard.ui.dashboardGadgetFactory</code>. The
 * registered extensions are responsible to create UI to configure and show a
 * gadget in the JFire dashboard.
 * 
 * @author abieber
 */
public interface IDashboardGadgetFactory extends IExecutableExtension {

	/**
	 * Get the identifier of the gadget-type this factory is responsible for.
	 * This corresponds to the
	 * {@link DashboardGadgetLayoutEntry#getEntryType() entryType} in the layout
	 * entry stored for the gadget.
	 */
	String getDashboardGadgetType();
	
	/**
	 * Get a short name of this factory. This will be shown to when configuring new gadgets.
	 */
	String getName();

	/**
	 * Get a longer description of this factory, or the type of gadgets it
	 * creates. This will be shown to when configuring new gadgets.
	 */
	String getDescription();

	/**
	 * Creates and returns an implementation of {@link IDashboardGadgetConfigPage} that can
	 * be used in order to configure a new or an existing gadget, i.e.
	 * {@link DashboardGadgetLayoutEntry}.
	 */
	@SuppressWarnings("unchecked")
	IDashboardGadgetConfigPage createConfigurationWizardPage();

	/**
	 * Creates and returns an implementation of {@link IDashboardGadget} that can be used in
	 * order to display the gadget to the user.
	 */
	IDashboardGadget createDashboardGadget();
	
}
