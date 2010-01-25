package org.nightlabs.jfire.base.ui.layout;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.clientui.ui.layout.GridLayoutUtil;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.layout.EditLayoutEntry;

@SuppressWarnings("unchecked")
public abstract class AbstractEditLayoutComposite<E extends EditLayoutEntry> extends XComposite {
	
	private List<E> editLayoutEntries;

	public AbstractEditLayoutComposite(Composite parent, int style, GridLayout gridLayout, List<E> editLayoutEntries, boolean createEntries) {
		super(parent, SWT.NONE, LayoutDataMode.GRID_DATA);
		
		setLayout(GridLayoutUtil.createGridLayout(gridLayout));
		this.editLayoutEntries = editLayoutEntries;

		if (createEntries)
			createEntries();
	}

	protected void createEntries() {
		for (E entry : editLayoutEntries) {
			Control entryControl;
			if (entry.getEntryType().equals(AbstractEditLayoutEntry.ENTRY_TYPE_SEPARATOR)) {
				entryControl = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			} else {
				entryControl = createEntryControl(entry, this);
			}
			
			if (entryControl != null)
				entryControl.setLayoutData(GridLayoutUtil.createGridData(entry.getGridData()));
		}
	}
	
	protected abstract Control createEntryControl(E entry, Composite parent);
}
