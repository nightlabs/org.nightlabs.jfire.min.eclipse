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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.query.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.query.ui.search.SearchResultFetcher;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.person.PersonSearchFilter;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;

/**
 * A {@link SearchFilterProvider} that uses {@link PropertySetSearchFilterProviderComposite} and a
 * sub-class of {@link PropertySetSearchEditLayoutConfigModule} in order to show the search-fields
 * according to the layout defined in the config-module.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PropertySetSearchFilterProvider
implements SearchFilterProvider
{
	private PropertySetSearchFilterProviderComposite searchFilterProviderComposite;
	private boolean createOwnSearchButton;
	private boolean createFilterProviderCompositeSearchButton;
	private XComposite wrapper;
	private Button searchButton;
	private SearchResultFetcher resultFetcher;
	private Class<? extends PropertySetSearchEditLayoutConfigModule> configModuleClass;
	private String propertySearchUseCase;
	private String quickSearchText;
	
	private SelectionListener searchListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			if (resultFetcher != null) {
				resultFetcher.searchTriggered(PropertySetSearchFilterProvider.this);
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	/**
	 * Create a new static person SearchFilterProvider.
	 * 
	 * @param resultFetcher A ResultFetcher to be triggered on search.
	 * @param createOwnSearchButton Whether to create an own search button, or to use the default
	 *            one of {@link PropertySetSearchFilterProviderComposite}.
	 * @param configModuleClass The concrete sub-class of
	 *            {@link PropertySetSearchEditLayoutConfigModule} this search-filter should read to
	 *            know which search-fields to display.
	 * @param propertySetSearchUseCase The use-case for the search to use. The use-case defines the
	 *            cfModID of the config-module that will be downloaded - in addition to the
	 *            client-type (RCP). See {@link AbstractEditLayoutConfigModule#getCfModID(String, String)}.
	 * @param quickSearchText An optional text that will be used to fill the quick-search-entry and
	 *            trigger an early search.
	 */
	public PropertySetSearchFilterProvider(SearchResultFetcher resultFetcher, boolean createOwnSearchButton,
			Class<? extends PropertySetSearchEditLayoutConfigModule> configModuleClass, String propertySetSearchUseCase, String quickSearchText) {
		this(resultFetcher, createOwnSearchButton, false, configModuleClass, propertySetSearchUseCase, quickSearchText);
	}

	/**
	 * Create a new static person SearchFilterProvider.
	 * 
	 * @param resultFetcher A ResultFetcher to be triggered on search.
	 * @param createOwnSearchButton Whether to create an own search button, or to use the default
	 *            one of {@link PropertySetSearchFilterProviderComposite}.
	 * @param createFilterProviderCompositeSearchButton Whether to create the search button in the
	 *            filter provider composite.
	 * @param configModuleClass The concrete sub-class of
	 *            {@link PropertySetSearchEditLayoutConfigModule} this search-filter should read to
	 *            know which search-fields to display.
	 * @param propertySetSearchUseCase The use-case for the search to use. The use-case defines the
	 *            cfModID of the config-module that will be downloaded - in addition to the
	 *            client-type (RCP). See {@link AbstractEditLayoutConfigModule#getCfModID(String, String)}.
	 * @param quickSearchText An optional text that will be used to fill the quick-search-entry and
	 *            trigger an early search.
	 */
	public PropertySetSearchFilterProvider(SearchResultFetcher resultFetcher, boolean createOwnSearchButton,
			boolean createFilterProviderCompositeSearchButton, Class<? extends PropertySetSearchEditLayoutConfigModule> configModuleClass, String propertySetSearchUseCase, String quickSearchText) {
		this.resultFetcher = resultFetcher;
		this.createOwnSearchButton = createOwnSearchButton;
		this.createFilterProviderCompositeSearchButton = createFilterProviderCompositeSearchButton;
		this.propertySearchUseCase = propertySetSearchUseCase;
		this.configModuleClass = configModuleClass;
		this.quickSearchText = quickSearchText;
	}
	
	/**
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterProvider#getComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createComposite(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		
		searchFilterProviderComposite = new PropertySetSearchFilterProviderComposite(wrapper, SWT.NONE,
				createFilterProviderCompositeSearchButton, configModuleClass, propertySearchUseCase, quickSearchText);
		
		if (createOwnSearchButton) {
			searchButton = new Button(searchFilterProviderComposite, SWT.PUSH);
			searchButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonSearchEditLayoutFilterProvider.searchButton.text")); //$NON-NLS-1$
			searchButton.addSelectionListener(searchListener);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.END;
			gd.widthHint = 80;
			searchButton.setLayoutData(gd);
		}
		
		if (createFilterProviderCompositeSearchButton) {
			searchFilterProviderComposite.getSearchButton().addSelectionListener(searchListener);
		}

		searchFilterProviderComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return wrapper;
	}
	
	public Composite getComposite() {
		return wrapper;
	}

	/**
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterProvider#getPersonSearchFilter()
	 */
	public SearchFilter getSearchFilter() {
		PropSearchFilter filter = createSearchFilter();
		
		for (IStructFieldSearchFilterItemEditor editor : searchFilterProviderComposite.getEditors()) {
			if (editor.hasSearchConstraint()) {
				filter.addSearchFilterItem(editor.getSearchFilterItem());
			}
		}
		
		return filter;
	}
	
	/**
	 * Create the search filter to be used for the search. Override this method and return an instance of another class if you
	 * want to get a different result type of the query.
	 * 
	 * @return the newly created search filter.
	 */
	protected PropSearchFilter createSearchFilter() {
		return new PersonSearchFilter();
	}
	
	/**
	 * Set the {@link SearchResultFetcher} this provider should use to do own searches.
	 * This only applies if createOwnSearchButton was set to <code>true</code>.
	 * 
	 * @param resultFetcher The {@link SearchResultFetcher} to set.
	 */
	@Override
	public void setResultFetcher(SearchResultFetcher resultFetcher) {
		this.resultFetcher = resultFetcher;
	}
	
	@Override
	public SearchResultFetcher getResultFetcher() {
		return resultFetcher;
	}
}
