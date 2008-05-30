package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection;
import org.nightlabs.jfire.security.User;

public abstract class AbstractAuthorityPage extends EntityEditorPageWithProgress
{
	public AbstractAuthorityPage(FormEditor editor, String id) {
		super(editor, id, "Authority");
	}

	private AbstractAuthoritySection authoritySection;
	private UserSection	userSection;
	private RoleGroupsSection roleGroupsSection;

	protected abstract AbstractAuthoritySection createAuthoritySection(Composite parent);
	protected abstract AuthorityPageControllerHelper getAuthorityPageControllerHelper();

	@Override
	protected void addSections(Composite parent) {
		authoritySection = createAuthoritySection(parent);
		getManagedForm().addPart(authoritySection);

		userSection = new UserSection(this, parent);
		getManagedForm().addPart(userSection);

		roleGroupsSection = new RoleGroupsSection(this, parent, true);
		getManagedForm().addPart(roleGroupsSection);

		userSection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				RoleGroupSecurityPreferencesModel model = null;
				List<User> selectedUsers = userSection.getSelectedUsers();
				User selectedUser = null;
				if (!selectedUsers.isEmpty())
					selectedUser = selectedUsers.get(0);

				if (selectedUser != null) {
					AuthorityPageControllerHelper helper = getAuthorityPageControllerHelper();
					model = helper.getUser2RoleGroupSecurityPreferencesModel().get(selectedUser);
				}

				roleGroupsSection.setModel(model);
			}
		});
	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		authoritySection.setPageController(getPageController());
		userSection.setAuthorityPageControllerHelper(getAuthorityPageControllerHelper());
		roleGroupsSection.setModel(null);
	}

	@Override
	protected String getPageFormTitle() {
		return "Authority configuration";
	}

}
