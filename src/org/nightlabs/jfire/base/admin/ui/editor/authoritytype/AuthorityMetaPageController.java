package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityPageControllerHelper;
import org.nightlabs.jfire.base.admin.ui.editor.authority.InheritedSecuringAuthorityResolver;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.security.AuthorityMeta;
import org.nightlabs.jfire.security.dao.AuthorityMetaDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityMetaID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class AuthorityMetaPageController
extends ActiveEntityEditorPageController<AuthorityMeta>
{
	public static final String[] FETCH_GROUPS = new String[] {FetchPlan.DEFAULT};

	private final AuthorityPageControllerHelper pageControllerHelper = new AuthorityPageControllerHelper() {
		@Override
		protected InheritedSecuringAuthorityResolver createInheritedSecuringAuthorityResolver() {
			return new InheritedSecuringAuthorityResolver() {
				@Override
				public AuthorityID getInheritedSecuringAuthorityID(ProgressMonitor monitor) {
					return getControllerObject().getSecuringAuthorityID();
				}
				@Override
				public Inheritable retrieveSecuredObjectInheritable(ProgressMonitor monitor) {
					return null;
				}
			};
		}
	};

	public AuthorityPageControllerHelper getPageControllerHelper() {
		return pageControllerHelper;
	}

	/**
	 * @param editor
	 */
	public AuthorityMetaPageController(EntityEditor editor) {
		super(editor);
	}

	/**
	 * @param editor
	 * @param startBackgroundLoading
	 */
	public AuthorityMetaPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController#getEntityFetchGroups()
	 */
	@Override
	protected String[] getEntityFetchGroups() {
		return FETCH_GROUPS;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController#retrieveEntity(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected AuthorityMeta retrieveEntity(ProgressMonitor monitor)
	{
		monitor.beginTask("Load Authority Meta", 100);
		AuthorityMetaID authorityMetaID = ((JDOObjectEditorInput<AuthorityMetaID>) getEntityEditor().getEditorInput()).getJDOObjectID();;
		AuthorityMeta authorityMeta = AuthorityMetaDAO.sharedInstance().getAuthorityMeta(
				authorityMetaID,
				getEntityFetchGroups(),
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				new SubProgressMonitor(monitor, 50));
		pageControllerHelper.load(authorityMeta, new SubProgressMonitor(monitor, 50));
		return authorityMeta;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController#storeEntity(java.lang.Object, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected AuthorityMeta storeEntity(AuthorityMeta authorityMeta, ProgressMonitor monitor)
	{
		pageControllerHelper.store(monitor);
		return authorityMeta;
//		return AuthorityMetaDAO.sharedInstance().storeAuthorityMeta(authorityMeta, true,
//				getEntityFetchGroups(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

}
