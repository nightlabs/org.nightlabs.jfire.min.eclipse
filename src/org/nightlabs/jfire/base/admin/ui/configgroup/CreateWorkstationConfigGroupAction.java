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

package org.nightlabs.jfire.base.admin.ui.configgroup;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jfire.base.admin.ui.editor.workstationgroup.WorkstationGroupEditor;
import org.nightlabs.jfire.base.admin.ui.editor.workstationgroup.WorkstationGroupEditorInput;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.action.LSDWorkbenchWindowActionDelegate;
import org.nightlabs.jfire.config.WorkstationConfigSetup;

/**
 * Action to create a new Workstation-ConfigGroup.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateWorkstationConfigGroupAction
extends  LSDWorkbenchWindowActionDelegate 
{
	@Override
	public void run(IAction action)
	{
		CreateConfigGroupWizard wiz = new CreateConfigGroupWizard(
				WorkstationConfigSetup.CONFIG_GROUP_CONFIG_TYPE_WORKSTATION_CONFIG,
				Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.CreateWorkstationConfigGroupAction.wizardTitle"), //$NON-NLS-1$
				Messages.getString("org.nightlabs.jfire.base.admin.ui.configgroup.CreateWorkstationConfigGroupAction.pageTitle")); //$NON-NLS-1$
		
		DynamicPathWizardDialog wzd = new DynamicPathWizardDialog(getShell(), wiz);
		if (wzd.open() == Window.OK) {
			try {
				RCPUtil.openEditor(new WorkstationGroupEditorInput(wiz.getCreatedConfigID()), WorkstationGroupEditor.EDITOR_ID);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
