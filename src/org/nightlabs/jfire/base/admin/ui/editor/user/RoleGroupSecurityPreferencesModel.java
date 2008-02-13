/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.nightlabs.jfire.security.RoleGroup;

/**
 * @author nick
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class RoleGroupSecurityPreferencesModel extends BaseModel
{
	/**
	 * The included role groups.
	 */
	private Collection<RoleGroup> roleGroups = Collections.emptySet();

	/**
	 * The included role groups inherited from UserGroups.
	 */
	private Collection<RoleGroup> roleGroupsFromUserGroups = Collections.emptySet();

	/**
	 * All available role groups
	 */
	private Collection<RoleGroup> availableRoleGroups = Collections.emptySet();
	
	public Collection<RoleGroup> getRoleGroups() {
		return Collections.unmodifiableCollection(roleGroups);
	}

	public void setRoleGroups(Collection<RoleGroup> roleGroups) {
		this.roleGroups = new HashSet<RoleGroup>(roleGroups);
		modelChanged();
	}
	
	public void addRoleGroup(RoleGroup roleGroup) {
		this.roleGroups.add(roleGroup);
		modelChanged();
	}
	
	public void removeRoleGroup(RoleGroup roleGroup) {
		this.roleGroups.remove(roleGroup);
		modelChanged();
	}

	public Collection<RoleGroup> getRoleGroupsFromUserGroups() {
		return Collections.unmodifiableCollection(roleGroupsFromUserGroups);
	}

	public void setRoleGroupsFromUserGroups(Collection<RoleGroup> roleGroupsFromUserGroups) {
		this.roleGroupsFromUserGroups = new HashSet<RoleGroup>(roleGroupsFromUserGroups);
		modelChanged();
	}
	
	public void setAvailableRoleGroups(Collection<RoleGroup> availableRoleGroups) {
		this.availableRoleGroups = new HashSet<RoleGroup>(availableRoleGroups);
		modelChanged();
	}
	
	public Collection<RoleGroup> getAvailableRoleGroups() {
		return Collections.unmodifiableCollection(availableRoleGroups);
	}
}
