package org.nightlabs.jfire.base.ui.security;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.resource.SharedImages.ImageDimension;
import org.nightlabs.base.ui.resource.SharedImages.ImageFormat;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.security.User;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserSearchDialog
//extends ResizableTrayDialog
extends ResizableTitleAreaDialog
{
	/**
	 * The flags defined in {@link UserSearchComposite}
	 */
	private int flags;

	/**
	 * @param parentShell the {@link Shell} for the dialog
	 * @param searchText the searchText to search for
	 * @param flags the flags defined in {@link UserSearchComposite}
	 */
	public UserSearchDialog(Shell parentShell, String searchText, int flags) {
		super(parentShell, Messages.RESOURCE_BUNDLE);
		this.searchText = searchText;
		this.flags = flags;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * @param parentShell the {@link Shell} for the dialog
	 * @param searchText the searchText to search for
	 */
	public UserSearchDialog(Shell parentShell, String searchText) {
		super(parentShell, Messages.RESOURCE_BUNDLE);
		this.searchText = searchText;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

//	/**
//	 * @param parentShell
//	 * @param searchText
//	 */
//	public UserSearchDialog(IShellProvider parentShell, String searchText) {
//		super(parentShell, null);
//		this.searchText = searchText;
//		setShellStyle(getShellStyle() | SWT.RESIZE);
//	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchDialog.SearchUser")); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Label titleBarSeparator = new Label(parent, 258);
        titleBarSeparator.setLayoutData(new GridData(768));

		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchDialog.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchDialog.message"), IMessageProvider.INFORMATION); //$NON-NLS-1$
		setTitleImage(SharedImages.getSharedImage(JFireBasePlugin.getDefault(), UserSearchDialog.class, "", ImageDimension._75x70, ImageFormat.png)); //$NON-NLS-1$

		if (flags != 0)
			userSearchComposite = new UserSearchComposite(parent, SWT.NONE, flags);
		else
			userSearchComposite = new UserSearchComposite(parent, SWT.NONE);
		if (searchText != null && !searchText.trim().equals("")) { //$NON-NLS-1$
			userSearchComposite.getUserIDText().setText(searchText);
		}
		userSearchComposite.getUserTable().addDoubleClickListener(userDoubleClickListener);
		userSearchComposite.getUserTable().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedUser = userSearchComposite.getSelectedUser();
				getButton(IDialogConstants.OK_ID).setEnabled(selectedUser != null);
			}
		});

		return userSearchComposite;
	}

	private UserSearchComposite userSearchComposite = null;
	private String searchText = ""; //$NON-NLS-1$
	private User selectedUser = null;
	public User getSelectedUser() {
		return selectedUser;
	}

	public static final int SEARCH_ID = IDialogConstants.CLIENT_ID + 1;

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		Button searchButton = createButton(parent, SEARCH_ID, Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchDialog.Search"), true); //$NON-NLS-1$
		searchButton.addSelectionListener(searchButtonListener);

		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	private SelectionListener searchButtonListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
			userSearchComposite.searchPressed();
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	@Override
	protected void okPressed()
	{
		selectedUser = userSearchComposite.getSelectedUser();
		super.okPressed();
	}

	@Override
	protected void cancelPressed()
	{
		selectedUser = null;
		super.cancelPressed();
	}

	private IDoubleClickListener userDoubleClickListener = new IDoubleClickListener(){
		public void doubleClick(DoubleClickEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof User) {
					selectedUser = (User) sel.getFirstElement();
					close();
				}
			}
		}
	};

}
