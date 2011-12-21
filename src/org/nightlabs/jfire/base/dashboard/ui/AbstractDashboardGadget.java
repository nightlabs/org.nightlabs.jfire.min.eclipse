package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;


/**
 * @author abieber
 *
 */
public abstract class AbstractDashboardGadget implements IDashboardGadget {

	private IDashboardGadgetContainer gadgetContainer;
	
	public AbstractDashboardGadget() {
	}

	@Override
	public void setGadgetContainer(IDashboardGadgetContainer gadgetContainer) {
		this.gadgetContainer = gadgetContainer;
	}
	
	public IDashboardGadgetContainer getGadgetContainer() {
		return gadgetContainer;
	}

	protected XComposite createDefaultWrapper(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE);
		wrapper.getGridData().minimumHeight = 200;
		return wrapper;
	}

}
