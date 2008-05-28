/**
 * 
 */
package org.nightlabs.jfire.base.ui.exceptionhandler;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.base.ui.exceptionhandler.DefaultErrorDialog;
import org.nightlabs.base.ui.exceptionhandler.ErrorDialogFactory;
import org.nightlabs.base.ui.exceptionhandler.IExceptionHandler;
import org.nightlabs.jfire.security.MissingRoleException;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.id.RoleID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author marco schulze - marco at nightlabs dot de
 */
public class InsufficientPermissionHandler implements IExceptionHandler
{
	private Logger logger = Logger.getLogger(InsufficientPermissionHandler.class);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window.IExceptionHandler#handleException(java.lang.Throwable)
	 */
	@Override
	public void handleException(Thread thread, Throwable thrownException, Throwable triggerException) {
		logger.info("handleException: called for this triggerException: " + triggerException);

		if (!(triggerException instanceof SecurityException))
			logger.error("triggerException is not an instance of SecurityException! Instead it is: " + (triggerException == null ? null : triggerException.getClass().getName()), thrownException);
		else {
			try {
				RemoteExceptionClassHandler handler = new RemoteExceptionClassHandler();
				if (handler.handleException(thread, thrownException, (SecurityException) triggerException))
					return;
			} catch (NoClassDefFoundError x) {
				// ignore and use fallback-dialog below
			}
		}

		ErrorDialogFactory.showError(DefaultErrorDialog.class, 
				"Insufficient permissions", 
				"You don't have enough permissions for the requested action. Contact your boss or your administrator.", 
				thrownException, triggerException);
	}

	// We handle the MissingRoleException in a subclass, because it might happen that we cannot load
	// classes needed for this and thus keep the InsufficientPermissionHandler even workable if this subclass is not functional.
	private static class RemoteExceptionClassHandler
	{
		private static Logger logger = Logger.getLogger(RemoteExceptionClassHandler.class);

		public boolean handleException(Thread thread, Throwable thrownException, SecurityException securityException) {
			Set<RoleID> requiredRoleIDs = null;
			Set<Role> requiredRoles = null;
			String authorityName = null;

			if (securityException instanceof MissingRoleException) {
				requiredRoles = ((MissingRoleException)securityException).getRequiredRoles();
				authorityName = ((MissingRoleException)securityException).getAuthority().getName().getText();
			}
			else {
				// check if the securityException comes from the server by scanning thrownException for a remote exception
				if (ExceptionUtils.indexOfThrowable(thrownException, java.rmi.AccessException.class) < 0) {
					logger.info("There is no java.rmi.AccessException in the stack trace, hence we don't try to parse the exception message and fall back to the default dialog.");
					return false;
				}

				// If we come here, the exception is thrown by the JavaEE server when checking the EJB method permissions.
				// So we try to parse the exception message.

				// TODO WARNING: This is JBoss dependent code. When we support another JavaEE server, we very likely have to change this code!
				//
				// JBoss 4.2.2: We search for a message like this:
				//   Insufficient method permissions, principal=marco@chezfrancois.jfire.org?sessionID=LKsC9cN-tCN9&workstationID=ws00&, ejbName=jfire/ejb/JFireBaseBean/JFireSecurityManager, method=getUserIDs, interface=REMOTE, requiredRoles=[org.nightlabs.jfire.security.JFireSecurityManager#accessRightManagement, TestTestTest], principalRoles=[_Guest_]
				//
				// Hence, we search for this to parse the role-ids:
				//   requiredRoles=[org.nightlabs.jfire.security.JFireSecurityManager#accessRightManagement, TestTestTest]

				String exceptionMessage = securityException.getMessage();
				if (exceptionMessage == null) {
					logger.info("The SecurityException's message is null, hence we cannot parse it and fall back to the default dialog.");
					return false;
				}

				String requiredRolesBeginToken = "requiredRoles=[";
				int indexOfRequiredRolesBegin = exceptionMessage.indexOf(requiredRolesBeginToken);
				if (indexOfRequiredRolesBegin < 0) {
					logger.info("The SecurityException's message does not contain begin-token \"" + requiredRolesBeginToken + "\" => falling back to default dialog.");
					return false;
				}

				indexOfRequiredRolesBegin += requiredRolesBeginToken.length();

				// now, indexOfRequiredRoles points to the first character of the first roleID

				String requiredRolesEndToken = "]";
				int indexOfRequiredRolesEnd = exceptionMessage.indexOf(requiredRolesEndToken, indexOfRequiredRolesBegin);
				if (indexOfRequiredRolesEnd < 0) {
					logger.info("The SecurityException's message does not contain end-token \"" + requiredRolesEndToken + "\" after begin-token \"" + requiredRolesBeginToken + "\" => falling back to default dialog.");
					return false;
				}

				// get the substring within the 2 tokens
				String requiredRolesString = exceptionMessage.substring(indexOfRequiredRolesBegin, indexOfRequiredRolesEnd);
				requiredRolesString = requiredRolesString.trim();
				if ("".equals(requiredRolesString)) {
					logger.info("The SecurityException's message does not contain any role between begin-token \"" + requiredRolesBeginToken + "\" and end-token \"" + requiredRolesEndToken + "\" => falling back to default dialog.");
					return false;
				}

				// split it, because it might be multiple separated by "," (and having spaces => trim)
				String[] requiredRoleIDStrings;
				if (requiredRolesString.contains(",")) {
					requiredRoleIDStrings = requiredRolesString.split(",");
					for (int i = 0; i < requiredRoleIDStrings.length; i++) {
						requiredRoleIDStrings[i] = requiredRoleIDStrings[i].trim();
					}
				}
				else
					requiredRoleIDStrings = new String[] { requiredRolesString };

				requiredRoleIDs = new HashSet<RoleID>(requiredRoleIDStrings.length);
				for (String roleIDString : requiredRoleIDStrings) {
					requiredRoleIDs.add(RoleID.create(roleIDString));
				}
			}

			InsufficientPermissionDialogContext context = new InsufficientPermissionDialogContext(requiredRoleIDs, requiredRoles);
			InsufficientPermissionDialog.setInsufficientPermissionDialogContext(context);
			try {

				String message;

				if (authorityName == null)
					message = "You don't have enough permissions for the requested action. Contact your boss or your administrator.\n\nYou need at least one of the following role groups:";
				else
					message = String.format(
							"You don't have enough permissions for the requested action within the authority \"%s\". Contact your boss or your administrator.\n\nYou need at least one of the following role groups:",
							authorityName);

				ErrorDialogFactory.showError(InsufficientPermissionDialog.class, 
						"Insufficient permissions", 
						message, 
						thrownException, securityException);
				
			} finally {
				InsufficientPermissionDialog.removeInsufficientPermissionDialogContext();
			}

//			debug_showErrorDialogDelayed(thread, thrownException, securityException, context);

			return true;
		}

//		private void debug_showErrorDialogDelayed(
//				final Thread thread,
//				final Throwable thrownException,
//				final SecurityException securityException,
//				final InsufficientPermissionDialogContext context
//		)
//		{
//			final Display display = Display.getCurrent();
//
//			Thread runner = new Thread() {
//				@Override
//				public void run() {
//					try {
//						Thread.sleep((long)(Math.random() * 5000) + 3000);
//					} catch (InterruptedException e) {
//						// ignore
//					}
//
//					display.asyncExec(new Runnable() {
//						public void run() {
//							InsufficientPermissionDialog.setInsufficientPermissionDialogContext(context);
//							try {
//								
//								ErrorDialogFactory.showError(InsufficientPermissionDialog.class, 
//										"Insufficient permissions", 
//										"You don't have enough permissions for the requested action. Contact your boss or your administrator.\n\nYou need at least one of the following role groups:", 
//										thrownException, securityException);
//								
//							} finally {
//								InsufficientPermissionDialog.removeInsufficientPermissionDialogContext();
//							}
//						}
//					});
//				}
//			};
//			runner.start();
//		}
	}
}
