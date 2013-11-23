/* *****************************************************************************
 * org.nightlabs.jdo.query.ui - NightLabs Eclipse utilities for JDO                     *
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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jdo.query.ui.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jdo.search.SearchFilter;

/**
 * SearchFilterProvider providing a changeable list of SearchFilterItems for the search. The class
 * has to be instantiated with a {@link SearchFilterItemListMutator} in order to change the item
 * list. <br/>
 * Subclasses have to override {@link #createSearchFilter()}, so they have full control of what type
 * of SearchFilter is instantiated.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractItemBasedSearchFilterProvider implements SearchFilterProvider {

	
	protected SearchFilterItemListMutator listMutator;
	protected AbstractItemBasedSearchFilterProviderComposite providerComposite;
	protected SearchResultFetcher resultFetcher;
	
	/**
	 * Used to create custom instances of implementors of SearchFilter.<br/>
	 * A typical implementation would look like:
	 * <pre>
	 * 	return new MyInheritorOfSearchFilter();
	 * </pre>
	 * 
	 * @return A new intance of the {@link SearchFilter} this object provides.
	 */
	protected abstract SearchFilter createSearchFilter();

	/**
	 * Creates new FilterProvider with listMutator used as callback for modifying the item list.<br/>
	 * Callback for the search button can be set with {@link #setResultFetcher(SearchResultFetcher)}
	 * 
	 * @param listMutator Object to utilize in order to modify the filter-item list.
	 */
	public AbstractItemBasedSearchFilterProvider(SearchFilterItemListMutator listMutator) {
		this.listMutator = listMutator;
	}

	/**
	 * Set the {@link SearchFilterItemListMutator} used by this provider.
	 * 
	 * @param listMutator Object to utilize in order to modify the filter-item list.
	 */
	public void setListMutator(SearchFilterItemListMutator listMutator) {
		this.listMutator = listMutator;
	}
	
	/**
	 * @return The {@link SearchFilterItemListMutator} currently used by this provider.
	 */
	public SearchFilterItemListMutator getListMutator() {
		return listMutator;
	}

	/**
	 * Create the Composite of this provider. In this method the complete ui for this
	 * filter-provider should be created as a subclass of
	 * {@link AbstractItemBasedSearchFilterProviderComposite}.
	 * 
	 * @param parent The parent to add the new Composite to.
	 * @param style The style to use for the Composite.
	 * @return A new implementation of {@link AbstractItemBasedSearchFilterProviderComposite}.
	 */
	protected abstract AbstractItemBasedSearchFilterProviderComposite createProviderComposite(
			Composite parent,
			int style
		);

	/**
	 * {@inheritDoc}
	 * <p>
	 * Uses {@link #createProviderComposite(Composite, int)} to create the Composite, remembers the
	 * result to implement {@link #getComposite()}.
	 * </p>
	 */
	@Override
	public Composite createComposite(Composite parent) {
		providerComposite = createProviderComposite(
			parent,
			SWT.NONE
		);
		return providerComposite;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Composite getComposite() {
		return providerComposite;
	}

	/**
	 * Calls createSearchFilter, clears the obtained filter and adds all items from the
	 * SearchFilterItemList.<br/>
	 * When overridden, super() should be called, then SearchFilterItems can be added or removed to
	 * the returned filter.
	 * 
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterProvider#getSearchFilter()
	 */
	@Override
	public SearchFilter getSearchFilter() {
		SearchFilter filter = createSearchFilter();
		filter.clear();
		filter.setConjunction(providerComposite.getConjuction());
		providerComposite.getItemList().addItemsToFilter(filter);
		return filter;
	}

	/**
	 * Set the {@link SearchResultFetcher} this provider should use to trigger searches with the
	 * filter this provider creates.
	 * 
	 * @param resultFetcher The resultFetcher to set.
	 */
	@Override
	public void setResultFetcher(SearchResultFetcher resultFetcher) {
		this.resultFetcher = resultFetcher;
	}
	
	/**
	 * @return the {@link SearchResultFetcher} currently set for this provider.
	 */
	@Override
	public SearchResultFetcher getResultFetcher() {
		return resultFetcher;
	}
}
