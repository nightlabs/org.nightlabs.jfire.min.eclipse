package org.nightlabs.jfire.base.admin.ui.timer;

import java.util.List;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Activates the list of selected Timer-{@link Task}s, from the selected entries in the {@link TaskListComposite}.
 *
 * @author khaireel at nightlabs dot de
 */
public class TimerTaskActivateControlAction extends TimerTaskControlAction {
	private IViewPart view;	
	private List<Task> selectedTasks = null;
	
	@Override
	public void init(IViewPart view) { this.view = view; }

	@Override
	public void run(IAction action) {
		if (selectedTasks == null || selectedTasks.isEmpty())
			return;
		
		// Update only those necessary ones.
		Job meJob = new Job("Updating Task...") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				for (Task selectedTask : selectedTasks) {
					if (!selectedTask.isEnabled()) {
						selectedTask = getTask((TaskID) JDOHelper.getObjectId(selectedTask), monitor);
						selectedTask.setEnabled(true);
						
						// Now we save it.
						// This should then inform whichever listeners that are interested that something about the Task has changed.
						saveTimerTask(selectedTask, monitor);
					}
				}

				monitor.worked(1);
				return Status.OK_STATUS;
			}
		};
		
		meJob.setUser(true);
		meJob.setPriority(Job.SHORT);
		meJob.schedule();		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		selectedTasks = retrieveTasksFromSelection(selection);
		
		// If we received multiple Tasks, check to see that at least ONE of them has been deactivated,
		// in order to enable this Action.
		boolean isMakeThisActionEnabled = false;
		if (selectedTasks != null) {
			for (Task selTask : selectedTasks)
				isMakeThisActionEnabled |= !selTask.isEnabled();
		}
		
		action.setEnabled(isMakeThisActionEnabled);
	}
}
