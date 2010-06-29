package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditor;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityMeta;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.dao.AuthorityMetaDAO;
import org.nightlabs.jfire.security.id.AuthorityMetaID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class AuthorityMetaEditor extends ActiveEntityEditor implements ICloseOnLogoutEditorPart
{
	public static final String EDITOR_ID = AuthorityMetaEditor.class.getName();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditor#getEditorTitleFromEntity(java.lang.Object)
	 */
	@Override
	protected String getEditorTitleFromEntity(Object entity)
	{
		Authority authority = (Authority) entity;
		if (authority != null) {
			return authority.getName().getText();
		}
		return "";
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditor#retrieveEntityForEditorTitle(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Object retrieveEntityForEditorTitle(ProgressMonitor monitor)
	{
		AuthorityMetaID authorityMetaID = ((JDOObjectEditorInput<AuthorityMetaID>) getEditorInput()).getJDOObjectID();
		AuthorityMeta authorityMeta = AuthorityMetaDAO.sharedInstance().getAuthorityMeta(
				authorityMetaID,
				new String[] {FetchPlan.DEFAULT},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);
		return AuthorityDAO.sharedInstance().getAuthority(
				authorityMeta.getSecuringAuthorityID(),
				new String[] {FetchPlan.DEFAULT, Authority.FETCH_GROUP_NAME},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);
	}

}
