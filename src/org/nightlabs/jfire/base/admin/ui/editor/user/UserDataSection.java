package org.nightlabs.jfire.base.admin.ui.editor.user;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.DisplayNameChangedListener;
import org.nightlabs.jfire.security.User;

public class UserDataSection extends RestorableSectionPart {

	private Text userIdText;
	private Text userNameText;
	private Text userDescriptionText;
	private Text password0Text;
	private Text password1Text;
	private Button autogenerateNameCheckBox;
	
	private User user;
	private boolean refreshing = false;
	private boolean passwordChanged = false;
	
	ModifyListener dirtyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (!refreshing)
				markDirty();
		}
	};
	private PersonPreferencesPage personPreferencesPage;
	
	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UserDataSection(FormPage page, Composite parent, String sectionDescriptionText) {
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		personPreferencesPage = (PersonPreferencesPage) page;
		createClient(getSection(), page.getEditor().getToolkit(), sectionDescriptionText);
	}

	private void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createDescriptionControl(section, toolkit, sectionDescriptionText);
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);
		GridLayout layout = (GridLayout) container.getLayout();
		layout.horizontalSpacing = 10;
		layout.numColumns = 3;
		
		createLabel(container, "User ID", 3);		
		userIdText = new Text(container, SWT.NONE);
		userIdText.setEditable(false);
		userIdText.setLayoutData(getGridData(3));		
		
		createLabel(container,	"User name", 3);
		userNameText = new Text(container, SWT.NONE);
		userNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		autogenerateNameCheckBox = new Button(container, SWT.CHECK);
		new Label(container, SWT.NONE).setText("Autogenerate");
		autogenerateNameCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				userNameText.setEnabled(!autogenerateNameCheckBox.getSelection());
				markDirty();
				updateDisplayName();
			}
		});
		
		createLabel(container, "User description", 3);
		userDescriptionText = new Text(container, SWT.NONE);
		userDescriptionText.setLayoutData(getGridData(3));
		
		createLabel(container, "Password", 3);
		password0Text = new Text(container, SWT.NONE);
		password0Text.setLayoutData(getGridData(3));
		password0Text.setEchoChar('*');
		
		createLabel(container, "Password confirm", 3);
		password1Text = new Text(container, SWT.NONE);
		password1Text.setLayoutData(getGridData(3));
		password1Text.setEchoChar('*');
		
		ModifyListener passwordModifyListener = new ModifyListener() {
			private static final String UNEQUAL_PASSWORDS = "unequalPasswords";
			public void modifyText(ModifyEvent e) {
				if (!password0Text.getText().equals(password1Text.getText())) {
					getManagedForm().getMessageManager().addMessage(UNEQUAL_PASSWORDS, "The confirmation password does not match the password.", null, IMessageProvider.ERROR, password1Text);
				} else {					
					getManagedForm().getMessageManager().removeMessage(UNEQUAL_PASSWORDS, password1Text);
				}
				passwordChanged = true;
			}
		};
		password0Text.addModifyListener(passwordModifyListener);
		password1Text.addModifyListener(passwordModifyListener);
		password0Text.addModifyListener(dirtyListener);
		password1Text.addModifyListener(dirtyListener);
	}
	
	private void createLabel(Composite container, String text, int span) {
		Label label = new Label(container, SWT.NONE);
		label.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		label.setLayoutData(gd);
	}
	
	private GridData getGridData(int span) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		return gd;
	}
	
	private void createDescriptionControl(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		if (sectionDescriptionText == null || "".equals(sectionDescriptionText)) //$NON-NLS-1$
			return;

		section.setText(sectionDescriptionText);
	}
	
	public void setUser(User _user) {
		this.user = _user;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				refreshing = true;
				userIdText.setText(user.getUserID());
				if (user.getName() != null)
					userNameText.setText(user.getName());
				if (user.getDescription() != null)
					userDescriptionText.setText(user.getDescription());
				
				password0Text.setText(user.getUserLocal().getPassword());
				password1Text.setText(user.getUserLocal().getPassword());
				
				autogenerateNameCheckBox.setSelection(user.isAutogenerateName());				
				userNameText.setEnabled(!autogenerateNameCheckBox.getSelection());
				
				personPreferencesPage.getUserPropertiesSection().setDisplayNameChangedListener(new DisplayNameChangedListener() {
					public void displayNameChanged(String displayName) {
						refreshing = true;
						updateDisplayName();
						refreshing = false;
					}
				});
				refreshing = false;
			}
		});		
	}
	
	@Override
	public void commit(boolean onSave) {
		if (!password0Text.getText().equals(password1Text.getText()))
			return;
		
		super.commit(onSave);		
		user.setDescription(userDescriptionText.getText());
		user.setName(userNameText.getText());
		user.setAutogenerateName(autogenerateNameCheckBox.getSelection());
		
		if (passwordChanged) {
			user.getUserLocal().setPasswordPlain(password0Text.getText());
			try {
				if (user.getUserID().equals(Login.getLogin().getUserID()) && user.getOrganisationID().equals(Login.getLogin().getOrganisationID()))
					Login.getLogin().setPassword(password0Text.getText());
			} catch (LoginException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	void updateDisplayName() {
		if (autogenerateNameCheckBox.getSelection()) {
			user.setNameAuto();
			userNameText.setText(user.getName());
		}
	}
}
