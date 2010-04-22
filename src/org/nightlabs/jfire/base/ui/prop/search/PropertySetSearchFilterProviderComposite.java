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

package org.nightlabs.jfire.base.ui.prop.search;

import java.util.List;

import javax.jdo.FetchPlan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.config.ConfigUtil;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutEntry;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.search.config.StructFieldSearchEditLayoutEntry;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * Composite that uses a sub-class of {@link PropertySetSearchEditLayoutConfigModule} to layout the
 * search-entries configured there. Internally this composite uses a
 * {@link PropertySetSearchEditLayoutComposite}, it can additionally create a search-button below
 * the search-composite.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PropertySetSearchFilterProviderComposite extends XComposite {
	
	private PropertySetSearchEditLayoutComposite personSearchEditLayoutComposite;
	private Button searchButton;

	/**
	 * Create a new {@link PropertySetSearchFilterProviderComposite}.
	 * 
	 * @param parent The parent to add the composite to.
	 * @param style The style to apply to the composite.
	 * @param createSearchButton Whether to create a search-button below the search-composite. If
	 *            this is <code>true</code>, the search-button can be accessed using
	 *            {@link #getSearchButton()}.
	 * @param configModuleClass The concrete sub-class of
	 *            {@link PropertySetSearchEditLayoutConfigModule} this search-filter should read to
	 *            know which search-fields to display.
	 * @param propertySetSearchUseCase The use-case for the search to use. The use-case defines the
	 *            cfModID of the config-module that will be downloaded - in addition to the
	 *            client-type (RCP). See {@link AbstractEditLayoutConfigModule#getCfModID(String, String)}.
	 * @param quickSearchText An optional text that will be used to fill the quick-search-entry and
	 *            trigger an early search.
	 */
	public PropertySetSearchFilterProviderComposite(Composite parent, int style, boolean createSearchButton, 
			Class<? extends PropertySetSearchEditLayoutConfigModule> configModuleClass, String propertySetSearchUseCase,
			String quickSearchText) {
		super(parent, style, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		final String[] fetchGroups = new String[] {
				AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES,
				AbstractEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT,
				AbstractEditLayoutEntry.FETCH_GROUP_GRID_DATA,
				PropertySetEditLayoutEntry.FETCH_GROUP_STRUCT_FIELDS,
				PersonSearchConfigModule.FETCH_GROUP_QUICK_SEARCH_ENTRY,
				StructField.FETCH_GROUP_NAME,
				IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA,
				FetchPlan.DEFAULT
		};
		
		final String cfModID = AbstractEditLayoutConfigModule.getCfModID(AbstractEditLayoutConfigModule.CLIENT_TYPE_RCP, propertySetSearchUseCase);
		PropertySetSearchEditLayoutConfigModule cfMod = ConfigUtil.getUserCfMod(configModuleClass, cfModID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
		
		List<PropertySetEditLayoutEntry> editLayoutEntries = cfMod.getEditLayoutEntries();
		
		// TODO Think about the generics tagging of PersonSearchConfigModule to get rid of the following cast.
		List<StructFieldSearchEditLayoutEntry> entries = CollectionUtil.castList(editLayoutEntries);
		personSearchEditLayoutComposite = new PropertySetSearchEditLayoutComposite(this, SWT.NONE, cfMod.getGridLayout(), entries, cfMod.getQuickSearchEntry(), quickSearchText);
		
		if (createSearchButton) {
			searchButton = new Button(this, SWT.PUSH);
			searchButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.person.search.StaticPersonSearchFilterProviderComposite.searchButton.text")); //$NON-NLS-1$
			GridData searchButtonLData = new GridData(GridData.VERTICAL_ALIGN_END);
			searchButtonLData.grabExcessHorizontalSpace = false;
			searchButton.setLayoutData(searchButtonLData);
		}

		this.layout();
	}
	
	/**
	 * @return All {@link IStructFieldSearchFilterItemEditor}s used by this composite.
	 */
	public List<IStructFieldSearchFilterItemEditor> getEditors() {
		return personSearchEditLayoutComposite.getSearchFilterItemEditors();
	}

	/**
	 * @return The search-button created for this composite, or <code>null</code> if this composite
	 *         was created with <code>createSearchButton == false</code>
	 */
	public Button getSearchButton() {
		return searchButton;
	}
}
