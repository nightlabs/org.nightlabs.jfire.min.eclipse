package org.nightlabs.jfire.base.ui.password;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.dialog.CenteredDialog;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;

public class ChangePasswordDialog extends CenteredDialog {
	
	private Text currentPasswordText;
	private Text password0Text;
	private Text password1Text;
	private Label errorLabel;

	public ChangePasswordDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA);
		Label label = new Label(wrapper, SWT.BOLD);
		label.setText("Here you can change your password.");
		GridData gd = new GridData();
		gd.heightHint = 40;
		label.setLayoutData(gd);
		
		new Label(wrapper, SWT.NONE).setText("Current password");
		currentPasswordText = new Text(wrapper, SWT.BORDER);
		currentPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		currentPasswordText.setEchoChar('*');
		
		new Label(wrapper, SWT.NONE).setText("New password");
		password0Text = new Text(wrapper, SWT.BORDER);
		password0Text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		password0Text.setEchoChar('*');
		
		new Label(wrapper, SWT.NONE).setText("New password confirm");
		password1Text = new Text(wrapper, SWT.BORDER);
		password1Text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		password1Text.setEchoChar('*');
		
		new Label(wrapper, SWT.NONE);
		errorLabel = new Label(wrapper, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		ModifyListener passwordModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (password1Text.getText().equals(password0Text.getText())) {
					errorLabel.setText("");
					getButton(Window.OK).setEnabled(true);
				} else {
					errorLabel.setText("The confirmation password does not match the password.");
					getButton(Window.OK).setEnabled(false);
				}
			}
		};
		
		password0Text.addModifyListener(passwordModifyListener);
		password1Text.addModifyListener(passwordModifyListener);
		
		return wrapper;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Change your password");
		newShell.setSize(400, 300);
	}
	
	@Override
	protected void okPressed() {
		if (!password1Text.getText().equals(password0Text.getText()))
			throw new RuntimeException("This should never happen");
		
		try {
			if (!currentPasswordText.getText().equals(Login.getLogin().getPassword())) {
				MessageDialog.openError(RCPUtil.getActiveShell(), "Authentication error", "The current password is invalid.");
				return;
			}
		} catch (LoginException e1) {
			throw new RuntimeException(e1);
		}

		try {
			UserManager um = UserManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			um.setUserPassword(password0Text.getText());
			Login.sharedInstance().setPassword(password0Text.getText());
			MessageDialog.openInformation(RCPUtil.getActiveShell(), "Password changed", "Your password has been changed successfully");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		super.okPressed();
	}

	public static void openDialog() {
		ChangePasswordDialog dialog = new ChangePasswordDialog(RCPUtil.getActiveShell());
		dialog.open();
	}
}
