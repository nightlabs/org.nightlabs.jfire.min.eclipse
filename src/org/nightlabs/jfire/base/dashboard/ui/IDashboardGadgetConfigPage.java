/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui;

import org.nightlabs.base.ui.wizard.IWizardHopPage;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * @author abieber
 *
 */
public interface IDashboardGadgetConfigPage<T> extends IWizardHopPage {

	void initialize(DashboardGadgetLayoutEntry<?> layoutEntry);
	
	void configure(DashboardGadgetLayoutEntry<?> layoutEntry);
		
}
