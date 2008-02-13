/**
 * 
 */
package org.nightlabs.jfire.base.ui.person.edit.blockbased.special;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.LabeledText;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockWizardPage;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PersonPersonalDataWizardPage
extends DataBlockWizardPage
implements DataBlockEditorChangedListener
{
	private LabeledText displayName;
	private Button autoCreateDisplayName;
	private String structLocalScope;
	
	/**
	 * @param pageName
	 */
	public PersonPersonalDataWizardPage(String pageName, String title, Person person) {
		super(pageName, title, person.getStructure(), person, PersonStruct.PERSONALDATA);
		if (person.getStructure() instanceof StructLocal) {
			this.structLocalScope = ((StructLocal) person.getStructure()).getScope();
		}
	}

	/**
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		createWrapper(parent);
		
		displayName = new LabeledText(getWrapperComp(), Messages.getString("org.nightlabs.jfire.base.ui.person.edit.blockbased.special.PersonPersonalDataWizardPage.displayName.caption")); //$NON-NLS-1$
		GridData displayNameGD = new GridData();
//		displayNameGD.grabExcessHorizontalSpace = true;
		displayNameGD.horizontalAlignment = GridData.FILL;
//		displayNameGD.widthHint = 200;
		displayNameGD.grabExcessHorizontalSpace = true;
		displayName.setLayoutData(displayNameGD);
		
		autoCreateDisplayName = new Button(getWrapperComp(), SWT.CHECK);
		GridData autoCreateDisplayNameGD = new GridData();
//		autoCreateDisplayNameGD.grabExcessHorizontalSpace = true;
//		autoCreateDisplayNameGD.horizontalAlignment = GridData.FILL;
		autoCreateDisplayName.setLayoutData(autoCreateDisplayNameGD);
		autoCreateDisplayName.setText(Messages.getString("org.nightlabs.jfire.base.ui.person.edit.blockbased.special.PersonPersonalDataWizardPage.autoCreateDisplayName.text")); //$NON-NLS-1$
		autoCreateDisplayName.addSelectionListener(
			new SelectionListener() {

				public void widgetSelected(SelectionEvent arg0) {
					displayName.getTextControl().setEnabled(!autoCreateDisplayName.getSelection());
				}
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			}
		);
		autoCreateDisplayName.setSelection(true);
		displayName.getTextControl().setEnabled(false);
		
		Composite dummy = new Composite(getWrapperComp(),SWT.NONE);
		GridData dummyGD = new GridData();
		dummyGD.heightHint = 10;
		dummy.setLayoutData(dummyGD);
		
		setPropDataBlockEditorColumnHint(1);
		createPropDataBlockEditors();
		getDataBlockGroupEditor(PersonStruct.PERSONALDATA).addPropDataBlockEditorChangedListener(this);
		pageChanged();
		return getWrapperComp();
	}

	protected void pageChanged() {
		try {
			updatePropertySet();
			TextDataField name = (TextDataField) getDataBlockGroup(PersonStruct.PERSONALDATA).getDataBlockByIndex(0).getDataField(PersonStruct.PERSONALDATA_NAME);
			TextDataField firstName = (TextDataField) getDataBlockGroup(PersonStruct.PERSONALDATA).getDataBlockByIndex(0).getDataField(PersonStruct.PERSONALDATA_FIRSTNAME);
			TextDataField companyName = (TextDataField) getDataBlockGroup(PersonStruct.PERSONALDATA).getDataBlockByIndex(0).getDataField(PersonStruct.PERSONALDATA_COMPANY);
			displayName.getTextControl().setText(getPropertySet().getDisplayName());

			if (companyName.isEmpty() && name.isEmpty())
				updateStatus(Messages.getString("org.nightlabs.jfire.base.ui.person.edit.blockbased.special.PersonPersonalDataWizardPage.errorNameMissing")); //$NON-NLS-1$
			else if (companyName.isEmpty() && firstName.isEmpty())
				updateStatus(Messages.getString("org.nightlabs.jfire.base.ui.person.edit.blockbased.special.PersonPersonalDataWizardPage.errorFirstNameMissing")); //$NON-NLS-1$
			else
				updateStatus(null);
		} catch (Throwable t) {
			ExceptionHandlerRegistry.syncHandleException(t);
		}
	}
	
	/**
	 * Overrides and additionally sets displayName and autoCreateDisplayName.
	 * 
	 * @see org.nightlabs.jfire.base.ui.person.edit.blockbased.PersonCompoundDataBlockWizardPage#updatePerson()
	 */
	@Override
	public void updatePropertySet() {
		super.updatePropertySet();
		getPropertySet().setAutoGenerateDisplayName(autoCreateDisplayName.getSelection());
		getPropertySet().setDisplayName(displayName.getTextControl().getText(), StructLocalDAO.sharedInstance().getStructLocal(Person.class, structLocalScope, new NullProgressMonitor()));
	}

	public void dataBlockEditorChanged(AbstractDataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		pageChanged();
	}
	
}
