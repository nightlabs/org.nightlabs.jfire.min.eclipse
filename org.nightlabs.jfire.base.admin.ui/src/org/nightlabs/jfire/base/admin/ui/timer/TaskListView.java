package org.nightlabs.jfire.base.admin.ui.timer;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.selection.SelectionProviderProxy;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.login.ui.part.LSDViewPart;

public class TaskListView
extends LSDViewPart
{
	public static final String ID_VIEW = TaskListView.class.getName();

	private TaskListComposite taskListComposite;
	private SelectionProviderProxy selectionProviderProxy = new SelectionProviderProxy();
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getSite().setSelectionProvider(selectionProviderProxy);
	}

	@Override
	public void createPartContents(Composite parent)
	{
		taskListComposite = new TaskListComposite(parent, SWT.NONE);
		taskListComposite.setSelectionZone(ID_VIEW);
		taskListComposite.loadTasks();
		
		
		// Allow for context-menus to directly perform activate/deactivate actions on selected Tasks from the table.
		taskListComposite.addContextMenuContribution(this, new TimerTaskActivateControlAction(), "Activate task(s)", SharedImages.getSharedImageDescriptor(BaseAdminPlugin.getDefault(), TimerTaskActivateControlAction.class));
		taskListComposite.addContextMenuContribution(this, new TimerTaskDeactivateControlAction(), "Deactivate task(s)", SharedImages.getSharedImageDescriptor(BaseAdminPlugin.getDefault(), TimerTaskDeactivateControlAction.class));
		
		// Use the prioriy-ordered menu framework to integrate the registered menus.
		IDoubleClickListener doubleClickListener = taskListComposite.integratePriorityOrderedContextMenu(taskListComposite, null, taskListComposite.getTableViewer().getControl());
		taskListComposite.addDoubleClickListener(doubleClickListener);
		
		// And finally...
		selectionProviderProxy.addRealSelectionProvider(taskListComposite);
	}
	
	@Override
	public void setFocus()
	{
		// TODO Auto-generated method stub
	}
	
}
