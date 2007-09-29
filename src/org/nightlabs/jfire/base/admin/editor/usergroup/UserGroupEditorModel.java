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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.editor.usergroup;

import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserGroupEditorModel
{
	/**
	 * The user group id.
	 */
	private UserID userGroupID;
	
	/**
	 * The user group
	 */
	private UserGroup userGroup;
	
	/**
	 * Create an instance of SecurityPreferencesModel.
	 * @param userID The user id.
	 */
	public UserGroupEditorModel(UserID userGroupID)
	{
		this.userGroupID = userGroupID;
	}

	/**
	 * Get the userGroup.
	 * @return the userGroup
	 */
	public UserGroup getUserGroup()
	{
		return userGroup;
	}

	/**
	 * Get the userGroupID.
	 * @return the userGroupID
	 */
	public UserID getUserGroupID()
	{
		return userGroupID;
	}

	/**
	 * Set the userGroup.
	 * @param userGroup the userGroup to set
	 */
	public void setUserGroup(UserGroup userGroup)
	{
		this.userGroup = userGroup;
	}
	
}
