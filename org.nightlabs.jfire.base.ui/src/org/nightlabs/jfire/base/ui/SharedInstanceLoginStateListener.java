/**
 * 
 */
package org.nightlabs.jfire.base.ui;

import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.jfire.base.login.ui.LoginStateChangeEvent;
import org.nightlabs.jfire.base.login.ui.LoginStateListener;

/**
 * @author daniel
 *
 */
public class SharedInstanceLoginStateListener implements LoginStateListener {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.login.ui.LoginStateListener#loginStateChanged(org.nightlabs.jfire.base.login.ui.LoginStateChangeEvent)
	 */
	@Override
	public void loginStateChanged(LoginStateChangeEvent event) 
	{
		if (LoginState.LOGGED_IN == event.getNewLoginState()) {
			JFireBasePlugin.getDefault().init();
		}
	}

}
