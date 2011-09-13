package org.nightlabs.jfire.base.ui.edit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;

/**
 * This shows check-boxes for all available options.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class MultiSelectionEditComposite<T>
extends AbstractInlineEditComposite
{
	private static Font italicLabelFont;
	
	private Composite wrapper;
	private ILabelProvider labelProvider;

	public MultiSelectionEditComposite(Composite parent, int style, final ILabelProvider labelProvider, boolean showTitle)
	{
		super(parent, style, showTitle);
		
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
		
		if (input != null && !input.isEmpty()){
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
		}else{
			Label noValuesLabel = new Label(wrapper, SWT.NONE);
			noValuesLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.edit.MultiSelectionEditComposite.noValuesAvailabeLabelText")); //$NON-NLS-1$
			if (italicLabelFont == null){
				FontData fontData = noValuesLabel.getFont().getFontData()[0];
				italicLabelFont = new Font(noValuesLabel.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.ITALIC));
			}
			noValuesLabel.setFont(italicLabelFont);
			noValuesLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		layout(true, true);
	}
	
	public void setSelection(Collection<T> selected) {
		for (Map.Entry<Button, T> buttonME : getButton2Values().entrySet()) {
			buttonME.getKey().setSelection(selected != null && selected.contains(buttonME.getValue()));
		}
	}
	
	public void setSelection(Set<String> selected) {
		setSelection(getValues(selected));
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
		for (Map.Entry<Button, T> buttonME : getButton2Values().entrySet()) {
			if (buttonME.getKey().getSelection()) {
				result.add(buttonME.getValue());
			}
		}
		return result;
	}
	
	private Set<T> getValues(Set<String> fieldValueIDs) {
		if(wrapper == null || fieldValueIDs == null)
			return null;
		Control[] children = wrapper.getChildren();
		Set<T> result = new HashSet<T>(children.length);
		for (T value : getButton2Values().values()) {
			if (fieldValueIDs.contains(((MultiSelectionStructFieldValue)value).getStructFieldValueID())) {
				result.add(value);
			}			
		}
		return result;
	}
	
	private Map<Button, T> getButton2Values() {
		if(wrapper == null)
			return null;
		Control[] children = wrapper.getChildren();
		Map<Button, T> result = new HashMap<Button, T>();
		for (Control control : children) {
			if(!(control instanceof Button))
				continue;
			Button b = (Button) control;
			if(!b.getSelection())
				continue;
			Object data = b.getData();
			if(data == null || !(data instanceof MultiSelectionStructFieldValue))
				continue;
			result.put(b, (T) data);
		}		
		return result;
	}
}
