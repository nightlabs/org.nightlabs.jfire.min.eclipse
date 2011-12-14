/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Composite;

/**
 * @author abieber
 *
 */
public interface IDashboardGadgetFactory extends IExecutableExtension {

	String getDashboardGadgetType();
	
	String getName();
	
	String getDescription();
	
	IDashboardGadgetConfigPage createConfigurationWizardPage();
	
	Composite createGadgetControl(Composite parent);
	
}
