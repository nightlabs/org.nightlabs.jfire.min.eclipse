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

package org.nightlabs.jfire.base.admin.ui.workstation;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationManagerRemote;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateWorkstationWizard extends DynamicPathWizard implements INewWizard
{
	private CreateWorkstationPage createWorkstationPage;
	private WorkstationID createdWorkstationID;

	public CreateWorkstationWizard() {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationWizard.wizardTitle")); //$NON-NLS-1$
		createWorkstationPage = new CreateWorkstationPage(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationWizard.pageTitle")); //$NON-NLS-1$
		addPage(createWorkstationPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		Workstation workstation;
		try {
			WorkstationID workstationID = WorkstationID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), createWorkstationPage.getWorkstationID());
			workstation = new Workstation(workstationID.organisationID, workstationID.workstationID);
			workstation.setDescription(createWorkstationPage.getWorkstationDescription());

			WorkstationManagerRemote workstationManager = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, Login.getLogin().getInitialContextProperties());
			workstationManager.storeWorkstation(workstation, false, null, -1);
			createdWorkstationID = workstationID;
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public WorkstationID getCreatedWorkstationID() {
		return createdWorkstationID;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// do nothing
	}
}
