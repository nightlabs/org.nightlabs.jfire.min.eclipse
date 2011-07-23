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

package org.nightlabs.jfire.base.login.rap.ui;

import java.net.SocketTimeoutException;

import javax.naming.InitialContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.eclipse.rwt.RWT;
import org.nightlabs.base.ui.context.UIContext;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.login.ui.ILoginHandler;
import org.nightlabs.jfire.base.login.ui.JFireLoginPlugin;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.login.ui.LoginConfigModule;
import org.nightlabs.jfire.base.login.ui.Login.AsyncLoginResult;
import org.nightlabs.jfire.server.ServerManagerRemote;
import org.nightlabs.util.ObjectCarrier;

public class JFireRAPLoginHandler implements ILoginHandler 
{
	private static final Logger logger = Logger.getLogger(JFireRAPLoginHandler.class);
	
	public void handleLogin(LoginData loginData, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult)
			throws LoginException {
		final ObjectCarrier<LoginData> loginDataBox = new ObjectCarrier<LoginData>();

		UIContext.getRunner().runInUIContext(new Runnable() {
			public void run() {
				loginDataBox.setObject((LoginData) RWT.getSessionStore().getHttpSession().getAttribute(LoginServlet.ATT_LOGIN));
			}
		});

		LoginData sessionLoginData = loginDataBox.getObject();
		if (sessionLoginData != null) {
			loginData.setSessionID(sessionLoginData.getSessionID());
			loginData.setUserID(sessionLoginData.getUserID());
			loginData.setPassword(sessionLoginData.getPassword());
			loginData.setInitialContextFactory(sessionLoginData.getInitialContextFactory());
			loginData.setOrganisationID(sessionLoginData.getOrganisationID());
			loginData.setProviderURL(sessionLoginData.getProviderURL());
			loginData.setSecurityProtocol(sessionLoginData.getSecurityProtocol());
			loginData.setWorkstationID(sessionLoginData.getWorkstationID());

//			Login.AsyncLoginResult result = Login.testLogin(loginData);
			Login.AsyncLoginResult result = testLogin(loginData);
			if (result.isSuccess()) {
				try {
					BeanUtils.copyProperties(loginResult, result);
				} catch (Exception e) {
					throw new LoginException(e.getMessage());
				}
			} else
				throw new LoginException(result.getMessage());
		}else
			throw new LoginException("no LoginData is associated with the http session");
	}
	
	public boolean needsRestartAfterSuccessfullLogin() {
		return false;
	}

	@Override
	public AsyncLoginResult testLogin(LoginData loginData) {
			Login.AsyncLoginResult loginResult = new Login.AsyncLoginResult();
			loginResult.setSuccess(false);
			loginResult.setMessage(null);
			loginResult.setException(null);

		// verify login
			try {
				logger.debug(Thread.currentThread().getContextClassLoader());
				logger.debug(JFireLoginPlugin.class.getClassLoader());
				logger.debug("**********************************************************"); //$NON-NLS-1$
				logger.debug("Create testing login"); //$NON-NLS-1$
				InitialContext initialContext = new InitialContext(loginData.getInitialContextProperties());
				try {
					ServerManagerRemote sm = (ServerManagerRemote) initialContext.lookup("ejb/byRemoteInterface/" + ServerManagerRemote.class.getName());
					sm.ping("testLogin");
				} finally {
					initialContext.close();
				}
				logger.debug("**********************************************************"); //$NON-NLS-1$
				loginResult.setSuccess(true);
			} catch (SecurityException x) {
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
//				if (x instanceof CommunicationException) {
//					loginResult.setWasCommunicationErr(true);
//				}
				if (x instanceof SocketTimeoutException) {
					loginResult.setWasSocketTimeout(true);
				}
				// cant create local bean stub
				logger.error("Login failed!", x); //$NON-NLS-1$
				LoginException loginE = new LoginException(x.getMessage());
				loginE.initCause(x);
//				loginResult.setMessage(Messages.getString("org.nightlabs.jfire.base.ui.login.Login.errorUnhandledExceptionMessage")); //$NON-NLS-1$
				loginResult.setException(loginE);
			}

			return loginResult;
	}
	
}
