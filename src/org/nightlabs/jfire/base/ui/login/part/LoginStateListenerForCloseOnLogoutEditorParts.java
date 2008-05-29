/**
 * 
 */
package org.nightlabs.jfire.base.ui.login.part;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent;
import org.nightlabs.jfire.base.ui.login.LoginStateListener;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class LoginStateListenerForCloseOnLogoutEditorParts 
implements LoginStateListener 
{
	@Override
	public void loginStateChanged(LoginStateChangeEvent event) {
		if (event.getNewLoginState() == LoginState.ABOUT_TO_LOG_OUT)
			closeAllEditors();
	}

	private void closeAllEditors() 
	{
		IWorkbench wb = PlatformUI.getWorkbench();
		for (IWorkbenchWindow win : wb.getWorkbenchWindows()) {
			for (IWorkbenchPage page : win.getPages()) {
				for (IEditorReference reference : page.getEditorReferences()) {
					IEditorPart editor = reference.getEditor(false);
					if (editor instanceof ICloseOnLogoutEditorPart)
						page.closeEditor(editor, true);
				}
			}			
		}
	}
	
}
