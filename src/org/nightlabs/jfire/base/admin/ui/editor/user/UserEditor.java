/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
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
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.user;

import javax.jdo.FetchPlan;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * A form editor for {@link User}s.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserEditor extends EntityEditor implements IConfigSetupEditor
{
	/**
	 * The editor id.
	 */
	public static final String EDITOR_ID = UserEditor.class.getName();

	/**
	 * Default constructor.
	 */
	public UserEditor()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.entityeditor.EntityEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		super.firePropertyChange(PROP_TITLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getTitle()
	 */
	@Override
	public String getTitle()
	{
		if(getEditorInput() == null)
			return super.getTitle();
		UserID userID = ((JDOObjectEditorInput<UserID>)getEditorInput()).getJDOObjectID();
		// given that the User had to be loaded to be shown in the tree, this should not take long.
		User user = UserDAO.sharedInstance().getUser(userID, new String[] {FetchPlan.DEFAULT}, 1, new NullProgressMonitor());
		if (user.getName() != null && !"".equals(user.getName())) //$NON-NLS-1$
			return user.getName();
		
		return user.getUserID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getTitleImage()
	 */
	@Override
	public Image getTitleImage()
	{
		return super.getTitleImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#getTitleToolTip()
	 */
	@Override
	public String getTitleToolTip()
	{
		// TODO: Better tool-tip
		return ((UserID)((JDOObjectEditorInput)getEditorInput()).getJDOObjectID()).userID;
	}

	public ConfigID getConfigID() {
		return ConfigID.create(getUserID().organisationID, getUserID(), User.class);
	}

	public UserID getUserID() {
		return ((UserID)((JDOObjectEditorInput)getEditorInput()).getJDOObjectID());
	}
}
