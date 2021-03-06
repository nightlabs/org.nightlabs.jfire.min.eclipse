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

import org.nightlabs.jdo.query.ui.search.SearchFilterItemList;
import org.nightlabs.jdo.query.ui.search.SearchFilterItemListMutator;
import org.nightlabs.jfire.prop.id.StructLocalID;

/**
 * Small class to change PersonSearchFilterItemLists.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetSearchFilterItemListMutator implements
		SearchFilterItemListMutator {

	private StructLocalID structLocalID;

	/**
	 * Create a new {@link PropertySetSearchFilterItemListMutator}.
	 * 
	 * @param structLocalID The id of the StructLocal to get the list of StructFields from that the
	 *            user will be able to search in.
	 */
	public PropertySetSearchFilterItemListMutator(StructLocalID structLocalID) {
		super();
		this.structLocalID = structLocalID;
	}

	/**
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterItemListMutator#addItemEditor(org.nightlabs.jdo.query.ui.search.SearchFilterItemList)
	 */
	public void addItemEditor(SearchFilterItemList list) {
		list.addItemEditor(new PropertySetSearchFilterItemEditor(structLocalID));
	}

}
