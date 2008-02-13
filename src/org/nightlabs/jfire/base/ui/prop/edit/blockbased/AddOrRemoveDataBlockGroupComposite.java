package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.prop.DataBlock;

public class AddOrRemoveDataBlockGroupComposite extends XComposite {
	private Button addButton;
	private Button removeButton;
	
	public static interface Listener {
		public void addDataBlock(int index);
		public void removeDataBlock(DataBlock block);
	}
	
	private Listener listener;

	protected AddOrRemoveDataBlockGroupComposite(Composite parent, final DataBlock block, final int index) {
		super(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		
		GridData gd = new GridData();
		gd.widthHint = 20;
		
		addButton = new Button(this, SWT.PUSH);
		addButton.setText("+");
		addButton.setLayoutData(gd);
		
		gd = new GridData();
		gd.widthHint = 20;
		removeButton = new Button(this, SWT.PUSH);
		removeButton.setText("-");
		removeButton.setLayoutData(gd);
		
		SelectionListener selectionListener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (listener == null)
					return;
				
				if (e.widget == addButton)
					listener.addDataBlock(index+1);
				else if (e.widget == removeButton)
					listener.removeDataBlock(block);
			}
		};
		
		addButton.addSelectionListener(selectionListener);
		removeButton.addSelectionListener(selectionListener);
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	public Button getRemoveButton() {
		return removeButton;
	}
	
	public Button getAddButton() {
		return addButton;
	}
}
