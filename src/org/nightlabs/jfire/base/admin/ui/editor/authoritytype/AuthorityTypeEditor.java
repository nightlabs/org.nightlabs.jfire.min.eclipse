package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class AuthorityTypeEditor
extends EntityEditor
{

	@Override
	public String getTitle() {
		if(getEditorInput() == null)
			return super.getTitle();

		Job loadTitleJob = new Job("Loading authority type") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				final String title = AuthorityTypeDAO.sharedInstance().getAuthorityType(
						(AuthorityTypeID) ((JDOObjectEditorInput<?>)getEditorInput()).getJDOObjectID(),
						new String[] { FetchPlan.DEFAULT, AuthorityType.FETCH_GROUP_NAME },
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor).getName().getText();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setPartName(title);
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadTitleJob.schedule();

		return super.getTitle();
	}

}
