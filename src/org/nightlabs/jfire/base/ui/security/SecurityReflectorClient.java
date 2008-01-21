package org.nightlabs.jfire.base.ui.security;

import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * 
 * @author Marco Schulze
 */
public class SecurityReflectorClient
extends SecurityReflector
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SecurityReflectorClient.class);

	@Override
	public UserDescriptor _getUserDescriptor() throws NoUserException {
		if (logger.isDebugEnabled())
			logger.debug("_getUserDescriptor: enter"); //$NON-NLS-1$

		Login l;
		try {
			l = Login.getLogin();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
		return new UserDescriptor(l.getOrganisationID(), l.getUserID(), l.getSessionID());
	}

	@Override
	public InitialContext _createInitialContext() throws NoUserException {
		try {
			return Login.getLogin().createInitialContext();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
	}

	@Override
	public Properties _getInitialContextProperties() throws NoUserException {
		try {
			return Login.getLogin().getInitialContextProperties();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
	}
}
