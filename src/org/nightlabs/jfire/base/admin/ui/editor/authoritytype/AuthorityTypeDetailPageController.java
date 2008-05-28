package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.util.Util;

public class AuthorityTypeDetailPageController extends EntityEditorPageController
{
	private static final String[] FETCH_GROUPS_AUTHORITY_TYPE = {
		FetchPlan.DEFAULT,
		AuthorityType.FETCH_GROUP_NAME,
		AuthorityType.FETCH_GROUP_DESCRIPTION,
//		AuthorityType.FETCH_GROUP_ROLE_GROUPS,
//		RoleGroup.FETCH_GROUP_NAME,
//		RoleGroup.FETCH_GROUP_DESCRIPTION
	};

	public AuthorityTypeDetailPageController(EntityEditor editor) {
		super(editor);
	}

	public AuthorityTypeDetailPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	private AuthorityType authorityType;

	@Override
	public void doLoad(IProgressMonitor monitor) {
		JDOObjectEditorInput<AuthorityTypeID> input = (JDOObjectEditorInput<AuthorityTypeID>) getEntityEditor().getEditorInput();
		AuthorityTypeID authorityTypeID = input.getJDOObjectID();
		this.authorityType = Util.cloneSerializable(
				AuthorityTypeDAO.sharedInstance().getAuthorityType(
						authorityTypeID,
						FETCH_GROUPS_AUTHORITY_TYPE,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new ProgressMonitorWrapper(monitor)
				)
		);
	}

	public AuthorityType getAuthorityType() {
		return authorityType;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

}
