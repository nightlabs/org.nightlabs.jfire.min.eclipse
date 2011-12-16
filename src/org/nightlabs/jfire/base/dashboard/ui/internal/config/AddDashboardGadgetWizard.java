/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.config;

import org.eclipse.jface.wizard.Wizard;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;

/**
 * @author abieber
 *
 */
public class AddDashboardGadgetWizard extends Wizard {

	private DashboardGadgetLayoutEntry<Object> layoutEntry;
	private DashboardGadgetTypePage gadgetTypePage;
	
	public AddDashboardGadgetWizard(DashboardLayoutConfigModule<?> configModule) {
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.config.AddDashboardGadgetWizard.windowTitle")); //$NON-NLS-1$
		gadgetTypePage = new DashboardGadgetTypePage(configModule);
		addPage(gadgetTypePage);
		setForcePreviousAndNextButtons(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean performFinish() {
		layoutEntry = (DashboardGadgetLayoutEntry<Object>) gadgetTypePage.getConfiguredLayoutEntry();
		return layoutEntry != null;
	}
	
	public DashboardGadgetLayoutEntry<Object> getLayoutEntry() {
		return layoutEntry;
	}

}
