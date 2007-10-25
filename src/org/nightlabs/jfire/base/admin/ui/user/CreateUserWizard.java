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

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedPropertySetEditorWizardHop;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class CreateUserWizard extends DynamicPathWizard
{
	private CreateUserPage cuPage;
	private BlockBasedPropertySetEditorWizardHop propertySetEditorWizardHop;
	private boolean canFinish = false;

	public CreateUserWizard()
	{
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserWizard.windowTitle")); //$NON-NLS-1$
	}

	@Override
	public void addPages() 
	{
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		StructLocal personStruct = StructLocalDAO.sharedInstance().getStructLocal(Person.class, StructLocal.DEFAULT_SCOPE, new NullProgressMonitor());
		person.inflate(personStruct);
		
		cuPage = new CreateUserPage() {
			@Override
			public void onHide() {
				canFinish = true;
			}
		};
		addPage(cuPage);
		
		propertySetEditorWizardHop = new BlockBasedPropertySetEditorWizardHop(person);
		String msg = "Here you can enter additional information for the new user";
		propertySetEditorWizardHop.addWizardPage(null, "RemainingData", "Additional data", msg);
		addPage(propertySetEditorWizardHop.getEntryPage());
	}

	@Override
	public boolean performFinish()
	{
		try {
			User newUser = new User(Login.getLogin().getOrganisationID(), cuPage.getUserID());
			newUser.setName(cuPage.getUserName());
			newUser.setDescription(cuPage.getUserDescription());
			
			newUser.setPerson((Person)propertySetEditorWizardHop.getPropertySet());
			newUser.getPerson().deflate();

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
	
	@Override
	public boolean canFinish() {
		return canFinish && super.canFinish();
	}
}
