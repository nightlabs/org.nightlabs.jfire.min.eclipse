//package org.nightlabs.jfire.base.ui.edit;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.widgets.Spinner;
//import org.nightlabs.base.ui.composite.XComposite;
//import org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldEditComposite;
//import org.nightlabs.jfire.base.ui.prop.edit.blockbased.NumberDataFieldEditor;
//import org.nightlabs.jfire.prop.datafield.NumberDataField;
//import org.nightlabs.jfire.prop.structfield.NumberStructField;
//
//class NumberDataFieldComposite extends AbstractInlineDataFieldEditComposite<NumberDataFieldEditor> {
//
////	private Label title;
//	private Spinner valueSpinner;
////	private NumberDataFieldEditor editor;
//	private ModifyListener modifyListener;
//
//	public NumberDataFieldComposite(Composite parent, final NumberDataFieldEditor _editor) {
//		super(parent, SWT.NONE, _editor);
//		if (!(parent.getLayout() instanceof GridLayout))
//			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$
//
////		this.editor = _editor;
////		setLayout(createLayout());
//
////		title = new Label(this, SWT.NONE);
////		title.setLayoutData(createLabelLayoutData());
//		valueSpinner = new Spinner(this, this.getBorderStyle());
//		valueSpinner.setLayoutData(createSpinnerLayoutData());
//
//		modifyListener = new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				_editor.setChanged(true);
//			}
//		};
//		valueSpinner.addModifyListener(modifyListener);
//
//		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, valueSpinner);
////		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, title);
//	}
//
//	protected GridData createLabelLayoutData() {
//		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
//		nameData.grabExcessHorizontalSpace = true;
//		return nameData;
//	}
//
//	protected GridData createSpinnerLayoutData() {
//		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
//		textData.grabExcessHorizontalSpace = true;
//		return textData;
//	}
//
//	protected int getTextBorderStyle() {
//		return getBorderStyle();
//	}
//
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
//
//	@Override
//	public void dispose() {
//		valueSpinner.removeModifyListener(modifyListener);
//		super.dispose();
//	}
//}

