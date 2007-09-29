package org.nightlabs.jfire.base.admin.timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.login.part.LSDViewPart;

public class TaskListView
extends LSDViewPart
{
	public static final String ID_VIEW = TaskListView.class.getName();

	private TaskListComposite taskListComposite;

	@Implement
	public void createPartContents(Composite parent)
	{
		taskListComposite = new TaskListComposite(parent, SWT.NONE);
		taskListComposite.setSelectionZone(ID_VIEW);
		taskListComposite.loadTasks();
	}
	
	@Override
	public void setFocus()
	{
		// TODO Auto-generated method stub
	}

}
