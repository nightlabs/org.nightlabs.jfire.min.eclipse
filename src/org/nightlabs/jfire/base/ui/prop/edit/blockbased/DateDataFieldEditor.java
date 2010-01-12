package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.edit.DateEditComposite;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.structfield.DateStructField;

/**
 * A data field editor for dates.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DateDataFieldEditor extends AbstractDataFieldEditor<DateDataField> {
	
	private DateEditComposite dateEditComposite;
	
	public DateDataFieldEditor(IStruct struct, DateDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<DateDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

		@Override
		public DataFieldEditor<DateDataField> createPropDataFieldEditor(IStruct struct, DateDataField data) {
			return new DateDataFieldEditor(struct, data);
		}

		public Class<DateDataField> getPropDataFieldType() {
			return DateDataField.class;
		}
	}
	
	@Override
	public Control createControl(Composite parent) {
		if (dateEditComposite == null) {
			DateStructField sf = (DateStructField) getStructField();
			dateEditComposite = new DateEditComposite(parent, sf.getDateTimeEditFlags());
			dateEditComposite.addModificationListener(getModifyListener());
		}
		
		return dateEditComposite;
	}

	@Override
	public void doRefresh() {
		if (dateEditComposite != null && getStructField() != null && getDataField() != null) {
			DateDataField ddf = getDataField();
//			DateStructField dsf = (DateStructField) getStructField();
			dateEditComposite.setInput(ddf.getDate());
		}
	}
	
	public Control getControl() {
		return dateEditComposite;
	}

	public void updatePropertySet() {
		if (!isChanged())
			return;
		
		getDataField().setDate(dateEditComposite.getDate());
	}

	@Override
	protected IEntryEditor getEntryViewer() {
		return dateEditComposite;
	}
}



