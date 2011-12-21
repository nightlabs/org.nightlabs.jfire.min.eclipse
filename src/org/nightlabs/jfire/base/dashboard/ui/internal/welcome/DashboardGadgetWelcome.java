package org.nightlabs.jfire.base.dashboard.ui.internal.welcome;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.dashboard.ui.AbstractDashboardGadget;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;

/**
 * @author abieber
 * @author sschefczyk
 */
public class DashboardGadgetWelcome extends AbstractDashboardGadget {

	public DashboardGadgetWelcome() {
	}

	@Override
	public Composite createControl(Composite parent) {
		XComposite welcomeGadget = new XComposite(parent, SWT.NONE);
		welcomeGadget.getGridLayout().numColumns = 2;
		
		welcomeGadget.getGridLayout().makeColumnsEqualWidth = false;
		appendNewRow(welcomeGadget, "icons/JFire-Logo.81x81.png", Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.welcome.DashboardGadgetWelcome.row1.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.welcome.DashboardGadgetWelcome.row1.message")); //$NON-NLS-1$
		
		return welcomeGadget;
	}

	private static void appendNewRow(Composite welcomeGadget, String iconPath, String caption, String rowText) {
		Label icon = new Label(welcomeGadget, SWT.NONE);
		icon.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("org.nightlabs.jfire.base.dashboard.ui", iconPath).createImage()); //$NON-NLS-1$
		icon.setLayoutData(new GridData());
		
		XComposite wrapper2 = new XComposite(welcomeGadget, SWT.NONE);
		wrapper2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label captionLabel = new Label(wrapper2, SWT.WRAP);
		captionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		captionLabel.setText(caption);
		RCPUtil.setControlFontStyle(captionLabel, SWT.BOLD, 0);
		
		Label textLabel = new Label(wrapper2, SWT.WRAP);
		textLabel.setText(rowText);
		textLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	@Override
	public void refresh() {
		getGadgetContainer().setTitle(getGadgetContainer().getLayoutEntry().getName());
	}

}
