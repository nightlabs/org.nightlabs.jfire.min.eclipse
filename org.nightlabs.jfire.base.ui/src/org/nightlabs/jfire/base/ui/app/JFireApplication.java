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

package org.nightlabs.jfire.base.ui.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.app.AbstractApplication;
import org.nightlabs.base.ui.app.AbstractWorkbenchAdvisor;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.base.login.ui.ILoginHandler;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.login.ui.LoginAbortedException;
import org.nightlabs.jfire.base.login.ui.LoginHandlerRegistry;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.singleton.ISingletonProvider;
import org.nightlabs.singleton.SingletonProviderFactory;
import org.nightlabs.singleton.ISingletonProvider.ISingletonFactory;
import org.nightlabs.util.IOUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * JFireApplication is the main executed class {@see JFireApplication#run(Object)}.
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class JFireApplication
extends AbstractApplication
{
	public static final String PLUGIN_ID = "org.nightlabs.jfire.base.ui"; //$NON-NLS-1$

	/**
	 * The logger instance used in this class.
	 */
	protected static final Logger logger = Logger.getLogger(JFireApplication.class);

	private static ISingletonProvider<LinkedList<JFireApplicationListener>> _applicationListener;

	private static synchronized ISingletonProvider<LinkedList<JFireApplicationListener>> getApplicationListener()
	{
		if (_applicationListener == null) {
			_applicationListener = SingletonProviderFactory.createProviderForFactory(new ISingletonFactory<LinkedList<JFireApplicationListener>>() {
				@Override
				public LinkedList<JFireApplicationListener> makeInstance() {
					return new LinkedList<JFireApplicationListener>();
				}
			});
		}
		return _applicationListener;
	}

	public static void addApplicationListener(JFireApplicationListener listener) {
		getApplicationListener().getInstance().add(listener);
	}

	public static void removeApplicationListener(JFireApplicationListener listener) {
		getApplicationListener().getInstance().remove(listener);
	}

	public static final int APPLICATION_EVENTTYPE_STARTED = 1;

	private ServiceReference loginHandler;

	void notifyApplicationListeners(int applicationEventType) {
		for (Iterator<JFireApplicationListener> iter = new ArrayList<JFireApplicationListener>(getApplicationListener().getInstance()).iterator(); iter.hasNext();) {
			JFireApplicationListener listener = iter.next();
			switch (applicationEventType) {
				case APPLICATION_EVENTTYPE_STARTED:
					listener.applicationStarted();
					break;
			}
		}
	}

	public String initApplicationName() {
		return "jfire"; //$NON-NLS-1$
	}

	@Override
	public void preCreateWorkbench()
	{
		try {
			// initialise truststore
			initSSLTruststore();

			initLogin();

			LanguageManager.sharedInstance().setLanguage();
		} catch(Exception e) {
			logger.error("preCreateWorkbench: " + e.getClass() + ": " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
			throw new RuntimeException(e);
		}
	}

	/**
	 * If no truststore is specified set the java system wide truststore to the default:
	 * config_dir/jfire-server.truststore and if no truststore is found at that location, the default
	 * truststore from this plugin/src/jfire-server.truststore is copied to the expected location.
	 *
	 * @throws IOException if the truststore couldn't be copied.
	 */
	protected void initSSLTruststore() throws IOException
	{
		if (System.getProperty("javax.net.ssl.trustStore") != null) //$NON-NLS-1$
			return;

		File truststoreFile = new File(getConfigDir(), "jfire-server.truststore").getAbsoluteFile(); //$NON-NLS-1$
		if (!truststoreFile.exists())
		{
			IOUtil.copyResource(JFireApplication.class, "/jfire-server.truststore", truststoreFile); //$NON-NLS-1$
		}

		System.setProperty("javax.net.ssl.trustStore", truststoreFile.getPath()); //$NON-NLS-1$
		System.setProperty("javax.net.ssl.trustStorePassword", "nightlabs"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public AbstractWorkbenchAdvisor initWorkbenchAdvisor(Display display) {
		return new JFireWorkbenchAdvisor();
	}

	protected void initLogin() throws LoginException, LoginAbortedException
	{
		// create log directory if not existent
		AbstractApplication.getLogDir();
		initializeLoginModule();
	}

	protected void initializeLoginModule() {
		JFireSecurityConfiguration.declareConfiguration();

		try {
			BundleContext context = JFireBasePlugin.getDefault().getBundle().getBundleContext();

			LoginHandlerRegistry.sharedInstance(); // triggers the extension-point processing and registration of OSGi service.

			loginHandler = context.getServiceReference(ILoginHandler.class.getName());

			if (loginHandler != null) {
				Login.getLogin(false).setLoginHandler((ILoginHandler) context.getService(loginHandler));
			} else
				throw new RuntimeException("login handler service (ILoginHandler) is not available. bundle with login handler is not deployed or not started");

		} catch (LoginException e) {
			throw new RuntimeException("How the hell could this happen?!", e); //$NON-NLS-1$
		}
	}

	@Override
	public void stop() {
		super.stop();

		if(loginHandler != null)
			JFireBasePlugin.getDefault().getBundle().getBundleContext().ungetService(loginHandler);
	}
}
