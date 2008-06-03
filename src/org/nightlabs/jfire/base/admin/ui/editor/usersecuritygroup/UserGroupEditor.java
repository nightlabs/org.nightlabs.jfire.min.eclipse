/**
 *
 */
package org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.dao.UserSecurityGroupDAO;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 * @author marco schulze - marco at nightlabs dot de
 */
public class UserGroupEditor extends EntityEditor
{
	private volatile String title = null;

	@Override
	public String getTitle() {
		if(getEditorInput() == null)
			return super.getTitle();

		if (title != null)
			return title;

		final UserSecurityGroupID groupID = (UserSecurityGroupID) ((JDOObjectEditorInput<?>)getEditorInput()).getJDOObjectID();

		Job job = new Job("Loading user security group") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				// given that the User had to be loaded to be shown in the tree, this should not take long.
				UserSecurityGroup group  = UserSecurityGroupDAO.sharedInstance().getUserSecurityGroup(
						groupID,
						new String[] {
								UserSecurityGroup.FETCH_GROUP_NAME
						},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						monitor
				);

				if (group.getName() != null && !"".equals(group.getName())) //$NON-NLS-1$
					title = group.getName();
				else
					title = group.getUserSecurityGroupID();

				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						setPartName(title);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.schedule();

		return "User security group " + groupID.userSecurityGroupID;
	}
}
