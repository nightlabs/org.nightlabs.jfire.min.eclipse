package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.progress.ProgressMonitor;

public class WorkstationPreferencesController extends EntityEditorPageController {
	private WorkstationID workstationID;
	private Workstation workstation;

	public WorkstationPreferencesController(EntityEditor editor) {
		super(editor);
		this.workstationID = ((JDOObjectEditorInput<WorkstationID>)editor.getEditorInput()).getJDOObjectID();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageController#doLoad(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public void doLoad(ProgressMonitor monitor) {
		workstation = WorkstationDAO.sharedInstance().getWorkstation(workstationID, new String[] { Workstation.FETCH_GROUP_THIS_WORKSTATION }, -1, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageController#doSave(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public boolean doSave(ProgressMonitor monitor) {
		WorkstationDAO.sharedInstance().storeWorkstation(workstation, false, null, -1, monitor);
		return true;
	}
	
	public Workstation getWorkstation() {
		return workstation;
	}
}
