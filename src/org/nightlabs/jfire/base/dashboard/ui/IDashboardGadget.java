package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * Implementations of this interface are served by
 * {@link IDashboardGadgetFactory#createDashboardGadget()}. They are used to
 * build create the ui of a gadget inside the JFire dashboard view.
 * <p>
 * {@link IDashboardGadget}s will be created and their
 * {@link IDashboardGadgetContainer dashboardContainer} will be set. The
 * container gives access to the {@link DashboardGadgetLayoutEntry layoutEntry}
 * of the gadget.
 * </p>
 * <p>
 * Note that after a call to {@link #createControl(Composite)} multiple calls to
 * {@link #refresh()} might occur and the config
 * {@link DashboardGadgetLayoutEntry layoutEntry} might have changed in between.
 * </p>
 * <p>
 * Use {@link AbstractDashbardGadget} as base for your implementation.
 * </p>
 * 
 * @author abieber
 */
public interface IDashboardGadget {
	
	/**
	 * Set the {@link IDashboardGadgetContainer gadgetContainer} for this gadget.
	 */
	void setGadgetContainer(IDashboardGadgetContainer gadgetContainer);

	/**
	 * Create the control of this gadget. Its contents should be loaded in
	 * {@link #refresh()}.
	 */
	Composite createControl(Composite parent);

	/**
	 * Refresh the contents of this gadget using a changed config in the
	 * {@link IDashboardGadgetContainer gadgetContainer}.
	 */
	void refresh();
}
