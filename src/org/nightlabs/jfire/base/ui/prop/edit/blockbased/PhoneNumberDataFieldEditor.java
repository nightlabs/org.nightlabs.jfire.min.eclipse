package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;

public class PhoneNumberDataFieldEditor
extends AbstractDataFieldEditor<PhoneNumberDataField> {

	public PhoneNumberDataFieldEditor(IStruct struct, PhoneNumberDataField data) {
		super(struct, data);
	}

	public static class PhoneNumberDataFieldEditorFactory extends AbstractDataFieldEditorFactory<PhoneNumberDataField> {
		//		@Override
		//		public Class<? extends DataFieldEditor<PhoneNumberDataField>> getDataFieldEditorClass() {
		//			return PhoneNumberDataFieldEditor.class;
		//		}

		@Override
		public DataFieldEditor<PhoneNumberDataField> createPropDataFieldEditor(IStruct struct, PhoneNumberDataField data) {
			return new PhoneNumberDataFieldEditor(struct, data);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

		@Override
		public Class<PhoneNumberDataField> getPropDataFieldType() {
			return PhoneNumberDataField.class;
		}

	}

	PhoneNumberDataFieldEditorComposite comp;

	@Override
	public Control createControl(Composite parent) {
		comp = new PhoneNumberDataFieldEditorComposite(parent, getSwtModifyListener());
		return comp;
	}

	@Override
	public void doRefresh() {
		PhoneNumberDataField dataField = getDataField();
		comp.countryCodeTextBox.setText(dataField.getCountryCode() != null ? dataField.getCountryCode() : ""); //$NON-NLS-1$
		comp.areaCodeTextBox.setText(dataField.getAreaCode() != null ? dataField.getAreaCode() : ""); //$NON-NLS-1$
		comp.localNumberTextBox.setText(dataField.getLocalNumber() != null ? dataField.getLocalNumber() : ""); //$NON-NLS-1$
		comp.setTitle(getStructField().getName().getText());
		//		comp.handleManagedBy(dataField.getManagedBy());
	}

	public Control getControl() {
		return comp;
	}

	public void updatePropertySet() {
		PhoneNumberDataField dataField = getDataField();
		dataField.setCountryCode(comp.countryCodeTextBox.getText());
		dataField.setAreaCode(comp.areaCodeTextBox.getText());
		dataField.setLocalNumber(comp.localNumberTextBox.getText());
	}

	@Override
	protected IEntryEditor getEntryViewer() {
		return comp;
	}
}

class PhoneNumberDataFieldEditorComposite
extends XComposite
implements IEntryEditor
{
	protected Text countryCodeTextBox;
	protected Text areaCodeTextBox;
	protected Text localNumberTextBox;
	protected Group compGroup;

	public PhoneNumberDataFieldEditorComposite(Composite parent, ModifyListener modifyListener) {
		super (parent, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 1);

		getGridLayout().horizontalSpacing = 0;
		getGridLayout().marginLeft = 0;
		getGridLayout().marginRight = 0;

		compGroup = new Group(this, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.numColumns = 6;
		gl.horizontalSpacing = 1;
		gl.marginLeft = 0;
		gl.marginRight = 0;
		compGroup.setLayout(gl);

		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, compGroup);

		new Label(compGroup, SWT.NONE).setText("+ "); //$NON-NLS-1$
		countryCodeTextBox = new Text(compGroup, getBorderStyle());
		GridData gd = new GridData();
		gd.widthHint = 30;
		countryCodeTextBox.setLayoutData(gd);
		gd = new GridData();
		gd.widthHint = 60;
		new Label(compGroup, SWT.NONE).setText(" - "); //$NON-NLS-1$
		areaCodeTextBox = new Text(compGroup, getBorderStyle());
		areaCodeTextBox.setLayoutData(gd);
		new Label(compGroup, SWT.NONE).setText(" - "); //$NON-NLS-1$
		localNumberTextBox = new Text(compGroup, getBorderStyle());
		//XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, areaCodeTextBox);
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, localNumberTextBox);

//		ModifyListener modifyListener = new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				phoneNumberDataFieldEditor.setChanged(true);
//			}
//		};
		countryCodeTextBox.addModifyListener(modifyListener);
		areaCodeTextBox.addModifyListener(modifyListener);
		localNumberTextBox.addModifyListener(modifyListener);
	}

	@Override
	public void setEnabledState(boolean enabled, String tooltip) {
		//		compGroup.setEnabled(managedBy == null);
		countryCodeTextBox.setEditable(enabled);
		areaCodeTextBox.setEditable(enabled);
		localNumberTextBox.setEditable(enabled);
		
		if (!enabled)
			setToolTipText(tooltip);
		else
			setToolTipText(null);
	}
	
	@Override
	public void setTitle(String title) {
		compGroup.setText(title);
	}
}
