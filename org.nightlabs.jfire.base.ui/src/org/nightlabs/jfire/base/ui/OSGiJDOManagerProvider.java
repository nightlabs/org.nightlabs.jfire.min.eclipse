package org.nightlabs.jfire.base.ui;

import org.nightlabs.jfire.base.jdo.GlobalJDOManagerProvider;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleManagerRCP;

public class OSGiJDOManagerProvider extends GlobalJDOManagerProvider {
	@Override
	protected JDOLifecycleManager createLifecycleManager() {
		return new JDOLifecycleManagerRCP();
	}
}
