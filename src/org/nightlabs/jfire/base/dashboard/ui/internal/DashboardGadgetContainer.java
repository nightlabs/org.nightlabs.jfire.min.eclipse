package org.nightlabs.jfire.base.dashboard.ui.internal;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadget;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetContainer;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;
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
		if (gadgetFactory != null) {
			this.gadget = gadgetFactory.createDashboardGadget();
			gadget.setGadgetContainer(this);
		}
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
		if (gadget != null) {
			gadget.refresh();
		}
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
		if (gadgetControl == null) {
			if (gadget != null) {
				gadgetControl = gadget.createControl(sectionPart.getContainer());
			} else {
				gadgetControl = new XComposite(sectionPart.getContainer(), SWT.NONE);
				Label l = new Label(gadgetControl, SWT.WRAP);
				l.setText(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.DashboardGadgetContainer.extensionMissing.text")); //$NON-NLS-1$
			}
			
		}
	}
	
	public IToolBarManager getToolBarManager() {
		return sectionPart.getToolBarManager();
	}
}
