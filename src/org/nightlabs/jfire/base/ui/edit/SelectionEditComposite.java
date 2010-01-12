package org.nightlabs.jfire.base.ui.edit;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.AbstractListComposite;
import org.nightlabs.base.ui.composite.XComboComposite;

public class SelectionEditComposite<T> extends AbstractInlineEditComposite {

	private XComboComposite<T> combo;
	
	public SelectionEditComposite(Composite parent, int style, LabelProvider labelProvider) {
		super(parent, style);
		
		combo = new XComboComposite<T>(
				this,
				AbstractListComposite.getDefaultWidgetStyle(this),
				(String) null,
				labelProvider,
				LayoutMode.TIGHT_WRAPPER
		);
//		combo.addModifyListener(getSwtModifyListener());
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				notifyModificationListeners();
			}
		});

		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(textData);
	}
	
	public void setInput(Collection<T> input) {
		combo.setInput(input);
	}
	
	public void setEnabled(boolean enabled, String tooltip) {
		combo.setEnabled(enabled);
		
		if (!enabled)
			combo.setToolTipText(tooltip);
		else
			combo.setToolTipText(null);
	}
	
	public T getSelectedElement() {
		return combo.getSelectedElement();
	}
	
	public void setSelectedElement(T selectedElement) {
		combo.selectElement(selectedElement);
	}
	
	public void setSelectedIndex(int index) {
		combo.setSelection(index);
	}
}