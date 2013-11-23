package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.edit.BooleanEditComposite;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.BooleanDataField;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class BooleanDataFieldEditor 
extends AbstractDataFieldEditor<BooleanDataField> 
{
	public static class Factory extends AbstractDataFieldEditorFactory<BooleanDataField> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

		@Override
		public DataFieldEditor<BooleanDataField> createPropDataFieldEditor(IStruct struct, BooleanDataField data) {
			return new BooleanDataFieldEditor(struct, data);
		}

		@Override
		public Class<BooleanDataField> getPropDataFieldType() {
			return BooleanDataField.class;
		}
	}
	
	private BooleanEditComposite booleanEditComposite;
	
	/**
	 * @param struct
	 * @param data
	 */
	public BooleanDataFieldEditor(IStruct struct, BooleanDataField data) {
		super(struct, data);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		BooleanDataField dataField = getDataField();
		booleanEditComposite.setValue(dataField.getValue());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) 
	{
		if (booleanEditComposite == null) {
			booleanEditComposite = new BooleanEditComposite(parent, SWT.NONE);
			booleanEditComposite.addModificationListener(getModifyListener());
		}
		return booleanEditComposite;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getControl()
	 */
	@Override
	public Control getControl() {
		return booleanEditComposite;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#updatePropertySet()
	 */
	@Override
	public void updatePropertySet() {
		if (!isChanged())
			return;

		getDataField().setValue(booleanEditComposite.getValue());
	}

	@Override
	protected IEntryEditor getEntryViewer() {
		return booleanEditComposite;
	}

}
