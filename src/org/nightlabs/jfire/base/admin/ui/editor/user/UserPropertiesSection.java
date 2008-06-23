/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.prop.ValidationUtil;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.ValidationResultManager;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * The section containing the person editor controls
 * for the {@link PersonPreferencesPage}.
 *
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @deprecated As far as I can see, this is not in use anymore. BlockBasedEditorSection is used instead
 */
@Deprecated
public class UserPropertiesSection extends RestorableSectionPart
{
	/**
	 * The person editor used in this section.
	 */
	BlockBasedEditor blockBasedPersonEditor;

	/**
	 * The person editor control showed in this section.
	 */
	Control blockBasedPersonEditorControl;

	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UserPropertiesSection(FormPage page, Composite parent)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave)
	{
		super.commit(onSave);
		blockBasedPersonEditor.updatePropertySet();
	}

	private static String VALIDATION_RESULT_MESSAGE_KEY = "validationResultMessageKey"; //$NON-NLS-1$

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit)
	{
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserPropertiesSection.sectionTitle")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);

		blockBasedPersonEditor = new BlockBasedEditor(false);
		blockBasedPersonEditor.setValidationResultManager(new ValidationResultManager() {
			@Override
			public void setValidationResult(ValidationResult validationResult) {
				IMessageManager messageManager = getManagedForm().getMessageManager();
				if (validationResult == null) {
					messageManager.removeMessage(VALIDATION_RESULT_MESSAGE_KEY);
				} else {
					int type = ValidationUtil.getIMessageProviderType(validationResult.getType());
					messageManager.addMessage(VALIDATION_RESULT_MESSAGE_KEY, validationResult.getMessage(), null, type);
				}
			}
		});
		blockBasedPersonEditorControl = blockBasedPersonEditor.createControl(container, false);
		//((GroupedContentComposite)blockBasedPersonEditorControl).addGroupedContentProvider(new WhateverGroupedContentProvider(), 0);
		blockBasedPersonEditorControl.setLayoutData(new GridData(GridData.FILL_BOTH));

		blockBasedPersonEditor.setChangeListener(new DataBlockEditorChangedListener() {
			public void dataBlockEditorChanged(AbstractDataBlockEditor dataBlockEditor, DataFieldEditor<?> dataFieldEditor) {
				markDirty();
			}
		});
	}

//	public void setUser(final User user) {
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				if(user.getPerson() == null)
//					user.setPerson(new Person(user.getOrganisationID(), Property.TEMPORARY_PROP_ID));
//				IStruct struct = StructLocalDAO.sharedInstance().getStructLocal(Person.class.getName());
//				struct.explodeProperty(user.getPerson());
//				blockBasedPersonEditor.setProperty(user.getPerson(), struct, true);
//			}
//		});
//	}
}
