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
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserGroup;
import org.nightlabs.jfire.security.UserLifecycleListenerFilter;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * Entity tree category for {@link UserGroup}s.
 * 
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class EntityTreeCategoryUserGroup
extends ActiveJDOEntityTreeCategory<UserID, UserGroup>
{
	protected class LabelProvider extends TableLabelProvider {
		public String getColumnText(Object o, int arg1) {
			if (o instanceof String)
				return (String)o;
			else if (o instanceof UserGroup) {
				final UserGroup group = (UserGroup)o;                //     cut off prefixed "!"
				return "".equals(group.getName()) ? group.getUserID().substring(User.USERID_PREFIX_TYPE_USERGROUP.length()) : group.getName(); //$NON-NLS-1$
			}
			else
				return o.toString();
		}
	}
	
	public IEditorInput createEditorInput(Object o)
	{
		UserGroup userGroup = (UserGroup)o;
		UserID userGroupID = UserID.create(userGroup.getOrganisationID(), userGroup.getUserID());
		return new UserGroupEditorInput(userGroupID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

	@Override
	protected Class getJDOObjectClass()
	{
		return UserGroup.class;
	}

	/**
	 * We override the default implementation in order to
	 * filter for the correct user-type (i.e. solely user-group)
	 * on the server.
	 */
	@Override
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new UserLifecycleListenerFilter(
				User.USERTYPE_USERGROUP, new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	@Override
	protected Collection<UserGroup> retrieveJDOObjects(Set<UserID> objectIDs, ProgressMonitor monitor)
	{
		return CollectionUtil.castCollection(
				UserDAO.sharedInstance().getUsers(
						objectIDs,
						FETCH_GROUPS_USER_GROUP,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
						monitor));
	}

	private String[] FETCH_GROUPS_USER_GROUP = { User.FETCH_GROUP_THIS_USER };

	@Override
	protected Collection<UserGroup> retrieveJDOObjects(ProgressMonitor monitor)
	{
		return UserDAO.sharedInstance().getUserGroups(
				FETCH_GROUPS_USER_GROUP,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
				monitor);
	}

	@Override
	protected void sortJDOObjects(List<UserGroup> objects)
	{
		Collections.sort(objects); // User implements Comparable - no Comparator needed
	}
}
