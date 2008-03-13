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

import org.nightlabs.jfire.base.admin.ui.editor.user.BaseModel;
import org.nightlabs.jfire.config.Config;
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

	private Set<User> users = Collections.emptySet();

	private Set<User> availableUsers = Collections.emptySet();

	private Config userConfig;

	/**
	 * Create an instance of SecurityPreferencesModel.
	 * @param userID The user id.
	 */
	public GroupSecurityPreferencesModel(UserID userID) {
//		ModelChangeListener listener = new ModelChangeListener() {
//			public void modelChanged(ModelChangeEvent event) {
//				GroupSecurityPreferencesModel.this.modelChanged();
//			}
//		};
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
