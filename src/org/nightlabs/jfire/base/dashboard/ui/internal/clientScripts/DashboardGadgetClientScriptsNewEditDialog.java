package org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;

public class DashboardGadgetClientScriptsNewEditDialog extends ResizableTitleAreaDialog {

	DashboardGadgetClientScriptsConfigPage.ClientScriptPropertiesWrapper data;
	
	public DashboardGadgetClientScriptsNewEditDialog(final Shell shell, 
		final DashboardGadgetClientScriptsConfigPage.ClientScriptPropertiesWrapper data) {
		
		super(shell, null);
		this.data = data;
	}
	
//	@Override
//	protected Point getInitialSize() {
//		return new Point(700, 600);
//	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite parent_ = (Composite) super.createDialogArea(parent);
		setTitle("Title");
		setMessage("Message");
		
		final Composite content = new XComposite(parent_, SWT.NONE, LayoutMode.TIGHT_WRAPPER, 2);
		GridData gd;
		
		final Label labelDescription1 = new Label(content, SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.verticalIndent = 10;
		labelDescription1.setLayoutData(gd);
		labelDescription1.setText("Set the name of the client script:");
		
		final Text textClientScriptName = new Text(content, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.verticalIndent = 10;
		textClientScriptName.setLayoutData(gd);
		textClientScriptName.setText(data.getClientScriptName() != null ? data.getClientScriptName() : "Client script name");
		textClientScriptName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent event) {
				if (event.getSource() instanceof Text)
					data.setClientScriptName(((Text) event.getSource()).getText());	// TODO add delay
			}
		});
		
		final Label labelDescription2 = new Label(content, SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd.verticalIndent = 10;
		labelDescription2.setLayoutData(gd);
		labelDescription2.setText("Set the content of the client script:");
		
		final Text textClientScriptContent = new Text(content, SWT.BORDER | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		textClientScriptContent.setLayoutData(gd);
		textClientScriptContent.setText(data.getClientScriptContent() != null ? data.getClientScriptContent() : "Client script content");
		textClientScriptContent.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent event) {
				if (event.getSource() instanceof Text)
					data.setClientScriptContent(((Text) event.getSource()).getText());	// TODO add delay
			}
		});
		
		return parent_;
	}
}
