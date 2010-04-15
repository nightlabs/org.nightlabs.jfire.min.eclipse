package org.nightlabs.jfire.base.admin.ui.timer;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.dao.TaskDAO;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * The super class to unite the more generalised methods when carrying out specific actions.
 *
 * @author khaireel at nightlabs dot de
 */
public abstract class TimerTaskControlAction implements IViewActionDelegate {
	private String[] fetchGroupTimerTasks = TaskDetailComposite.FETCH_GROUPS_TASK; // Default.
	
	/**
	 * Spawns a job to save the given {@link Task}.
	 */
	protected void saveTimerTask(final Task _task, ProgressMonitor monitor) {
		TaskDAO.sharedInstance().storeTask(
				_task, true, 
				fetchGroupTimerTasks,  
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor
		);

		monitor.worked(1);
	}
	
	/**
	 * Sets the fetch-group for retrieving Tasks.
	 */
	protected void setFetchGroupTimerTasks(String[] fetchGroupTimerTasks) {
		this.fetchGroupTimerTasks = fetchGroupTimerTasks;
	}
	
	protected Task getTask(TaskID taskID, ProgressMonitor monitor) {
		return TaskDAO.sharedInstance().getTask(taskID, fetchGroupTimerTasks, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
	
	/**
	 * @return a List of {@link Task}s from the given {@link ISelection}.
	 */
	protected List<Task> retrieveTasksFromSelection(ISelection selection) {
		// Guard 1.
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
			return null;
		
		// Guard 2.
		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel == null || sel.isEmpty())
			return null;
		

		// Gather only Tasks, and return them for processing.
		List<Task> selTasks = new LinkedList<Task>();
		for (Object selObj : sel.toList()) {
			if (selObj instanceof Task)
				selTasks.add((Task) selObj);
		}
		
		return selTasks;
	}
}
