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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;
import org.nightlabs.jfire.base.admin.ui.editor.user.BaseModel;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
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
public class GroupSecurityPreferencesModel extends BaseModel
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
	//	 * The included users the users had before changes.
	//	 */
	//	private Collection<User> includedUsersUnchanged;
	//	
	//	/**
	//	 * The excluded users the group had before changes.
	//	 */
	//	private Collection<User> excludedUsersUnchanged;
	//
	//	/**
	//	 * The included users.
	//	 */
	//	private Collection<User> includedUsers;
	//	
	//	/**
	//	 * The excluded users.
	//	 */
	//	private Collection<User> excludedUsers;
	//
	//	/**
	//	 * The included role groups.
	//	 */
	//	private Collection<RoleGroup> roleGroups;
	//
	//	/**
	//	 * The included role groups from the users user groups.
	//	 */
	//	private Collection<RoleGroup> includedRoleGroupsFromUserGroups;
	//	
	//	/**
	//	 * The excluded role groups.
	//	 */
	//	private Collection<RoleGroup> availableRoleGroups;

//	private RoleGroupSecurityPreferencesModel roleGroupModel;

	//	private CollectionModel<User> userModel = new CollectionModel<User>();

	private Set<User> users = Collections.emptySet();

	private Set<User> availableUsers = Collections.emptySet();

	private Config userConfig;

	/**
	 * Create an instance of SecurityPreferencesModel.
	 * @param userID The user id.
	 */
	public GroupSecurityPreferencesModel(UserID userID) {
		ModelChangeListener listener = new ModelChangeListener() {
			public void modelChanged(ModelChangeEvent event) {
				GroupSecurityPreferencesModel.this.modelChanged();
			}
		};
	}

	/**
	 * Get included users.
	 * @return the added Users
	 */
	public Collection<User> getIncludedUsers() {
		return Collections.unmodifiableSet(users);
	}

	/**
	 * Set included users.
	 * @param includedUsers the added Users to set
	 */
	public void setUsers(Collection<User> includedUsers) {
		this.users = new HashSet<User>(includedUsers);
		modelChanged();
	}
	
	public void addUser(User user) {
		this.users.add(user);
	}
	
	public void removeUser(User user) {
		this.users.remove(user);
	}
	
	public Set<User> getAvailableUsers() {
		return Collections.unmodifiableSet(availableUsers);
	}
	
	public void setAvailableUsers(Collection<User> availableUsers) {
		this.availableUsers = new HashSet<User>(availableUsers);
		modelChanged();
	}

	//	/**
	//	 * Get excluded users.
	//	 * @return the removed Users
	//	 */
	//	public Collection<User> getExcludedUsers()
	//	{
	//		return excludedUsers;
	//	}
	//
	//	/**
	//	 * Set excluded users.
	//	 * @param excludedUsers the removed Users to set
	//	 */
	//	public void setExcludedUsers(Collection<User> excludedUsers)
	//	{
	//		this.excludedUsers = excludedUsers;
	//	}

	/**
	 * Get the usergroup.
	 * @return the usergroup
	 */
	public UserGroup getUserGroup() {
		return userGroup;
	}

	/**
	 * Set the usergroup.
	 * @param userGroup the user group to set
	 */
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
		modelChanged();
	}

//	/**
//	 * Get the excluded role groups.
//	 * @return the availableRoleGroups
//	 */
//	public Collection<RoleGroup> getAvailableRoleGroups() {
//		return this.roleGroupModel.getAvailableRoleGroups();
//	}
//
//	/**
//	 * Set the excluded role groups.
//	 * @param availableRoleGroups the availableRoleGroups to set
//	 */
//	public void setAvailableRoleGroups(Collection<RoleGroup> availableRoleGroups) {
//		this.roleGroupModel.setAvailableRoleGroups(availableRoleGroups);
//		modelChanged();
//	}
//
//	/**
//	 * Get the included role groups.
//	 * @return the roleGroups
//	 */
//	public Collection<RoleGroup> getRoleGroups() {
//		return this.roleGroupModel.getRoleGroups();
//	}
//
//	/**
//	 * Set the included role groups.
//	 * @param roleGroups the roleGroups to set
//	 */
//	public void setRoleGroups(Collection<RoleGroup> roleGroups) {
//		this.roleGroupModel.setRoleGroups(roleGroups);
//	}
//
//	/**
//	 * Get the included role groups from the users user groups.
//	 * @return the included role groups from the users user groups
//	 */
//	public Collection<RoleGroup> getRoleGroupsFromUserGroups() {
//		return this.roleGroupModel.getRoleGroupsFromUserGroups();
//	}
//
//	/**
//	 * Set the included role groups from the users user groups.
//	 * @param roleGroupsFromUserGroups the included role groups from the users user groups to set
//	 */
//	public void setRoleGroupsFromUserGroups(Collection<RoleGroup> roleGroupsFromUserGroups) {
//		this.roleGroupModel.setRoleGroupsFromUserGroups(roleGroupsFromUserGroups);
//	}
	
	//	/**
	//	 * Get the excludedUsersUnchanged.
	//	 * @return the excludedUsersUnchanged
	//	 */
	//	public Collection<User> getExcludedUsersUnchanged()
	//	{
	//		return excludedUsersUnchanged;
	//	}
	//
	//	/**
	//	 * Set the excludedUsersUnchanged.
	//	 * @param excludedUsersUnchanged the excludedUsersUnchanged to set
	//	 */
	//	public void setExcludedUsersUnchanged(
	//			Collection<User> excludedUsersUnchanged)
	//	{
	//		this.excludedUsersUnchanged = excludedUsersUnchanged;
	//	}
	//
	//	/**
	//	 * Get the included users the group had before changes.
	//	 * @return the included users the group had before changes
	//	 */
	//	public Collection<User> getIncludedUsersUnchanged()
	//	{
	//		return includedUsersUnchanged;
	//	}
	//
	//	/**
	//	 * Set the included users the group had before changes.
	//	 * @param includedUsersUnchanged the included users the group had before changes
	//	 */
	//	public void setIncludedUsersUnchanged(
	//			Collection<User> includedUsersUnchanged)
	//	{
	//		this.includedUsersUnchanged = includedUsersUnchanged;
	//	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public UserID getUserID() {
		return userID;
	}

	/**
	 * Get the userConfig.
	 * @return the userConfig
	 */
	public Config getUserConfig() {
		return userConfig;
	}

	/**
	 * Set the userConfig.
	 * @param userConfig the userConfig to set
	 */
	public void setUserConfig(Config userConfig) {
		this.userConfig = userConfig;
		modelChanged();
	}
}
