/**
 * 
 */
package org.nightlabs.jfire.base.ui.language;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.LanguageChooserList;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.language.LanguageCf;
import org.nightlabs.util.NLLocale;

/**
 * @author Alexander Bieber <!-- alex [at] nightlabs [dot] de -->
 *
 */
public class SwitchLanguageDialog extends Dialog {

	private LanguageChooserList languageChooser;
	
	/**
	 * @param parentShell
	 */
	public SwitchLanguageDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);
		Label l = new Label(wrapper, SWT.WRAP);
		l.setText(Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.label.selectLanguage")); //$NON-NLS-1$
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		languageChooser = new LanguageChooserList(wrapper);
		languageChooser.setLayoutData(new GridData(GridData.FILL_BOTH));
		languageChooser.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		return wrapper;
	}

	@Override
	protected void okPressed() {
		LanguageCf langCf = languageChooser.getLanguage();
		super.okPressed();
		if (NLLocale.getDefault().getLanguage().equals(langCf.getLanguageID()))
			return; // nothing to do, same language
		LanguageManager.sharedInstance().setLanguageID(langCf.getLanguageID());
		PlatformUI.getWorkbench().restart();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.base.ui.language.SwitchLanguageDialog.window.title")); //$NON-NLS-1$
		newShell.setSize(300, 300);
//		setToCenteredLocationPreferredSize(newShell, 300, 300);
	}

	
	
}
