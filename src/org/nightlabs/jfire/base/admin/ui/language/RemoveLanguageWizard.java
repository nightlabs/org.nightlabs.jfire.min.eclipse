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
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.language.id.LanguageID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.language.LanguageCf;

/**
 * Wizard for removing languages from client and server.
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class RemoveLanguageWizard extends DynamicPathWizard implements INewWizard {

	/** Log4j logger used for this class. */
	private static final Logger logger = Logger.getLogger(RemoveLanguageWizard.class);
	/** The page added to this wizard. */
	private RemoveLanguagePage removeLanguagePage;

	/**
	 * The constructor.
	 */
	public RemoveLanguageWizard() {
		super();
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.RemoveLanguageWizard.wizardTitle")); //$NON-NLS-1$
		removeLanguagePage = new RemoveLanguagePage(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.RemoveLanguageWizard.pageTitle")); //$NON-NLS-1$
		addPage(removeLanguagePage);
	}

	@Override
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final String chosenLanguageID = removeLanguagePage.getChosenLanguageID();
					if (!chosenLanguageID.equals("")) {
						final LanguageCf langCf = LanguageManager.createLanguage(chosenLanguageID);
						final LanguageID languageID = LanguageID.create(langCf.getLanguageID());
						final LanguageManagerRemote lm = JFireEjb3Factory.getRemoteBean(
							LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
						if (languageID != null) {
							final boolean res = lm.deleteLanguage(languageID);		// server-side
							if (res) {
								logger.info("Removed existing language on server-side");
							}
						}
						LanguageManager.sharedInstance().removeLanguage(chosenLanguageID);
					}
					result[0] = true;
				}
			});
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
		return result[0];
	}

	@Override
	public void init(IWorkbench arg0, IStructuredSelection arg1) {
	}
}
