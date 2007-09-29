package org.nightlabs.jfire.base.admin.editor.userconfiggroup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.admin.editor.configgroup.AbstractConfigGroupPageController;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserConfigGroupMemberPageController 
//extends EntityEditorPageController 
extends AbstractConfigGroupPageController
{
	public UserConfigGroupMemberPageController(EntityEditor editor) {
		super(editor);
	}

	public UserConfigGroupMemberPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	public void doLoad(IProgressMonitor monitor) 
	{
		monitor.beginTask("Load User Config Group Members", 2); //$NON-NLS-1$ // this is probably never shown since this method finishes really quickly (there's nothing to do) => we don't localise it
		monitor.worked(1);
		monitor.worked(1);
	}

	public void doSave(IProgressMonitor monitor) 
	{
		for (IFormPage page : getPages()) {
			if (page instanceof UserConfigGroupMemberPage) {
				UserConfigGroupMemberPage userConfigGroupMemberPage = (UserConfigGroupMemberPage) page;
				try {
					userConfigGroupMemberPage.getUserConfigGroupMemberSection().getConfigGroupMembersEditComposite().save();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}		
	}

}
