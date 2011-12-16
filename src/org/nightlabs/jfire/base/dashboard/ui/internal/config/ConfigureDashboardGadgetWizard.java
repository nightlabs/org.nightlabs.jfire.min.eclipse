/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.config;

import org.eclipse.jface.wizard.Wizard;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetConfigPage;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * @author abieber
 *
 */
public class ConfigureDashboardGadgetWizard extends Wizard {

	private DashboardGadgetLayoutEntry<?> layoutEntry;
	private IDashboardGadgetConfigPage<?> gadgetConfigPage;
	
	public ConfigureDashboardGadgetWizard(IDashboardGadgetFactory gadgetFactory, DashboardGadgetLayoutEntry<?> layoutEntry) {
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.config.ConfigureDashboardGadgetWizard.windowTitle")); //$NON-NLS-1$
		this.layoutEntry = layoutEntry;
		gadgetConfigPage = gadgetFactory.createConfigurationWizardPage();
		gadgetConfigPage.initialize(layoutEntry);
		addPage(gadgetConfigPage);
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public boolean performFinish() {
		gadgetConfigPage.configure(layoutEntry);
		return true;
	}
	
	public DashboardGadgetLayoutEntry<?> getLayoutEntry() {
		return layoutEntry;
	}

}
