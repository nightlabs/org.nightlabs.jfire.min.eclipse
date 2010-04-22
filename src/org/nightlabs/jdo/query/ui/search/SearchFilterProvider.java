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

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jdo.search.SearchFilter;

/**
 * Common interface to handle different scenarios of building {@link SearchFilter}s. Implementations
 * of this interface are used to present the user filter-fields that will be used to create a
 * {@link SearchFilter}.
 * <p>
 * Optionally one can set a {@link SearchResultFetcher} so the filter-ui can trigger a search
 * directly.
 * <p>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface SearchFilterProvider {
	
	/**
	 * Should create and return a GUI-representation of this
	 * SearchFilterProvider as Composite.
	 * 
	 * @param parent The parent Composite to create the filter-provider-ui for.
	 * @return A <b>new</b> Composite that is the parent of all created filter-provider-ui. 
	 */
	public Composite createComposite(Composite parent);
	
	/**
	 * Should return the Composite created in {@link #createComposite(Composite)}.
	 * @return The Composite created in {@link #createComposite(Composite)}.
	 */
	public Composite getComposite();

	/**
	 * Return the {@link SearchFilter} build up by this SearchFilterProvider.
	 * <p>
	 * Note, that usually this method requires to be called on the UI thread as it reads the current
	 * values from the ui to be able to build the filter-items
	 * </p>
	 * 
	 * @return the {@link SearchFilter} build up by this SearchFilterProvider.
	 */
	public SearchFilter getSearchFilter();

	/**
	 * Set the {@link SearchResultFetcher} this filter-provider should use when a search should be
	 * triggered with the filter created by this provider.
	 * <p>
	 * Note, that if this instance of {@link SearchFilterProvider} was already asked to create its
	 * UI, this method has to ensure that this UI uses the given resultFetcher after this method call.
	 * </p>
	 * 
	 * @param resultFetcher The {@link SearchResultFetcher} to set.
	 */
	void setResultFetcher(SearchResultFetcher resultFetcher);

	/**
	 * @return The {@link SearchResultFetcher} this filter-provider currently uses when a search is
	 *         triggered, or <code>null</code> if none is set.
	 */
	SearchResultFetcher getResultFetcher();
}
