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

package org.nightlabs.jfire.base.ui.person.search;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.query.ui.search.EarlySearchFilterProvider;
import org.nightlabs.jdo.query.ui.search.SearchResultFetcher;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.base.ui.prop.search.IStructFieldSearchFilterItemEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.person.PersonSearchFilter;
import org.nightlabs.jfire.prop.search.PropSearchFilter;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PersonSearchEditLayoutFilterProvider
implements EarlySearchFilterProvider
{
	private PersonSearchEditLayoutSearchFilterProviderComposite searchFilterProviderComposite;
	private boolean createOwnSearchButton;
	private boolean createFilterProviderCompositeSearchButton;
	private XComposite wrapper;
	private Button searchButton;
	private SearchResultFetcher resultFetcher;
	private ListenerList modifyListener;
	private String personSearchUseCase;
	private String quickSearchText;
	
	private SelectionListener searchListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			if (resultFetcher != null) {
				resultFetcher.searchTriggered(PersonSearchEditLayoutFilterProvider.this);
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};
	
	/**
	 * Create a new static person SearchFilterProvider.
	 * 
	 * @param resultFetcher A ResultFetcher to be triggered on search.
	 * @param createOwnSearchButton Whether to create an own search button, or to use the default one of {@link PersonSearchEditLayoutSearchFilterProviderComposite}.
	 */
	public PersonSearchEditLayoutFilterProvider(SearchResultFetcher resultFetcher, boolean createOwnSearchButton, String personSearchUseCase, String quickSearchText) {
		this(resultFetcher, createOwnSearchButton, false, personSearchUseCase, quickSearchText);
	}
	
	/**
	 * Create a new static person SearchFilterProvider.
	 * 
	 * @param resultFetcher A ResultFetcher to be triggered on search.
	 * @param createOwnSearchButton Whether to create an own search button, or to use the default one of {@link PersonSearchEditLayoutSearchFilterProviderComposite}.
	 * @param createFilterProviderCompositeSearchButton Whether to create the search button in the filter provider composite.
	 */
	public PersonSearchEditLayoutFilterProvider(SearchResultFetcher resultFetcher, boolean createOwnSearchButton, boolean createFilterProviderCompositeSearchButton, String personSearchUseCase, String quickSearchText) {
		this.resultFetcher = resultFetcher;
		this.createOwnSearchButton = createOwnSearchButton;
		this.createFilterProviderCompositeSearchButton = createFilterProviderCompositeSearchButton;
		this.modifyListener = new ListenerList();
		this.personSearchUseCase = personSearchUseCase;
		this.quickSearchText = quickSearchText;
	}
	
	/**
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterProvider#getComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createComposite(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		
		searchFilterProviderComposite = new PersonSearchEditLayoutSearchFilterProviderComposite(wrapper, SWT.NONE, createFilterProviderCompositeSearchButton, personSearchUseCase, quickSearchText);
		
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
		PropSearchFilter filter = new PersonSearchFilter();
		
		for (IStructFieldSearchFilterItemEditor editor : searchFilterProviderComposite.getEditors()) {
			if (editor.hasSearchConstraint()) {
				filter.addSearchFilterItem(editor.getSearchFilterItem());
			}
		}
		
		return filter;
	}
	
	public static class ParsedNameCriteria {
		public String company;
		public String name;
		public String firstName;
		public long personID = -1;
		public String completeString;
	}
	
	public static Collection<String> parseNameNeedles(String needle) {
		String[] toks = needle.split("[:;,. ]+"); //$NON-NLS-1$
		Collection<String> result = new ArrayList<String>(toks.length);
		for (int i = 0; i < toks.length; i++) {
			result.add(toks[i]);
		}
		return result;
	}
	
	public static ParsedNameCriteria parseNameNeedle(String needle) {
//		String text = searchFilterProviderComposite.getControlName().getTextControl().getText();
		// sTok will return Delims
		ParsedNameCriteria result = new ParsedNameCriteria();
		String[] toks = needle.split("[:;,. ]+"); //$NON-NLS-1$
		result.completeString = needle;
		for (int i = 0; i < toks.length; i++) {
			try {
				long tmpLong = Long.parseLong(toks[i]);
				result.personID = tmpLong;
				result.completeString.replace(toks[i], ""); //$NON-NLS-1$
			} catch (NumberFormatException e) {}
		}
		switch (toks.length) {
			case 3:
				result.company = toks[0];
				result.name = toks[1];
				result.firstName = toks[2];
				break;
			case 2:
				result.company = ""; //$NON-NLS-1$
				result.name = toks[0];
				result.firstName = toks[1];
				break;
			case 1:
				if (needle.indexOf(":") > 0 || needle.indexOf(";") > 0) { //$NON-NLS-1$ //$NON-NLS-2$
					result.company = toks[0];
					result.name = ""; //$NON-NLS-1$
				}
				else {
					result.company = ""; //$NON-NLS-1$
					result.name = toks[0];
				}
				result.firstName = ""; //$NON-NLS-1$
				break;
			default:
				if (toks.length != 0) {
					// TODO: think about this
					result.company = toks[0];
					result.name = toks[1];
					result.firstName = toks[toks.length-1];
				}
				break;
		}
		return result;
	}
	
	
	protected PropSearchFilter createSearchFilter() {
		return new PersonSearchFilter();
	}
	
	public void setEarlySearchText(String earlySearchText) {
		
//		searchFilterProviderComposite.getControlName().getTextControl().setText(earlySearchText);
	}
	
	@Override
	public String getSearchText() {
		return null;
	}

	@Override
	public void addSearchTextModifyListener(ModifyListener listener) {
		modifyListener.add(listener);
	}

	@Override
	public void removeSearchTextModifyListener(ModifyListener listener) {
		modifyListener.remove(listener);
	}
}
