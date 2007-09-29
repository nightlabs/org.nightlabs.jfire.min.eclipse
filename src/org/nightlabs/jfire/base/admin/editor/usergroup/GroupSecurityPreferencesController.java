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
package org.nightlabs.jfire.base.admin.editor.usergroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.editor.user.SecurityPreferencesModel;
import org.nightlabs.jfire.base.admin.resource.Messages;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupListCarrier;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.dao.RoleGroupDAO;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Controller class for the security preferences of a user.
 * 
 * @version $Revision: 4472 $ - $Date: 2006-08-28 20:21:33 +0000 (Mon, 28 Aug 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 */
public class GroupSecurityPreferencesController extends EntityEditorPageController
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(GroupSecurityPreferencesController.class);

	/**
	 * The user id.
	 */
	UserID userGroupID;

	/**
	 * The user editor.
	 */
	EntityEditor editor;

	/**
	 * The editor usergroup model.
	 */
	GroupSecurityPreferencesModel userGroupModel;

	/**
	 * The editor rolegroup model.
	 */
	SecurityPreferencesModel roleGroupModel;

	/**
	 * Create an instance of this controller for
	 * an {@link UserEditor} and load the data.
	 */
	public GroupSecurityPreferencesController(EntityEditor editor)
	{
		super(editor, true);
		this.userGroupID = ((UserGroupEditorInput)editor.getEditorInput()).getJDOObjectID();
		this.editor = editor;
		this.userGroupModel = new GroupSecurityPreferencesModel(userGroupID);
		this.roleGroupModel = new SecurityPreferencesModel(userGroupID);
		JDOLifecycleManager.sharedInstance().addNotificationListener(UserGroup.class, userGroupChangedListener);
	}

	@Override
	public void dispose()
	{
		JDOLifecycleManager.sharedInstance().removeNotificationListener(UserGroup.class, userGroupChangedListener);
		super.dispose();
	}
	
	private NotificationListener userGroupChangedListener = new NotificationAdapterJob(Messages.getString("org.nightlabs.jfire.base.admin.editor.usergroup.GroupSecurityPreferencesController.loadingChangedGroup")) //$NON-NLS-1$
	{
		public void notify(NotificationEvent notificationEvent) {
			doLoad(getProgressMonitor());
		}
	};

	/**
	 * Load the usergroup data and users.
	 * @param monitor The progress monitor to use.
	 */
	public void doLoad(IProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.editor.usergroup.SecurityPreferencesController.loadingUsers"), 4); //$NON-NLS-1$
		try {
			ProgressMonitor pMonitor = new ProgressMonitorWrapper(monitor);
			Thread.sleep(1000);
			if(userGroupID != null) {
				logger.info("Loading usergroup "+userGroupID.userID); //$NON-NLS-1$
				// load user with person data
				UserGroup userGroup = UserDAO.sharedInstance().getUserGroup(
						userGroupID,
						new String[] {
								UserGroup.FETCH_GROUP_THIS_USER,
								UserGroup.FETCH_GROUP_USERS,
//								PropertySet.FETCH_GROUP_FULL_DATA 
								},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								pMonitor);
				monitor.worked(1);
				userGroupModel.setUserGroup(userGroup);

				// load users
				Collection<User> users = UserDAO.sharedInstance().getUsers(
						new String[] {
								User.FETCH_GROUP_THIS_USER},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								pMonitor);

				Collection<User> excludedUsers = new HashSet<User>(users);
				excludedUsers.removeAll(userGroup.getUsers());
				userGroupModel.setExcludedUsers(excludedUsers);
				userGroupModel.setExcludedUsersUnchanged(new ArrayList<User>(excludedUsers));

				Collection<User> includedUsers = new HashSet<User>(userGroup.getUsers());
				userGroupModel.setIncludedUsers(includedUsers);
				userGroupModel.setIncludedUsersUnchanged(new ArrayList<User>(includedUsers));

				// load role groups
				RoleGroupListCarrier roleGroups = RoleGroupDAO.sharedInstance().getUserRoleGroups(
						userGroupID, 
						Authority.AUTHORITY_ID_ORGANISATION, 
						new String[] {RoleGroup.FETCH_GROUP_THIS}, 
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
						pMonitor);
				roleGroupModel.setIncludedRoleGroups(roleGroups.assigned);
				roleGroupModel.setIncludedRoleGroupsFromUserGroups(roleGroups.assignedByUserGroup);
				roleGroupModel.setExcludedRoleGroups(roleGroups.excluded);
				Set<RoleGroup> allRoleGroups = new HashSet<RoleGroup>(
						roleGroups.assigned.size() + 
						roleGroups.assignedByUserGroup.size() + 
						roleGroups.excluded.size());
				logger.info("Loading usergroup "+userGroupID.userID+" done without errors"); //$NON-NLS-1$ //$NON-NLS-2$
				fireModifyEvent(null, userGroup);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally { 
			monitor.done();
		}	
	}

	/**
	 * Save the user data.
	 * @param monitor The progress monitor to use.
	 */
	public void doSave(IProgressMonitor monitor)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("***********************************"); //$NON-NLS-1$
			logger.debug("doSave()"); //$NON-NLS-1$
			logger.debug("***********************************"); //$NON-NLS-1$
		}

		if (!isLoaded()) {
			logger.info("UserGroup not loaded will return. UserGroup "+userGroupID.userID); //$NON-NLS-1$
			return;
		}
		logger.info("Saving usergroup "+userGroupID.userID); //$NON-NLS-1$
		ProgressMonitor pMonitor = new ProgressMonitorWrapper(monitor);
		int taskTicks = roleGroupModel.getIncludedRoleGroups().size() + roleGroupModel.getExcludedRoleGroups().size() +
						userGroupModel.getIncludedUsers().size() + userGroupModel.getExcludedUsers().size();
		pMonitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.editor.usergroup.SecurityPreferencesController.doSave.monitor.taskName"), taskTicks); //$NON-NLS-1$
		try	{
			UserGroup userGroup = userGroupModel.getUserGroup();
			Collection<User> includedUsers = userGroupModel.getIncludedUsers();
			if(includedUsers != null) {
				for (User user : includedUsers) {
					if(!userGroup.getUsers().contains(user))
						UserDAO.sharedInstance().addUserToUserGroup(user, userGroup, new SubProgressMonitor(pMonitor, 1));
				}
			}

			Collection<User> excludedUsers = userGroupModel.getExcludedUsers();
			if(excludedUsers != null) {
				for (User user : excludedUsers) {
					if(userGroup.getUsers().contains(user))
						UserDAO.sharedInstance().removeUserFromUserGroup(user, userGroup, new SubProgressMonitor(pMonitor, 1));
				}
			}

			Collection<RoleGroup> includedRoleGroups = roleGroupModel.getIncludedRoleGroups();
			if (includedRoleGroups != null) {
				for (RoleGroup roleGroup : includedRoleGroups) {
					UserDAO.sharedInstance().addUserToRoleGroup(
							(UserID)JDOHelper.getObjectId(userGroup),
							AuthorityID.create(Login.getLogin().getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION),
							(RoleGroupID)JDOHelper.getObjectId(roleGroup), new SubProgressMonitor(pMonitor, 1));
				}
			}

			Collection<RoleGroup> excludedRoleGroups = roleGroupModel.getExcludedRoleGroups();
			if (excludedRoleGroups != null) {
				for (RoleGroup roleGroup : excludedRoleGroups) {
					UserDAO.sharedInstance().removeUserFromRoleGroup(
							(UserID)JDOHelper.getObjectId(userGroup),
							AuthorityID.create(Login.getLogin().getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION),
							(RoleGroupID)JDOHelper.getObjectId(roleGroup), new SubProgressMonitor(pMonitor, 1));
				}
			}
			
			monitor.done();
			logger.info("Saving user "+userGroupID.userID+" done without errors"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(Exception e) {
			logger.error("Saving user failed", e); //$NON-NLS-1$
			pMonitor.setCanceled(true);
			throw new RuntimeException(e);
		}

	}

	/**
	 * Get the editor.
	 * @return the editor
	 */
	public EntityEditor getEditor()
	{
		return editor;
	}

	/**
	 * Get the usergroup model.
	 * @return the usergroup model
	 */
	public GroupSecurityPreferencesModel getUserGroupModel()
	{
		if (!isLoaded())
			throw new IllegalStateException("Cannot access model if controller not loaded."); //$NON-NLS-1$
		return userGroupModel;
	}

	/**
	 * Get the rolegroup model.
	 * @return the rolegroup model
	 */
	public SecurityPreferencesModel getRoleGroupModel()
	{
		if (!isLoaded())
			throw new IllegalStateException("Cannot access model if controller not loaded."); //$NON-NLS-1$
		return roleGroupModel;
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public UserID getUserGroupID()
	{
		return userGroupID;
	}

	public void setPage(IFormPage page) {
		// TODO: Nothing done here yet
	}
}
