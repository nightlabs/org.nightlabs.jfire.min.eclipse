package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.core.runtime.IExecutableExtension;

/**
 * @author abieber
 *
 */
public interface IDashboardGadgetFactory extends IExecutableExtension {

	String getDashboardGadgetType();
	
	String getName();
	
	String getDescription();
	
	IDashboardGadgetConfigPage createConfigurationWizardPage();
	
	IDashboardGadget createDashboardGadget();
	
}
