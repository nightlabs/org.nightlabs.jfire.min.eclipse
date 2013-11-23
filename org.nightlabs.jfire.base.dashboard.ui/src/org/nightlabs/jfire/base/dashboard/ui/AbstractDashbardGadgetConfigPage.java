package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * Use this as base for implementing {@link IDashboardGadgetConfigPage}. It will store the
 * {@link #getLayoutEntry() layoutEntry} set for this page make it accessible in your
 * implementation of the {@link WizardHopPage#createPageContents(Composite)} method.
 * 
 * @author abieber
 */
public abstract class AbstractDashbardGadgetConfigPage<T> extends WizardHopPage implements IDashboardGadgetConfigPage<T> {

	public AbstractDashbardGadgetConfigPage(String pageName) {
		super(pageName);
	}

	public AbstractDashbardGadgetConfigPage(String pageName, String title) {
		super(pageName, title);
	}

	public AbstractDashbardGadgetConfigPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	private DashboardGadgetLayoutEntry<?> layoutEntry;
	
	@Override
	public void initialize(DashboardGadgetLayoutEntry<?> layoutEntry) {
		this.layoutEntry = layoutEntry;
	}

	protected DashboardGadgetLayoutEntry<?> getLayoutEntry() {
		return layoutEntry;
	}

}
