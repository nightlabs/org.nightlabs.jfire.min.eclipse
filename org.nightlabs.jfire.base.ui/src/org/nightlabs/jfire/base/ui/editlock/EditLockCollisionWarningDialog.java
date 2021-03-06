package org.nightlabs.jfire.base.ui.editlock;

import java.util.List;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.editlock.AcquireEditLockResult;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.dao.EditLockDAO;
import org.nightlabs.progress.ProgressMonitor;

public class EditLockCollisionWarningDialog
//extends ResizableTrayDialog
extends ResizableTitleAreaDialog
{
	private AcquireEditLockResult acquireEditLockResult;

	private EditLockTable editLockTable;

	public EditLockCollisionWarningDialog(Shell parentShell, AcquireEditLockResult acquireEditLockResult)
	{
		super(parentShell, Messages.getBundle());
		this.acquireEditLockResult = acquireEditLockResult;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setBlockOnOpen(false);
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton)
	{
		if (id == CANCEL)
			return null;

		return super.createButton(parent, id, label, defaultButton);
	}

	private static final String[] FETCH_GROUPS_EDIT_LOCK = {
		FetchPlan.DEFAULT, EditLock.FETCH_GROUP_LOCK_OWNER_USER
	};

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite page = (Composite) super.createDialogArea(parent);

//		Label l = new Label(page, SWT.WRAP);
//		l.setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockCollisionWarningDialog.warningText")); //$NON-NLS-1$
//		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getShell().setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockCollisionWarningDialog.windowTitle")); //$NON-NLS-1$
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockCollisionWarningDialog.dialogTitle")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockCollisionWarningDialog.warningText")); //$NON-NLS-1$
		
		editLockTable = new EditLockTable(page, SWT.NONE);
		editLockTable.setInput(new String[] {Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockCollisionWarningDialog.loadingLabel")}); //$NON-NLS-1$

		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockCollisionWarningDialog.loadingJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor)
			{
				final List<EditLock> editLocks = EditLockDAO.sharedInstance().getEditLocks(
						acquireEditLockResult.getEditLock().getLockedObjectID(),
						FETCH_GROUPS_EDIT_LOCK,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						monitor);

				editLocks.remove(acquireEditLockResult.getEditLock()); // remove our own lock

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						if (editLockTable.isDisposed())
							return;

						if (editLocks.isEmpty()) { // in the mean time (while opening the dialog) the other editLock was released - close the dialog immediately
							close();
							return;
						}

						editLockTable.setInput(editLocks);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		job.schedule();

		return page;
	}
}
