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

package org.nightlabs.jfire.base.admin.ui.user;

import org.eclipse.jface.wizard.Wizard;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class CreateUserWizard extends Wizard
{
	private CreateUserPage cuPage;

	public CreateUserWizard()
	{
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserWizard.windowTitle")); //$NON-NLS-1$
	}

	public void addPages() 
	{
		cuPage = new CreateUserPage();
		addPage(cuPage);
	}

	public boolean performFinish()
	{
		try {
			User newUser = new User(Login.getLogin().getOrganisationID(), cuPage.getUserID());
			newUser.setName(cuPage.getUserName());
			newUser.setDescription(cuPage.getUserDescription());

			UserManager userManager = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			userManager.saveUser(newUser, cuPage.getPassword1());
			return true;
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
