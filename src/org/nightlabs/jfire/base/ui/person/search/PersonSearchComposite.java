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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jdo.query.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.query.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.ui.prop.PropertySetSearchComposite;
import org.nightlabs.jfire.base.ui.prop.PropertySetTable;
import org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemListMutator;
import org.nightlabs.jfire.person.Person;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class PersonSearchComposite extends PropertySetSearchComposite<Person> {
	/**
	 * See {@link PropertySetSearchComposite#PropertySetSearchComposite(Composite, int, String, String)}
	 */
	public PersonSearchComposite(Composite parent, int style,
			String quickSearchText, String useCase) {
		super(parent, style, quickSearchText, useCase);
	}

	/**
	 * See {@link PropertySetSearchComposite#PropertySetSearchComposite(Composite, int, String, boolean, String)}
	 */
	public PersonSearchComposite(Composite parent, int style,
			String quickSearchText, boolean doIDSearchAndUsePropertySetCache, String useCase) {
		super(parent, style, quickSearchText, doIDSearchAndUsePropertySetCache, useCase);
	}

	@Override
	protected PropertySetTable<Person> createResultTable(Composite parent) {
		return new PersonResultTable(parent, SWT.NONE);
	}

	@Override
	protected SearchFilterProvider createStaticSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new StaticPersonSearchFilterProvider(resultFetcher, false);
	}

	@Override
	protected SearchFilterProvider createDynamicSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new DynamicPersonSearchFilterProvider(new PropertySetSearchFilterItemListMutator(), resultFetcher);
	}

}
