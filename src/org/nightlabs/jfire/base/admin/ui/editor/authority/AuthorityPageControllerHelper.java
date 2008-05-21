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

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupSetCarrier;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.dao.RoleGroupDAO;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

/**
 * An instance of this class should be used by all <code>AuthorityPageController</code>s, i.e. whenever the <code>Authority</code>
 * attached to a certain object is edited.
 *
 * @author marco
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
		User.FETCH_GROUP_NAME
	};

	private static final String[] FETCH_GROUPS_AUTHORITY = {
		FetchPlan.DEFAULT,
		Authority.FETCH_GROUP_NAME
	};

	public void load(AuthorityTypeID authorityTypeID, AuthorityID authorityID, ProgressMonitor monitor)
	{
		load(authorityTypeID, authorityID, null, monitor);
	}

	public void load(AuthorityTypeID authorityTypeID, Authority newAuthority, ProgressMonitor monitor)
	{
		load(authorityTypeID, null, newAuthority, monitor);
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
	public void load(AuthorityTypeID authorityTypeID, AuthorityID authorityID, Authority newAuthority, ProgressMonitor monitor)
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
			roleGroupsInAuthorityType = new HashSet<RoleGroup>();
			monitor.worked(40);
		}
		else {
			authorityType = AuthorityTypeDAO.sharedInstance().getAuthorityType(
					authorityTypeID,
					FETCH_GROUPS_AUTHORITY_TYPE,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 15));

			Set<RoleGroupID> roleGroupIDs = NLJDOHelper.getObjectIDSet(authorityType.getRoleGroups());
			roleGroupsInAuthorityType = new HashSet<RoleGroup>(
					RoleGroupDAO.sharedInstance().getRoleGroups(
							roleGroupIDs,
							FETCH_GROUPS_ROLE_GROUP,
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
							new SubProgressMonitor(monitor, 25))
			);
		}

		changedModels.clear();
		roleGroupSecurityPreferencesModel2User = new HashMap<RoleGroupSecurityPreferencesModel, User>();
		user2RoleGroupSecurityPreferencesModel = new HashMap<User, RoleGroupSecurityPreferencesModel>();
		if (authorityID == null) {
			if (this.authority == null) {
				users = new HashMap<User, Boolean>();
				monitor.worked(60);
			}
			else {
				Collection<User> c = UserDAO.sharedInstance().getUsers(
						IDGenerator.getOrganisationID(),
						(String[])null,
						FETCH_GROUPS_USER,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 60));

				users = new HashMap<User, Boolean>(c.size());
				for (User u : c)
					users.put(u, Boolean.FALSE);
			}
		}
		else {
			authority = Util.cloneSerializable(AuthorityDAO.sharedInstance().getAuthority(
					authorityID,
					FETCH_GROUPS_AUTHORITY,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 20)));

			Map<User, RoleGroupSetCarrier> user2RoleGroupSetCarrier = RoleGroupDAO.sharedInstance().getRoleGroupSetCarriers(
					authorityID,
					FETCH_GROUPS_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					FETCH_GROUPS_ROLE_GROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					true,
					new SubProgressMonitor(monitor, 35));

			users = new HashMap<User, Boolean>(user2RoleGroupSetCarrier.size());

			for (Map.Entry<User, RoleGroupSetCarrier> me : user2RoleGroupSetCarrier.entrySet()) {
				if (me.getValue() == null) {
					users.put(me.getKey(), Boolean.FALSE);
					continue;
				}
				users.put(me.getKey(), Boolean.TRUE);

				RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = new RoleGroupSecurityPreferencesModel();
				roleGroupSecurityPreferencesModel.setAvailableRoleGroups(roleGroupsInAuthorityType);
				roleGroupSecurityPreferencesModel.setRoleGroups(me.getValue().assigned);
				roleGroupSecurityPreferencesModel.setRoleGroupsFromUserGroups(me.getValue().assignedByUserGroup);
				roleGroupSecurityPreferencesModel.addModelChangeListener(roleGroupSecurityPreferencesModelChangeListener);
				user2RoleGroupSecurityPreferencesModel.put(me.getKey(), roleGroupSecurityPreferencesModel);
				roleGroupSecurityPreferencesModel2User.put(roleGroupSecurityPreferencesModel, me.getKey());
			}

			monitor.worked(5);
		}

		usersToAdd = new HashSet<User>();
		usersToRemove = new HashSet<User>();

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

				// TODO do this!
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

	private Set<RoleGroup> roleGroupsInAuthorityType = new HashSet<RoleGroup>();

	private Map<User, RoleGroupSecurityPreferencesModel> user2RoleGroupSecurityPreferencesModel = new HashMap<User, RoleGroupSecurityPreferencesModel>();
	private Map<RoleGroupSecurityPreferencesModel, User> roleGroupSecurityPreferencesModel2User = new HashMap<RoleGroupSecurityPreferencesModel, User>();

	private Map<User, Boolean> users = new HashMap<User, Boolean>();
	private Set<User> usersToAdd = new HashSet<User>();
	private Set<User> usersToRemove = new HashSet<User>();
	private Set<RoleGroupSecurityPreferencesModel> changedModels = new HashSet<RoleGroupSecurityPreferencesModel>();

	/**
	 * Get a read-only view of the {@link RoleGroup}s within the currently managed {@link AuthorityType}.
	 *
	 * @return a read-only <code>Set</code> of {@link RoleGroup}s.
	 */
	public Set<RoleGroup> getRoleGroupsInAuthorityType() {
		return Collections.unmodifiableSet(roleGroupsInAuthorityType);
	}

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

	/**
	 * Get the users that will be added to the authority when the data is stored to the server.
	 *
	 * @return the set of users to be added to the current authority.
	 */
	public Set<User> getUsersToAdd() {
		return Collections.unmodifiableSet(usersToAdd);
	}

	/**
	 * Get the users that will be removed from the authority when the data is stored to the server.
	 *
	 * @return the set of users to be removed from the current authority.
	 */
	public Set<User> getUsersToRemove() {
		return Collections.unmodifiableSet(usersToRemove);
	}

	public void addUserToAuthority(User user)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null");
		if (user == null)
			throw new IllegalArgumentException("user == null");

		if (user2RoleGroupSecurityPreferencesModel.containsKey(user))
			return; // nothing to do - it's already there

		usersToAdd.add(user);
		usersToRemove.remove(user);

		RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = new RoleGroupSecurityPreferencesModel();
		roleGroupSecurityPreferencesModel.setAvailableRoleGroups(roleGroupsInAuthorityType);
		roleGroupSecurityPreferencesModel.addModelChangeListener(roleGroupSecurityPreferencesModelChangeListener);

		user2RoleGroupSecurityPreferencesModel.put(user, roleGroupSecurityPreferencesModel);
		roleGroupSecurityPreferencesModel2User.put(roleGroupSecurityPreferencesModel, user);

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_ADDED, null, user);
	}

	public void removeUserFromAuthority(User user)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null");
		if (user == null)
			throw new IllegalArgumentException("user == null");

		if (!user2RoleGroupSecurityPreferencesModel.containsKey(user))
			return; // nothing to do - it's not there

		usersToAdd.remove(user);
		usersToRemove.add(user);

		RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = user2RoleGroupSecurityPreferencesModel.remove(user);
		roleGroupSecurityPreferencesModel2User.remove(roleGroupSecurityPreferencesModel);

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_REMOVED, null, user);
	}

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
