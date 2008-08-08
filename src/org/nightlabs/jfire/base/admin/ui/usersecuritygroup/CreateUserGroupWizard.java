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

package org.nightlabs.jfire.base.admin.ui.usersecuritygroup;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.UserSecurityGroupDAO;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class CreateUserGroupWizard extends DynamicPathWizard
{
	private CreateUserGroupPage cugPage;
	private UserSecurityGroupID createdUserSecurityGroupID;
//	private BlockBasedPropertySetEditorWizardHop propertySetEditorWizardHop;

	public CreateUserGroupWizard()
	throws LoginException, NamingException, RemoteException, CreateException
	{
		super();

		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.usersecuritygroup.CreateUserGroupWizard.windowTitle")); //$NON-NLS-1$
	}

	@Override
	public void addPages()
	{
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		StructLocal personStruct = StructLocalDAO.sharedInstance().getStructLocal(
				Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE, new NullProgressMonitor());
		person.inflate(personStruct);
		cugPage = new CreateUserGroupPage();
		addPage(cugPage);
		
//		Tobias: We don't want a usergroup to have a person, thus we prohibit editing here :)
//		propertySetEditorWizardHop = new BlockBasedPropertySetEditorWizardHop(person);
//		String msg = "Here you can edit all information for the selected contact";
//		propertySetEditorWizardHop.addWizardPage(null, "RemainingData", "Remaining data", msg);
//		addPage(propertySetEditorWizardHop.getEntryPage());
	}

	@Override
	public boolean performFinish()
	{
		try {
			UserSecurityGroupID groupID = UserSecurityGroupID.create(
					SecurityReflector.getUserDescriptor().getOrganisationID(),
					cugPage.getUserGroupID());
			UserSecurityGroup newGroup = new UserSecurityGroup(
					groupID.organisationID, groupID.userSecurityGroupID);
			newGroup.setName(cugPage.getUserName());
			newGroup.setDescription(cugPage.getUserGroupDescription());
			
//			newGroup.setPerson((Person)propertySetEditorWizardHop.getPropertySet());
//			newGroup.getPerson().deflate();
			
//			JFireSecurityManager userManager = JFireSecurityManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
////			userManager.saveUser(newGroup, null);
//			userManager.storeUser(newGroup, null, false, null, 1);
			UserSecurityGroupDAO.sharedInstance().storeUserSecurityGroup(
					newGroup,
					false,
					(String[]) null,
					1,
					new NullProgressMonitor()); // TODO do this asynchronously in a job!
			
			createdUserSecurityGroupID = groupID;
			return true;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public UserSecurityGroupID getCreatedUserSecurityGroupID() {
		return createdUserSecurityGroupID;
	}
}
