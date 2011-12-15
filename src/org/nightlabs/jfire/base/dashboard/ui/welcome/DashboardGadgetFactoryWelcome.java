package org.nightlabs.jfire.base.dashboard.ui.welcome;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.dashboard.ui.AbstractDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetConfigPage;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author abieber
 *
 */
public class DashboardGadgetFactoryWelcome extends AbstractDashboardGadgetFactory implements IDashboardGadgetFactory {

	Logger logger = LoggerFactory.getLogger(DashboardGadgetFactoryWelcome.class);
	
	/**
	 * 
	 */
	public DashboardGadgetFactoryWelcome() {
	}

	@Override
	public IDashboardGadgetConfigPage<?> createConfigurationWizardPage() {
		return new ConfigureWelcomeGadgetPage();
	}

	@Override
	public Composite createGadgetControl(Composite parent) {
		XComposite welcomeGadget = new XComposite(parent, SWT.NONE);
		welcomeGadget.getGridLayout().numColumns = 2;
		
		welcomeGadget.getGridLayout().makeColumnsEqualWidth = false;
		appendNewRow(welcomeGadget, "icons/JFire-Logo.81x81.png", "Thank you for using JFire", 
				"JFire v1.3 is the most easiest ERP/CRM ever seen in the JFire universe!!!");
		
		return welcomeGadget;
	}
	
	private static void appendNewRow(Composite welcomeGadget, String iconPath, String caption, String rowText) {
		Label icon = new Label(welcomeGadget, SWT.NONE);
		icon.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.nightlabs.jfire.base.dashboard.ui", iconPath).createImage());
		icon.setLayoutData(new GridData());
		
		XComposite wrapper2 = new XComposite(welcomeGadget, SWT.NONE);
		wrapper2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label captionLabel = new Label(wrapper2, SWT.WRAP);
		captionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		captionLabel.setText(caption);
		RCPUtil.setControlFontStyle(captionLabel, SWT.BOLD, 0);
		
		Text text = new Text(wrapper2, SWT.WRAP);
		text.setText(rowText);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

}
