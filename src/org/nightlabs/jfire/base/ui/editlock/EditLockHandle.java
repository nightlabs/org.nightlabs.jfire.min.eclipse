package org.nightlabs.jfire.base.ui.editlock;

import org.eclipse.swt.widgets.Shell;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

public class EditLockHandle {
	private EditLockTypeID editLockTypeID;
	private ObjectID objectID;
	private String description;
	private EditLockCallback editLockCallback;
	private Shell shell;
	
	public EditLockHandle(EditLockTypeID editLockTypeID, ObjectID objectID, String description, EditLockCallback editLockCallback, Shell shell) {
		super();
		this.editLockTypeID = editLockTypeID;
		this.objectID = objectID;
		this.description = description;
		this.editLockCallback = editLockCallback;
		this.shell = shell;
	}
	
	public EditLockTypeID getEditLockTypeID() {
		return editLockTypeID;
	}
	public ObjectID getObjectID() {
		return objectID;
	}
	public String getDescription() {
		return description;
	}
	public EditLockCallback getEditLockCallback() {
		return editLockCallback;
	}
	public Shell getShell() {
		return shell;
	}
	
	public void refresh() {
		refresh(new NullProgressMonitor());
	}
	
	public void refresh(ProgressMonitor monitor) {
		EditLockMan.sharedInstance().acquireEditLock(editLockTypeID, objectID, description, editLockCallback, shell, monitor);
	}
	
	public void release() {
		release(new NullProgressMonitor());
	}
	
	public void release(ProgressMonitor monitor) {
		EditLockMan.sharedInstance().releaseEditLock(objectID, monitor);
	}
}
