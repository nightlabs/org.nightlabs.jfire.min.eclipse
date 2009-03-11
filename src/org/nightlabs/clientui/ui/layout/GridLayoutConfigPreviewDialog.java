/**
 * 
 */
package org.nightlabs.clientui.ui.layout;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;

/**
 * Dialog that previews an {@link IGridLayoutConfig} by using simple place-holders of the cells ({@link IGridDataEntry})
 * showing only their name. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class GridLayoutConfigPreviewDialog extends ResizableTitleAreaDialog {

	private IGridLayoutConfig config;

	/**
	 * Construct a new {@link GridLayoutConfigPreviewDialog}.
	 * 
	 * @param shell The parent shell.
	 * @param resourceBundle The {@link ResourceBundle} to get initial size and position from.
	 * @param config The {@link IGridLayoutConfig} to preview.
	 */
	public GridLayoutConfigPreviewDialog(Shell shell, ResourceBundle resourceBundle, IGridLayoutConfig config) {
		super(shell, resourceBundle);
		this.config = config;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Layout preview");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE);
		
		wrapper.setLayout(GridLayoutUtil.createGridLayout(config.getGridLayout()));
		
		for (IGridDataEntry entry : config.getGridDataEntries()) {
			XComposite entryComp = new XComposite(wrapper, SWT.BORDER);
			Label label = new Label(entryComp, SWT.LEFT);
			org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH);
			label.setText(entry.getName().getText());
			label.setLayoutData(gd);
			entryComp.setLayoutData(GridLayoutUtil.createGridData(entry.getGridData()));
		}
		
		setTitle("Layout preview");
		setMessage("This is a preview of the current layout with place-holders for the cells.");
		
		return wrapper;
	}

}
