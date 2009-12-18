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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.language.LanguageException;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.language.LanguageCf;

/**
 *
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class AddLanguageWizard extends DynamicPathWizard implements INewWizard {

	/** LOG4J logger used by this class. */
	private static final Logger logger = Logger.getLogger(AddLanguageWizard.class);

	private AddLanguagePage addLanguagePage;

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
							boolean isLangCreatedRemote = true;
							// Create new LanguageCf instance.
							LanguageCf langCf = LanguageManager.createLanguage(LanguageManager.getLanguageID(localeDescriptor.getLocale()));
							LanguageManagerRemote lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
							try {
								lm.createLanguage(langCf, true, true);		// server-side
							} catch (LanguageException e) {
								isLangCreatedRemote = false;
								logger.error("Failed creating language: " + langCf.getLanguageID(), e); //$NON-NLS-1$
							}
							// Language is added on client side only in the case it has successfully been created on server side before.
							if (isLangCreatedRemote) {
								logger.info("Created language on server-side");
								LanguageManager.sharedInstance().addLanguage(localeDescriptor.getLocale());		// client-side
							}
						}
					}
					result[0] = true;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
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
