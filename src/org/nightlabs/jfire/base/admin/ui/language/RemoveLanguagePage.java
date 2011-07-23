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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.Formular;
import org.nightlabs.base.ui.composite.FormularChangeListener;
import org.nightlabs.base.ui.composite.FormularChangedEvent;
import org.nightlabs.base.ui.custom.XCombo;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.i18n.I18nUtil;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage;
import org.nightlabs.jfire.compatibility.CompatibleSWT;
import org.nightlabs.language.LanguageCf;

/**
 * Wizard page for removing languages from client and server. Every language can be removed except the last
 * one registered and the one that is currently used.
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class RemoveLanguagePage extends DynamicPathWizardPage implements FormularChangeListener {

	/** {@link XCombo} representing all registered languages that can be removed. */
	private XCombo combo;
	/** {@link MouseWheelListener} implementation used for enabling scrolling. */
	private MouseWheelListenerImpl mouseWheelListener;
	/** Keeps track of all display name/language ID pairs registered.<br>
	 *  TODO do not use display name as key although it seems that it could be considered as one, but... */
	private Map<String, String> displayNameToLanguageID = new HashMap<String, String>();
	/** True if there is only one language registered, otherwise false. */
	private boolean lastItem = false;

	/**
	 * The constructor.
	 * @param title The title of the page.
	 */
	public RemoveLanguagePage(String title) {
		// TODO Find appropriate image for this wizard.
		super(
			RemoveLanguagePage.class.getName(),
			title,
			SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(), CreateWorkstationPage.class));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.RemoveLanguagePage.infoText"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createPageContents(Composite parent) {
		final List<String> displayNames = new ArrayList<String>();
		final Locale currentLocale = LanguageManager.getLocale(LanguageManager.sharedInstance().getCurrentLanguageID());
		final String currentLocaleDisplayName = currentLocale.getDisplayName();
		final Collection<LanguageCf> registeredLanguages = LanguageManager.sharedInstance().getLanguages();
		final Iterator<LanguageCf> it = registeredLanguages.iterator();
		if (registeredLanguages.size() <= 1) {
			lastItem = true;
		}

		// Create contents of this wizard page.
		final Formular f = new Formular(parent, SWT.NONE, this);
		if (lastItem) {
			// There is only one item left that cannot be removed.
			final Label label = new Label(f, SWT.NULL);
			label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.RemoveLanguagePage.labelText_RemovingNotPossible"));
			setPageComplete(false);
			return f;
		}
		final Label label = new Label(f, SWT.NULL);
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.RemoveLanguagePage.labelText"));
		combo = new XCombo(f, SWT.READ_ONLY);
		combo.setVisibleItemCount(8);
		mouseWheelListener = new MouseWheelListenerImpl();
		CompatibleSWT.addMouseWheelListener(combo, mouseWheelListener);

		while (it.hasNext()) {
			final LanguageCf languageCf = it.next();
			final String languageID = languageCf.getLanguageID();
			final Locale locale = LanguageManager.getLocale(languageID);
			final String displayName = locale.getDisplayName();
			displayNameToLanguageID.put(displayName, languageID);
			displayNames.add(displayName);
		}

		Collections.sort(displayNames);
		for (String displayName : displayNames) {
			if (displayName.equals(currentLocaleDisplayName))
				// The current Locale cannot be removed.
				continue;
			final String languageID = displayNameToLanguageID.get(displayName);
			if (languageID != null) {
				final String flagResourceLanguage = "resource/language/" + languageID + ".png";
				final InputStream isLanguage = I18nUtil.class.getResourceAsStream(flagResourceLanguage);
				if (isLanguage != null) {
					final ImageData imageData = new ImageData(isLanguage);
					final ImageDescriptor imageDescriptor = ImageDescriptor.createFromImageData(imageData);
					final Image img = imageDescriptor.createImage();
					combo.add(img, displayName);
				} else {
					combo.add(null, displayName);
				}
				try {
					if (isLanguage != null) {
						isLanguage.close();
					}
				} catch (final IOException exception) {
					throw new RuntimeException(exception);
				}
			}
		}
		if (combo.getSelectionIndex() == -1 && combo.getItemCount() > 0) {
			combo.select(0);
		}
		return f;
	}

	/**
	 * Gets language ID according to text of selected {@link XCombo} item.
	 * Called when performing finish in {@link RemoveLanguageWizard}.
	 * @return language ID according to text of selected {@link XCombo} item
	 * or the empty string if index is out of range or no item has been selected.
	 */
	public String getChosenLanguageID() {
		int idx = combo.getSelectionIndex();
		if (idx > -1 && idx < combo.getItemCount()) {
			final String chosenDisplayName = combo.getItem(idx).getText();
			final String chosenLanguageID = displayNameToLanguageID.get(chosenDisplayName);
			if (chosenLanguageID != null) {
				return chosenLanguageID;
			}
		}
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void formularChanged(FormularChangedEvent event) {
	}

	/**
	 * {@link MouseWheelListener} implementation used for {@link XCombo}.
	 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
	 */
	private class MouseWheelListenerImpl implements MouseWheelListener {
		@Override
		public void mouseScrolled(MouseEvent arg0) {
			final int count = CompatibleSWT.getMouseEventCount(arg0);
			final int direction = count > 0 ? -1 : 1;
			final int newSelection_ = combo.getSelectionIndex() + direction;
			final int newSelection;
			if (newSelection_ == -1) {
				newSelection = combo.getItemCount() - 1;
			} else if (newSelection_ == combo.getItemCount()) {
				newSelection = 0;
			} else {
				newSelection = newSelection_;
			}
			combo.select(newSelection);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (combo != null && mouseWheelListener != null) {
			CompatibleSWT.removeMouseWheelListener(combo, mouseWheelListener);
		}
	}

	/**
	 * @return true in the case only one language is registered, otherwise false.
	 */
	public boolean isLastItem() {
		return lastItem;
	}
}
