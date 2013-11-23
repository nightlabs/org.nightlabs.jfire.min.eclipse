package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthorityPage;
import org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityPageControllerHelper;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class AuthorityMetaPage extends AbstractAuthorityPage
{
	public static final String PAGE_ID = AuthorityMetaPage.class.getName();

	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new AuthorityMetaPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new AuthorityMetaPageController(editor);
		}
	}

	/**
	 * @param editor
	 */
	public AuthorityMetaPage(FormEditor editor) {
		super(editor, PAGE_ID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthorityPage#getAuthorityPageControllerHelper()
	 */
	@Override
	protected AuthorityPageControllerHelper getAuthorityPageControllerHelper() {
		return ((AuthorityMetaPageController) getPageController()).getPageControllerHelper();
	}

}
