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
package org.nightlabs.jfire.base.admin.editor.user;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.resource.Messages;

/**
 * An editor page for security related user stuff, i.e. user
 * groups and role groups.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SecurityPreferencesPage extends EntityEditorPageWithProgress
{
	/**
	 * The id of this page.
	 */
	public static final String ID_PAGE = SecurityPreferencesPage.class.getName();
	
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link SecurityPreferencesPage}. 
	 */
	public static class Factory implements IEntityEditorPageFactory {

		public IFormPage createPage(FormEditor formEditor) {
			return new SecurityPreferencesPage(formEditor);
		}
		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new SecurityPreferencesController(editor);
		}
		
	}
	
	UserGroupsSection userGroupsSection;
	RoleGroupsSection roleGroupsSection;

	/**
	 * Create an instance of SecurityPreferencesPage.
	 * <p>
	 * This constructor is used by the entity editor
	 * page extension system.
	 * 
	 * @param editor The editor for which to create this
	 * 		form page. 
	 */
	public SecurityPreferencesPage(FormEditor editor)
	{
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.editor.user.SecurityPreferencesPage.pageTitle")); //$NON-NLS-1$
	}

	@Override
	protected void addSections(Composite parent) {
		userGroupsSection = new UserGroupsSection(this, parent);
 		getManagedForm().addPart(userGroupsSection);
 		roleGroupsSection = new RoleGroupsSection(this, parent);
 		getManagedForm().addPart(roleGroupsSection);
	}

	@Override
	protected void asyncCallback() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				SecurityPreferencesModel model = ((SecurityPreferencesController)getPageController()).getModel();
				userGroupsSection.setModel(model);
				roleGroupsSection.setModel(model);
				switchToContent();
			}
		});
	}
	
	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.editor.user.SecurityPreferencesPage.pageFormTitle"); //$NON-NLS-1$
	}
}
