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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.i18n.I18nUtil;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage;

/**
 *
 * @author Frederik Loeser - frederik[at]nightlabs[dot]de
 */
public class AddLanguagePage extends DynamicPathWizardPage implements FormularChangeListener {

	private static final Logger LOGGER = Logger.getLogger(AddLanguagePage.class);

	private static final String PATH_RESOURCE_LANGUAGES = "resource/language/";

	private static final String PATH_RESOURCE_COUNTRIES = "resource/country/";

	/** {@link XCombo} representing all available languages to add. */
	private XCombo combo;
	/** {@link MouseWheelListener} implementation used for enabling scrolling. */
	private MouseWheelListenerImpl mouseWheelListener;
	/** Keeps track of all wrapper classes used for wrapping Locale objects and further local-specific information. */
	private static Set<LocaleDescriptor> localeDescriptors = new HashSet<LocaleDescriptor>();
	/** Keeps track of all contributions added to language XCombo. */
	private static List<ComboContributionDescriptor> comboContributionDescriptors = null;
	/** True if local-specific information has already been prepared, otherwise false. */
	private static boolean localesPrepared = false;

	/** The constructor. */
	public AddLanguagePage(final String title) {
		// TODO Find appropriate image for this wizard.
		super(AddLanguagePage.class.getName(), title,
			SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(), CreateWorkstationPage.class));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.AddLanguagePage.infoText"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createPageContents(Composite parent) {
//		final Locale currentLocale = LanguageManager.getLocale(LanguageManager.sharedInstance().getCurrentLanguageID());

		AddLanguagePage.prepareLocales();

		// Create contents of page (Label and XCombo).
		final Formular f = new Formular(parent, SWT.NONE, this);
		final Label label = new Label(f, SWT.NULL);
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.AddLanguagePage.labelText"));
		combo = new XCombo(f, SWT.READ_ONLY, 2);
		combo.setVisibleItemCount(8);

		mouseWheelListener = new MouseWheelListenerImpl();
		combo.addMouseWheelListener(mouseWheelListener);

		for (ComboContributionDescriptor ccDesc : comboContributionDescriptors) {
			combo.add(ccDesc.getImgLanguage(), ccDesc.getDisplayName(), ccDesc.getImgCountry(), ccDesc.getPos());
		}
		if (combo.getSelectionIndex() == -1 && combo.getItemCount() > 0) {
			combo.select(0);
		}
		return f;
	}

	private static void prepareLocales() {
		if (!localesPrepared) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("preparing Locales...");
			}

			comboContributionDescriptors = new ArrayList<ComboContributionDescriptor>();
			final List<String> displayNames = new ArrayList<String>();
			final Map<String, LocaleDescriptor> displayNameToLocaleDescriptor = new HashMap<String, LocaleDescriptor>();
			final Locale[] locales = Locale.getAvailableLocales();

			for (int i = 0; i < locales.length; i++) {
				final Locale locale = locales[i];
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("***************************************************");
					LOGGER.debug("Locale country code: " + locale.getCountry());
					LOGGER.debug("Locale country code (display): " + locale.getDisplayCountry());
					LOGGER.debug("Locale language code: " + locale.getLanguage());
					LOGGER.debug("Locale language code (display): " + locale.getDisplayLanguage());
					LOGGER.debug("Locale name (display): " + locale.getDisplayName());
					LOGGER.debug("Locale ISO3 country: " + locale.getISO3Country());
					LOGGER.debug("Locale IOS3 language: " + locale.getISO3Language());
					LOGGER.debug("***************************************************");
				}

				/* Example:
				Locale country code: 			CH
				Locale country code (display): 	Schweiz
				Locale language code: 			de
				Locale language code (display): Deutsch
				Locale name (display): 			Deutsch (Schweiz)
				Locale ISO3 country: 			CHE
				Locale IOS3 language: 			deu
				*/

//				if (locale.getDisplayName().contains("("))		// This is not very sophisticated, but...
//					continue;

				final String displayName = locale.getDisplayName();
				displayNames.add(displayName);
				final LocaleDescriptor localeDescriptor = new LocaleDescriptor(locale, displayName);
				displayNameToLocaleDescriptor.put(displayName, localeDescriptor);
				localeDescriptors.add(localeDescriptor);
			}
			Collections.sort(displayNames);

			for (String displayName : displayNames) {
				final LocaleDescriptor localeDescriptor = displayNameToLocaleDescriptor.get(displayName);
				if (localeDescriptor != null) {
					final String languageID = localeDescriptor.getLocale().getLanguage();
					final String countryID = localeDescriptor.getLocale().getCountry().toLowerCase();
					final String flagResourceLanguage = PATH_RESOURCE_LANGUAGES + languageID + ".png";
					final String flagResourceCountry = PATH_RESOURCE_COUNTRIES + countryID + ".png";
//					final String countryID = localeDescriptor.getLocale().getCountry().toLowerCase();
//					final Image img = LanguageManager.sharedInstance().getFlag16x16Image(languageID);	// not possible as languages are not created yet
					final InputStream isLanguage = I18nUtil.class.getResourceAsStream(flagResourceLanguage);
					final InputStream isCountry = I18nUtil.class.getResourceAsStream(flagResourceCountry);
					if (isLanguage != null) {
						final ImageData imageDataLanguage = new ImageData(isLanguage);
						final ImageDescriptor imageDescriptorLanguage = ImageDescriptor.createFromImageData(imageDataLanguage);
						final Image imgLanguage = imageDescriptorLanguage.createImage();
						Image imgCountry = null;
						if (isCountry != null) {
							final ImageData imageDataCountry = new ImageData(isCountry);
							final ImageDescriptor imageDescriptorCountry = ImageDescriptor.createFromImageData(imageDataCountry);
							imgCountry = imageDescriptorCountry.createImage();
						}
						if (imgCountry == null) {
							comboContributionDescriptors.add(new ComboContributionDescriptor(imgLanguage, displayName, null, -1));
						} else {
							comboContributionDescriptors.add(new ComboContributionDescriptor(imgLanguage, displayName, imgCountry, -1));
						}
					} else {
						comboContributionDescriptors.add(new ComboContributionDescriptor(null, displayName, null, -1));
					}
					try {
						if (isLanguage != null) {
							isLanguage.close();
						}
						if (isCountry != null) {
							isCountry.close();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			localesPrepared = true;
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Locales have already been prepared");
			}
		}
	}


	public String getChosenDisplayName() {
		int idx = combo.getSelectionIndex();
		if (idx > -1 && idx < combo.getItemCount()) {
			return combo.getItem(idx).getText();
		}
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void formularChanged(FormularChangedEvent event) {
	}

	@Override
	public void setPageComplete(boolean complete) {
		super.setPageComplete(complete);
	}

	/**
	 * {@inheritDoc}
	 */
/*	@Override
	public boolean isPageComplete() {
//		int idx = combo.getSelectionIndex();
//		if (idx > -1 && idx < combo.getItemCount() && !combo.getItem(idx).getText().equals("") && super.isPageComplete()) {
		if (!pristine && super.isPageComplete()) {
			return true;
		}
		return false;
	}*/

	private class MouseWheelListenerImpl implements MouseWheelListener {
		@Override
		public void mouseScrolled(MouseEvent arg0) {
			final int count = arg0.count;
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

	public static class LocaleDescriptor {

		private Locale locale;
		private String displayName;

		public LocaleDescriptor(Locale locale, String displayName) {
			this.locale = locale;
			this.displayName = displayName;
		}

		public Locale getLocale() {
			return locale;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public static class ComboContributionDescriptor {

		private Image imgLanguage;
		private String displayName;
		private Image imgCountry;
		private int pos;

		public ComboContributionDescriptor(final Image imgLanguage, final String displayName, final Image imgCountry, final int pos) {
			this.imgLanguage = imgLanguage;
			this.displayName = displayName;
			this.imgCountry = imgCountry;
			this.pos = pos;
		}

		public Image getImgLanguage() {
			return imgLanguage;
		}

		public String getDisplayName() {
			return displayName;
		}

		public Image getImgCountry() {
			return imgCountry;
		}

		public int getPos() {
			return pos;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (combo != null && mouseWheelListener != null) {
			combo.removeMouseWheelListener(mouseWheelListener);
		}
	}

	public MouseWheelListenerImpl getMouseWheelListener() {
		return mouseWheelListener;
	}

	public Set<LocaleDescriptor> getLocaleDescriptors() {
		return localeDescriptors;
	}
}
