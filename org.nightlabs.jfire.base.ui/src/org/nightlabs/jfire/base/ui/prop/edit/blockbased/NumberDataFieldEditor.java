/**
 *
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.edit.NumberEditComposite;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.language.LanguageCf;
import org.nightlabs.util.NLLocale;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class NumberDataFieldEditor extends AbstractDataFieldEditor<NumberDataField> {

	public static class Factory extends AbstractDataFieldEditorFactory<NumberDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

//		@Override
//		public Class<? extends DataFieldEditor<NumberDataField>> getDataFieldEditorClass() {
//			return NumberDataFieldEditor.class;
//		}

		@Override
		public DataFieldEditor<NumberDataField> createPropDataFieldEditor(IStruct struct, NumberDataField data) {
			return new NumberDataFieldEditor(struct, data);
		}

		@Override
		public Class<NumberDataField> getPropDataFieldType() {
			return NumberDataField.class;
		}
	}

	private LanguageCf language;
	private NumberEditComposite numberEditComposite;

	public NumberDataFieldEditor(IStruct struct, NumberDataField data) {
		super(struct, data);
		language = new LanguageCf(NLLocale.getDefault().getLanguage());
	}

	@Override
	protected void setDataField(NumberDataField dataField) {
		super.setDataField(dataField);
	}

	@Override
	public Control createControl(Composite parent) {
		if (numberEditComposite == null) {
			numberEditComposite = new NumberEditComposite(parent);
			numberEditComposite.addModificationListener(getModifyListener());
		}

		return numberEditComposite;
//		comp = new XComposite(parent, SWT.NONE, LayoutMode.T, LayoutDataMode.GRID_DATA_HORIZONTAL);
//
//		title = new Label(comp, SWT.NONE);
//		valueSpinner = new Spinner(comp, comp.getBorderStyle());
//		valueSpinner.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				setChanged(true);
//			}
//		});
//		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, valueSpinner);
//		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, title);
//		return comp;
	}

	@Override
	public void doRefresh() {
		NumberDataField numberDataField = getDataField();
		NumberStructField numberStructField = (NumberStructField) getStructField();
//		title.setText(numberStructField.getName().getText(editor.getLanguage().getLanguageID()));

		int min, max, digits, number;
		
		if (numberStructField.isBounded()) {
			max = numberStructField.getMax();
			min = numberStructField.getMin();
		} else {
			max = Integer.MAX_VALUE;
			min = 0;
		}
		digits = numberStructField.getDigits();
		number = numberDataField.getIntValue();
		
		numberEditComposite.setContent(number, min, max, digits);
//		NumberDataField numberDataField = getDataField();
//		NumberStructField numberStructField = (NumberStructField) getStructField();
//		title.setText(numberStructField.getName().getText(language.getLanguageID()));
//
//		if (numberStructField.isBounded()) {
//			valueSpinner.setMaximum(numberStructField.getSpinnerMax());
//			valueSpinner.setMinimum(numberStructField.getSpinnerMin());
//		} else {
//			valueSpinner.setMaximum(Integer.MAX_VALUE);
//			valueSpinner.setMinimum(0);
//		}
//		valueSpinner.setDigits(numberStructField.getDigits());
//
//		valueSpinner.setSelection(numberDataField.getIntValue());
	}

	public Control getControl() {
		return numberEditComposite;
	}

	public void updatePropertySet() {
		if (!isChanged())
			return;

		getDataField().setValue(numberEditComposite.getIntValue());
	}

	public LanguageCf getLanguage() {
		return language;
	}

	@Override
	protected IEntryEditor getEntryViewer() {
		return numberEditComposite;
	}
}

