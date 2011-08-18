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

package org.nightlabs.jfire.base.login.rcp.ui;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.nightlabs.base.ui.NLBasePlugin;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.classloader.osgi.DelegatingClassLoaderOSGI;
import org.nightlabs.j2ee.InitialContextProvider;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin;
import org.nightlabs.jfire.base.j2ee.RemoteResourceFilterRegistry;
import org.nightlabs.jfire.base.login.ui.ILoginHandler;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.login.ui.Login.AsyncLoginResult;
import org.nightlabs.jfire.base.login.ui.LoginConfigModule;
import org.nightlabs.jfire.base.login.ui.LoginConfiguration;
import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate;
import org.nightlabs.jfire.classloader.remote.backend.JFireRCLBackendRemote;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.IOUtil;

/**
 * @see org.nightlabs.jfire.base.login.ui.ILoginHandler
 *
 * @author Alexander Bieber
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireRCPLoginHandler implements ILoginHandler {
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireRCPLoginHandler.class);

	/** Constant for the application parameter that overrides the userID */
	private static final String LOGIN_APP_PARAM_USERID = "login.userID"; //$NON-NLS-1$
	/** Constant for the application parameter that overrides the users password */
	private static final String LOGIN_APP_PARAM_PASSWORD = "login.password"; //$NON-NLS-1$
	/** Constant for the application parameter that overrides the users organistationID */
	private static final String LOGIN_APP_PARAM_ORGANISATIONID = "login.organisationID"; //$NON-NLS-1$
	/** Constant for the application parameter that overrides the workstationID */
	private static final String LOGIN_APP_PARAM_WORKSTATIONID = "login.workstationID"; //$NON-NLS-1$
	/** Constant for the application parameter that overrides the loginContextFactory */
	private static final String LOGIN_APP_PARAM_INITIAL_CTX_FACTORY = "login.initialContextFactory"; //$NON-NLS-1$
	/** Constant for the application parameter that overrides the serverURL */
	private static final String LOGIN_APP_PARAM_SERVER_URL = "login.serverURL"; //$NON-NLS-1$
	
	/** The names of all supported application parameters */
	private static final Set<String> LOGIN_APP_PARAMS = new HashSet<String>(Arrays.asList(new String[] { 
			LOGIN_APP_PARAM_USERID, LOGIN_APP_PARAM_PASSWORD, LOGIN_APP_PARAM_ORGANISATIONID,
			LOGIN_APP_PARAM_WORKSTATIONID, LOGIN_APP_PARAM_INITIAL_CTX_FACTORY, LOGIN_APP_PARAM_SERVER_URL
	}));
	
	private boolean autoLoginWithParams = true; // will be set false after it's done for the first time.

	/**
	 * Opens an instance of {@link LoginDialog}.
	 * The dialog sets the loginResult and loginContext values.
	 * A login verification is performed to be sure the user can
	 * be identified by the credentials he specified.
	 * @see org.nightlabs.jfire.base.login.ui.ILoginHandler#handleLogin(org.nightlabs.jfire.base.ui.login.JFireLoginContext)
	 * @see LoginDialog
	 */
	public void handleLogin(LoginData loginData, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult)
	throws LoginException
	{
		boolean hasApplicationParams = false;
		// if the user specified the necessary parameters and the login succeeds, we don't show any login dialog
		try {
			// generate new SessionID (this has to be done everytime some logs in)
			Base62Coder coder = Base62Coder.sharedInstance();
			loginData.setSessionID(
					coder.encode(System.currentTimeMillis(), 1) + '-' +
					coder.encode((long)(Math.random() * 14776335), 1)); // 14776335 is the highest value encoded in 4 digits ("zzzz")

			//  -> initialise to last used configuration values if none were given
			loginConfigModule.acquireWriteLock();
			try {
				LoginConfiguration latestConfig = loginConfigModule.getLatestLoginConfiguration();

				if (latestConfig == null) {
					// there is no last configuration -> initialise with default values (describe local JBoss server)
					latestConfig = new LoginConfiguration();
					latestConfig.setLoginConfigModule(loginConfigModule);
					LoginData defaultData = latestConfig.getLoginData();
					defaultData.setDefaultValues();
					defaultData.setInitialContextFactory("org.jboss.security.jndi.LoginInitialContextFactory"); // TODO need a jboss-independent solution! Maybe a list of possible ones (for multiple servers!) accessible via a "..."-button and initially an empty text field?! Marco. //$NON-NLS-1$
				}

				LoginData lastUsed = latestConfig.getLoginData();

				if (loginData.getUserID() == null || "".equals(loginData.getUserID())) //$NON-NLS-1$
					loginData.setUserID(lastUsed.getUserID());

				if (loginData.getOrganisationID() == null || "".equals(loginData.getOrganisationID())) //$NON-NLS-1$
					loginData.setOrganisationID(lastUsed.getOrganisationID());

				if (loginData.getWorkstationID() == null || "".equals(loginData.getWorkstationID())) //$NON-NLS-1$
					loginData.setWorkstationID(lastUsed.getWorkstationID());

				if (loginData.getInitialContextFactory() == null || "".equals(loginData.getInitialContextFactory())) //$NON-NLS-1$
					loginData.setInitialContextFactory(lastUsed.getInitialContextFactory());

				if (loginData.getProviderURL() == null || "".equals(loginData.getProviderURL())) //$NON-NLS-1$
					loginData.setProviderURL(lastUsed.getProviderURL());

				if (loginData.getSecurityProtocol() == null || "".equals(loginData.getSecurityProtocol())) //$NON-NLS-1$
					loginData.setSecurityProtocol(lastUsed.getSecurityProtocol());

				loginConfigModule.setLatestLoginConfiguration(loginData, null);

				// Check login parameters given via startup parameters
				if (autoLoginWithParams) {
					String[] args = NLBasePlugin.getDefault().getApplication().getArguments();
					for (int i = 0; i < args.length; i++) {
						String arg = args[i];
						String val = i + 1 < args.length ? args[i + 1] : null;
						
						if (!hasApplicationParams && arg != null && arg.length() > 2 && val != null) {
							if (LOGIN_APP_PARAMS.contains(arg.substring(2))) {
								hasApplicationParams = true;
							}
						}
						
						if ("--login.userID".equals(arg)) //$NON-NLS-1$
							loginData.setUserID(val);
						else if ("--login.password".equals(arg)) //$NON-NLS-1$
							loginData.setPassword(val);
						else if ("--login.organisationID".equals(arg)) //$NON-NLS-1$
							loginData.setOrganisationID(val);
						else if ("--login.workstationID".equals(arg)) //$NON-NLS-1$
							loginData.setWorkstationID(val);
						else if ("--login.initialContextFactory".equals(arg)) //$NON-NLS-1$
							loginData.setInitialContextFactory(val);
						else if ("--login.serverURL".equals(arg)) //$NON-NLS-1$
							loginData.setProviderURL(val);
					}
					autoLoginWithParams = false;
				}

				// perform a test login
//				Login.AsyncLoginResult res = Login.testLogin(loginData);
				Login.AsyncLoginResult res = testLogin(loginData);
				if (res.isSuccess()) {
					BeanUtils.copyProperties(loginResult, res);
					return;
				}
				else if (res.isWasAuthenticationErr())
					throw new LoginException("Authentication error"); //$NON-NLS-1$
				else if (res.getException() != null)
					throw res.getException();
				else if ((res.getMessage() != null))
					throw new LoginException(res.getMessage());
				else
					throw new LoginException("Login failed and I have no idea, why!!!"); //$NON-NLS-1$
			} finally {
				loginConfigModule.releaseLock();
			}
		} catch (Throwable x) {
			// sth. went wrong => log and show normal login dialog
			logger.error("Could not login using the specified program arguments!", x); //$NON-NLS-1$
		}

		handleSWTLogin(loginData, loginConfigModule, loginResult, hasApplicationParams);
	}

	// TODO: should the creation and registration of login dialog be synchronized??
	protected void handleSWTLogin(LoginData loginData, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult, boolean hasApplicationParams)
	throws LoginException
	{
		LoginDialog loginDialog = new LoginDialog(RCPUtil.getActiveShell(), loginResult, loginConfigModule, loginData, hasApplicationParams);
		// LoginDialog does all the work
		loginDialog.open();
	}
	
	@Override
	public boolean needsRestartAfterSuccessfullLogin() 
	{
		JFireRCDLDelegate.sharedInstance().register(DelegatingClassLoaderOSGI.getSharedInstance()); // this method does nothing, if already registered.
		
		boolean needRestart = false;
		try {
//			needRestart = JFireRCPLoginPlugin.getDefault().updateManifest();
			needRestart = JFireJ2EEPlugin.getDefault().updateManifest();
		} catch (Exception e) {
			logger.error(e, e);
			needRestart = false;
		}
		
		return needRestart;		
	}
	
	protected void initRemoteClassLoading() {
		InitialContextProvider icp = new InitialContextProvider() {
			public InitialContext getInitialContext()
			throws LoginException, NamingException
			{
				return new InitialContext(getInitialContextProperties());
			}
			public Hashtable getInitialContextProperties()
			throws LoginException
			{
				return Login.sharedInstance().getInitialContextProperties();
			}
		};
		File classLoaderCacheDir;
		try {
			classLoaderCacheDir = new File(IOUtil.createUserTempDir("jfire.", null), "classloader.cache");
			JFireRCDLDelegate.createSharedInstance(
					icp,
					classLoaderCacheDir).setFilter(RemoteResourceFilterRegistry.sharedInstance());
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
	}
	
	public JFireRCPLoginHandler() {
		initRemoteClassLoading();
	}

	@Override
	public AsyncLoginResult testLogin(LoginData loginData) {
			Login.AsyncLoginResult loginResult = new Login.AsyncLoginResult();
			loginResult.setSuccess(false);
			loginResult.setMessage(null);
			loginResult.setException(null);

			// verify login
			JFireRCLBackendRemote jfireRCLBackend = null;
			if (jfireRCLBackend == null) {
				try {
					logger.debug(Thread.currentThread().getContextClassLoader());
					logger.debug(JFireRCPLoginPlugin.class.getClassLoader());
					logger.debug("**********************************************************"); //$NON-NLS-1$
					logger.debug("Create testing login"); //$NON-NLS-1$
					InitialContext initialContext = new InitialContext(loginData.getInitialContextProperties());
					try {
						jfireRCLBackend = (JFireRCLBackendRemote) initialContext.lookup("ejb/byRemoteInterface/" + JFireRCLBackendRemote.class.getName());
						jfireRCLBackend.ping("testLogin");
					} finally {
						initialContext.close();
					}
					logger.debug("**********************************************************"); //$NON-NLS-1$
					loginResult.setSuccess(true);
				} catch (EJBAccessException x) {
					loginResult.setWasAuthenticationErr(true);
//				} catch (RemoteException remoteException) {
//					Throwable cause = remoteException.getCause();
//					if (cause != null && cause.getCause() instanceof EJBException) {
//						EJBException ejbE = (EJBException)cause.getCause();
//						if (ejbE != null) {
//							if (ejbE.getCausedByException() instanceof SecurityException)
//								// SecurityException authentication failure
//								loginResult.setWasAuthenticationErr(true);
//						}
//					}
//					else if (cause != null && ExceptionUtils.indexOfThrowable(cause, LoginException.class) >= 0) {
//						loginResult.setWasAuthenticationErr(true);
//						loginResult.setException(remoteException);
//					}
//					else {
//						if (ExceptionUtils.indexOfThrowable(cause, SecurityException.class) >= 0) {
//							loginResult.setWasAuthenticationErr(true);
//							loginResult.setSuccess(false);
//						}
//						else {
//							loginResult.setException(remoteException);
//							loginResult.setSuccess(false);
//						}
//					}
				} catch (Exception x) {
//					if (x instanceof CommunicationException) {
//						loginResult.setWasCommunicationErr(true);
//					}
					if (x instanceof SocketTimeoutException) {
						loginResult.setWasSocketTimeout(true);
					}
					// cant create local bean stub
					logger.error("Login failed!", x); //$NON-NLS-1$
					LoginException loginE = new LoginException(x.getMessage());
					loginE.initCause(x);
//					loginResult.setMessage(Messages.getString("org.nightlabs.jfire.base.ui.login.Login.errorUnhandledExceptionMessage")); //$NON-NLS-1$
					loginResult.setException(loginE);
				}
			}

			return loginResult;
	}
	
}
