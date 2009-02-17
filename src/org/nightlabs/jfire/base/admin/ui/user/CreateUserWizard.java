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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedPropertySetEditorWizardHop;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class CreateUserWizard extends DynamicPathWizard implements INewWizard
{
	private CreateUserPage cuPage;
	private BlockBasedPropertySetEditorWizardHop propertySetEditorWizardHop;
	private boolean canFinish = false;

	private UserID createdUserID;
	
	public CreateUserWizard()
	{
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserWizard.windowTitle")); //$NON-NLS-1$
	}

	@Override
	public void addPages()
	{
		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
		StructLocal personStruct = StructLocalDAO.sharedInstance().getStructLocal(
				Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE, new NullProgressMonitor());
		person.inflate(personStruct);
		
		cuPage = new CreateUserPage() {
			@Override
			public void onHide() {
				canFinish = true;
			}
		};
		addPage(cuPage);
		
		propertySetEditorWizardHop = new BlockBasedPropertySetEditorWizardHop(person);
		String msg = Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserWizard.label.message"); //$NON-NLS-1$
		propertySetEditorWizardHop.addWizardPage(null, Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserWizard.page.title.remainigData"), Messages.getString("org.nightlabs.jfire.base.admin.ui.user.CreateUserWizard.page.title.additionalData"), msg); //$NON-NLS-1$ //$NON-NLS-2$
		addPage(propertySetEditorWizardHop.getEntryPage());
	}

	@Override
	public boolean performFinish()
	{
		try {
			UserID userID = UserID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), cuPage.getUserID());
			User newUser = new User(userID.organisationID, userID.userID);
			newUser.setName(cuPage.getUserName());
			newUser.setDescription(cuPage.getUserDescription());
			newUser.setAutogenerateName(cuPage.isAutogenerateName());

			newUser.setPerson((Person)propertySetEditorWizardHop.getPropertySet());
			newUser.getPerson().deflate();

//			JFireSecurityManager userManager = JFireEjbFactory.getBean(JFireSecurityManager.class, Login.getLogin().getInitialContextProperties());
////			userManager.saveUser(newUser, cuPage.getPassword1());
//			userManager.storeUser(newUser, cuPage.getPassword1(), false, null, 1);
			UserDAO.sharedInstance().storeUser(newUser, cuPage.getPassword1(), false, null, 1, new NullProgressMonitor()); // TODO do this asynchronously in a job!
			createdUserID = userID;
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
	
	public UserID getCreatedUserID() {
		return createdUserID;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// do nothing
	}
}
