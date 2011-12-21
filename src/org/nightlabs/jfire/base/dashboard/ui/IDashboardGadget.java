package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * @author abieber
 *
 */
public interface IDashboardGadget {

	void setGadgetContainer(IDashboardGadgetContainer gadgetContainer);
	Composite createControl(Composite parent);
	void refresh();
}
