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

package org.nightlabs.jfire.base.admin.ui.organisation.registration;

import org.eclipse.jface.wizard.Wizard;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RegisterOrganisationWizard extends Wizard
{
	private RegisterOrganisationPage registerOrganisationPage;

	public RegisterOrganisationWizard()
	{
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages()
	{
		registerOrganisationPage = new RegisterOrganisationPage();
		addPage(registerOrganisationPage);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish()
	{
		try {
			OrganisationManagerRemote organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, Login.getLogin().getInitialContextProperties());
			organisationManager.beginRegistration(
					registerOrganisationPage.getAnonymousInitialContextFactory(),
					registerOrganisationPage.getInitialContextURL(),
					registerOrganisationPage.getOrganisationID());
			return true;
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

}
