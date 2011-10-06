/**
 * 
 */
package org.nightlabs.jfire.rap;

import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.context.IUIContextRunner;
import org.nightlabs.jfire.compatibility.SessionStoreRegistry;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [dOt] de -->
 *
 */
public class RAPUIContextRunner implements IUIContextRunner {

	private Display display;
	private ISessionStore sessionStore;
	
	/**
	 * 
	 */
	public RAPUIContextRunner(Display display, ISessionStore sessionStore) {
		this.display = display;
		this.sessionStore = sessionStore;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.context.IUIContextRunner#getDisplay()
	 */
	@Override
	public Display getDisplay() {
		return display;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.context.IUIContextRunner#runInUIContext(java.lang.Runnable)
	 */
	@Override
	public void runInUIContext(Runnable runnable) {
		ISessionStore savedSessionStore = SessionStoreRegistry.getSessionStore();
		if (savedSessionStore == null) {
			SessionStoreRegistry.associateThread(Thread.currentThread(), sessionStore);
		}
		try {
			UICallBack.runNonUIThreadWithFakeContext(display, runnable);
		} finally {
			if (savedSessionStore == null) {
				SessionStoreRegistry.disposeThread(Thread.currentThread());
			}
		}
	}

}
