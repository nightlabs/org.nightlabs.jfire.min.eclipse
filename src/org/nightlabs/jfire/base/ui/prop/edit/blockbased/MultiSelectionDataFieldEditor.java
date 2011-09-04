package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.Collections;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.edit.MultiSelectionEditComposite;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.MultiSelectionDataField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de --> (Original SelectionDataFieldEditor code)
 */
public class MultiSelectionDataFieldEditor
extends AbstractDataFieldEditor<MultiSelectionDataField> {

	public MultiSelectionDataFieldEditor(IStruct struct, MultiSelectionDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<MultiSelectionDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

//		@Override
//		public Class<? extends DataFieldEditor<SelectionDataField>> getDataFieldEditorClass() {
//			return SelectionDataFieldEditor.class;
//		}
	
		@Override
		public DataFieldEditor<MultiSelectionDataField> createPropDataFieldEditor(
				IStruct struct, MultiSelectionDataField data)
		{
			return new MultiSelectionDataFieldEditor(struct, data);
		}

		@Override
		public Class<MultiSelectionDataField> getPropDataFieldType() {
			return MultiSelectionDataField.class;
		}

	};

	private MultiSelectionEditComposite<MultiSelectionStructFieldValue> multiSelectionEditComposite;
	private static final ILabelProvider LABEL_PROVIDER = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (!MultiSelectionStructFieldValue.class.isAssignableFrom(element.getClass()))
				throw new RuntimeException("Given element is not of type MultiSelectionStructFieldValue");
			
			MultiSelectionStructFieldValue msfv = (MultiSelectionStructFieldValue) element;
			
			return msfv.getValueName().getText();
		};
	};

	@Override
	public Control createControl(Composite parent) {
		if (multiSelectionEditComposite == null) {
			multiSelectionEditComposite = new MultiSelectionEditComposite<MultiSelectionStructFieldValue>(parent, SWT.NONE, LABEL_PROVIDER, true);
			multiSelectionEditComposite.addModificationListener(getModifyListener());
		}
		
		return multiSelectionEditComposite;
	}

	@Override
	public void doRefresh() {
		MultiSelectionDataField df = getDataField();
		MultiSelectionStructField sf = (MultiSelectionStructField) getStructField();
		
//		if (multiSelectionEditComposite != null)
//			multiSelectionEditComposite.refresh();
		
		multiSelectionEditComposite.setInput(Collections.unmodifiableCollection(sf.getStructFieldValues()), Collections.unmodifiableCollection(df.getStructFieldValues()));
	}

	public Control getControl() {
		return multiSelectionEditComposite;
	}

	public void updatePropertySet()	{
		getDataField().setSelection(multiSelectionEditComposite.getSelection());
	}

	@Override
	protected IEntryEditor getEntryViewer() {
		return multiSelectionEditComposite;
	}
}


