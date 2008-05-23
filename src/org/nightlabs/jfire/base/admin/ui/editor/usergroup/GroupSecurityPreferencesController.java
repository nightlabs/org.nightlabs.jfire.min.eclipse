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
import java.util.HashSet;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
import org.nightlabs.jfire.base.admin.ui.editor.user.UserEditor;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
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
	RoleGroupSecurityPreferencesModel roleGroupModel;

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
		this.roleGroupModel = new RoleGroupSecurityPreferencesModel();
		JDOLifecycleManager.sharedInstance().addNotificationListener(UserGroup.class, userGroupChangedListener);
	}

	@Override
	public void dispose()
	{
		JDOLifecycleManager.sharedInstance().removeNotificationListener(UserGroup.class, userGroupChangedListener);
		super.dispose();
	}
	
	private NotificationListener userGroupChangedListener = new NotificationAdapterJob(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.GroupSecurityPreferencesController.loadingChangedGroup")) //$NON-NLS-1$
	{
		public void notify(NotificationEvent notificationEvent) {
			doLoad(getProgressMonitor());
		}
	};

	/**
	 * Load the usergroup data and users.
	 * @param _monitor The progress monitor to use.
	 */
	public void doLoad(IProgressMonitor _monitor)
	{
		_monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.SecurityPreferencesController.loadingUsers"), 100); //$NON-NLS-1$
		try {
			ProgressMonitor monitor = new ProgressMonitorWrapper(_monitor);
			if(userGroupID != null) {
				logger.info("Loading usergroup "+userGroupID.userID); //$NON-NLS-1$
				// load user with person data
				UserGroup userGroup = UserDAO.sharedInstance().getUserGroup(
						userGroupID,
						new String[] {
								User.FETCH_GROUP_THIS_USER,
								UserGroup.FETCH_GROUP_USERS,
//								PropertySet.FETCH_GROUP_FULL_DATA
								},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new SubProgressMonitor(monitor, 33));

				userGroupModel.setUserGroup(userGroup);

				// load users
				Collection<User> users = UserDAO.sharedInstance().getUsers(
						IDGenerator.getOrganisationID(),
						new String[] { User.USERTYPE_ORGANISATION, User.USERTYPE_USER },
						new String[] {
								User.FETCH_GROUP_THIS_USER},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new SubProgressMonitor(monitor, 33));
				
				userGroupModel.setAvailableUsers(users);
				userGroupModel.setUsers(userGroup.getUsers());

//				Collection<User> excludedUsers = new HashSet<User>(users);
//				excludedUsers.removeAll(userGroup.getUsers());
//				userGroupModel.setExcludedUsers(excludedUsers);
//				userGroupModel.setExcludedUsersUnchanged(new ArrayList<User>(excludedUsers));
//
//				Collection<User> includedUsers = new HashSet<User>(userGroup.getUsers());
//				userGroupModel.setIncludedUsers(includedUsers);
//				userGroupModel.setIncludedUsersUnchanged(new ArrayList<User>(includedUsers));

				// load role groups
				RoleGroupSetCarrier roleGroupSetCarrier = RoleGroupDAO.sharedInstance().getUserRoleGroupSetCarrier(
						userGroupID,
						getAuthorityID(),
						(String[])null, 1, // not interested in User
						(String[])null, 1, // not interested in Authority
						new String[] { FetchPlan.DEFAULT, RoleGroup.FETCH_GROUP_NAME, RoleGroup.FETCH_GROUP_DESCRIPTION},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 34));
				
				roleGroupModel.setRoleGroupsAssignedDirectly(roleGroupSetCarrier.getAssignedToUser());
				roleGroupModel.setRoleGroupsAssignedToUserGroups(roleGroupSetCarrier.getAssignedToUserGroups());
				roleGroupModel.setRoleGroupsAssignedToOtherUser(roleGroupSetCarrier.getAssignedToOtherUser());
				roleGroupModel.setAllRoleGroupsInAuthority(roleGroupSetCarrier.getAllInAuthority());
				roleGroupModel.setInAuthority(roleGroupSetCarrier.isInAuthority());
				roleGroupModel.setControlledByOtherUser(roleGroupSetCarrier.isControlledByOtherUser());

				logger.info("Loading usergroup "+userGroupID.userID+" done without errors"); //$NON-NLS-1$ //$NON-NLS-2$
				fireModifyEvent(null, userGroup);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			_monitor.done();
		}
	}

	protected AuthorityID getAuthorityID()
	{
		try {
			return AuthorityID.create(Login.getLogin().getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION);
		} catch (LoginException e) {
			throw new RuntimeException(e);
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
		
		Collection<RoleGroup> includedRoleGroups = roleGroupModel.getRoleGroupsAssignedDirectly();
		Collection<RoleGroup> excludedRoleGroups = new HashSet<RoleGroup>(roleGroupModel.getAllRoleGroupsInAuthority());
		excludedRoleGroups.removeAll(includedRoleGroups);
		
		Collection<User> includedUsers = userGroupModel.getIncludedUsers();
		Collection<User> excludedUsers = new HashSet<User>(userGroupModel.getAvailableUsers());
		excludedUsers.removeAll(includedUsers);
		
		int taskTicks = roleGroupModel.getAllRoleGroupsInAuthority().size() + userGroupModel.getAvailableUsers().size();
		
		pMonitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.SecurityPreferencesController.doSave.monitor.taskName"), taskTicks); //$NON-NLS-1$
		try	{
			UserGroup userGroup = userGroupModel.getUserGroup();
			if(includedUsers != null) {
				for (User user : includedUsers) {
					if(!userGroup.getUsers().contains(user))
						UserDAO.sharedInstance().addUserToUserGroup(user, userGroup, new SubProgressMonitor(pMonitor, 1));
				}
			}

			if(excludedUsers != null) {
				for (User user : excludedUsers) {
					if(userGroup.getUsers().contains(user))
						UserDAO.sharedInstance().removeUserFromUserGroup(user, userGroup, new SubProgressMonitor(pMonitor, 1));
				}
			}

			if (includedRoleGroups != null) {
				for (RoleGroup roleGroup : includedRoleGroups) {
					UserDAO.sharedInstance().addRoleGroupToUser(
							(UserID)JDOHelper.getObjectId(userGroup),
							getAuthorityID(),
							(RoleGroupID)JDOHelper.getObjectId(roleGroup), new SubProgressMonitor(pMonitor, 1));
				}
			}

			if (excludedRoleGroups != null) {
				for (RoleGroup roleGroup : excludedRoleGroups) {
					UserDAO.sharedInstance().removeRoleGroupFromUser(
							(UserID)JDOHelper.getObjectId(userGroup),
							getAuthorityID(),
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
	public RoleGroupSecurityPreferencesModel getRoleGroupModel()
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
