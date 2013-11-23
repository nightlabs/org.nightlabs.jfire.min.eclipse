/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.admin.ui.language;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.FormularChangeListener;
import org.nightlabs.base.ui.composite.FormularChangedEvent;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage;
import org.nightlabs.jfire.language.LanguageConfig;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.language.LanguageSyncMode;
import org.nightlabs.jfire.security.GlobalSecurityReflector;

/**
 * Wizard page for configuring language-specific settings,
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class ConfigureLanguageModePage extends DynamicPathWizardPage implements FormularChangeListener {

	private Combo languageSyncModeCombo;

	/**
	 * The constructor.
	 * @param title The title of the page.
	 */
	public ConfigureLanguageModePage(final String title) {
		// TODO Find appropriate image for this wizard.
		super(
			ConfigureLanguageModePage.class.getName(),
			title,
			SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(), CreateWorkstationPage.class));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.ConfigureLanguageModePage.infoText"));
	}

	@Override
	public Control createPageContents(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE);
		
		final Label label = new Label(wrapper, SWT.WRAP);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.ConfigureLanguageModePage.labelText"));

		languageSyncModeCombo = new Combo(wrapper, SWT.READ_ONLY);
		languageSyncModeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final LanguageManagerRemote lm = JFireEjb3Factory.getRemoteBean(
			LanguageManagerRemote.class, GlobalSecurityReflector.sharedInstance().getInitialContextProperties());
		LanguageConfig languageConfig = lm.getLanguageConfig(null, -1);
		final String currentLanguageSyncMode = languageConfig.getLanguageSyncMode().toString();

		int idx = 0;
		LanguageSyncMode[] syncModes = LanguageSyncMode.values();
		for (int i = 0; i < syncModes.length; i++) {
			final String syncModeName = syncModes[i].toString();
			languageSyncModeCombo.add(syncModeName);
			if (syncModeName.equals(currentLanguageSyncMode)) {
				idx = i;
			}
		}
//		languageSyncModeCombo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		languageSyncModeCombo.select(idx);
		languageSyncModeCombo.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
				event.doit = false;
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
		});
		return wrapper;
	}

	public String getChosenLanguageSyncMode() {
		int idx = languageSyncModeCombo.getSelectionIndex();
		if (idx > -1 && idx < languageSyncModeCombo.getItemCount()) {
			return languageSyncModeCombo.getItem(idx);
		}
		return "";
	}

	@Override
	public void formularChanged(FormularChangedEvent event) {
	}
}
