package org.nightlabs.jfire.base.ui.layout;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.clientui.ui.layout.GridLayoutUtil;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.EditLayoutEntry;

/**
 * Compostie that displays a list of {@link EditLayoutEntry}s inside the SWT-representation of a
 * {@link GridLayout} and applies the appropriate layout-data for each entry. It can be used as
 * basis for Composites applying the layout configured in a {@link AbstractEditLayoutConfigModule}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @param <EditLayoutEntryType> The type (sub-class) of {@link EditLayoutEntry} this composite uses.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractEditLayoutComposite<EditLayoutEntryType extends EditLayoutEntry> extends XComposite {
	
	private List<EditLayoutEntryType> editLayoutEntries;

	/**
	 * Create a new {@link AbstractEditLayoutComposite} for the given {@link GridLayout} and
	 * entries.
	 * 
	 * @param parent The parent to add the composite to.
	 * @param style The style to use for the composite.
	 * @param gridLayout The {@link GridLayout} whose SWT-representation that should be applied to
	 *            the composite.
	 * @param editLayoutEntries The entries that should be placed inside the layout.
	 * @param createEntries Whether the entry-controls should be created in this constructor. If
	 *            <code>false</code> is passed here, the entries can be created using the
	 *            {@link #createEntries()} method.
	 */
	public AbstractEditLayoutComposite(Composite parent, int style, GridLayout gridLayout, List<EditLayoutEntryType> editLayoutEntries, boolean createEntries) {
		super(parent, SWT.NONE, LayoutDataMode.GRID_DATA);
		
		setLayout(GridLayoutUtil.createGridLayout(gridLayout));
		this.editLayoutEntries = editLayoutEntries;

		if (createEntries)
			createEntries();
	}

	/**
	 * Create the controls for all {@link EditLayoutEntry}s this composite was constructed with and
	 * apply an appropriate layout-data for each one. This method will create entries of type
	 * {@link EditLayoutEntry#ENTRY_TYPE_SEPARATOR} itself as a separator-Label and will delegate
	 * the creation of other types of entries to the implementation of
	 * {@link #createEntryControl(EditLayoutEntry, Composite)}.
	 */
	protected void createEntries() {
		for (EditLayoutEntryType entry : editLayoutEntries) {
			Control entryControl;
			if (entry.getEntryType().equals(EditLayoutEntry.ENTRY_TYPE_SEPARATOR)) {
				entryControl = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			} else {
				entryControl = createEntryControl(entry, this);
			}
			
			if (entryControl != null)
				entryControl.setLayoutData(GridLayoutUtil.createGridData(entry.getGridData()));
		}
	}

	/**
	 * Create the control of a {@link EditLayoutEntry} whose type is not
	 * {@link EditLayoutEntry#ENTRY_TYPE_SEPARATOR}. Note that the control does not need to have a
	 * layout-data after creation, this will be applied by the calling method.
	 * 
	 * @param entry The entry whose control is to be created.
	 * @param parent The parent to add the control to.
	 * @return The newly create control.
	 */
	protected abstract Control createEntryControl(EditLayoutEntryType entry, Composite parent);
}
