/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.usergroup;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jfire.base.admin.ui.editor.user.UserEditor;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 */
public class UserGroupEditor extends UserEditor {

	public UserGroupEditor() {
		super();
	}
	
	@Override
	public String getTitle() {
		if(getEditorInput() == null)
			return super.getTitle();
		
		final UserID groupID = (UserID)((JDOObjectEditorInput)getEditorInput()).getJDOObjectID();
		// given that the User had to be loaded to be shown in the tree, this should not take long.
		User userGroup  = UserDAO.sharedInstance().getUser(groupID, new String[] {FetchPlan.DEFAULT}, 1, new NullProgressMonitor());
		if (userGroup.getName() != null && !"".equals(userGroup.getName())) //$NON-NLS-1$
			return userGroup.getName();

		return userGroup.getUserID().substring(1); // cuts of the prefixing "!"
	}
}
