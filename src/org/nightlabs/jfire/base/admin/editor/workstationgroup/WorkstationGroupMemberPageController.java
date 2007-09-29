package org.nightlabs.jfire.base.admin.editor.workstationgroup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.admin.editor.configgroup.AbstractConfigGroupPageController;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class WorkstationGroupMemberPageController 
extends AbstractConfigGroupPageController 
{
	private static final long serialVersionUID = 1L;

	public WorkstationGroupMemberPageController(EntityEditor editor) {
		super(editor);
	}

	public WorkstationGroupMemberPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	public void doLoad(IProgressMonitor monitor) {
		monitor.beginTask("Load Workstation Config Group Members", 1); //$NON-NLS-1$ // very fast => no externalisation necessary
		monitor.worked(1);
		monitor.done();
	}

	public void doSave(IProgressMonitor monitor) 
	{
		for (IFormPage page : getPages()) {
			if (page instanceof WorkstationGroupMemberPage) {
				WorkstationGroupMemberPage workstationGroupMemberPage = (WorkstationGroupMemberPage) page;
				try {
					workstationGroupMemberPage.getWorkstationGroupMemberSection().getConfigGroupMembersEditComposite().save();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}	
	}

}
