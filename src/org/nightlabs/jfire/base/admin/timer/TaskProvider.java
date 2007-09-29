package org.nightlabs.jfire.base.admin.timer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.TimerManager;
import org.nightlabs.jfire.timer.TimerManagerUtil;
import org.nightlabs.jfire.timer.id.TaskID;

public class TaskProvider
		extends JDOObjectProvider
{
	private static TaskProvider _sharedInstance = null;
	public static TaskProvider sharedInstance() {
		if (_sharedInstance == null)
			_sharedInstance = new TaskProvider();

		return _sharedInstance;
	}

	public Task getTask(TaskID taskID, String[] fetchGroups, int maxFetchDepth)
	{
		return (Task) getJDOObject(null, taskID, fetchGroups, maxFetchDepth);
	}

	@Override
	protected Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups, int maxFetchDepth)
			throws Exception
	{
		TimerManager timerManager = TimerManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		LinkedList<TaskID> taskIDs = new LinkedList<TaskID>();
		taskIDs.add((TaskID) objectID);
		List<Task> tasks = timerManager.getTasks(taskIDs, fetchGroups, maxFetchDepth);
		return tasks.get(0);
	}

	private TimerManager timerManager;

	public synchronized List<Task> getTasks(String[] fetchGroups, int maxFetchDepth)
	// this method is synchronized because of the object variable ipanema1BaseManager
	{
		try {
			timerManager = TimerManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			try {
				List<TaskID> promoterIDs = timerManager.getTaskIDs();
	
				return (List<Task>) getJDOObjects(null, promoterIDs, fetchGroups, maxFetchDepth);
			} finally {
				timerManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	protected Collection retrieveJDOObjects(String scope, Set objectIDs, String[] fetchGroups, int maxFetchDepth)
			throws Exception
	{
		try {
			return timerManager.getTasks(objectIDs, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
