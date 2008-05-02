package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.UserGroup;

public class UserGroupPreferencePage extends EntityEditorPageWithProgress {
	
	/**
	 * The id of this page.
	 */
	public static final String ID_PAGE = PersonPreferencesPage.class.getName();
	
//	private BlockBasedEditorSection userPropertiesSection;
	private UserGroupDataSection userGroupDataSection;

	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link PersonPreferencesPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {

		public IFormPage createPage(FormEditor formEditor) {
			return new UserGroupPreferencePage(formEditor);
		}
		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new PersonPreferencesController(editor);
		}
	}

	/**
	 * Create an instance of PersonPreferencesPage.
	 * <p>
	 * This constructor is used by the entity editor
	 * page extension system.
	 * 
	 * @param editor The editor for which to create this
	 * 		form page.
	 */
	public UserGroupPreferencePage(FormEditor editor)
	{
		super(editor, ID_PAGE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.PersonPreferencesPage.pageTitle")); //$NON-NLS-1$
	}

	@Override
	protected void addSections(Composite parent) {
		userGroupDataSection = new UserGroupDataSection(this, parent, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserGroupPreferencePage.userGroupDataSectionTitle")); //$NON-NLS-1$
		getManagedForm().addPart(userGroupDataSection);
	}

	@Override
	protected void asyncCallback() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				PersonPreferencesController controller = (PersonPreferencesController)getPageController();
				UserGroup userGroup = (UserGroup) controller.getUser();
				userGroupDataSection.setUserGroup(userGroup);
				switchToContent();
			}
		});
	}
	
	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.PersonPreferencesPage.pageFormTitle"); //$NON-NLS-1$
	}
}
