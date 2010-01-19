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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.language.LanguageConfig;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.language.LanguageSyncMode;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * Wizard for configuring language-specific settings.
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class ConfigureLanguageModeWizard extends DynamicPathWizard {

	private ConfigureLanguageModePage configureLanguageModePage;

	public ConfigureLanguageModeWizard() {
		super();
		setWindowTitle(Messages.getString(
			"org.nightlabs.jfire.base.admin.ui.language.ConfigureLanguageModeWizard.wizardTitle")); //$NON-NLS-1$
		configureLanguageModePage = new ConfigureLanguageModePage(Messages.getString(
			"org.nightlabs.jfire.base.admin.ui.language.ConfigureLanguageModeWizard.pageTitle")); //$NON-NLS-1$
		addPage(configureLanguageModePage);
	}

	@Override
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final String chosenLanguageSyncMode = configureLanguageModePage.getChosenLanguageSyncMode();
					if (chosenLanguageSyncMode.equals("")) {
						result[0] = true;
						return;
					}

					final LanguageManagerRemote lm = JFireEjb3Factory.getRemoteBean(
						LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
					LanguageConfig languageConfig = lm.getLanguageConfig(null, -1);
					if (languageConfig.getLanguageSyncMode().toString().equals(chosenLanguageSyncMode)) {
						result[0] = true;
						return;
					}

					LanguageSyncMode[] syncModes = LanguageSyncMode.values();
					for (int i = 0; i < syncModes.length; i++) {
						if (syncModes[i].toString().equals(chosenLanguageSyncMode)) {
							languageConfig.setLanguageSyncMode(syncModes[i]);
							System.out.println("new language sync mode: " + languageConfig.getLanguageSyncMode().toString());
							break;
						}
					}
					result[0] = true;
				}
			});
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
		return result[0];
	}
}
