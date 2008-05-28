package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;

public class AuthorityTypeDetailPage extends EntityEditorPageWithProgress
{
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link AuthorityTypeDetailPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new AuthorityTypeDetailPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new AuthorityTypeDetailPageController(editor);
		}
	}

	public AuthorityTypeDetailPage(FormEditor editor) {
		super(editor, AuthorityTypeDetailPage.class.getName(), "General");
	}

	@Override
	protected void addSections(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void asyncCallback() {
		// TODO can we get rid of this method? isn't there a better API based on listeners? Marco.
		switchToContent();
	}

	@Override
	protected String getPageFormTitle() {
		return "General";
	}

}
