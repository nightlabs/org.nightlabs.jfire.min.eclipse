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
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;

/**
 * Abstract super class for the easy implementation of an {@link Authority}-editor-page.
 * <p>
 * For details about how to use this, please read
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/UI%20for%20editing%20the%20Authority%20of%20a%20SecuredObject">UI for editing the Authority of a SecuredObject</a>
 * in our wiki.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public abstract class AbstractAuthorityPage extends EntityEditorPageWithProgress
{
	public AbstractAuthorityPage(FormEditor editor, String id) {
		super(editor, id, "Authority");
	}

	private AbstractAuthoritySection authoritySection;
	private AuthorizedObjectSection	authorizedObjectSection;
	private RoleGroupsSection roleGroupsSection;

	/**
	 * Implement this method and return your subclass of {@link AbstractAuthoritySection}.
	 *
	 * @param parent the container composite for the section.
	 * @return the section for editing the authority's basic information (e.g. name and description).
	 */
	protected abstract AbstractAuthoritySection createAuthoritySection(Composite parent);

	/**
	 * Delegate to your page-controller and return the helper it uses. Usually, your implementation
	 * of this method simply looks like the following code:<br/><br/>
	 * <code>
	 * return ((AuthorityPageController)getPageController()).getAuthorityPageControllerHelper();
	 * </code>
	 *
	 * @return the <code>AuthorityPageControllerHelper</code> used for managing your authority-page.
	 */
	protected abstract AuthorityPageControllerHelper getAuthorityPageControllerHelper();

	@Override
	protected void addSections(Composite parent) {
		authoritySection = createAuthoritySection(parent);
		getManagedForm().addPart(authoritySection);

		authorizedObjectSection = new AuthorizedObjectSection(this, parent);
		getManagedForm().addPart(authorizedObjectSection);

		roleGroupsSection = new RoleGroupsSection(this, parent, true);
		getManagedForm().addPart(roleGroupsSection);

		authorizedObjectSection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				RoleGroupSecurityPreferencesModel model = null;
				List<AuthorizedObject> selectedAuthorizedObjects = authorizedObjectSection.getSelectedAuthorizedObjects();
				AuthorizedObject selectedAuthorizedObject = null;
				if (!selectedAuthorizedObjects.isEmpty())
					selectedAuthorizedObject = selectedAuthorizedObjects.get(0);

				if (selectedAuthorizedObject != null) {
					AuthorityPageControllerHelper helper = getAuthorityPageControllerHelper();
					model = helper.getAuthorizedObject2RoleGroupSecurityPreferencesModel().get(selectedAuthorizedObject);
				}

				roleGroupsSection.setModel(model);
			}
		});
	}

	@Override
	protected void handleControllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
		authoritySection.setPageController(getPageController());
		authorizedObjectSection.setAuthorityPageControllerHelper(getAuthorityPageControllerHelper());
		roleGroupsSection.setModel(null);
	}

	@Override
	protected String getPageFormTitle() {
		return "Authority configuration";
	}

}
