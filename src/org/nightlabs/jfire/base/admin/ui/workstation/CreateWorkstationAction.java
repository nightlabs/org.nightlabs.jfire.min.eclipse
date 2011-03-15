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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;

/**
 * Action to create a new Workstation using a {@link CreateWorkstationWizard}.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Fitas Amine - fitas[at]nightlabs[dot]de
 */
public class CreateWorkstationAction
implements IViewActionDelegate {

	private IViewPart viewPart;
	@Override
	public void init(IViewPart viewPart) {
		this.viewPart = viewPart;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
		CreateWorkstationWizard wiz = new CreateWorkstationWizard();
//		DynamicPathWizardDialog wzd = new DynamicPathWizardDialog(wiz);
		DynamicPathWizardDialog wzd = new DynamicPathWizardDialog(viewPart.getSite().getShell(), wiz);
		if (wzd.open() == Window.OK) {
//			try {
//				Editor2PerspectiveRegistry.sharedInstance().openEditor(
//						new WorkstationEditorInput(wiz.getCreatedWorkstationID()), WorkstationEditor.EDITOR_ID);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
		}
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {}
}
