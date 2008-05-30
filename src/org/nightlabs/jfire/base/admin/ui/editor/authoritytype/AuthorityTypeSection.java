package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.MessageSectionPart;
import org.nightlabs.jfire.security.AuthorityType;

public class AuthorityTypeSection extends MessageSectionPart
{
	private AuthorityType authorityType;
	private Text nameText;
	private Text descriptionText;

//	private IFormPage page;

	public AuthorityTypeSection(IFormPage page, Composite parent) {
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.CLIENT_INDENT, "Authority type");
//		this.page = page;
		((GridData)this.getSection().getLayoutData()).grabExcessVerticalSpace = false;

		XComposite client = new XComposite(getContainer(), SWT.NONE);

		new Label(client, SWT.NONE).setText("Name");
		nameText = new Text(client, page.getEditor().getToolkit().getBorderStyle() | SWT.READ_ONLY);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label descriptionLabel = new Label(client, SWT.NONE);
		descriptionLabel.setText("Description");
		descriptionLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		descriptionText = new Text(
				client,
				page.getEditor().getToolkit().getBorderStyle() | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 64;
		descriptionText.setLayoutData(gd);
	}

	public void setAuthorityType(AuthorityType _authorityType) {
		this.authorityType = _authorityType;
		this.nameText.setText(authorityType == null ? "" : authorityType.getName().getText());
		this.descriptionText.setText(authorityType == null ? "" : authorityType.getDescription().getText());

//		relayoutEditor();
	}

//	private void relayoutEditor()
//	{
//		getManagedForm().getForm().reflow(true);
//	}
}
