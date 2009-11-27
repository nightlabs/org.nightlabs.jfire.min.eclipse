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

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.Formular;
import org.nightlabs.base.ui.composite.FormularChangeListener;
import org.nightlabs.base.ui.composite.FormularChangedEvent;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.workstation.WorkstationManagerRemote;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateWorkstationPage extends DynamicPathWizardPage implements FormularChangeListener
{
	private Text workstationID;
	private Text workstationDescription;
	private boolean pristine = true;

	public CreateWorkstationPage(String title)
	{
		super(CreateWorkstationPage.class.getName(), title,
				SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(),
						CreateWorkstationPage.class));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage.workstationID.infoText")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent)
	{
		Formular f = new Formular(parent, SWT.NONE, this);
		workstationID = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage.workstationID.labelText"), null); //$NON-NLS-1$
		workstationDescription = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage.description.labelText"), null); //$NON-NLS-1$
		return f;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#onShow()
	 */
	@Override
	public void onShow()
	{
		super.onShow();
//		verifyInput();
	}

	/**
	 * Extended verification mechanism. 
	 * It is now tested whether a workstation with the chosen ID already exists.
	 * Frederik Loeser <frederik[AT]nightlabs[DOT]de>
	 */
	protected void verifyInput() {
		if ("".equals(getWorkstationID())) {
			updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage.errorWorkstationIDMissing")); //$NON-NLS-1$
		} else if (!ObjectIDUtil.isValidIDString(getWorkstationID())) {
			updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage.errorWorkstationIDInvalid")); //$NON-NLS-1$
		} else {
			WorkstationManagerRemote workstationManager = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());
			String chosenWSID = getWorkstationID();
			Set<WorkstationID> wsIDs = workstationManager.getWorkstationIDs();
			for (WorkstationID wsID : wsIDs) {
				if (wsID.workstationID.equals(chosenWSID)) {
					updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage.errorWorkstationIDCollision")); //$NON-NLS-1$
					return;
				}
			}
			updateStatus(null);
		}
	}

	@Override
	public boolean isPageComplete() {
		return !pristine && super.isPageComplete();
	}
	
	public String getWorkstationID()
	{
		return workstationID.getText();
	}

	public String getWorkstationDescription()
	{
		return workstationDescription.getText();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.composite.FormularChangeListener#formularChanged(org.nightlabs.base.ui.composite.FormularChangedEvent)
	 */
	public void formularChanged(FormularChangedEvent event)
	{
		pristine = false;
		verifyInput();
	}
	
}
