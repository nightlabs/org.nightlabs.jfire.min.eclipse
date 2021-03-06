package org.nightlabs.jfire.base.ui.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.GlobalJDOManagerProvider;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.AbstractSecurityReflector;
import org.nightlabs.jfire.security.AuthorizedObjectRefLifecycleListenerFilter;
import org.nightlabs.jfire.security.ISecurityReflector;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserLocalID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class SecurityReflectorRCP
extends AbstractSecurityReflector
implements ISecurityReflector
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SecurityReflectorRCP.class);

	@Override
	public UserDescriptor getUserDescriptor() throws NoUserException {
		if (logger.isDebugEnabled())
			logger.debug("_getUserDescriptor: enter"); //$NON-NLS-1$

		Login l;
		try {
			l = Login.getLogin();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
		return new UserDescriptor(l.getOrganisationID(), l.getUserID(), l.getWorkstationID(), l.getSessionID());
	}

	@Override
	public Object getCredential() throws NoUserException {
		Login login = null;
		try {
			login = Login.getLogin();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
		if (login == null){
			throw new NoUserException("Login is null, so it seems that no user is logged in!");
		}
		return login.getPassword();
	}

	@Override
	public InitialContext createInitialContext() throws NoUserException {
		try {
			return Login.getLogin().createInitialContext();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
	}

	@Override
	public Properties getInitialContextProperties() throws NoUserException {
		try {
			return Login.getLogin().getInitialContextProperties();
		} catch (Exception e) {
			throw new NoUserException(e);
		}
	}

	private Map<AuthorityID, Set<RoleID>> cache_authorityID2roleIDSet = new HashMap<AuthorityID, Set<RoleID>>();

	@Override
	public synchronized Set<RoleID> getRoleIDs(AuthorityID authorityID) throws NoUserException
	{
		Set<RoleID> result = cache_authorityID2roleIDSet.get(authorityID);
		if (result != null)
			return result;

		try {
			JFireSecurityManagerRemote jfireSecurityManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, getInitialContextProperties());
			result = jfireSecurityManager.getRoleIDs(authorityID);
		} catch (NoUserException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			if (ExceptionUtils.indexOfThrowable(e, NoUserException.class) < 0)
				throw new RuntimeException(e);
			else
				throw new NoUserException(e);
		}

		cache_authorityID2roleIDSet.put(authorityID, result);
		return result;
	}

	private JDOLifecycleListener authorizedObjectRefLifecycleListener = null;

	private class AuthorizedObjectRefLifecycleListener extends JDOLifecycleAdapterJob
	{
		private IJDOLifecycleListenerFilter filter;

		public AuthorizedObjectRefLifecycleListener() {
			UserDescriptor userDescriptor = getUserDescriptor();
			filter = new AuthorizedObjectRefLifecycleListenerFilter(
					UserLocalID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID(), userDescriptor.getOrganisationID()),
					JDOLifecycleState.DIRTY, JDOLifecycleState.DELETED
			);
		}

		@Override
		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter() {
			return filter;
		}

		@Override
		public void notify(JDOLifecycleEvent event) {
			synchronized (SecurityReflectorRCP.this) {
				cache_authorityID2roleIDSet.clear();
			}
		}
	}

	protected synchronized void unregisterAuthorizedObjectRefLifecycleListener()
	{
		if (authorizedObjectRefLifecycleListener != null) {
			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().removeLifecycleListener(authorizedObjectRefLifecycleListener);
			authorizedObjectRefLifecycleListener = null;
			cache_authorityID2roleIDSet.clear();
		}
	}

	protected synchronized void registerAuthorizedObjectRefLifecycleListener()
	{
		unregisterAuthorizedObjectRefLifecycleListener();

		authorizedObjectRefLifecycleListener = new AuthorizedObjectRefLifecycleListener();
		GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().addLifecycleListener(authorizedObjectRefLifecycleListener);
		cache_authorityID2roleIDSet.clear();
	}
}
