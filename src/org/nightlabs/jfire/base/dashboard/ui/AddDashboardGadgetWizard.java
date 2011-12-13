/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.jface.wizard.Wizard;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;

/**
 * @author abieber
 *
 */
public class AddDashboardGadgetWizard extends Wizard {

	private DashboardGadgetLayoutEntry<Object> layoutEntry;
	private DashboardGadgetTypePage gadgetTypePage;
	
	/**
	 * 
	 */
	public AddDashboardGadgetWizard(DashboardLayoutConfigModule<?> configModule) {
		gadgetTypePage = new DashboardGadgetTypePage(configModule);
		addPage(gadgetTypePage);
		setForcePreviousAndNextButtons(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		layoutEntry = (DashboardGadgetLayoutEntry<Object>) gadgetTypePage.getConfiguredLayoutEntry();
		return layoutEntry != null;
	}
	
	public DashboardGadgetLayoutEntry<Object> getLayoutEntry() {
		return layoutEntry;
	}

}
