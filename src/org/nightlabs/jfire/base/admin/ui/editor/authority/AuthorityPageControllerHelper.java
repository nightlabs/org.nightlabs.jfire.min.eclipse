package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
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
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

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

	/**
	 * Load the data.
	 *
	 * @param authorityTypeID the id of the {@link AuthorityType} or <code>null</code> to clear all data. 
	 * @param authorityID the id of the {@link Authority}. Can be <code>null</code> to indicate that there
	 *		is no authority assigned to the object which is currently edited.
	 * @throws NamingException if a problem with JNDI arises.
	 * @throws CreateException if an EJB cannot be created.
	 * @throws LoginException if login fails.
	 * @throws RemoteException if communication via RMI fails.
	 */
	public void load(AuthorityTypeID authorityTypeID, AuthorityID authorityID, ProgressMonitor monitor)
	throws RemoteException, LoginException, CreateException, NamingException
	{
		monitor.beginTask("Loading authority data", 100);

		if (authorityTypeID == null)
			authorityID = null;

		this.authorityTypeID = authorityTypeID;
		this.authorityID = authorityID;

		this.authorityType = null;
		this.authority = null;

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
			monitor.worked(60);
		}
		else {
			authority = AuthorityDAO.sharedInstance().getAuthority(
					authorityID,
					FETCH_GROUPS_AUTHORITY,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 20));

			Map<User, RoleGroupSetCarrier> user2RoleGroupSetCarrier = RoleGroupDAO.sharedInstance().getRoleGroupSetCarriers(
					authorityID,
					FETCH_GROUPS_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					FETCH_GROUPS_ROLE_GROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new SubProgressMonitor(monitor, 35));

			for (Map.Entry<User, RoleGroupSetCarrier> me : user2RoleGroupSetCarrier.entrySet()) {
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

			propertyChangeSupport.firePropertyChange(PROPERTY_NAME_ROLE_GROUP_SECURITY_PREFERENCES_MODEL_CHANGED, roleGroupSecurityPreferencesModel, roleGroupSecurityPreferencesModel);
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

	public void addUserToAuthority(UserID userID, ProgressMonitor monitor)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null");
		if (userID == null)
			throw new IllegalArgumentException("userID == null");

		monitor.beginTask("Loading user", 100);
		try {
			User user = UserDAO.sharedInstance().getUser(userID, FETCH_GROUPS_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 100));

			if (user2RoleGroupSecurityPreferencesModel.containsKey(user))
				return; // nothing to do - it's already there

			usersToAdd.add(user);
			usersToRemove.remove(user);

			RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = new RoleGroupSecurityPreferencesModel();
			roleGroupSecurityPreferencesModel.setAvailableRoleGroups(roleGroupsInAuthorityType);
			roleGroupSecurityPreferencesModel.addModelChangeListener(roleGroupSecurityPreferencesModelChangeListener);

			user2RoleGroupSecurityPreferencesModel.put(user, roleGroupSecurityPreferencesModel);
			roleGroupSecurityPreferencesModel2User.put(roleGroupSecurityPreferencesModel, user);

			propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_ADDED, user, user);
		} finally {
			monitor.done();
		}
	}

	public void removeUserFromAuthority(User user)
	{
		if (authority == null)
			throw new IllegalStateException("authority == null");
		if (user == null)
			throw new IllegalArgumentException("userID == null");

		if (!user2RoleGroupSecurityPreferencesModel.containsKey(user))
			return; // nothing to do - it's not there

		usersToAdd.remove(user);
		usersToRemove.add(user);
		RoleGroupSecurityPreferencesModel roleGroupSecurityPreferencesModel = user2RoleGroupSecurityPreferencesModel.remove(user);
		roleGroupSecurityPreferencesModel2User.remove(roleGroupSecurityPreferencesModel);

		propertyChangeSupport.firePropertyChange(PROPERTY_NAME_USER_REMOVED, user, user);
	}

	//////////////////
	// BEGIN PropertyChangeSupport
	//////////////////
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
