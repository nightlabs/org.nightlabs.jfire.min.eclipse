package org.nightlabs.jfire.base.ui.edit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.ui.composite.XComposite;

public class NumberEditComposite extends AbstractInlineEditComposite {

	private Spinner valueSpinner;

	public NumberEditComposite(Composite parent) {
		super(parent, SWT.NONE);

//		this.editor = _editor;
//		setLayout(createLayout());

//		title = new Label(this, SWT.NONE);
//		title.setLayoutData(createLabelLayoutData());
		valueSpinner = new Spinner(this, this.getBorderStyle());
		valueSpinner.setLayoutData(createSpinnerLayoutData());

		valueSpinner.addModifyListener(getSwtModifyListener());

		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, valueSpinner);
//		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, title);
	}

	protected GridData createLabelLayoutData() {
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.grabExcessHorizontalSpace = true;
		return nameData;
	}

	protected GridData createSpinnerLayoutData() {
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		return textData;
	}

	protected int getTextBorderStyle() {
		return getBorderStyle();
	}
	
	public void setIntValue(int intValue) {
		valueSpinner.setSelection(intValue);
	}
	
	public void setContent(int intValue, int min, int max, int digits) {
		valueSpinner.setMinimum(min);
		valueSpinner.setMaximum(max);
		valueSpinner.setDigits(digits);
		valueSpinner.setSelection(intValue);
	}
	
	public int getIntValue() {
		return valueSpinner.getSelection();
	}

//	@Override
//	public void _refresh() {
//		NumberDataField numberDataField = getEditor().getDataField();
//		NumberStructField numberStructField = (NumberStructField) getEditor().getStructField();
////		title.setText(numberStructField.getName().getText(editor.getLanguage().getLanguageID()));
//
//		if (numberStructField.isBounded()) {
//			valueSpinner.setMaximum(numberStructField.getMax());
//			valueSpinner.setMinimum(numberStructField.getMin());
//		} else {
//			valueSpinner.setMaximum(Integer.MAX_VALUE);
//			valueSpinner.setMinimum(0);
//		}
//		valueSpinner.setDigits(numberStructField.getDigits());
//
//
//		valueSpinner.setSelection(numberDataField.getIntValue());
//	}
//
//	public int getSpinnerValue() {
//		return valueSpinner.getSelection();
//	}

	@Override
	public void dispose() {
		valueSpinner.removeModifyListener(getSwtModifyListener());
		super.dispose();
	}
}

