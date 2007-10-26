package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.id.WorkstationID;

public class WorkstationPreferencesController extends EntityEditorPageController {
	private WorkstationID workstationID;
	private Workstation workstation;

	public WorkstationPreferencesController(EntityEditor editor) {
		super(editor);
		this.workstationID = ((JDOObjectEditorInput<WorkstationID>)editor.getEditorInput()).getJDOObjectID();
	}

	public void doLoad(IProgressMonitor monitor) {
		workstation = WorkstationDAO.sharedInstance().getWorkstation(workstationID, new String[] { Workstation.FETCH_GROUP_THIS_WORKSTATION }, -1, new ProgressMonitorWrapper(monitor));
	}

	public void doSave(IProgressMonitor monitor) {
		WorkstationDAO.sharedInstance().storeWorkstation(workstation, false, null, -1, new ProgressMonitorWrapper(monitor));
	}
	
	public Workstation getWorkstation() {
		return workstation;
	}
}
