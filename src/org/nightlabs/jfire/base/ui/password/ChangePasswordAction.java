package org.nightlabs.jfire.base.ui.password;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.eclipse.ui.dialog.ChangePasswordDialog;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;

public class ChangePasswordAction implements IWorkbenchWindowActionDelegate {

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub

	}

	public void run(IAction arg0) {
		InputDialog dialog = new InputDialog(RCPUtil.getActiveShell(), "Enter Password", "Enter your current password to proceed.", "", null) {
			@Override
			protected void okPressed() {
				try {
					if (Login.getLogin().getPassword().equals(getValue()))
						super.okPressed();
					else {
						Thread.sleep(2000);
						MessageDialog.openError(RCPUtil.getActiveShell(), "Wrong password", "The entered password is not correct.\n\nPlease try again.");
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
			
			@Override
			protected Control createDialogArea(Composite parent) {
				Control dialogArea = super.createDialogArea(parent);
				getText().setEchoChar('*');
				return dialogArea;
			}
		};
		
		if (dialog.open() != Window.OK)
			return;
		
		IInputValidator newPasswordValidator = new IInputValidator() {
			@Override
			public String isValid(String password) {
				if (password.length() < UserLocal.MIN_PASSWORD_LENGTH)
					return "Minimum password length: " + UserLocal.MIN_PASSWORD_LENGTH;
				if (password.matches("\\*+"))
					return "Passwords only consisting of '*' are not valid.";

				return null;
			}
		};
		String newPassword = ChangePasswordDialog.openDialog(RCPUtil.getActiveShell(), newPasswordValidator, null);
		try {
			UserManager um = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			um.setUserPassword(newPassword);
			Login.sharedInstance().setPassword(newPassword);
			MessageDialog.openInformation(RCPUtil.getActiveShell(), "Password changed", "Your password has been changed successfully.");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
//		ChangePasswordDialog.openDialog();
	}

	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

}
