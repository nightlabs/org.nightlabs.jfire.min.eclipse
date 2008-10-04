package org.nightlabs.jfire.base.ui.jdo.cache;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.nightlabs.jfire.base.jdo.cache.Cache;

public class RefreshAllAction
implements IWorkbenchWindowActionDelegate
{
	@Override
	public void dispose() {
		// nothing to do
	}

	private IWorkbenchWindow window;

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		Cache.sharedInstance().refreshAll();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
	}

}
