/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadget;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetContainer;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * @author abieber
 *
 */
public class DashboardGadgetContainer implements IDashboardGadgetContainer {

	private ToolBarSectionPart sectionPart;
	private DashboardGadgetLayoutEntry<?> layoutEntry;
	
	private IDashboardGadgetFactory gadgetFactory;
	private IDashboardGadget gadget;
	
	private Composite gadgetControl;
	
	public DashboardGadgetContainer(ToolBarSectionPart sectionPart, IDashboardGadgetFactory gadgetFactory) {
		this.sectionPart = sectionPart;
		this.gadgetFactory = gadgetFactory;
		this.gadget = gadgetFactory.createDashboardGadget();
		gadget.setGadgetContainer(this);
	}
	
	public DashboardGadgetLayoutEntry<?> getLayoutEntry() {
		return layoutEntry;
	}
	
	public void setLayoutEntry(DashboardGadgetLayoutEntry<?> layoutEntry, boolean refreshGadget) {
		this.layoutEntry = layoutEntry;
		if (refreshGadget)
			refreshGadget();
	}
	
	public void refreshGadget() {
		sectionPart.getSection().setText(layoutEntry.getName());
		gadget.refresh();
	}
	
	public void setTitle(String title) {
		sectionPart.getSection().setText(title);
	}
	
	public IDashboardGadget getGadget() {
		return gadget;
	}
	
	public IDashboardGadgetFactory getGadgetFactory() {
		return gadgetFactory;
	}
	
	public void createGadgetControl() {
		if (gadgetControl == null)
			gadgetControl = gadget.createControl(sectionPart.getContainer());
	}
	
	public IToolBarManager getToolBarManager() {
		return sectionPart.getToolBarManager();
	}
}
