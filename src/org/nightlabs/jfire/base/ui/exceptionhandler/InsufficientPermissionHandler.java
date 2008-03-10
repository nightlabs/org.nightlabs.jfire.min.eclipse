/**
 * 
 */
package org.nightlabs.jfire.base.ui.exceptionhandler;

import org.nightlabs.base.ui.exceptionhandler.DefaultErrorDialog;
import org.nightlabs.base.ui.exceptionhandler.ErrorDialogFactory;
import org.nightlabs.base.ui.exceptionhandler.IExceptionHandler;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class InsufficientPermissionHandler implements IExceptionHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window.IExceptionHandler#handleException(java.lang.Throwable)
	 */
	@Override
	public void handleException(Thread thread, Throwable thrownException, Throwable triggerException) {		
		ErrorDialogFactory.showError(DefaultErrorDialog.class, 
				"Not Enough Permissions", 
				"You don't have enough permissions for the following action", 
				thrownException, triggerException);
	}

}
