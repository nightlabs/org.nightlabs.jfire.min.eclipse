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

package org.nightlabs.jfire.base.admin.ui.language;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.jfire.base.ui.login.Login;

/**
 * Action opening wizard dialog for {@link ConfigureLanguageModeWizard}.
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class ConfigureLanguageModeAction implements IViewActionDelegate {

	private IViewPart viewPart;
	@Override
	public void init(IViewPart viewPart) {
		this.viewPart = viewPart;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IAction action) {
		try {
			Login.getLogin(false).setForceLogin(true);
			Login.getLogin();
			final ConfigureLanguageModeWizard wiz = new ConfigureLanguageModeWizard();
			final WizardDialog wizardDialog = new WizardDialog(viewPart.getSite().getShell(), wiz);
			wizardDialog.open();
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {}
}
