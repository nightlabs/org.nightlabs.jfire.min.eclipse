package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;

/**
 * Use this as base for implementing {@link IDashboardGadget}. It will store the
 * {@link #getGadgetContainer() gadgetContainer} and make it accessible in your
 * implementation of {@link IDashboardGadget#refresh()}.
 * <p>
 * Use {@link #createDefaultWrapper(Composite)} to create a wrapper for your
 * gadget that will have a default height, so all gadgets look similar.
 * </p>
 * 
 * @author abieber
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

	/**
	 * Use this to create a wrapper for your gadget with default height.
	 */
	protected XComposite createDefaultWrapper(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE);
		wrapper.getGridData().minimumHeight = 200;
		wrapper.getGridData().heightHint = 200;
		return wrapper;
	}

}
