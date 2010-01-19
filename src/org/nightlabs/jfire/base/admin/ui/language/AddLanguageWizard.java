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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.language.LanguageException;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.language.LanguageCf;

/**
 * Wizard for adding new languages to client and server.
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class AddLanguageWizard extends DynamicPathWizard implements INewWizard {

	/** Log4j logger used for this class. */
	private static final Logger logger = Logger.getLogger(AddLanguageWizard.class);
	/** The page added to this wizard. */
	private AddLanguagePage addLanguagePage;

	/**
	 * The constructor.
	 */
	public AddLanguageWizard() {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.AddLanguageWizard.wizardTitle")); //$NON-NLS-1$
		addLanguagePage = new AddLanguagePage(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.AddLanguageWizard.pageTitle")); //$NON-NLS-1$
		addPage(addLanguagePage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final String chosenDisplayName = addLanguagePage.getChosenDisplayName();
					for (AddLanguagePage.LocaleDescriptor localeDescriptor : addLanguagePage.getLocaleDescriptors()) {
						if (localeDescriptor.getDisplayName().equals(chosenDisplayName)) {
							boolean isLanguageCreatedServerSide = true;
							final String languageID = LanguageManager.getLanguageID(localeDescriptor.getLocale());
							final LanguageCf langCf = LanguageManager.createLanguage(languageID);
							final LanguageManagerRemote lm = JFireEjb3Factory.getRemoteBean(
								LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
							final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							try {
								lm.createLanguage(langCf, true, true);		// server-side
							} catch (final LanguageException exception) {
								isLanguageCreatedServerSide = false;
								logger.error("Failed creating language: " + langCf.getLanguageID(), exception); //$NON-NLS-1$
								// Actually this is somehow not an error in the case language sync mode is set to "one only".
								// However, the creation of the selected language failed.
								MessageDialog.openError(shell,
									Messages.getString(
										"org.nightlabs.jfire.base.admin.ui.language.AddLanguageWizard.addLanguageErrorTitle"), //$NON-NLS-1$
									Messages.getString(
										"org.nightlabs.jfire.base.admin.ui.language.AddLanguageWizard.addLanguageErrorMessage")); //$NON-NLS-1$
							}
							// The selected language is added on client-side only in the case it has been created successfully on server-side before.
							if (isLanguageCreatedServerSide) {
								logger.info("Created language on server-side"); //$NON-NLS-1$
								LanguageManager.sharedInstance().addLanguage(localeDescriptor.getLocale());		// client-side
								MessageDialog.openInformation(shell,
									Messages.getString(
										"org.nightlabs.jfire.base.admin.ui.language.AddLanguageWizard.addLanguageSuccessTitle"), //$NON-NLS-1$
									Messages.getString(
										"org.nightlabs.jfire.base.admin.ui.language.AddLanguageWizard.addLanguageSuccessMessage")); //$NON-NLS-1$
							}
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench arg0, IStructuredSelection arg1) {
	}
}
