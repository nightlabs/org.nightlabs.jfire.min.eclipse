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
package org.nightlabs.jfire.base.admin.ui.editor.usergroup;

import java.util.Collection;

import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.id.UserID;

/**
 * A model for a the security preferences of a usergroup.
 * 
 * @version $Revision: 5032 $ - $Date: 2006-11-20 18:46:17 +0100 (Mon, 20 Nov 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 */
public class GroupSecurityPreferencesModel
{
	/**
	 * The user group id.
	 */
	private UserID userID;
	
	/**
	 * The user
	 */
	private UserGroup userGroup;
	
//	/**
//	 * The user groups the user belongs to.
//	 */
//	Collection<UserGroup> userGroups;
	
	/**
	 * The included users the users had before changes.
	 */
	private Collection<User> includedUsersUnchanged;
	
	/**
	 * The excluded users the group had before changes.
	 */
	private Collection<User> excludedUsersUnchanged;

	/**
	 * The included users.
	 */
	private Collection<User> includedUsers;
	
	/**
	 * The excluded users.
	 */
	private Collection<User> excludedUsers;

	/**
	 * The included role groups.
	 */
	private Collection<RoleGroup> includedRoleGroups;

	/**
	 * The included role groups from the users user groups.
	 */
	private Collection<RoleGroup> includedRoleGroupsFromUserGroups;
	
	/**
	 * The excluded role groups.
	 */
	private Collection<RoleGroup> excludedRoleGroups;
	
	private Config userConfig;
	
	/**
	 * Create an instance of SecurityPreferencesModel.
	 * @param userID The user id.
	 */
	public GroupSecurityPreferencesModel(UserID userID)
	{
		this.userID = userID;
	}

	/**
	 * Get included users.
	 * @return the added Users
	 */
	public Collection<User> getIncludedUsers()
	{
		return includedUsers;
	}

	/**
	 * Set included users.
	 * @param includedUsers the added Users to set
	 */
	public void setIncludedUsers(Collection<User> includedUsers)
	{
		this.includedUsers = includedUsers;
	}

	/**
	 * Get excluded users.
	 * @return the removed Users
	 */
	public Collection<User> getExcludedUsers()
	{
		return excludedUsers;
	}

	/**
	 * Set excluded users.
	 * @param excludedUsers the removed Users to set
	 */
	public void setExcludedUsers(Collection<User> excludedUsers)
	{
		this.excludedUsers = excludedUsers;
	}

	/**
	 * Get the usergroup.
	 * @return the usergroup
	 */
	public UserGroup getUserGroup()
	{
		return userGroup;
	}

	/**
	 * Set the usergroup.
	 * @param user the user to set
	 */
	public void setUserGroup(UserGroup userGroup)
	{
		this.userGroup = userGroup;
	}

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
	 * Get the excludedUsersUnchanged.
	 * @return the excludedUsersUnchanged
	 */
	public Collection<User> getExcludedUsersUnchanged()
	{
		return excludedUsersUnchanged;
	}

	/**
	 * Set the excludedUsersUnchanged.
	 * @param excludedUsersUnchanged the excludedUsersUnchanged to set
	 */
	public void setExcludedUsersUnchanged(
			Collection<User> excludedUsersUnchanged)
	{
		this.excludedUsersUnchanged = excludedUsersUnchanged;
	}

	/**
	 * Get the included users the group had before changes.
	 * @return the included users the group had before changes
	 */
	public Collection<User> getIncludedUsersUnchanged()
	{
		return includedUsersUnchanged;
	}

	/**
	 * Set the included users the group had before changes.
	 * @param includedUsersUnchanged the included users the group had before changes
	 */
	public void setIncludedUsersUnchanged(
			Collection<User> includedUsersUnchanged)
	{
		this.includedUsersUnchanged = includedUsersUnchanged;
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
