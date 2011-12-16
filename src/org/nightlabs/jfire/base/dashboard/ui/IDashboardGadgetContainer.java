package org.nightlabs.jfire.base.dashboard.ui;

import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

public interface IDashboardGadgetContainer {

	DashboardGadgetLayoutEntry<?> getLayoutEntry();

	void setTitle(String title);

}