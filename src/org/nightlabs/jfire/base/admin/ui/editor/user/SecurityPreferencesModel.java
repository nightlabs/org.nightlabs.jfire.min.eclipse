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

import java.util.Collection;
import java.util.Collections;

import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.id.UserID;

/**
 * A model for a the security preferences of a user.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SecurityPreferencesModel
{
	/**
	 * The user id.
	 */
	private UserID userID;
	
	/**
	 * The user
	 */
	private User user;
	
//	/**
//	 * The user groups the user belongs to.
//	 */
//	Collection<UserGroup> userGroups;
	
	/**
	 * The included user groups the users had before changes.
	 */
	private Collection<UserGroup> includedUserGroupsUnchanged = Collections.EMPTY_LIST;
	
	/**
	 * The excluded user groups the users had before changes.
	 */
	private Collection<UserGroup> excludedUserGroupsUnchanged = Collections.EMPTY_LIST;

	/**
	 * The included user groups.
	 */
	private Collection<UserGroup> includedUserGroups = Collections.EMPTY_LIST;
	
	/**
	 * The excluded user groups.
	 */
	private Collection<UserGroup> excludedUserGroups = Collections.EMPTY_LIST;

//	/**
//	 * The role groups the user belongs to.
//	 */
//	Collection<RoleGroup> roleGroups;
	
	/**
	 * The included role groups.
	 */
	private Collection<RoleGroup> includedRoleGroups = Collections.EMPTY_LIST;

	/**
	 * The included role groups from the users user groups.
	 */
	private Collection<RoleGroup> includedRoleGroupsFromUserGroups = Collections.EMPTY_LIST;
	
	/**
	 * The excluded role groups.
	 */
	private Collection<RoleGroup> excludedRoleGroups = Collections.EMPTY_LIST;
	
	private Config userConfig;
	
	/**
	 * Create an instance of SecurityPreferencesModel.
	 * @param userID The user id.
	 */
	public SecurityPreferencesModel(UserID userID)
	{
		this.userID = userID;
	}

	/**
	 * Get included user groups.
	 * @return the addedUserGroups
	 */
	public Collection<UserGroup> getIncludedUserGroups()
	{
		return includedUserGroups;
	}

	/**
	 * Set included user groups.
	 * @param addedUserGroups the addedUserGroups to set
	 */
	public void setIncludedUserGroups(Collection<UserGroup> includedUserGroups)
	{
		this.includedUserGroups = includedUserGroups;
	}

	/**
	 * Get excluded user groups.
	 * @return the removedUserGroups
	 */
	public Collection<UserGroup> getExcludedUserGroups()
	{
		return excludedUserGroups;
	}

	/**
	 * Set excluded user groups.
	 * @param removedUserGroups the removedUserGroups to set
	 */
	public void setExcludedUserGroups(Collection<UserGroup> excludedUserGroups)
	{
		this.excludedUserGroups = excludedUserGroups;
	}

	/**
	 * Get the user.
	 * @return the user
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Set the user.
	 * @param user the user to set
	 */
	public void setUser(User user)
	{
		this.user = user;
	}

//	/**
//	 * Get the user groups.
//	 * @return the userGroups
//	 */
//	public Collection<UserGroup> getUserGroups()
//	{
//		return userGroups;
//	}

//	/**
//	 * Set the user groups.
//	 * @param userGroups the userGroups to set
//	 */
//	public void setUserGroups(Collection<UserGroup> userGroups)
//	{
//		this.userGroups = userGroups;
//	}

	/**
	 * Get the excluded role groups.
	 * @return the excludedRoleGroups
	 */
	public Collection<RoleGroup> getExcludedRoleGroups()
	{
		return excludedRoleGroups;
	}

	/**
	 * Set the excluded role groups.
	 * @param excludedRoleGroups the excludedRoleGroups to set
	 */
	public void setExcludedRoleGroups(Collection<RoleGroup> excludedRoleGroups)
	{
		this.excludedRoleGroups = excludedRoleGroups;
	}

	/**
	 * Get the included role groups.
	 * @return the includedRoleGroups
	 */
	public Collection<RoleGroup> getIncludedRoleGroups()
	{
		return includedRoleGroups;
	}

	/**
	 * Set the included role groups.
	 * @param includedRoleGroups the includedRoleGroups to set
	 */
	public void setIncludedRoleGroups(Collection<RoleGroup> includedRoleGroups)
	{
		this.includedRoleGroups = includedRoleGroups;
	}

//	/**
//	 * Get the role groups.
//	 * @return the roleGroups
//	 */
//	public Collection<RoleGroup> getRoleGroups()
//	{
//		return roleGroups;
//	}

//	/**
//	 * Set the role groups.
//	 * @param roleGroups the roleGroups to set
//	 */
//	public void setRoleGroups(Collection<RoleGroup> roleGroups)
//	{
//		this.roleGroups = roleGroups;
//	}

	/**
	 * Get the included role groups from the users user groups.
	 * @return the included role groups from the users user groups
	 */
	public Collection<RoleGroup> getIncludedRoleGroupsFromUserGroups()
	{
		return includedRoleGroupsFromUserGroups;
	}

	/**
	 * Set the included role groups from the users user groups.
	 * @param includedRoleGroupsFromUserGroups the included role groups from the users user groups to set
	 */
	public void setIncludedRoleGroupsFromUserGroups(
			Collection<RoleGroup> includedRoleGroupsFromUserGroups)
	{
		this.includedRoleGroupsFromUserGroups = includedRoleGroupsFromUserGroups;
	}

	/**
	 * Get the excludedUserGroupsUnchanged.
	 * @return the excludedUserGroupsUnchanged
	 */
	public Collection<UserGroup> getExcludedUserGroupsUnchanged()
	{
		return excludedUserGroupsUnchanged;
	}

	/**
	 * Set the excludedUserGroupsUnchanged.
	 * @param excludedUserGroupsUnchanged the excludedUserGroupsUnchanged to set
	 */
	public void setExcludedUserGroupsUnchanged(
			Collection<UserGroup> excludedUserGroupsUnchanged)
	{
		this.excludedUserGroupsUnchanged = excludedUserGroupsUnchanged;
	}

	/**
	 * Get the included user groups the users had before changes.
	 * @return the included user groups the users had before changes
	 */
	public Collection<UserGroup> getIncludedUserGroupsUnchanged()
	{
		return includedUserGroupsUnchanged;
	}

	/**
	 * Set the included user groups the users had before changes.
	 * @param includedUserGroupsUnchanged the included user groups the users had before changes
	 */
	public void setIncludedUserGroupsUnchanged(
			Collection<UserGroup> includedUserGroupsUnchanged)
	{
		this.includedUserGroupsUnchanged = includedUserGroupsUnchanged;
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public UserID getUserID()
	{
		return userID;
	}

	/**
	 * Get the userConfig.
	 * @return the userConfig
	 */
	public Config getUserConfig()
	{
		return userConfig;
	}

	/**
	 * Set the userConfig.
	 * @param userConfig the userConfig to set
	 */
	public void setUserConfig(Config userConfig)
	{
		this.userConfig = userConfig;
	}
}
