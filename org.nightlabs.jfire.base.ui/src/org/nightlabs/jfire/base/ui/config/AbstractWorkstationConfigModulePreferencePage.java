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

package org.nightlabs.jfire.base.ui.config;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationResolveStrategy;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class AbstractWorkstationConfigModulePreferencePage
extends AbstractConfigModulePreferencePage
{
	private static final Logger logger = Logger.getLogger(AbstractWorkstationConfigModulePreferencePage.class);
	
	/**
	 * 
	 */
	public AbstractWorkstationConfigModulePreferencePage() {
		super();
	}

	/**
	 * @param title
	 */
	public AbstractWorkstationConfigModulePreferencePage(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public AbstractWorkstationConfigModulePreferencePage(String title,
			ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Returns the cfModID part of the ConfigID the ConfigModule should be edited for.
	 * <p>
	 * The default implementation returns <code>null</code>.
	 * </p>
	 * @return The cfModID part of the ConfigID the ConfigModule should be edited for.
	 */
	protected String getConfigModuleID() {
		return null;
	}
	
	/**
	 * Sets the current ConfigID to the workstation of currently logged in user.
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		try {
			Login login = Login.getLogin();
			getConfigModuleController().setConfigID(
					WorkstationConfigSetup.getWorkstationConfigID(
						Workstation.getWorkstationID(
							login.getOrganisationID(), login.getWorkstationID(), WorkstationResolveStrategy.FALLBACK)),
							false, getConfigModuleID());
		} catch (Exception e) {
			logger.info("User decided to work offline!"); //$NON-NLS-1$
		}
		super.init(workbench);
	}
	
}
