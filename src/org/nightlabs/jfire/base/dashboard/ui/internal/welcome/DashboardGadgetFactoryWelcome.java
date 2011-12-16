package org.nightlabs.jfire.base.dashboard.ui.internal.welcome;
import org.nightlabs.jfire.base.dashboard.ui.AbstractDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadget;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetConfigPage;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;

/**
 * @author abieber
 *
 */
public class DashboardGadgetFactoryWelcome extends AbstractDashboardGadgetFactory implements IDashboardGadgetFactory {

	public DashboardGadgetFactoryWelcome() {
	}

	@Override
	public IDashboardGadgetConfigPage<?> createConfigurationWizardPage() {
		return new ConfigureWelcomeGadgetPage();
	}

	@Override
	public IDashboardGadget createDashboardGadget() {
		return new DashboardGadgetWelcome();
	}
}
