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

/**
 * Default implementation of an ItemBased SearchFilterProviderComposite.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class ItemBasedSearchFilterProviderComposite extends
		AbstractItemBasedSearchFilterProviderComposite {

	/**
	 * Create a new {@link ItemBasedSearchFilterProviderComposite}.
	 * 
	 * @param parent The parent Composite to create the new Composite for.
	 * @param style The style for the new Composite.
	 * @param searchFilterProvider The item-based search-filter-provider the composite should use.
	 */
	public ItemBasedSearchFilterProviderComposite(Composite parent, int style,
			AbstractItemBasedSearchFilterProvider searchFilterProvider) {
		super(parent, style, searchFilterProvider);
	}

	/**
	 * @see org.nightlabs.jdo.query.ui.search.AbstractItemBasedSearchFilterProviderComposite#createSearchFilterItemList(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected SearchFilterItemList createSearchFilterItemList(Composite parent,
			int style) {
		return new SearchFilterItemList(parent, style);
	}

}
