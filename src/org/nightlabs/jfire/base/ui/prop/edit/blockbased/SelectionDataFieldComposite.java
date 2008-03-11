package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.AbstractListComposite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

public class SelectionDataFieldComposite
extends AbstractInlineDataFieldComposite<SelectionDataFieldEditor>
{
	private XComboComposite<StructFieldValue> fieldValueCombo;

	/**
	 * Assumes to have a parent composite with GridLayout and
	 * adds it own GridData.
	 * @param editor the SelectionDataFieldEditor
	 * @param parent the parent composite
	 * @param style the SWT style
	 */
	public SelectionDataFieldComposite(final SelectionDataFieldEditor editor,
			Composite parent, int style, final ModifyListener modifyListener)
	{
		super(parent, style, editor);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$

		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof StructFieldValue) {
					StructFieldValue value = (StructFieldValue) element;
					return value.getValueName().getText();
				}
				return ""; //$NON-NLS-1$
			}
		};

		fieldValueCombo = new XComboComposite<StructFieldValue>(
				this,
				AbstractListComposite.getDefaultWidgetStyle(this),
				(String) null,
				labelProvider,
				LayoutMode.TIGHT_WRAPPER
		);

		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		fieldValueCombo.setLayoutData(textData);

		final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				modifyListener.modifyData();
			}
		};

		fieldValueCombo.addSelectionChangedListener(selectionChangedListener);
		fieldValueCombo.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				fieldValueCombo.removeSelectionChangedListener(selectionChangedListener);
			}
		});
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite#refresh()
	 */
	@Override
	public void _refresh() {
		SelectionStructField field = (SelectionStructField) getEditor().getStructField();
		fieldValueCombo.setInput( field.getStructFieldValues() );
		if (getEditor().getDataField().getStructFieldValueID() != null) {
			try {
				fieldValueCombo.selectElement(field.getStructFieldValue(getEditor().getDataField().getStructFieldValueID()));
			} catch (StructFieldValueNotFoundException e) {
				if (fieldValueCombo.getItemCount() > 0) {
					fieldValueCombo.selectElementByIndex(0);
				}
				else {
					fieldValueCombo.selectElementByIndex(-1);
				}
				throw new RuntimeException("Could not find the referenced structFieldValue with id "+getEditor().getDataField().getStructFieldValueID()); //$NON-NLS-1$
			}
		} else {
			if (field.getDefaultValue() != null)
				fieldValueCombo.selectElement(field.getDefaultValue());
			else
				fieldValueCombo.selectElementByIndex(-1);
		}
	}

	public XComboComposite<StructFieldValue> getFieldValueCombo() {
		return fieldValueCombo;
	}
}
