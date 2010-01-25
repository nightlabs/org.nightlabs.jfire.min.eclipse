/**
 *
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.edit.SelectionEditComposite;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class SelectionDataFieldEditor extends AbstractDataFieldEditor<SelectionDataField> {

	public SelectionDataFieldEditor(IStruct struct, SelectionDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<SelectionDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}
	
		@Override
		public DataFieldEditor<SelectionDataField> createPropDataFieldEditor(IStruct struct, SelectionDataField data) {
			return new SelectionDataFieldEditor(struct, data);
		}

		@Override
		public Class<SelectionDataField> getPropDataFieldType() {
			return SelectionDataField.class;
		}

	};

	private SelectionEditComposite<StructFieldValue> selectionEditComposite;
	
	private LabelProvider labelProvider = new LabelProvider() {
		@Override
		public String getText(Object element) {
//			FieldValueHolder valueHolder = (FieldValueHolder) element;
//			if (valueHolder == EMPTY_SELECTION)
//				return "[enpty]";
//			else
//				return valueHolder.value.getValueName().getText();
			StructFieldValue value = (StructFieldValue) element;
			if (value == null)
				return Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.SelectionDataFieldComposite.value.empty"); //$NON-NLS-1$
			else
				return value.getValueName().getText();
		}
	};

	@Override
	public Control createControl(Composite parent) {
		if (selectionEditComposite == null) {
			selectionEditComposite = new SelectionEditComposite<StructFieldValue>(parent, SWT.NONE, labelProvider, true);
			selectionEditComposite.addModificationListener(getModifyListener());
		}
		return selectionEditComposite;
	}

	@Override
	public void doRefresh() {
		if (selectionEditComposite != null) {
			SelectionStructField field = (SelectionStructField) getStructField();

			List<StructFieldValue> structFieldValues = new LinkedList<StructFieldValue>(field.getStructFieldValues());
			if (field.allowsEmptySelection())
				structFieldValues.add(0, null);

			selectionEditComposite.setInput(structFieldValues);

			SelectionDataField dataField = getDataField();
			if (dataField.getStructFieldValueID() != null) {
				try {
					selectionEditComposite.setSelectedElement(field.getStructFieldValue(dataField.getStructFieldValueID()));
				} catch (StructFieldValueNotFoundException e) {
					if (structFieldValues.size() > 0) {
						selectionEditComposite.setSelectedIndex(0);
					}
					else {
						selectionEditComposite.setSelectedIndex(-1);
					}
					throw new RuntimeException("Could not find the referenced structFieldValue with id "+dataField.getStructFieldValueID()); //$NON-NLS-1$
				}
			} else {
				selectionEditComposite.setSelectedElement(null);
//				if (field.getDefaultValue() != null)
//					fieldValueCombo.selectElement(field.getDefaultValue());
//				else
//					fieldValueCombo.selectElementByIndex(-1);
			}
			
//			fieldValueCombo.setEnabled(getEditor().getDataField().getManagedBy() == null);
		}
	}

	public Control getControl() {
		return selectionEditComposite;
	}

	public void updatePropertySet() {
		getDataField().setSelection(selectionEditComposite.getSelectedElement());
	}

	@Override
	protected IEntryEditor getEntryViewer() {
		return selectionEditComposite;
	}
}


