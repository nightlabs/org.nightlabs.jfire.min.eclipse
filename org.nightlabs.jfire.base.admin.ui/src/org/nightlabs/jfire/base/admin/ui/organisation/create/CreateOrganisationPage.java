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

package org.nightlabs.jfire.base.admin.ui.organisation.create;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.Formular;
import org.nightlabs.base.ui.composite.FormularChangeListener;
import org.nightlabs.base.ui.composite.FormularChangedEvent;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.organisation.Organisation;

class CreateOrganisationPage extends WizardPage implements FormularChangeListener
{
  private Text organisationID;
  private Text organisationDisplayName;
  private Text userID;
  private Text password0;
  private Text password1;
  private Button isServerAdminButton;

  public CreateOrganisationPage()
  {
    super(
    	CreateOrganisationPage.class.getName(),
    	Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.title"), //$NON-NLS-1$
    	SharedImages.getWizardPageImageDescriptor(
    			BaseAdminPlugin.getDefault(),
    			CreateOrganisationPage.class
    		)
    );
    setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.description")); //$NON-NLS-1$
  }

  public void createControl(Composite parent)
  {
  	Formular f = new Formular(parent, SWT.NONE, this);
  	organisationID = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.organisationID.labelText"), null); //$NON-NLS-1$
  	organisationDisplayName = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.organisationDisplayName.labelText"), null); //$NON-NLS-1$
  	userID = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.userID.labelText"), null); //$NON-NLS-1$
  	password0 = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.password.labelText"), null); //$NON-NLS-1$
  	password0.setEchoChar('*');
  	password1 = f.addTextInput(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.passwordConfirmation.labelText"), null); //$NON-NLS-1$
  	password1.setEchoChar('*');
  	isServerAdminButton = f.addCheckBox(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.isServerAdminButton.text"), false); //$NON-NLS-1$

    dialogChanged();
    setControl(f);
  }
  
  private void dialogChanged()
  {
    if ("".equals(getOrganisationID()))  //$NON-NLS-1$
      updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.errorOrganisationIDMissing")); //$NON-NLS-1$
    else if (!Organisation.isValidOrganisationID(getOrganisationID()))
    	updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.errorOrganisationIDContainsIllegalCharacters")); //$NON-NLS-1$
    else if ("".equals(getOrganisationDisplayName()))  //$NON-NLS-1$
      updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.errorOrganisationDisplayNameMissing")); //$NON-NLS-1$
    else if ("".equals(getUserID()))  //$NON-NLS-1$
      updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.errorUserIDMissing")); //$NON-NLS-1$
    else if ("".equals(getPassword0()) || "".equals(getPassword1()))  //$NON-NLS-1$ //$NON-NLS-2$
      updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.errorPasswordMissing")); //$NON-NLS-1$
    else if (!getPassword0().equals(getPassword1()))
      updateStatus(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.create.CreateOrganisationPage.errorPasswordConfirmationDoesNotMatch")); //$NON-NLS-1$
    else
    	updateStatus(null);
  }
  
  private void updateStatus(String message)
  {
    setErrorMessage(message);
    setPageComplete(message == null);
  }
  
  public String getOrganisationDisplayName()
  {
    return organisationDisplayName.getText();
  }
  public String getOrganisationID()
  {
    return this.organisationID.getText();
  }
  public String getPassword0()
  {
    return password0.getText();
  }
  public String getPassword1()
  {
    return password1.getText();
  }
  public String getUserID()
  {
    return userID.getText();
  }
  public boolean isServerAdmin()
  {
  	if (isServerAdminButton.isDisposed())
  		return false;
  	else return isServerAdminButton.getSelection();
  }
  
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.composite.FormularChangeListener#formularChanged(org.nightlabs.base.ui.composite.FormularChangedEvent)
	 */
	public void formularChanged(FormularChangedEvent event)
	{
		dialogChanged();
	}
}
