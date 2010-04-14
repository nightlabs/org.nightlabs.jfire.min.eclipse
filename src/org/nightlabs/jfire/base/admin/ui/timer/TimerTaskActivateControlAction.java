package org.nightlabs.jfire.base.admin.ui.timer;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.nightlabs.jfire.timer.Task;

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
		
		// We shall assume that at this point, the Tasks has already been loaded and are valid.
		for (Task selectedTask : selectedTasks) {
			if (!selectedTask.isEnabled()) {
				selectedTask.setEnabled(true);
				
				// Now we save it.
				// This should then inform whichever listeners that are interested that something about the Task has changed.
				saveTimerTask(selectedTask);
			}
		}
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
