package org.nightlabs.jfire.base.admin.ui.language.preference;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.security.auth.login.LoginException;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.language.LanguageConfig;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.language.LanguageSyncMode;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * Preference page used for setting language synchronisation mode.
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class LanguageSyncModePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private LanguageManagerRemote lm;
	private LanguageConfig languageConfig;
	private String currentLanguageSyncMode;
	private String selectedLanguageSyncMode;
	private XComboComposite<String> languageSyncModeChooseCombo = null;
	private int idx = 0;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite content = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			Login.getLogin(false).setForceLogin(true);
			Login.getLogin();
		} catch (final LoginException e) {
			setValid(false);
			return content;
		}

		// Create label and combo.
		final Label languageSyncModeChooseLabel = new Label(content, SWT.NONE);
		languageSyncModeChooseLabel.setText(Messages.getString(
			"org.nightlabs.jfire.base.admin.ui.language.preference.LanguageSyncModePreferencePage.labelText")); //$NON-NLS-1$
		languageSyncModeChooseCombo = new XComboComposite<String>(content, SWT.READ_ONLY, languageSyncModeLabelProvider);
		languageSyncModeChooseCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Get language manager, configuration and current sync mode.
		lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
		languageConfig = lm.getLanguageConfig(new String[]{FetchPlan.DEFAULT}, -1);
		currentLanguageSyncMode = languageConfig.getLanguageSyncMode().toString();

		// Collect all available language synchronisation modes.
		final List<String> languageSyncModeNames = new ArrayList<String>();
		final LanguageSyncMode[] syncModes = LanguageSyncMode.values();
		for (int i = 0; i < syncModes.length; i++) {
			final String syncModeName = syncModes[i].toString();
			languageSyncModeNames.add(syncModeName);
			if (syncModeName.equals(currentLanguageSyncMode)) {
				idx = i;
			}
		}

		// Fill combo, select current mode and add selection listener.
		languageSyncModeChooseCombo.setInput(languageSyncModeNames);
		languageSyncModeChooseCombo.selectElementByIndex(idx);
		languageSyncModeChooseCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				selectedLanguageSyncMode = languageSyncModeChooseCombo.getSelectedElement();
			}
		});
		return content;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench arg0) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performOk() {
		if (!currentLanguageSyncMode.equals(selectedLanguageSyncMode)) {
			LanguageSyncMode[] syncModes = LanguageSyncMode.values();
			for (int i = 0; i < syncModes.length; i++) {
				if (syncModes[i].toString().equals(selectedLanguageSyncMode)) {
					languageConfig.setLanguageSyncMode(syncModes[i]);
					lm.storeLanguageConfig(languageConfig, false, null, -1);
					break;
				}
			}
		}
		return super.performOk();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void performDefaults() {
		if (Login.isLoggedIn()) {
			languageSyncModeChooseCombo.selectElementByIndex(idx);
			super.performDefaults();
		}
	}

	/**
	 * {@link ILabelProvider} for language sync mode combo.
	 */
	private ILabelProvider languageSyncModeLabelProvider = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof String) {
				return (String) element;
			}
			return ""; //$NON-NLS-1$
		}
	};
}
