/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Collections;

import org.nightlabs.jfire.security.RoleGroup;

/**
 * @author nick
 *
 */
public class RoleGroupSecurityPreferencesModel
{
	/**
	 * The included role groups.
	 */
	private Collection<RoleGroup> includedRoleGroups = Collections.EMPTY_LIST;

	/**
	 * The included role groups inherited from UserGroups.
	 */
	private Collection<RoleGroup> includedRoleGroupsFromUserGroups = Collections.EMPTY_LIST;

	/**
	 * The excluded role groups.
	 */
	private Collection<RoleGroup> excludedRoleGroups = Collections.EMPTY_LIST;

	public Collection<RoleGroup> getIncludedRoleGroups()
	{
		return includedRoleGroups;
	}

	public void setIncludedRoleGroups(Collection<RoleGroup> includedRoleGroups)
	{
		this.includedRoleGroups = includedRoleGroups;
	}

	public Collection<RoleGroup> getIncludedRoleGroupsFromUserGroups()
	{
		return includedRoleGroupsFromUserGroups;
	}

	public void setIncludedRoleGroupsFromUserGroups(
			Collection<RoleGroup> includedRoleGroupsFromUserGroups)
	{
		this.includedRoleGroupsFromUserGroups = includedRoleGroupsFromUserGroups;
	}

	public Collection<RoleGroup> getExcludedRoleGroups()
	{
		return excludedRoleGroups;
	}

	public void setExcludedRoleGroups(Collection<RoleGroup> excludedRoleGroups)
	{
		this.excludedRoleGroups = excludedRoleGroups;
	}

}
