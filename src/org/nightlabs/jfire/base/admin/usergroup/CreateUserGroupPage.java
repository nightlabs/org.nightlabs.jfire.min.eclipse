/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.admin.usergroup;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.Formular;
import org.nightlabs.base.ui.composite.FormularChangeListener;
import org.nightlabs.base.ui.composite.FormularChangedEvent;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.admin.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.resource.Messages;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateUserGroupPage extends WizardPage implements FormularChangeListener
{
	private Text userGroupID;
	private Text name;
	private Text description;

	public CreateUserGroupPage() 
	{
		super(CreateUserGroupPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.usergroup.CreateUserGroupPage.title"), null); //$NON-NLS-1$
		setImageDescriptor(
				SharedImages.getWizardPageImageDescriptor(
						BaseAdminPlugin.getDefault(), 
						CreateUserGroupPage.class
				));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.usergroup.CreateUserGroupPage.description")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) 
	{
		Formular f = new Formular(parent, SWT.NONE, this);
		userGroupID = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.usergroup.CreateUserGroupPage.userGroupID.labelText"), null); //$NON-NLS-1$
		name = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.usergroup.CreateUserGroupPage.name.labelText"), null); //$NON-NLS-1$
		description = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.usergroup.CreateUserGroupPage.description.labelText"), null); //$NON-NLS-1$

		verifyInput();
		setControl(f);
	}

	private void verifyInput()
	{
		try {
			if("".equals(getUserGroupID()))  //$NON-NLS-1$
				updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.usergroup.CreateUserGroupPage.errorUserGroupIDMissing")); //$NON-NLS-1$
			else {
				UserManager userManager = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
				if (userManager.userIDAlreadyRegistered(UserID.create(Login.getLogin().getLoginContext().getOrganisationID(), getUserGroupID())))
					updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.usergroup.CreateUserGroupPage.errorUserGroupIDConflict")); //$NON-NLS-1$
				else
					updateStatus(null);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void updateStatus(String message) 
	{
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * @return Returns the userGroupID.
	 */
	public String getUserGroupID()
	{
		return userGroupID.getText();
	}

	/**
	 * @return Returns the description.
	 */
	public String getUserGroupDescription()
	{
		return description.getText();
	}
	/**
	 * @return Returns the name.
	 */
	public String getUserGroupName()
	{
		return name.getText();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.composite.FormularChangeListener#formularChanged(org.nightlabs.base.ui.composite.FormularChangedEvent)
	 */
	public void formularChanged(FormularChangedEvent event)
	{
		verifyInput();
	}
}
