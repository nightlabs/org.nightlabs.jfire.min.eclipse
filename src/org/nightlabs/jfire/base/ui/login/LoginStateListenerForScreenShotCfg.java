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
	private Config config;

	@Override
	public void loginStateChanged(LoginStateChangeEvent event) {
		if (event.getNewLoginState() == LoginState.LOGGED_IN)
			loadCfg();
		if (event.getNewLoginState() == LoginState.ABOUT_TO_LOG_OUT)
			saveCfg();
	}

	private void loadCfg() 
	{
//		// TODO:  API for querying access rights on the client side
//		// Logical Understanding
//		// create and get a shared instance:
//		config = Config.createSharedInstance( "screenshotCfg.xml",
//				true,
//				System.getProperty("user.home"));
//		// create the config module lazily:
//		ScreenShotCfMod myConfigModule = (ScreenShotCfMod)config
//		.createConfigModule(ScreenShotCfMod.class);
//		// set the value
//		// the value will be read from the access rights API
//		myConfigModule.setConfigScreenShotAllowed(false);
	}

	private void saveCfg() 
	{ 
//		// save the config 
//		config.save(true);
	}

}
