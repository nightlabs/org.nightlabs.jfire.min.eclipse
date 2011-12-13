/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.welcome;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetConfigPage;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * @author abieber
 *
 */
public class ConfigureWelcomeGadgetPage extends WizardHopPage implements IDashboardGadgetConfigPage<Object> {

	/**
	 * @param pageName
	 */
	public ConfigureWelcomeGadgetPage() {
		super(ConfigureWelcomeGadgetPage.class.getName());
		setTitle("Welcome Gadget");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		Label l = new Label(parent, SWT.WRAP);
		l.setText("The JFire welcome gadget does not need to be configured");
		return l;
	}

	@Override
	public void configure(DashboardGadgetLayoutEntry<?> layoutEntry) {
		layoutEntry.getEntryName().setText(Locale.ENGLISH, "Welcome to JFire");
	}

	@Override
	public void initialize(DashboardGadgetLayoutEntry<?> layoutEntry) {
	}
}
