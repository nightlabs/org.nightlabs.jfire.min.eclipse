package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.dao.RoleGroupDAO;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

/**
 * An instance of this class should be used by all <code>AuthorityPageController</code>s, i.e. whenever the <code>Authority</code>
 * attached to a certain object is edited.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class AuthorityPageControllerHelper
{

	private AuthorityTypeID authorityTypeID;
	private AuthorityID authorityID;

	private AuthorityType authorityType;
	private Authority authority;

	public AuthorityPageControllerHelper() { }

	private static final String[] FETCH_GROUPS_AUTHORITY_TYPE = {
		FetchPlan.DEFAULT,
		AuthorityType.FETCH_GROUP_ROLE_GROUPS
	};

	private static final String[] FETCH_GROUPS_ROLE_GROUP = {
		FetchPlan.DEFAULT,
		RoleGroup.FETCH_GROUP_NAME,
		RoleGroup.FETCH_GROUP_DESCRIPTION
	};

	private static final String[] FETCH_GROUPS_USER = {
		FetchPlan.DEFAULT,
		User.FETCH_GROUP_NAME,
		User.FETCH_GROUP_USERGROUPS,
		UserGroup.FETCH_GROUP_USERS
	};

	private static final String[] FETCH_GROUPS_AUTHORITY = {
		FetchPlan.DEFAULT,
		Authority.FETCH_GROUP_NAME,
		Authority.FETCH_GROUP_DESCRIPTION
	};

//	public void load(AuthorityTypeID authorityTypeID, AuthorityID authorityID, ProgressMonitor monitor)
//	{
//		load(authorityTypeID, authorityID, null, monitor);
//	}
//
//	public void load(AuthorityTypeID authorityTypeID, Authority newAuthority, ProgressMonitor monitor)
//	{
//		load(authorityTypeID, null, newAuthority, monitor);
//	}

	private SecuredObject securedObject;

	public void load(SecuredObject securedObject, ProgressMonitor monitor)
	{
		this.securedObject = securedObject;
		load(
				(securedObject == null ? null : securedObject.getSecuringAuthorityTypeID()),
				(securedObject == null ? null : securedObject.getSecuringAuthorityID()),
				null,
				monitor
		);
	}

	/**
	 * Load the data.
	 *
	 * @param authorityTypeID the id of the {@link AuthorityType} or <code>null</code> to clear all data. 
	 * @param authorityID the id of the {@link Authority}. Can be <code>null</code> if <code>newAuthority</code>
	 *		is passed instead or to indicate that there
	 *		is no authority assigned to the object which is currently edited.
	 * @param newAuthority If a new <code>Authority</code> has been created (and not yet persisted), it has no object-id
	 *		assigned. Hence, instead of passing the <code>authorityID</code>, you can pass the new authority.
	 * @throws NamingException if a problem with JNDI arises.
	 * @throws CreateException if an EJB cannot be created.
	 * @throws LoginException if login fails.
	 * @throws RemoteException if communication via RMI fails.
	 */
	protected synchronized void load(AuthorityTypeID authorityTypeID, AuthorityID authorityID, Authority newAuthority, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading authority data", 100);

		if (authorityTypeID == null) {
			authorityID = null;
			newAuthority = null;
		}

		if (JDOHelper.getObjectId(newAuthority) != null) {
			authorityID = (AuthorityID) JDOHelper.getObjectId(newAuthority);
			newAuthority = null;
		}

		if (authorityID != null)
			newAuthority = null;

		this.authorityTypeID = authorityTypeID;
		this.authorityID = authorityID;

		this.authorityType = null;
		this.authority = newAuthority;

		if (authorityTypeID == null) {
			monitor.worked(40);
		}
		else {
			authorityType = AuthorityTypeDAO.sharedInstance().getAuthorityType(
					authorityTypeID,
					FETCH_GROUPS_AUTHORITY_TYPE,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 30));
		}

		changedModels.clear();
		roleGroupSecurityPreferencesModel2User = new HashMap<RoleGroupSecurityPreferencesModel, User>();
		user2RoleGroupSecurityPreferencesModel = new HashMap<User, RoleGroupSecurityPreferencesModel>();
		if (authorityID == null) {
			if (this.authority == null) {
				users = new HashMap<User, Boolean>();
				monitor.worked(70);
			}
			else {
				Collection<User> c = UserDAO.sharedInstance().getUsers(
						IDGenerator.getOrganisationID(),
						(String[])null,
						FETCH_GROUPS_USER,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 35));

				Set<RoleGroupID> roleGroupIDs = NLJDOHelper.getObjectIDSet(authorityType.getRoleGroups());
				Set<RoleGroup> roleGroupsInAuthorityType = new HashSet<RoleGroup>(
						RoleGroupDAO.sharedInstance().getRoleGroups(
								roleGroupIDs,
								FETCH_GROUPS_ROLE_GROUP,
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new SubProgressMonitor(monitor, 35))
				);

				users = new HashMap<User, Boolean>(c.size());
				for (User u : c) {
					// ignore the system user - it always has all access rights anyway and cannot be configured
					if (User.USERID_SYSTEM.equals(u.getUserID()))
						continue;

					users.put(u, Boolean.FALSE);

					RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = new RoleGroupSecurityPreferencesModel();
					roleGroupSecurityPreferencesModel.setAllRoleGroupsInAuthority(roleGroupsInAuthorityType);
					roleGroupSecurityPreferencesModel.setRoleGroupsAssignedDirectly(new HashSet<RoleGroup>());
					roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToUserGroups(new HashSet<RoleGroup>());
					roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToOtherUser(new HashSet<RoleGroup>());
					roleGroupSecurityPreferencesModel.setControlledByOtherUser(true);
					roleGroupSecurityPreferencesModel.setInAuthority(false);

					roleGroupSecurityPreferencesModel.addModelChangeListener(roleGroupSecurityPreferencesModelChangeListener);
					user2RoleGroupSecurityPreferencesModel.put(u, roleGroupSecurityPreferencesModel);
					roleGroupSecurityPreferencesModel2User.put(roleGroupSecurityPreferencesModel, u);
				}
			}
		}
		else {
			authority = Util.cloneSerializable(AuthorityDAO.sharedInstance().getAuthority(
					authorityID,
					FETCH_GROUPS_AUTHORITY,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 20)));

			Collection<RoleGroupSetCarrier> roleGroupSetCarriers = RoleGroupDAO.sharedInstance().getRoleGroupSetCarriers(
					authorityID,
					FETCH_GROUPS_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					FETCH_GROUPS_AUTHORITY, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, // was just fetched with exactly this and should be in the cache
					FETCH_GROUPS_ROLE_GROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 35));

			users = new HashMap<User, Boolean>(roleGroupSetCarriers.size());

			for (RoleGroupSetCarrier roleGroupSetCarrier : roleGroupSetCarriers) {
				// ignore the system user - it always has all access rights anyway and cannot be configured
				if (User.USERID_SYSTEM.equals(roleGroupSetCarrier.getUser().getUserID()))
					continue;

				users.put(roleGroupSetCarrier.getUser(), roleGroupSetCarrier.isInAuthority() ? Boolean.TRUE : Boolean.FALSE);

				RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = new RoleGroupSecurityPreferencesModel();
				roleGroupSecurityPreferencesModel.setAllRoleGroupsInAuthority(roleGroupSetCarrier.getAllInAuthority());
				roleGroupSecurityPreferencesModel.setRoleGroupsAssignedDirectly(roleGroupSetCarrier.getAssignedToUser());
				roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToUserGroups(roleGroupSetCarrier.getAssignedToUserGroups());
				roleGroupSecurityPreferencesModel.setRoleGroupsAssignedToOtherUser(roleGroupSetCarrier.getAssignedToOtherUser());
				roleGroupSecurityPreferencesModel.setInAuthority(roleGroupSetCarrier.isInAuthority());
				roleGroupSecurityPreferencesModel.setControlledByOtherUser(roleGroupSetCarrier.isControlledByOtherUser());

				roleGroupSecurityPreferencesModel.addModelChangeListener(roleGroupSecurityPreferencesModelChangeListener);
				user2RoleGroupSecurityPreferencesModel.put(roleGroupSetCarrier.getUser(), roleGroupSecurityPreferencesModel);
				roleGroupSecurityPreferencesModel2User.put(roleGroupSecurityPreferencesModel, roleGroupSetCarrier.getUser());
			}

			monitor.worked(5);
		}

		// check if our users have all fetch-groups we need by accessing some fields
		for (User u : users.keySet()) {
			u.getUserGroups();
			if (u instanceof UserGroup)
				((UserGroup)u).getUsers();
		}

//		usersToAdd = new HashSet<User>();
//		usersToRemove = new HashSet<User>();

		monitor.done();

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_AUTHORITY_LOADED, null, authority);
	}

	private ModelChangeListener roleGroupSecurityPreferencesModelChangeListener = new ModelChangeListener() {
		@Override
		public void modelChanged(ModelChangeEvent event) {
			RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = (RoleGroupSecurityPreferencesModel) event.getSource();
			changedModels.add(roleGroupSecurityPreferencesModel);
			User user = roleGroupSecurityPreferencesModel2User.get(roleGroupSecurityPreferencesModel);
			if (user == null)
				throw new IllegalStateException("roleGroupSecurityPreferencesModel2User.get(roleGroupSecurityPreferencesModel) returned null!");

			if (user instanceof UserGroup) {
				// A user-group has been modified - that affects all users that are members of this user-group!
				// Therefore, we need to recalculate their group-added role-groups.
				UserGroup userGroup = (UserGroup) user;
				for (User u : userGroup.getUsers())
					recalculateUser_RoleGroupsAssignedToUserGroups(u);
			}
			else if (User.USERID_OTHER.equals(user.getUserID())) {
				Set<RoleGroup> rightsOfOtherUser = new HashSet<RoleGroup>();
				if (roleGroupSecurityPreferencesModel.isInAuthority()) {
					rightsOfOtherUser.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedDirectly());
					rightsOfOtherUser.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedToUserGroups()); // not sure if it can be in groups, but better assume that yes
				}

				// recalculate rights for all users that are neither directly nor via a user-group in this authority
				for (User u : users.keySet()) {
					RoleGroupSecurityPreferencesModel m = user2RoleGroupSecurityPreferencesModel.get(u);
					if (!m.isControlledByOtherUser())
						continue;

					m.setRoleGroupsAssignedToOtherUser(rightsOfOtherUser);
				}
			}

			propertyChangeSupport.firePropertyChange(PROPERTY_NAME_ROLE_GROUP_SECURITY_PREFERENCES_MODEL_CHANGED, null, roleGroupSecurityPreferencesModel);
		}
	};

	public AuthorityTypeID getAuthorityTypeID() {
		return authorityTypeID;
	}
	public AuthorityType getAuthorityType() {
		return authorityType;
	}
	public AuthorityID getAuthorityID() {
		return authorityID;
	}
	public Authority getAuthority() {
		return authority;
	}

//	private Set<RoleGroup> roleGroupsInAuthorityType = new HashSet<RoleGroup>();

	private Map<User, RoleGroupSecurityPreferencesModel> user2RoleGroupSecurityPreferencesModel = new HashMap<User, RoleGroupSecurityPreferencesModel>();
	private Map<RoleGroupSecurityPreferencesModel, User> roleGroupSecurityPreferencesModel2User = new HashMap<RoleGroupSecurityPreferencesModel, User>();

	private Map<User, Boolean> users = new HashMap<User, Boolean>();
//	private Set<User> usersToAdd = new HashSet<User>();
//	private Set<User> usersToRemove = new HashSet<User>();
	private Set<RoleGroupSecurityPreferencesModel> changedModels = new HashSet<RoleGroupSecurityPreferencesModel>();

	/**
	 * Get a read-only mapping from {@link User} to {@link RoleGroupSecurityPreferencesModel}.
	 * The contents of this {@link Map} are the same as those in the <code>Map</code> returned by
	 * {@link #getRoleGroupSecurityPreferencesModel2User()}.
	 *
	 * @return a read-only {@link Map}.
	 */
	public Map<User, RoleGroupSecurityPreferencesModel> getUser2RoleGroupSecurityPreferencesModel() {
		return Collections.unmodifiableMap(user2RoleGroupSecurityPreferencesModel);
	}

	/**
	 * Get a read-only mapping from {@link RoleGroupSecurityPreferencesModel} to {@link User}.
	 * This <code>Map</code> contains exactly the same records as the <code>Map</code>
	 * returned by {@link #getUser2RoleGroupSecurityPreferencesModel()} - only the key and value
	 * is switched for each record.
	 *
	 * @return a read-only {@link Map}.
	 */
	public Map<RoleGroupSecurityPreferencesModel, User> getRoleGroupSecurityPreferencesModel2User() {
		return Collections.unmodifiableMap(roleGroupSecurityPreferencesModel2User);
	}

	/**
	 * Get all users with a flag indicating whether they are in the authority at the moment the data is loaded.
	 * This flag does not change, when
	 * {@link #addUserToAuthority(User)} or {@link #removeUserFromAuthority(User)} is called. It only changes,
	 * when data was stored to the server and {@link #load(AuthorityTypeID, AuthorityID, Authority, ProgressMonitor)} has
	 * been called again.
	 *
	 * @return all users of the local organisation with a flag indicating whether they are in the current authority or not.
	 */
	public Map<User, Boolean> getUsers() {
		return Collections.unmodifiableMap(users);
	}

	public List<Map.Entry<User, Boolean>> createModifiableUserList()
	{
		List<Map.Entry<User, Boolean>> result = new ArrayList<Map.Entry<User,Boolean>>(users.size());

		for (Map.Entry<User, Boolean> me : users.entrySet())
			result.add(new UserBooleanMapEntry(me.getKey(), me.getValue()));

		return result;
	}

	private class UserBooleanMapEntry implements Map.Entry<User, Boolean>
	{
		private User key;
		private Boolean value;

		public UserBooleanMapEntry(User key, Boolean value) {
			if (key == null)
				throw new IllegalArgumentException("key must not be null!");
			if (value == null)
				throw new IllegalArgumentException("value must not be null!");

			this.key = key;
			this.value = value;
		}

		@Override
		public User getKey() {
			return key;
		}

		@Override
		public Boolean getValue() {
			return value;
		}

		@Override
		public Boolean setValue(Boolean value) {
			if (value == null)
				throw new IllegalArgumentException("value must not be null!");

			Boolean oldValue = this.value;

			if (!value.equals(oldValue)) {
				if (value.booleanValue())
					addUserToAuthority(key);
				else
					removeUserFromAuthority(key);

				this.value = value;
			}

			return oldValue;
		}
	}

//	/**
//	 * Get the users that will be added to the authority when the data is stored to the server.
//	 *
//	 * @return the set of users to be added to the current authority.
//	 */
//	public Set<User> getUsersToAdd() {
//		return Collections.unmodifiableSet(usersToAdd);
//	}

//	/**
//	 * Get the users that will be removed from the authority when the data is stored to the server.
//	 *
//	 * @return the set of users to be removed from the current authority.
//	 */
//	public Set<User> getUsersToRemove() {
//		return Collections.unmodifiableSet(usersToRemove);
//	}

	public void addUserToAuthority(User user)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null");
		if (user == null)
			throw new IllegalArgumentException("user == null");

		RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = user2RoleGroupSecurityPreferencesModel.get(user);
		// replace the user by our internal one (where we are sure about fetch-groups
		user = roleGroupSecurityPreferencesModel2User.get(roleGroupSecurityPreferencesModel);

//		usersToAdd.add(user);
//		usersToRemove.remove(user);

		roleGroupSecurityPreferencesModel.beginDeferModelChangedEvents();
		try {
			roleGroupSecurityPreferencesModel.setInAuthority(true);
			roleGroupSecurityPreferencesModel.setControlledByOtherUser(false);

			if (user instanceof UserGroup) {
				UserGroup userGroup = (UserGroup) user;
				for (User u : userGroup.getUsers()) {
					RoleGroupSecurityPreferencesModel m = user2RoleGroupSecurityPreferencesModel.get(u);
					m.setControlledByOtherUser(false);

					recalculateUser_RoleGroupsAssignedToUserGroups(u);
				}
			}
			else if (User.USERID_OTHER.equals(user.getUserID())) {
				Set<RoleGroup> rightsOfOtherUser = new HashSet<RoleGroup>();
				rightsOfOtherUser.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedDirectly());
				rightsOfOtherUser.addAll(roleGroupSecurityPreferencesModel.getRoleGroupsAssignedToUserGroups()); // not sure if it can be in groups, but better assume that yes
				Set<RoleGroup> emptySet = Collections.emptySet();
				for (User u : users.keySet()) {
					RoleGroupSecurityPreferencesModel m = user2RoleGroupSecurityPreferencesModel.get(u);

					if (m.isControlledByOtherUser())
						m.setRoleGroupsAssignedToOtherUser(rightsOfOtherUser);
					else
						m.setRoleGroupsAssignedToOtherUser(emptySet);
				}
			}
		} finally {
			roleGroupSecurityPreferencesModel.endDeferModelChangedEvents();
		}

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_ADDED, null, user);
	}

	private void recalculateUser_RoleGroupsAssignedToUserGroups(User user)
	{
		Set<RoleGroup> roleGroupsAssignedToUserGroups = new HashSet<RoleGroup>();
		for (UserGroup userGroup : user.getUserGroups()) {
			RoleGroupSecurityPreferencesModel m = user2RoleGroupSecurityPreferencesModel.get(userGroup);
			if (m.isInAuthority()) {
				roleGroupsAssignedToUserGroups.addAll(m.getRoleGroupsAssignedDirectly());
				roleGroupsAssignedToUserGroups.addAll(m.getRoleGroupsAssignedToUserGroups()); // I don't think we should support nested groups, but if we do one day, this line is important
			}
		}
		user2RoleGroupSecurityPreferencesModel.get(user).setRoleGroupsAssignedToUserGroups(roleGroupsAssignedToUserGroups);
	}

	public void removeUserFromAuthority(User user)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null");
		if (user == null)
			throw new IllegalArgumentException("user == null");

		RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = user2RoleGroupSecurityPreferencesModel.get(user);
		// replace the user by our internal one (where we are sure about fetch-groups
		user = roleGroupSecurityPreferencesModel2User.get(roleGroupSecurityPreferencesModel);

//		usersToAdd.remove(user);
//		usersToRemove.add(user);
		
		roleGroupSecurityPreferencesModel.beginDeferModelChangedEvents();
		try {
			roleGroupSecurityPreferencesModel.setInAuthority(false);

			if (!resolveUserHasUserGroupInAuthority(user))
				roleGroupSecurityPreferencesModel.setControlledByOtherUser(true);

			if (user instanceof UserGroup) {
				UserGroup userGroup = (UserGroup) user;
				for (User u : userGroup.getUsers()) {
					RoleGroupSecurityPreferencesModel m = user2RoleGroupSecurityPreferencesModel.get(u);
					if (!m.isInAuthority()) {
						// the user u is not directly in this authority - is one of its groups in the authority?
						if (!resolveUserHasUserGroupInAuthority(u))
							m.setControlledByOtherUser(true);
					}

					recalculateUser_RoleGroupsAssignedToUserGroups(u);
				}
			}
			else if (User.USERID_OTHER.equals(user.getUserID())) {
				Set<RoleGroup> emptySet = Collections.emptySet();
				for (RoleGroupSecurityPreferencesModel m : user2RoleGroupSecurityPreferencesModel.values())
					m.setRoleGroupsAssignedToOtherUser(emptySet);

//				for (User u : users.keySet()) {
//					RoleGroupSecurityPreferencesModel m = user2RoleGroupSecurityPreferencesModel.get(u);
//					m.setRoleGroupsAssignedToOtherUser(emptySet);
//				}
			}
		} finally {
			roleGroupSecurityPreferencesModel.endDeferModelChangedEvents();
		}

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_REMOVED, null, user);
	}

	/**
	 * @param user the user
	 * @return <code>true</code>, if the user has at least one user-group in the current authority. <code>false</code>, if none of the user's user-groups is in this authority.
	 */
	private boolean resolveUserHasUserGroupInAuthority(User user)
	{
		for (UserGroup userGroup : user.getUserGroups()) {
			if (user2RoleGroupSecurityPreferencesModel.get(userGroup).isInAuthority())
				return true;
		}
		return false;
	}

	/**
	 * This method assigns the securing authority to the server, if {@link #isAssignSecuringAuthorityRequested()}
	 * returns <code>true</code>. Otherwise, it returns without writing to the server. It clears the flag
	 * {@link #assignSecuringAuthorityRequested}.
	 *
	 * @param monitor the monitor for progress feedback
	 */
	protected void assignSecuringAuthority(ProgressMonitor monitor)
	{
		monitor.beginTask("Assigning authority", 100);
		try {
			if (assignSecuringAuthorityRequested) {
				AuthorityDAO.sharedInstance().assignSecuringAuthority(
						JDOHelper.getObjectId(securedObject), assignSecuringAuthorityID, assignSecuringAuthorityInherited,
						new SubProgressMonitor(monitor, 100));

				assignSecuringAuthorityRequested = false;
			}
			else
				monitor.worked(100);
		} finally {
			monitor.done();
		}
	}

	public synchronized void store(ProgressMonitor monitor)
	{
		monitor.beginTask("Saving authority", 200);
		try {
			if (this.securedObject == null)
				throw new IllegalStateException("this.securedObject == null");
			if (this.authorityType == null)
				throw new IllegalStateException("this.authorityType == null");
			if (this.authority == null)
				throw new IllegalStateException("this.authority == null");

			if (authorityID == null || JDOHelper.isDirty(authority)) {
				Authority a = AuthorityDAO.sharedInstance().storeAuthority(
						authority,
						true,
						FETCH_GROUPS_AUTHORITY,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 20)
				);
				AuthorityID aid = (AuthorityID) JDOHelper.getObjectId(a);
				if (aid == null)
					throw new IllegalStateException("Authority returned by server does not have an object-id assigned!");

				authorityID = aid;
				authority = a;
			}
			else
				monitor.worked(20);

////			Set<UserID> userIDsToRemove = NLJDOHelper.getObjectIDSet(usersToRemove);
////			UserDAO.sharedInstance().removeUsersFromAuthority(
////					userIDsToRemove,
////					authorityID,
////					new SubProgressMonitor(monitor, 10)
////			);
//			usersToRemove.clear();
//
////			Set<UserID> userIDsToAdd = NLJDOHelper.getObjectIDSet(usersToAdd);
////			UserDAO.sharedInstance().removeUsersFromAuthority(
////					userIDsToAdd,
////					authorityID,
////					new SubProgressMonitor(monitor, 10)
////			);
//			usersToAdd.clear();

			{
				int ticksForThisWorkPart = 80;

				if (changedModels.isEmpty())
					monitor.worked(ticksForThisWorkPart);
				else {
					int ticksPerModel = ticksForThisWorkPart / changedModels.size();
					int ticksDone = 0;
					for (RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel : changedModels) {
						User user = roleGroupSecurityPreferencesModel2User.get(roleGroupSecurityPreferencesModel);
						UserID userID = (UserID) JDOHelper.getObjectId(user);
						if (userID == null)
							throw new IllegalStateException("JDOHelper.getObjectId(user) returned null for user: " + user);


						Set<RoleGroupID> roleGroupIDs = null;

						if (roleGroupSecurityPreferencesModel.isInAuthority()) {
							roleGroupIDs = NLJDOHelper.getObjectIDSet(
									roleGroupSecurityPreferencesModel.getRoleGroupsAssignedDirectly()
							);
						}

						// roleGroupIDs being null means that the user is removed from the authority by the following call.
						UserDAO.sharedInstance().setRoleGroupsOfUser(userID, authorityID, roleGroupIDs,
								new SubProgressMonitor(monitor, ticksPerModel));

						ticksDone += ticksPerModel;
					}
					changedModels.clear();

					int ticksLeft = ticksForThisWorkPart - ticksDone; // maybe there is some left
					if (ticksLeft > 0)
						monitor.worked(ticksLeft);
				}
			}

			// assign the new securingAuthority (if necessary)
			assignSecuringAuthority(new SubProgressMonitor(monitor, 10));

			// reload everything
			load(authorityTypeID, authorityID, null, new SubProgressMonitor(monitor, 90));
		} finally {
			monitor.done();
		}
	}

	//////////////////
	// BEGIN stuff for assigning a new authority
	//////////////////

	/**
	 * Indicates whether the property <code>securingAuthorityID</code> of the <code>SecuredObject</code> shall be modified
	 * on the server when the contents of this page are saved. If this method returns <code>true</code>, your implementation of
	 * {@link IEntityEditorPageController} (preferably a subclass of {@link ActiveEntityEditorPageController}) used to manage the
	 * {@link SecuredObject} shall assign the {@link Authority} by a call to
	 * {@link AuthorityDAO#assignSecuringAuthority(Object, AuthorityID, boolean, org.nightlabs.progress.ProgressMonitor)}. Note,
	 * that this method should be called after 
	 */
	private boolean assignSecuringAuthorityRequested;

	/**
	 * @see #getAssignSecuringAuthorityID()
	 */
	private AuthorityID assignSecuringAuthorityID;

	/**
	 * Get the id of the newly assigned authority. This can be <code>null</code> in order to indicate that the
	 * property <code>securingAuthorityID</code> of the <code>SecuredObject</code> shall be set to <code>null</code>.
	 *
	 * @return <code>null</code> or the new authority-id.
	 */
	public AuthorityID getAssignSecuringAuthorityID() {
		return assignSecuringAuthorityID;
	}

	public void setAssignSecuringAuthority(AuthorityID newAuthorityID, boolean inherited) {
		this.assignSecuringAuthorityID = newAuthorityID;
		this.assignSecuringAuthorityInherited = inherited;
		assignSecuringAuthorityRequested = true;
	}

	private boolean assignSecuringAuthorityInherited;

	//////////////////
	// BEGIN PropertyChangeSupport
	//////////////////
	/**
	 * The {@link #load(AuthorityTypeID, AuthorityID, Authority, ProgressMonitor)} method has been called (and is finished).
	 * The loaded authority can be accessed by {@link PropertyChangeEvent#getNewValue()}.
	 */
	public static final String PROPERTY_NAME_AUTHORITY_LOADED = "authorityLoaded";

	/**
	 * A {@link PropertyChangeEvent} with this property name is fired, when a user has been removed from the
	 * currently managed {@link Authority}. The affected user object can be accessed by
	 * {@link PropertyChangeEvent#getNewValue()}.
	 */
	public static final String PROPERTY_NAME_USER_REMOVED = "userRemoved";

	/**
	 * A {@link PropertyChangeEvent} with this property name is fired, when a user has been added to the
	 * currently managed {@link Authority}. The affected user object can be accessed by
	 * {@link PropertyChangeEvent#getNewValue()}.
	 */
	public static final String PROPERTY_NAME_USER_ADDED = "userAdded";

	/**
	 * A {@link PropertyChangeEvent} with this property name is fired, when a {@link RoleGroupSecurityPreferencesModel}
	 * has been changed which is part of the currently managed {@link Authority}. The affected {@link RoleGroupSecurityPreferencesModel}
	 * can be accessed by {@link PropertyChangeEvent#getNewValue()}.
	 * <p>
	 * The affected user can be obtained via the <code>Map</code> returned by {@link #getRoleGroupSecurityPreferencesModel2User()}.
	 * </p>
	 */
	public static final String PROPERTY_NAME_ROLE_GROUP_SECURITY_PREFERENCES_MODEL_CHANGED = "roleGroupSecurityPreferencesModelChanged";

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}
	//////////////////
	// END PropertyChangeSupport
	//////////////////
}
