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
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;

/**
 * 
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 */
public class DashboardGadgetClientScriptsNewEditDialog extends ResizableTitleAreaDialog {

	DashboardGadgetClientScriptsConfigPage.ClientScriptPropertiesWrapper data;
	
	public DashboardGadgetClientScriptsNewEditDialog(final Shell shell, 
		final DashboardGadgetClientScriptsConfigPage.ClientScriptPropertiesWrapper data) {
		
		super(shell, Messages.RESOURCE_BUNDLE);
		System.out.println("constructor");
		this.data = data;
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		System.out.println("createDialogArea");
		final Composite parent_ = (Composite) super.createDialogArea(parent);
		setTitle(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts.DashboardGadgetClientScriptsNewEditDialog.createDialogArea.dialog.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts.DashboardGadgetClientScriptsNewEditDialog.createDialogArea.dialog.message")); //$NON-NLS-1$
		
		final Composite content = new XComposite(parent_, SWT.NONE, LayoutMode.TIGHT_WRAPPER, 2);
		GridData gd;
		
		final Label labelDescription1 = new Label(content, SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd.verticalIndent = 10;
		labelDescription1.setLayoutData(gd);
		labelDescription1.setText(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts.DashboardGadgetClientScriptsNewEditDialog.createDialogArea.label1.name")); //$NON-NLS-1$
		
		// TODO add validation for script names (not empty, not already set,...)
		final Text textClientScriptName = new Text(content, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.verticalIndent = 10;
		textClientScriptName.setLayoutData(gd);
		textClientScriptName.setText(data.getClientScriptName() != null ? data.getClientScriptName() : ""); //$NON-NLS-1$
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
		labelDescription2.setText(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts.DashboardGadgetClientScriptsNewEditDialog.createDialogArea.label2.name")); //$NON-NLS-1$
		
		final Text textClientScriptContent = new Text(content, SWT.BORDER | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		textClientScriptContent.setLayoutData(gd);
		textClientScriptContent.setText(data.getClientScriptContent() != null ? data.getClientScriptContent() : ""); //$NON-NLS-1$
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
