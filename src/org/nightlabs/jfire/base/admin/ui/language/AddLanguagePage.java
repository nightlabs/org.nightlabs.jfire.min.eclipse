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
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.admin.ui.workstation.CreateWorkstationPage;
import org.nightlabs.language.LanguageCf;

/**
 * @author Frederik Löser <frederik[AT]nightlabs[DOT]de>
 */
public class AddLanguagePage extends DynamicPathWizardPage implements FormularChangeListener {
	
	private XCombo combo;
	private MouseWheelListenerImpl mouseWheelListener;
	private Set<LocaleDescriptor> localeDescriptors = new HashSet<LocaleDescriptor>();

	public AddLanguagePage(String title) {
		super(AddLanguagePage.class.getName(), title,
		// TODO create image for this wizard	
		SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(), CreateWorkstationPage.class));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.AddLanguagePage.infoText"));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control createPageContents(Composite parent) {
		int idx = 0;
		final List<String> displayNames = new ArrayList<String>();
		final Map<String, LocaleDescriptor> displayNameToLocaleDescriptor = new HashMap<String, LocaleDescriptor>();
		
		// Collect all available Locale objects and create appropriate descriptors.
		final Locale[] locales = Locale.getAvailableLocales();
		final Locale currentLocale = LanguageManager.getLocale(LanguageManager.sharedInstance().getCurrentLanguageID());
		final String currentLangDisplayName = currentLocale.getDisplayName();
		
		for (int i = 0; i < locales.length; i++) {
			final Locale locale = locales[i];
//			System.out.println("Locale country code: " + locale.getCountry());
//			System.out.println("Locale country code (display): " + locale.getDisplayCountry());
//			System.out.println("Locale language code: " + locale.getLanguage());
//			System.out.println("Locale language code (display): " + locale.getDisplayLanguage());
//			System.out.println("Locale name (display): " + locale.getDisplayName());
//			System.out.println("Locale ISO3 country: " + locale.getISO3Country());
//			System.out.println("Locale IOS3 language: " + locale.getISO3Language());
			
			/* Example:
			Locale country code: 			CH
			Locale country code (display): 	Schweiz
			Locale language code: 			de
			Locale language code (display): Deutsch
			Locale name (display): 			Deutsch (Schweiz)
			Locale ISO3 country: 			CHE
			Locale IOS3 language: 			deu
			*/
			
			final String displayName = locale.getDisplayName();
			displayNames.add(displayName);
			final LocaleDescriptor localeDescriptor = new LocaleDescriptor(locale, displayName);
			displayNameToLocaleDescriptor.put(displayName, localeDescriptor);
			localeDescriptors.add(localeDescriptor);
		}

		// Create contents of page (Label and XCombo).	
		final Formular f = new Formular(parent, SWT.NONE, this);
		final Label label = new Label(f, SWT.NULL);
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.AddLanguagePage.labelText"));
		combo = new XCombo(f, SWT.READ_ONLY);
		combo.setVisibleItemCount(8);
		
		mouseWheelListener = new MouseWheelListenerImpl();
		combo.addMouseWheelListener(mouseWheelListener);
		
		Collections.sort(displayNames);
		for (String displayName : displayNames) {
			final LocaleDescriptor localeDescriptor = displayNameToLocaleDescriptor.get(displayName);
			if (localeDescriptor != null) {
				final String languageID = localeDescriptor.getLocale().getLanguage();
//				final Image img = LanguageManager.sharedInstance().getFlag16x16Image(languageID);	// not possible as languages are not created yet
				final String flagResource = "resource/" + languageID + ".png";
				final InputStream in = LanguageCf.class.getResourceAsStream(flagResource);
				if (in != null) {
					final ImageData imageData = new ImageData(in);
					final ImageDescriptor imageDescriptor = ImageDescriptor.createFromImageData(imageData);
					final Image img = imageDescriptor.createImage();
					combo.add(img, displayName);
				} else {
					combo.add(null, displayName);
				}
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				combo.add(null, displayName);
			}
			if (currentLangDisplayName.equals(displayName)) {
				combo.select(idx);
			}
			idx++;
		}
		if (combo.getSelectionIndex() == -1 && combo.getItemCount() > 0) {
			combo.select(0);
		}
		return f;
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
