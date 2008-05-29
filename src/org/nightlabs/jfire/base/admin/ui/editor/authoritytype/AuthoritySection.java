package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthoritySection;
import org.nightlabs.jfire.base.admin.ui.editor.authority.InheritedAuthorityResolver;

public class AuthoritySection
extends AbstractAuthoritySection
{
	public AuthoritySection(IFormPage page, Composite parent) {
		super(page, parent);
	}

	private AuthorityPageController authorityPageController;

	@Override
	public void setPageController(IEntityEditorPageController pageController) {
		authorityPageController = (AuthorityPageController) pageController;
		setAuthorityPageControllerHelper(authorityPageController.getAuthorityPageControllerHelper());
	}

	@Override
	protected InheritedAuthorityResolver createInheritedAuthorityResolver() {
		return null; // no inheritance for AuthorityTypes
	}
}
