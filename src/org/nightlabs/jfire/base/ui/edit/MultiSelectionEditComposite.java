package org.nightlabs.jfire.base.ui.edit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;

/**
 * This shows check-boxes for all available options.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class MultiSelectionEditComposite<T>
extends AbstractInlineEditComposite
{
	private Composite wrapper;
	private ILabelProvider labelProvider;

	public MultiSelectionEditComposite(Composite parent, int style, final ILabelProvider labelProvider)
	{
		super(parent, style);
		
		this.labelProvider = labelProvider;
		
		setLayout(new GridLayout());
		
		if(wrapper != null) {
			wrapper.dispose();
			wrapper = null;
		}
		
		createWrapper();
		
//		Set<MultiSelectionStructFieldValue> selectedValues = new HashSet<MultiSelectionStructFieldValue>(dataField.getStructFieldValues());
//		List<MultiSelectionStructFieldValue> structFieldValues = structField.getStructFieldValues();
//		for (MultiSelectionStructFieldValue structFieldValue : structFieldValues) {
//			Button b = new Button(wrapper, SWT.CHECK);
//			b.setData(structFieldValue);
//			b.setText(structFieldValue.getValueName().getText());
//			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			if(selectedValues.contains(structFieldValue))
//				b.setSelection(true);
//			if(modifyListener != null) {
//				b.addSelectionListener(new SelectionAdapter() {
//					@Override
//					public void widgetSelected(SelectionEvent e) {
//						modifyListener.modifyData();
//					}
//				});
//			}
//		}
		layout(true, true);
	}

	private void createWrapper() {
		wrapper = new Composite(this, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		wrapper.setLayout(gl);
		wrapper.setLayoutData(new GridData());
	}
	
	public void setInput(final Collection<T> input, Collection<T> selected) {
//		Set<MultiSelectionStructFieldValue> selectedValues = new HashSet<MultiSelectionStructFieldValue>(dataField.getStructFieldValues());
//		List<MultiSelectionStructFieldValue> structFieldValues = structField.getStructFieldValues();
//		for (MultiSelectionStructFieldValue structFieldValue : structFieldValues) {
		if (wrapper != null) {
			wrapper.dispose();
			wrapper = null;
			createWrapper();
		}
		
		
		for (T entry : input) {
			Button b = new Button(wrapper, SWT.CHECK);
			b.setData(entry);
			b.setText(labelProvider.getText(entry));
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			if(selected.contains(entry))
				b.setSelection(true);
			
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					notifyModificationListeners();
				}
			});
		}
		layout(true, true);
	}

	/**
	 * Get the selected values.
	 * @return the selected values
	 */
	public Collection<T> getSelection()
	{
		if(wrapper == null)
			return null;
		Control[] children = wrapper.getChildren();
		Set<T> result = new HashSet<T>(children.length);
		for (Control control : children) {
			if(!(control instanceof Button))
				continue;
			Button b = (Button) control;
			if(!b.getSelection())
				continue;
			Object data = b.getData();
			if(data == null || !(data instanceof MultiSelectionStructFieldValue))
				continue;
			result.add((T) data);
		}
		return result;
	}
}
