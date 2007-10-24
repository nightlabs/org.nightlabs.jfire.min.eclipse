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

package org.nightlabs.jfire.base.admin.ui.usergroup;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.eclipse.jface.wizard.Wizard;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.admin.ui.user.CreateUserPage;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedPropertySetEditorWizardHop;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class CreateUserGroupWizard extends DynamicPathWizard
{
	private CreateUserGroupPage cugPage;
	private boolean canFinish = false;
	private BlockBasedPropertySetEditorWizardHop propertySetEditorWizardHop;

	public CreateUserGroupWizard()
	throws LoginException, NamingException, RemoteException, CreateException
	{
		super();

		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.usergroup.CreateUserGroupWizard.windowTitle")); //$NON-NLS-1$
	}

	public void addPages() 
	{
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		StructLocal personStruct = StructLocalDAO.sharedInstance().getStructLocal(Person.class, StructLocal.DEFAULT_SCOPE, new NullProgressMonitor());
		person.inflate(personStruct);
		
		cugPage = new CreateUserGroupPage() {
			@Override
			public void onHide() {
				canFinish = true;
			}
		};
		addPage(cugPage);
		
		propertySetEditorWizardHop = new BlockBasedPropertySetEditorWizardHop(person);
		String msg = "Here you can edit all information for the selected contact";
		propertySetEditorWizardHop.addWizardPage(null, "RemainingData", "Remaining data", msg);
		addPage(propertySetEditorWizardHop.getEntryPage());
	}

	public boolean performFinish()
	{
		try {
			UserGroup newGroup = new UserGroup(Login.getLogin().getOrganisationID(), User.USERID_PREFIX_TYPE_USERGROUP + cugPage.getUserGroupID());
			newGroup.setDescription(cugPage.getUserGroupDescription());
			
			newGroup.setPerson((Person)propertySetEditorWizardHop.getPropertySet());
			newGroup.getPerson().deflate();
			
			UserManager userManager = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			userManager.saveUser(newGroup, null);
			return true;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean canFinish() {
		return canFinish && super.canFinish();
	}
}
