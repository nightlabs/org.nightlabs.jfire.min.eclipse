package org.nightlabs.jfire.base.ui.edit;

import org.eclipse.swt.widgets.Composite;

// TODO Add validation again
public class RegexEditComposite
extends TextEditComposite
{
	public RegexEditComposite(Composite parent, int style, int lineCount) {
		super(parent, style, 1);
	}
}

//public class RegexDataFieldEditor extends AbstractDataFieldEditor<RegexDataField> {
//
//	public static class Factory extends AbstractDataFieldEditorFactory<RegexDataField> {
//
//		/**
//		 * {@inheritDoc}
//		 */
//		@Override
//		public String[] getEditorTypes() {
//			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
//		}
//
////		@Override
////		public Class<? extends DataFieldEditor<RegexDataField>> getDataFieldEditorClass() {
////			return RegexDataFieldEditor.class;
////		}
//
//		@Override
//		public Class<RegexDataField> getPropDataFieldType() {
//			return RegexDataField.class;
//		}
//
//		@Override
//		public DataFieldEditor<RegexDataField> createPropDataFieldEditor(IStruct struct, RegexDataField data) {
//			return new RegexDataFieldEditor(struct, data);
//		}
//	}
//
//	private LanguageCf language;
//	private XComposite comp;
//	private Label title;
//	private Text valueText;
//	private boolean modified = false;
//	private boolean ignoreModify = false;
//
//	private RegexDataField regexDataField;
//	private RegexStructField regexStructField;
//
//	public RegexDataFieldEditor(IStruct struct, RegexDataField data) {
//		super(struct, data);
//		language = new LanguageCf(NLLocale.getDefault().getLanguage());
//	}
//
//	@Override
//	protected void setDataField(RegexDataField dataField) {
//		super.setDataField(dataField);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
//	 */
//	@Override
//	public Control createControl(Composite parent) {
//		comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
//		comp.getGridLayout().horizontalSpacing = 0;
//// TODO: this is a quickfix for the Formtoolkit Boarderpainter, which paints to the
//// 	outside of the elements -> there needs to be space in the enclosing composite for the borders
//		comp.getGridLayout().verticalSpacing = 2;
//		comp.getGridLayout().marginHeight = 2;
//		comp.getGridLayout().marginWidth = 2;
//		title = new Label(comp, SWT.NONE);
//		valueText = new Text(comp, comp.getBorderStyle());
//		valueText.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				if (!ignoreModify) {
//					modified = true;
//					setChanged(modified);
//				}
//
//			}
//		});
//
////		// TODO: Validation disabled, see https://www.jfire.org/modules/bugs/view.php?id=692
////		valueText.addFocusListener(new FocusListener() {
////			public void focusGained(FocusEvent e) {}
////			public void focusLost(FocusEvent e) {
////				if (regexStructField != null) {
////					String text = valueText.getText();
////					if (!regexStructField.validateValue(text)) {
////						MessageBox box = new MessageBox(RCPUtil.getActiveShell(), SWT.OK);
////						box.setMessage(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.RegexDataFieldEditor.invalidInputMessageBox.message")); //$NON-NLS-1$
////						box.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.RegexDataFieldEditor.invalidInputMessageBox.text")); //$NON-NLS-1$
////						box.open();
////					} else if (modified) {
////						setChanged(true);
////					}
////				}
////			}
////		});
//
//		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, valueText);
//		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, title);
//		return comp;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#doRefresh()
//	 */
//	@Override
//	public void doRefresh() {
//		regexDataField = getDataField();
//		regexStructField = (RegexStructField) getStructField();
//		title.setText(regexStructField.getName().getText(language.getLanguageID()));
//		valueText.setToolTipText(
//				String.format(
//						Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.RegexDataFieldEditor.valueText.toolTipText"), //$NON-NLS-1$
//						new Object[] { regexStructField.getRegex() }
//				)
//		);
//		ignoreModify = true;
//		if (!regexDataField.isEmpty())
//			valueText.setText(regexDataField.getText());
//		else
//			valueText.setText("");
//		ignoreModify = false;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getControl()
//	 */
//	public Control getControl() {
//		return comp;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#updateProp()
//	 */
//	public void updatePropertySet() {
//		if (!isChanged())
//			return;
//
//		String text = valueText.getText();
//
////		// TODO: Validation disabled, see https://www.jfire.org/modules/bugs/view.php?id=692
////		if (regexStructField.validateValue(text)) {
////				regexDataField.setText(text);
////		}
//		regexDataField.setText(text);
//		// END Validation disabled
//
//		modified = false;
//	}
//
//	public LanguageCf getLanguage() {
//		return language;
//	}
//
//	@Override
//	protected IEntryEditor getEntryViewer() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}
//

