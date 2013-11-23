/**
 * 
 */
package org.nightlabs.jfire.base.login.ui;

import org.apache.log4j.Logger;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.jfire.base.jdo.GlobalJDOManagerProvider;
import org.nightlabs.jfire.base.jdo.JDOManagerProvider;
import org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor;

/**
 * @author daniel
 *
 */
public class JDOObjectID2PCClassNotificationInterceptorLoginStateListener implements LoginStateListener {

	private static final Logger logger = Logger.getLogger(JDOObjectID2PCClassNotificationInterceptorLoginStateListener.class);
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.login.ui.LoginStateListener#loginStateChanged(org.nightlabs.jfire.base.login.ui.LoginStateChangeEvent)
	 */
	@Override
	public void loginStateChanged(LoginStateChangeEvent event) 
	{
		JDOObjectID2PCClassNotificationInterceptor objectID2PCClassNotificationInterceptor = Login.sharedInstance().getObjectID2PCClassNotificationInterceptor();
		if (LoginState.LOGGED_IN == event.getNewLoginState()) {
			JDOManagerProvider jdoManagerProvider = GlobalJDOManagerProvider.sharedInstance();
			try {
				jdoManagerProvider.getCache().open(Login.sharedInstance().getSessionID()); // the cache is opened implicitely now by default, but it is closed *after* a logout.
			} catch (Throwable t) {
				logger.error("Cache could not be opened!", t); //$NON-NLS-1$
			}
			if (objectID2PCClassNotificationInterceptor == null) {
				JDOObjectID2PCClassNotificationInterceptor interceptor = new JDOObjectID2PCClassNotificationInterceptor(jdoManagerProvider);
				Login.sharedInstance().setObjectID2PCClassNotificationInterceptor(interceptor);
				SelectionManager.sharedInstance().addInterceptor(interceptor);
				jdoManagerProvider.getLifecycleManager().addInterceptor(interceptor);
			}
		}
		if (LoginState.LOGGED_OUT == event.getNewLoginState() && objectID2PCClassNotificationInterceptor != null) {
			JDOManagerProvider jdoManagerProvider = GlobalJDOManagerProvider.sharedInstance();
			SelectionManager.sharedInstance().removeInterceptor(objectID2PCClassNotificationInterceptor);
			jdoManagerProvider.getLifecycleManager().removeInterceptor(objectID2PCClassNotificationInterceptor);
			Login.sharedInstance().setObjectID2PCClassNotificationInterceptor(null);
		}
	}

}
