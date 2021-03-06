/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.login.ui.action;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.login.ui.LoginStateChangeEvent;
import org.nightlabs.jfire.base.login.ui.LoginStateListener;

/**
 * Provides login-state-dependency for WorkbenchWindowActions which are
 * actions contributed into the workbench window menu or tool bar.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class LSDWorkbenchWindowActionDelegate
implements IWorkbenchWindowActionDelegate, IViewActionDelegate, LoginStateListener
{
	// private static final Logger logger = Logger.getLogger(LSDWorkbenchWindowActionDelegate.class);
	private IWorkbenchWindow window;
	private IViewPart viewPart;

	/**
	 * Default implementation of dispose removes this instance
	 * as LoginStateListener, so make sure to always call super.dispose().
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		try {
			Login.getLogin(false).removeLoginStateListener(this);
		} catch (LoginException e) {
			throw new RuntimeException("Login.getLogin(false) should never throw this exception!", e); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the IWorkbenchWindow passed in {@link #init(IWorkbenchWindow)}
	 * @return
	 */
	protected IWorkbenchWindow getWindow() {
		return window;
	}
	
	@Override
	public void init(IViewPart viewPart) {
		this.viewPart = viewPart;
	}

	/**
	 * Default implementation of init remembers the
	 * passed IWorkbenchWindow and makes it accessible
	 * through {@link #getWindow()}
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		this.window = window;
		_init();
	}

	private void _init()
	{
		// normally it should not happen, but it just happened to me on a windows machine :-( therefore we need these null checks. Marco.
		if (window.getActivePage() == null || window.getActivePage().getActivePart() == null || window.getActivePage().getActivePart().getSite() == null)
		{
			Display.getDefault().timerExec(300, new Runnable() {
				public void run()
				{
					_init();
				}
			});
			return;
		}

		ISelectionProvider oldProvider = window.getActivePage().getActivePart().getSite().getSelectionProvider();
		ISelectionProvider test = new ISelectionProvider(){
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}
			public ISelection getSelection() {
				return new StructuredSelection();
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
			}
		};
		try {
			window.getActivePage().getActivePart().getSite().setSelectionProvider(test);
			test.setSelection(null);
		} finally {
			window.getActivePage().getActivePart().getSite().setSelectionProvider(oldProvider);
		}
	}

	/**
	 * Has to be implemented.
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public abstract void run(IAction action);

	/**
	 * Subclasses may override this but have to make sure
	 * super.selectionChanged(action,selection) is called to
	 * further provide login-state-dependency
	 *
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Do we need really to add the listener on each selection changed, IMHO it would enough to add it once in init
		Login.sharedInstance().addLoginStateListener(this, action);
	}

	/**
	 * Default implementation of loginStateChanged disables the action if the user is logged out,
	 * if your implementation needs a different behavior override this method.
	 * To keep this behavior subclasses should therefore always call super.loginStateChanged(event)
	 * when overriding.
	 *
	 * @see LoginStateListener#afterLoginStateChange(int, int, IAction)
	 */
	public void loginStateChanged(LoginStateChangeEvent event) {
		event.getAction().setEnabled(Login.isLoggedIn());
	}

	/**
	 * Returns the Shell 
	 * @return Shell
	 */
	protected Shell getShell()
	{
		if (viewPart == null)
			return getWindow().getShell();
		else
			return viewPart.getSite().getShell();
	}
}
