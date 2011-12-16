/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.welcome;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jfire.base.dashboard.ui.AbstractDashbardGadgetConfigPage;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModuleInitialiser;

/**
 * @author abieber
 *
 */
public class ConfigureWelcomeGadgetPage extends AbstractDashbardGadgetConfigPage<Object> {

	private I18nTextEditor gadgetTitle;
	
	public ConfigureWelcomeGadgetPage() {
		super(ConfigureWelcomeGadgetPage.class.getName());
		setTitle("Welcome Gadget");
	}

	@Override
	public Control createPageContents(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		
		Label l = new Label(wrapper, SWT.WRAP);
		l.setText("The JFire welcome gadget does not need to be configured.\nYou can, however, change its title.");
		
		gadgetTitle = new I18nTextEditor(wrapper, "Gadget title");
		if (!getLayoutEntry().getEntryName().isEmpty()) {
			gadgetTitle.setI18nText(getLayoutEntry().getEntryName());
		} else {
			gadgetTitle.setI18nText(createInitialName());
		}
		
		return wrapper;
	}
	
	private I18nText createInitialName() {
		I18nTextBuffer textBuffer = new I18nTextBuffer();
		DashboardLayoutConfigModuleInitialiser.initializeWelcomeGadgetName(textBuffer);
		return textBuffer;
	}

	@Override
	public void configure(DashboardGadgetLayoutEntry<?> layoutEntry) {
		if (gadgetTitle != null) {
			layoutEntry.getEntryName().copyFrom(gadgetTitle.getI18nText());
		} else {
			layoutEntry.getEntryName().copyFrom(createInitialName());			
		}
	}

}
