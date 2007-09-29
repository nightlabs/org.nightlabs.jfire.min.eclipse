package org.nightlabs.jfire.base.admin.editor.prop;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.editor.userconfiggroup.UserConfigGroupMemberPage;
import org.nightlabs.jfire.base.admin.resource.Messages;
import org.nightlabs.jfire.prop.id.StructLocalID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class StructEditorPage 
extends EntityEditorPageWithProgress 
{
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link UserConfigGroupMemberPage}. 
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new StructEditorPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new StructLocalEditorPageController(editor);
		}
	}
	
	public StructEditorPage(FormEditor editor) {
		super(editor, StructEditorPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.editor.prop.StructEditorPage.pageName")); //$NON-NLS-1$
	}

	private StructEditorSection structEditorSection;
	public StructEditorSection getStructEditorSection() {
		return structEditorSection;
	}
	
	@Override
	protected void addSections(Composite parent) {
		structEditorSection = new StructEditorSection(this, parent);
		getManagedForm().addPart(structEditorSection);
	}

	@Override
	protected void asyncCallback() 
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				StructLocalID structLocalID = ((JDOObjectEditorInput<StructLocalID>)getEditor().getEditorInput()).getJDOObjectID();
				structEditorSection.getStructEditor().setCurrentStructLocalID(structLocalID);
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.editor.prop.StructEditorPage.pageTitle"); //$NON-NLS-1$
	}

}
