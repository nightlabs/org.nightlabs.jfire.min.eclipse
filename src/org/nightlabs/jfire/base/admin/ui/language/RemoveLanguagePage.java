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
import java.util.HashSet;
import java.util.Iterator;
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
public class RemoveLanguagePage extends DynamicPathWizardPage implements FormularChangeListener {

	private XCombo combo;
	private MouseWheelListenerImpl mouseWheelListener;
	private Map<String, String> displayNameToLanguageID = new HashMap<String, String>();
	private Set<String> languageIDsInalienable = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("en");
//			add("de");
		}
	};
	
	public RemoveLanguagePage(String title) {
		super(RemoveLanguagePage.class.getName(), title,
		// TODO create image for this wizard
		SharedImages.getWizardPageImageDescriptor(BaseAdminPlugin.getDefault(), CreateWorkstationPage.class));
		setDescription(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.RemoveLanguagePage.infoText"));
	}
	
	@Override
	public Control createPageContents(Composite parent) {
		int idx = 0;
		final List<String> displayNames = new ArrayList<String>();
		final Locale currentLocale = LanguageManager.getLocale(LanguageManager.sharedInstance().getCurrentLanguageID());
		final String currentLangDisplayName = currentLocale.getDisplayName();
		final Collection<LanguageCf> registeredLanguages = LanguageManager.sharedInstance().getLanguages();
		final Iterator<LanguageCf> it = registeredLanguages.iterator();
		
		// Create contents of page (Label and XCombo).	
		final Formular f = new Formular(parent, SWT.NONE, this);
		final Label label = new Label(f, SWT.NULL);
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.language.RemoveLanguagePage.labelText"));
		combo = new XCombo(f, SWT.READ_ONLY);
		combo.setVisibleItemCount(8);
		
		mouseWheelListener = new MouseWheelListenerImpl();
		combo.addMouseWheelListener(mouseWheelListener);
		
		while (it.hasNext()) {
			final LanguageCf languageCf = it.next();
			final String languageID = languageCf.getLanguageID();
			if (!languageIDsInalienable.contains(languageID)) {
				final Locale locale = LanguageManager.getLocale(languageID);
				final String displayName = locale.getDisplayName();
				displayNameToLanguageID.put(displayName, languageID);
				displayNames.add(displayName);
			}
		}
		
		Collections.sort(displayNames);
		for (String displayName : displayNames) {
			final String languageID = displayNameToLanguageID.get(displayName);
			if (languageID != null) {
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
				if (currentLangDisplayName.equals(displayName)) {
					combo.select(idx);
				}
				idx++;
			}
		}
		if (combo.getSelectionIndex() == -1 && combo.getItemCount() > 0) {
			combo.select(0);
		}
		return f;
	}

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
	
	@Override
	public void formularChanged(FormularChangedEvent event) {
	}
	
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (combo != null && mouseWheelListener != null) {
			combo.removeMouseWheelListener(mouseWheelListener);
		}
	}
}
