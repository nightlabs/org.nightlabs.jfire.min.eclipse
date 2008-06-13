package org.nightlabs.jfire.base.ui.login;

import org.nightlabs.base.ui.config.ScreenShotCfMod;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.config.Config;
import org.nightlabs.jfire.base.ui.login.LoginStateChangeEvent;
import org.nightlabs.jfire.base.ui.login.LoginStateListener;


/**
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */
public class LoginStateListenerForScreenShotCfg 
implements LoginStateListener 
{
	@Override
	public void loginStateChanged(LoginStateChangeEvent event) {
		if (event.getNewLoginState() == LoginState.LOGGED_IN)
			loadCfg();
		if (event.getNewLoginState() == LoginState.ABOUT_TO_LOG_OUT)
			saveCfg();
	}

	private void loadCfg() 
	{
		ScreenShotCfMod screeShotCfMod = Config.sharedInstance().createConfigModule(ScreenShotCfMod.class);
		// TODO: set the value according to access right of the user
		screeShotCfMod.setConfigScreenShotAllowed(false);
	}

	private void saveCfg() 
	{
		// TODO: Not really needed. JFire saves all changed configs when it exits.
		Config.sharedInstance().save();
	}

}
