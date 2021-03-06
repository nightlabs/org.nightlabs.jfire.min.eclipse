package org.nightlabs.jfire.base.ui.editlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.editlock.AcquireEditLockResult;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.editlock.ReleaseReason;
import org.nightlabs.jfire.editlock.dao.EditLockDAO;
import org.nightlabs.jfire.editlock.id.EditLockID;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class EditLockMan
{
	private static final Logger logger = Logger.getLogger(EditLockMan.class);

	private static EditLockMan sharedInstance = null;
	private EditLockDAO editLockDAO = EditLockDAO.sharedInstance();

	public static EditLockMan sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (EditLockMan.class) {
				if (sharedInstance == null)
					sharedInstance = new EditLockMan();
			}
		}
		return sharedInstance;
	}

	private class EditLockRefreshJob extends Job
	{
		boolean aborted = false;
		
		private EditLockCarrier editLockCarrier;
		private EditLockID editLockID;
		private EditLock editLock;
		private EditLockType editLockType;
		private EditLockTypeID editLockTypeID;

		public EditLockRefreshJob(EditLockCarrier editLockCarrier)
		{
			super(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockMan.jobName")); //$NON-NLS-1$
			setEditLockCarrier(editLockCarrier);
		}

		private void setEditLockCarrier(EditLockCarrier editLockCarrier)
		{
			this.editLockCarrier = editLockCarrier;
			this.editLock = editLockCarrier.getEditLock();
			this.editLockID = (EditLockID) JDOHelper.getObjectId(editLock);
			this.editLockType = editLock.getEditLockType();
			this.editLockTypeID = (EditLockTypeID) JDOHelper.getObjectId(editLockType);
		}

		public EditLockCarrier getEditLockCarrier()
		{
			return editLockCarrier;
		}
		public EditLockID getEditLockID()
		{
			return editLockID;
		}
		public EditLock getEditLock()
		{
			return editLock;
		}

		private void handleExpiredUserActivityIfNecessary(ProgressMonitor monitor)
		{
			long now = System.currentTimeMillis();
			if (now >= expiryUserInactivityTimestamp) {
				boolean userInactivityReachedLimit;
				synchronized (editLockCarrierMutex) {
					userInactivityReachedLimit = editLockCarrier.getLastUserActivityDT().getTime() + editLockType.getEditLockExpiryUserInactivityMSec() < now;
					if (userInactivityReachedLimit)
						editLockCarrier.setLastUserActivityDT();
				}
				if (userInactivityReachedLimit) {
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							InactivityAction inactivityAction = InactivityAction.DIALOG_BLOCKING_DUE_TO_INACTIVITY;
							EditLockCallback callbackListener = editLockCarrier.getEditLockCallbackListener();
							if (callbackListener != null)
								inactivityAction = callbackListener.getEditLockAction(editLockCarrier);
							else
								inactivityAction = InactivityAction.DIALOG_BLOCKING_DUE_TO_INACTIVITY;

							switch (inactivityAction) {
							case DIALOG_ABOUT_TO_EXPIRE:
								if (editLockAboutToExpireDialog == null) {
									editLockAboutToExpireDialog = new EditLockAboutToExpireDueToUserInactivityDialog(EditLockMan.this, RCPUtil.getActiveShell());
									editLockAboutToExpireDialog.setBlockOnOpen(false);
									editLockAboutToExpireDialog.open();
								}
								// add the EditLock to the dialog
								editLockAboutToExpireDialog.addEditLockCarrier(editLockCarrier);
								break;
							case DIALOG_BLOCKING_DUE_TO_INACTIVITY:
								if (blockingDueToInactivityDialog == null) {
									blockingDueToInactivityDialog = new BlockingDueToInactivityDialog(EditLockMan.this, RCPUtil.getActiveShell());
									blockingDueToInactivityDialog.setBlockOnOpen(false);
									blockingDueToInactivityDialog.open();
								}
								blockingDueToInactivityDialog.addEditLockCarrier(editLockCarrier);
								break;
							case REFRESH_LOCK:
								editLock.setLastAcquireDT();
								break;
							case RELEASE_LOCK:
								releaseEditLock(editLock.getLockedObjectID());
							default:
							}
						}
					});
				}
			} // if (now >= expiryUserInactivityTimestamp) {
		}

		private void reacquireEditLockOnServerIfNecessary(ProgressMonitor monitor)
		{
			logger.info("reacquireEditLockOnServerIfNecessary: enter"); //$NON-NLS-1$
			if (System.currentTimeMillis() >= expiryClientLostTimestamp) { // it's time to refresh the server-side editLock
				logger.info("reacquireEditLockOnServerIfNecessary: reacquiring"); //$NON-NLS-1$
				AcquireEditLockResult acquireEditLockResult = editLockDAO.acquireEditLock(
						editLockTypeID, editLock.getLockedObjectID(), editLock.getDescription(),
						FETCH_GROUPS_EDIT_LOCK, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				EditLockCarrier newEditLockCarrier = createEditLockCarrier(acquireEditLockResult.getEditLock(), editLockCarrier);
				setEditLockCarrier(newEditLockCarrier);
			}
		}

		@Override
		protected IStatus run(ProgressMonitor monitor) throws Exception
		{
			if (aborted)
				return Status.OK_STATUS;
			
			synchronized (editLockID2Job) {
				if (editLockID2Job.get(editLockID) != this) {
					logger.info("EditLockRefreshJob.run: job has been replaced - cancelling."); //$NON-NLS-1$
					return Status.CANCEL_STATUS; // a new job has already been created - this job is out-of-charge now
				}
			}

			if (!Login.isLoggedIn()) {
				logger.info("EditLockRefreshJob.run: we are not logged in anymore [1] - cancelling."); //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			}

			handleExpiredUserActivityIfNecessary(monitor);

			if (!Login.isLoggedIn()) {
				logger.info("EditLockRefreshJob.run: we are not logged in anymore [2] - cancelling."); //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			}

			reacquireEditLockOnServerIfNecessary(monitor);

			if (!Login.isLoggedIn()) {
				logger.info("EditLockRefreshJob.run: we are not logged in anymore [3] - cancelling."); //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			}

			scheduleWithEditLockTypeDelay();
			return Status.OK_STATUS;
		}

		public void scheduleWithEditLockTypeDelay()
		{
			EditLockType editLockType = editLock.getEditLockType();
			long now = System.currentTimeMillis();

			// Calculate when we need to wake-up in order to re-acquire the lock on the server in time.
			expiryClientLostTimestamp = editLock.getLastAcquireDT().getTime() + editLockType.getEditLockExpiryClientLostMSec();
			// TODO we should somehow handle time differences between client and server... As the time should be UTC (according to the javadoc of java.util.Date), this
			// should work fine if client + server are synchronized with an internet clock, but otherwise this causes problems. We need to change this code
			// somehow in order to take a wrong client-clock into account!
			long delayClientLost = expiryClientLostTimestamp - now;
//			long reduction = delayClientLost / 4;
//			reduction = Math.max(reduction, 1L * 60L * 1000L); // 1 minute before the server releases - is hopefully enough to react in time TODO should be configurable!
			long reduction = 2L * 60L * 1000L; // 2 minutes before the server releases - is hopefully enough to react in time TODO should be configurable!
			delayClientLost = delayClientLost - reduction;
			if (delayClientLost < 0) delayClientLost = 0;
			expiryClientLostTimestamp = now + delayClientLost;

			// Calculate when we need to wake-up in order to ask the user
			expiryUserInactivityTimestamp = editLockCarrier.getLastUserActivityDT().getTime() + editLockType.getEditLockExpiryUserInactivityMSec();
			// these times are managed within the client alone => no time-difference-problems
			long delayUserInactivity = expiryUserInactivityTimestamp - now;
			if (delayUserInactivity < 0) delayUserInactivity = 0;

			long delay = Math.min(delayClientLost, delayUserInactivity);
			logger.info("scheduleWithEditLockTypeDelay: delay="+delay+" delayClientLost="+delayClientLost + " delayUserInactivity="+delayUserInactivity); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			schedule(delay); // wake up as soon as some action is required
		}
		
		void abort() {
			this.aborted = true;
		}
	}

	private long expiryClientLostTimestamp;
	private long expiryUserInactivityTimestamp;

	private EditLockAboutToExpireDueToUserInactivityDialog editLockAboutToExpireDialog = null;
	private BlockingDueToInactivityDialog blockingDueToInactivityDialog = null;

	protected void onCloseEditLockAboutToExpireDueToUserInactivityDialog()
	{
		this.editLockAboutToExpireDialog = null;
	}

	protected void onCloseBlockingDueToInactivityDialog() {
		this.blockingDueToInactivityDialog = null;
	}

	private Map<EditLockID, EditLockRefreshJob> editLockID2Job = new HashMap<EditLockID, EditLockRefreshJob>();

	private Map<ObjectID, EditLockCarrier> objectID2EditLockCarrier = new HashMap<ObjectID, EditLockCarrier>();
	private Map<EditLockID, EditLockCarrier> editLockID2EditLockCarrier = new HashMap<EditLockID, EditLockCarrier>();
	private Object editLockCarrierMutex = new Object();

	protected EditLockMan()
	{
	}

	private static final String[] FETCH_GROUPS_EDIT_LOCK = {
		FetchPlan.DEFAULT, EditLock.FETCH_GROUP_LOCK_OWNER_USER, EditLock.FETCH_GROUP_EDIT_LOCK_TYPE
	};

	private void createJob(EditLockCarrier editLockCarrier)
	{
		EditLockRefreshJob job = new EditLockRefreshJob(editLockCarrier);
		EditLockID editLockID = job.getEditLockID();
		EditLockRefreshJob oldJob;
		synchronized (editLockID2Job) {
			oldJob = editLockID2Job.put(editLockID, job);
		}
		if (oldJob != null)
			oldJob.cancel();

		job.scheduleWithEditLockTypeDelay();
	}

	void cancelAllJobs()
	{
		synchronized (editLockID2Job) {
			for (EditLockRefreshJob job : editLockID2Job.values())
				job.cancel();

			editLockID2Job.clear();
		}
	}

	private EditLockCarrier createEditLockCarrier(EditLock editLock, EditLockCarrier oldEditLockCarrier)
	{
		EditLockID editLockID = (EditLockID) JDOHelper.getObjectId(editLock);
		EditLockCarrier editLockCarrier = new EditLockCarrier(editLock, oldEditLockCarrier); // copy properties from the old carrier

		synchronized (editLockCarrierMutex) {
			objectID2EditLockCarrier.put(editLock.getLockedObjectID(), editLockCarrier);
			editLockID2EditLockCarrier.put(editLockID, editLockCarrier);
		}

		return editLockCarrier;
	}

	/**
	 * Asynchronously checks for <code>EditLock</code>s of the given type corresponding to the {@link EditLockTypeID}
	 * of the object corresponding to the given {@link ObjectID}.
	 *
	 * @param editLockTypeID The ID referencing the {@link EditLockType} to which the new {@link EditLock} will belong. This parameter is ignored, if the
	 *		<code>EditLock</code> already exists.
	 * @param objectID The ID of the object that shall be locked.
	 * @param description A human-readable description describing the object that is locked.
	 * @param editLockCallback Either <code>null</code> or your callback-implementation.
	 *
	 * @return An {@link EditLockHandle} on the acquired lock that serves to conveniently refresh or release the lock.
	 */
	public EditLockHandle acquireEditLockAsynchronously(final EditLockTypeID editLockTypeID,
			final ObjectID objectID, final String description, final EditLockCallback editLockCallback)
	{
		EditLockHandle handle = new EditLockHandle(editLockTypeID, objectID, description, editLockCallback, (Shell) null);
		Job lockJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockMan.acquireEditLockAsynchronously.job.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				acquireEditLock(editLockTypeID, objectID, description, editLockCallback, monitor);
				return Status.OK_STATUS;
			}
		};

		lockJob.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
//		lockJob.setUser(true); // notify user of checking locks?
		lockJob.schedule();

		return handle;
	}

	/**
	 * This method acquires an <code>EditLock</code> (or refreshs it in order to prevent release due to user-inactivity).
	 *
	 * @param editLockTypeID The ID referencing the {@link EditLockType} to which the new {@link EditLock} will belong. This parameter is ignored, if the
	 *		<code>EditLock</code> already exists.
	 * @param objectID The ID of the object that shall be locked.
	 * @param description A human-readable description describing the object that is locked.
	 * @param editLockCallback Either <code>null</code> or your callback-implementation.
	 * @param monitor As this method synchronously communicates with the server (if necessary), it takes this "tagging" parameter.
	 *
	 * @return An {@link EditLockHandle} on the acquired lock that serves to conveniently refresh or release the lock.
	 */
	public EditLockHandle acquireEditLock(EditLockTypeID editLockTypeID, ObjectID objectID, String description, EditLockCallback editLockCallback, ProgressMonitor monitor)
	{
		EditLockHandle handle = new EditLockHandle(editLockTypeID, objectID, description, editLockCallback, (Shell) null);
		acquireEditLock(handle, monitor);
		return handle;
	}

	/**
	 * This method acquires an <code>EditLock</code> (or refreshs it in order to prevent release due to user-inactivity).
	 *
	 * @param editLockTypeID The ID referencing the {@link EditLockType} to which the new {@link EditLock} will belong. This parameter is ignored, if the
	 *		<code>EditLock</code> already exists.
	 * @param objectID The ID of the object that shall be locked.
	 * @param description A human-readable description describing the object that is locked.
	 * @param editLockCallback Either <code>null</code> or your callback-implementation.
	 * @param parentShell Can be <code>null</code>. If it's undefined, {@link RCPUtil#getActiveShell()} will be used to obtain the parent shell. This shell will be used as parent for the collision-dialog, if the object is already locked by someone else. It is not used
	 * @param monitor As this method synchronously communicates with the server (if necessary), it takes this "tagging" parameter.
	 *
	 * @return An {@link EditLockHandle} on the acquired lock that serves to conveniently refresh or release the lock.
	 */
	public EditLockHandle acquireEditLock(EditLockTypeID editLockTypeID, ObjectID objectID, String description, EditLockCallback editLockCallback, final Shell parentShell, ProgressMonitor monitor)
	{
		EditLockHandle handle = new EditLockHandle(editLockTypeID, objectID, description, editLockCallback, parentShell);
		acquireEditLock(handle, monitor);
		return handle;
	}

	private void acquireEditLock(EditLockHandle handle, ProgressMonitor monitor) {
		EditLockTypeID editLockTypeID = handle.getEditLockTypeID();
		ObjectID objectID = handle.getObjectID();
		String description = handle.getDescription();
		EditLockCallback editLockCallback = handle.getEditLockCallback();
		final Shell parentShell = handle.getShell();

		if (editLockTypeID == null)
			throw new IllegalArgumentException("editLockTypeID must not be null!"); //$NON-NLS-1$

		if (objectID == null)
			throw new IllegalArgumentException("objectID must not be null!"); //$NON-NLS-1$

		if (description == null)
			throw new IllegalArgumentException("description must not be null!"); //$NON-NLS-1$

		// callback can be null => don't check it

//		if (parentShell == null)
//			throw new IllegalArgumentException("parentShell must not be null!");

		if (monitor == null)
			throw new IllegalArgumentException("monitor must not be null!"); //$NON-NLS-1$


		EditLockCarrier oldEditLockCarrier;
		synchronized (editLockCarrierMutex) {
			oldEditLockCarrier = objectID2EditLockCarrier.get(objectID);
			if (oldEditLockCarrier != null) { // If we already manage a lock, we only set the new lastUserActivity
				oldEditLockCarrier.setLastUserActivityDT();
				oldEditLockCarrier.setEditLockCallbackListener(editLockCallback);
			}
		}

		if (oldEditLockCarrier == null) { // we only need to communicate with the server, if the object is not yet locked there. and we don't open a dialog when refreshing - only when new.
			final AcquireEditLockResult acquireEditLockResult;

			monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockMan.aquireLockTask"), 1); //$NON-NLS-1$
			try {
				acquireEditLockResult = editLockDAO.acquireEditLock(
						editLockTypeID, objectID, description, FETCH_GROUPS_EDIT_LOCK, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			} finally {
				monitor.worked(1);
			}

			EditLockCarrier newEditLockCarrier;
			synchronized (editLockCarrierMutex) {
				newEditLockCarrier = createEditLockCarrier(acquireEditLockResult.getEditLock(), null); // we ignore the old carrier and create a clean one
				newEditLockCarrier.setEditLockCallbackListener(editLockCallback);
			}

			if (acquireEditLockResult.getEditLockCount() > 1) { // there's another lock => we show a warning
				Runnable dialogOpener = new Runnable() {
					public void run() {
						Shell pshell = parentShell;
						if (pshell == null)
							pshell = RCPUtil.getActiveShell();

						EditLockCollisionWarningDialog dialog = new EditLockCollisionWarningDialog(pshell, acquireEditLockResult);
						dialog.open();
						dialog.setBlockOnOpen(false);
					}
				};

				if (Display.getCurrent() == null)
					Display.getDefault().asyncExec(dialogOpener);
				else
					dialogOpener.run();
			}
			createJob(newEditLockCarrier);
		}
	}

	private void releaseEditLockWithJob(final List<ObjectID> objectIDs, final ReleaseReason releaseReason)
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.editlock.EditLockMan.releaseLockJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				releaseEditLocks(objectIDs, releaseReason, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	/**
	 * This method uses a {@link Job} to release the {@link EditLock} asynchronously.
	 *
	 * @param objectID The ID of the JDO object which has been locked.
	 * @see #releaseEditLock(ObjectID, IProgressMonitor)
	 *
	 * @deprecated This method will be made protected soon. Use {@link EditLockHandle#release()} instead on the
	 * 	{@link EditLockHandle} acquired by the <code>acquireLock</code> methods of this class.
	 */
	@Deprecated
	public void releaseEditLock(ObjectID objectID)
	{
		ArrayList<ObjectID> objectIDs = new ArrayList<ObjectID>(1);
		objectIDs.add(objectID);
		releaseEditLockWithJob(objectIDs, ReleaseReason.normal);
	}

	/**
	 * This method releases the {@link EditLock} synchronously and might thus block your UI if not called from
	 * within a <code>Job</code> or worker-<code>Thread</code>.
	 *
	 * @param objectID The ID of the JDO object which has been locked.
	 * @see #releaseEditLock(ObjectID)
	 *
	 * @deprecated This method will be made protected soon. Use {@link EditLockHandle#release(ProgressMonitor)} instead on the
	 * 	{@link EditLockHandle} acquired by the <code>acquireLock</code> methods of this class.
	 */
	@Deprecated
	public void releaseEditLock(ObjectID objectID, ProgressMonitor monitor)
	{
		ArrayList<ObjectID> objectIDs = new ArrayList<ObjectID>(1);
		objectIDs.add(objectID);
		releaseEditLocks(objectIDs, ReleaseReason.normal, monitor);
	}

	private void releaseEditLocks(List<ObjectID> objectIDs, ReleaseReason releaseReason, ProgressMonitor monitor)
	{
		for (ObjectID objectID : objectIDs) {
			EditLockCarrier editLockCarrier;
			synchronized (editLockCarrierMutex) {
				editLockCarrier = objectID2EditLockCarrier.remove(objectID);
				if (editLockCarrier != null) {
					editLockCarrier.setLastUserActivityDT();
					EditLockID editLockID = (EditLockID) JDOHelper.getObjectId(editLockCarrier.getEditLock());
					if (editLockID == null)
						throw new IllegalStateException("JDOHelper.getObjectId(editLockCarrier.getEditLock()) removed null!");

					editLockID2EditLockCarrier.remove(editLockID);
				}
			}

			if (editLockCarrier != null) {
				Job job;
				synchronized (editLockID2Job) {
					job = editLockID2Job.remove(JDOHelper.getObjectId(editLockCarrier.getEditLock()));
				}
				if (job != null && (job instanceof EditLockRefreshJob)) {
					((EditLockRefreshJob) job).abort();
				}
			}

			editLockDAO.releaseEditLock(objectID, ReleaseReason.normal, monitor);
		}
	}

	void processEditLockAction(EditLockCarrier carrier, ProcessLockAction action) {
		EditLockCallback callback = carrier.getEditLockCallbackListener();
		callCallbackMethod(action, callback);

		if (action == ProcessLockAction.RELEASE_AND_DISCARD || action == ProcessLockAction.RELEASE_AND_SAVE)
			releaseEditLock(carrier.getEditLock().getLockedObjectID());
		else if (action == ProcessLockAction.REFRESH_AND_CONTINUE)
			carrier.getEditLock().setLastAcquireDT();
	}

	private void callCallbackMethod(ProcessLockAction action, EditLockCallback callback) {
		if (callback == null)
			return;

		switch (action) {
		case REFRESH_AND_CONTINUE:
			callback.doContinueAndRefresh();
			break;
		case RELEASE_AND_DISCARD:
			callback.doDiscardAndRelease();
			break;
		case RELEASE_AND_SAVE:
			callback.doSaveAndRelease();
			break;
		}
	}
}
