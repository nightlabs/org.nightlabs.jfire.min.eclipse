package org.nightlabs.jfire.base.admin.ui.timer;

import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.FadeableComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.timepattern.TimePatternSetComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.timepattern.TimePatternSetJDOImpl;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.dao.TaskDAO;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.notification.NotificationAdapterCallerThread;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

public class TaskDetailComposite
extends FadeableComposite
implements ISelectionProvider // needed for updating ViewActions
{
	private Button checkBoxEnabled;
	private TimePatternSetComposite timePatternSetComposite;
//	private Label labelDirty;

	public TaskDetailComposite(Composite parent)
	{
		super(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);

//		labelDirty = new Label(this, SWT.NONE);
//		labelDirty.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		checkBoxEnabled = new Button(this, SWT.CHECK);
		checkBoxEnabled.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailComposite.checkBoxEnabled.text")); //$NON-NLS-1$
		checkBoxEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				taskEnabled = checkBoxEnabled.getSelection();
			}
		});

		timePatternSetComposite = new TimePatternSetComposite(this, SWT.NONE);

		setTaskID(null);

		JDOLifecycleManager.sharedInstance().addNotificationListener(Task.class, changeListener);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e)
			{
				JDOLifecycleManager.sharedInstance().removeNotificationListener(Task.class, changeListener);
			}
		});
	}

	private NotificationListener changeListener = new NotificationAdapterCallerThread() {
		public void notify(org.nightlabs.notification.NotificationEvent notificationEvent) {
//			DirtyObjectID dirtyObjectID = (DirtyObjectID) notificationEvent.getFirstSubject();
//			TaskID taskID = (TaskID) dirtyObjectID.getObjectID();
//			
//			if (taskID != null && taskID.equals(getTaskID())) {
//				// cause a reload from server
//				setTaskID(taskID);
//			}
			
			// It seems that we are not properly processing what we receive here.
			// There seems to be more than one TaskID in the dirtyObjects received, and that among them, one will
			// match getTaskID().
			Set<?> subjects = notificationEvent.getSubjects();
			TaskID currentTaskID = getTaskID();
			if (subjects != null && !subjects.isEmpty() && currentTaskID != null) {
				// Search for the correct TaskID to process. Ignore all others.
				for (Object object : subjects)
					if (object instanceof DirtyObjectID) {
						TaskID taskID = (TaskID) ((DirtyObjectID) object).getObjectID();
						if (taskID.equals(currentTaskID)) {
							setTaskID(taskID);
							break;
						}
					}
			}
			
//			if (taskID != null) {
//				System.err.println("::: Received @TaskDetailComposite's changeListener: taskID = " + taskID.taskID);
//				System.err.println(":::                                        --> getTaskID() = " + getTaskID().taskID);
//				
//				System.err.println(":::                                             --> size() = " + notificationEvent.getSubjects().size());
//				for (Object object : subjects) {
//					System.err.println("::: ---- >> object.class: " + object.getClass().getName());
//					if (object instanceof DirtyObjectID) {
//						TaskID tID = (TaskID) ((DirtyObjectID) object).getObjectID();
//						System.err.println("::: ---- ---- >> tID: " + tID.taskID);
//					}
//				}
//			}
		}
	};

	/**
	 * The currently selected task.
	 */
	private Task task;
	private boolean taskEnabled;

	public static final String[] FETCH_GROUPS_TASK = {
		FetchPlan.DEFAULT,
		Task.FETCH_GROUP_NAME,
		Task.FETCH_GROUP_DESCRIPTION,
		Task.FETCH_GROUP_TIME_PATTERN_SET,
		Task.FETCH_GROUP_USER,
		TimePatternSetJDOImpl.FETCH_GROUP_TIME_PATTERNS};

	/**
	 * You can override this method in order to retrieve a {@link Task} with
	 * more fetchgroups than defined by {@link #FETCH_GROUPS_TASK}.
	 *
	 * @return Returns <code>null</code> or a String array with the desired addtional fetch groups.
	 */
	protected String[] getAdditionalFetchGroupsTask() { return null; }

	private String[] getFetchGroupsTask()
	{
		String[] fetchGroups = getAdditionalFetchGroupsTask();
		if (fetchGroups == null)
			fetchGroups = FETCH_GROUPS_TASK;
		else {
			Set<String> fgSet = CollectionUtil.array2HashSet(FETCH_GROUPS_TASK);
			fgSet.addAll(CollectionUtil.array2ArrayList(fetchGroups));
			fetchGroups = CollectionUtil.collection2TypedArray(fgSet, String.class);
		}
		return fetchGroups;
	}

	public Task getTask()
	{
		return task;
	}

	public TaskID getTaskID()
	{
		return (TaskID) JDOHelper.getObjectId(task);
	}

	private Job currentJob;

	public void setTaskID(final TaskID taskID)
	{
		currentJob = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailComposite.setTaskID.job.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor)
			{
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						setFaded(true);
						fireSelectionChangedEvent();
					}
				});

				Task newTask = null;
				if (taskID != null)
//					newTask = Util.cloneSerializable(
//							TaskProvider.sharedInstance().getTask(taskID, getFetchGroupsTask(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT));
					newTask = Util.cloneSerializable(
							TaskDAO.sharedInstance().getTask(
									taskID, getFetchGroupsTask(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor));

				final Task finalNewTask = newTask;
				final Job thisJob = this;
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						try {
							if (thisJob != currentJob)
								return;

							currentJob = null;

							task = finalNewTask;
							taskEnabled = finalNewTask == null ? false : finalNewTask.isEnabled();
							updateUI();
						} finally {
							setFaded(false);
							fireSelectionChangedEvent();
						}
					}
				});

				return Status.OK_STATUS;
			}
		};
		currentJob.setPriority(Job.SHORT);
		currentJob.schedule();
	}

	protected void updateUI()
	{
		if (!isDisposed()) {
			checkBoxEnabled.setSelection(false);
			timePatternSetComposite.setTimePatternSet(null);

			if (task != null) {
				checkBoxEnabled.setSelection(taskEnabled);
				timePatternSetComposite.setTimePatternSet(task.getTimePatternSet());
			}

			setEnabled(task != null);			
		}
	}

	public void createTimePattern()
	{
		timePatternSetComposite.createTimePattern();
	}

	public void removeSelectedTimePatterns()
	{
		timePatternSetComposite.removeSelectedTimePatterns();
	}

	public void submit()
	{
		if (task == null)
			return;

		setFaded(true);

		final Task _task = this.task;
		Job currentJob = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailComposite.submit.job.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailComposite.submit.monitor.taskName_storing"), 2); //$NON-NLS-1$

				monitor.setTaskName(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailComposite.submit.monitor.taskName_calculatingNextExecDT")); //$NON-NLS-1$
				_task.setEnabled(taskEnabled); // setEnabled calls calculateNextExecDT
//				task.calculateNextExecDT(); // in case the time pattern has changed
				monitor.worked(1);
				monitor.setTaskName(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailComposite.submit.monitor.taskName_storingToServer")); //$NON-NLS-1$

				try {
					Task newTask = TaskDAO.sharedInstance().storeTask(
							_task, true, getFetchGroupsTask(), NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);

					monitor.worked(1);

					final Task clonedNewTask = Util.cloneSerializable(newTask);
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							try {
								if (!Util.equals(task, clonedNewTask))
									return;

								task = clonedNewTask;
								taskEnabled = clonedNewTask == null ? false : task.isEnabled();
								updateUI();
							} finally {
								fireSelectionChangedEvent();
							}
						}
					});

				} catch (Exception x) {
					throw new RuntimeException(x);
				} finally {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							setFaded(false);
						}
					});
				}

				monitor.done();
				return Status.OK_STATUS;
			}
		};
		fireSelectionChangedEvent();
		currentJob.setUser(true);
		currentJob.setPriority(Job.SHORT);
		currentJob.schedule();
	}

	private ListenerList selectionChangedListeners = new ListenerList();

	protected void fireSelectionChangedEvent()
	{
		if (selectionChangedListeners.isEmpty())
			return;

		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());

		Object[] listeners = selectionChangedListeners.getListeners();
		for (Object lo : listeners) {
			ISelectionChangedListener l = (ISelectionChangedListener) lo;
			l.selectionChanged(event);
		}
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		selectionChangedListeners.add(listener);
	}

	public ISelection getSelection()
	{
		if (getTask() == null || isFaded())
			return new StructuredSelection(new Task[] {});

		return new StructuredSelection(getTask());
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		selectionChangedListeners.remove(listener);
	}

	public void setSelection(ISelection selection)
	{
		throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
	}

	/**
	 * Sets the task directly (without the fetch job that gets it)
	 * and updates the UI. Make sure you pass an properly detached
	 * task (fetch-groups like {@link #FETCH_GROUPS_TASK}).
	 *
	 */
	public void setTask(Task task) {
		this.task = task;
		updateUI();
	}
}
