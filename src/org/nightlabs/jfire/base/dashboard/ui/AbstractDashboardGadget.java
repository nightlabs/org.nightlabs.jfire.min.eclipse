package org.nightlabs.jfire.base.dashboard.ui;


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
	
	protected IDashboardGadgetContainer getGadgetContainer() {
		return gadgetContainer;
	}

}
