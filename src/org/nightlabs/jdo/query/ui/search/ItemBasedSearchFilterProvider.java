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
 * Nearly complete implementation of an item-based SearchFilterProvider using the a
 * {@link SearchFilterItemListMutator}. All subclasses have to do to use this Provider is to create
 * the SearchFilter-instance in {@link #createSearchFilter()}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class ItemBasedSearchFilterProvider extends
		AbstractItemBasedSearchFilterProvider {

	public ItemBasedSearchFilterProvider(SearchFilterItemListMutator listMutator) {
		super(listMutator);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Uses {@link ItemBasedSearchFilterProviderComposite}.
	 * </p>
	 */
	@Override
	protected AbstractItemBasedSearchFilterProviderComposite createProviderComposite(
			Composite parent,
			int style
		) {
		return new ItemBasedSearchFilterProviderComposite(
				parent,
				style,
				this
			);
	}

}
