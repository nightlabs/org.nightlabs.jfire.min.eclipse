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

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jdo.query.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.query.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite;
import org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemListMutator;
import org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterProvider;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;

/**
 * A {@link PropertySetSearchComposite} that can be used for {@link Person}-searches. As this
 * composite sub-classes {@link PropertySetSearchComposite} its upper part (the
 * search-filter-provider) as well as its lower part (the result-viewer) is configured using a
 * concrete instance of {@link PropertySetSearchEditLayoutConfigModule}. The coordinates to the
 * config-module are set in the constructor of a {@link PropertySetSearchComposite}
 * (configModuleClass and propertySetSearchUseCase), this compoiste pre-defines the
 * config-module-class to {@link PersonSearchConfigModule}, only the use-case is variable.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class PersonSearchComposite extends PropertySetSearchComposite<PropertySetID, Person> {
	/**
	 * Create a new {@link PersonSearchComposite}.
	 * 
	 * @param parent The parent to add the composite to.
	 * @param style The style to apply to the composite.
	 * @param quickSearchText An optional text that will be applied to the defaul search-entry and trigger an early search.
	 * @param propertySetSearchUseCase The use-case for the search (see {@link AbstractEditLayoutConfigModule#getCfModID(String, String)}).
	 */
	public PersonSearchComposite(Composite parent, int style,
			String quickSearchText, String propertySetSearchUseCase) {
		super(parent, style, quickSearchText, PersonSearchConfigModule.class, propertySetSearchUseCase);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Implemented using {@link PropertySetSearchFilterProvider}.
	 * </p>
	 */
	@Override
	protected SearchFilterProvider createStaticSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new PropertySetSearchFilterProvider(resultFetcher, false, getConfigModuleClass(), getPropertySetSearchUseCase(), getSearchText());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Implemented using {@link DynamicPersonSearchFilterProvider}.
	 * </p>
	 */
	@Override
	protected SearchFilterProvider createDynamicSearchFilterProvider(SearchResultFetcher resultFetcher) {
		return new DynamicPersonSearchFilterProvider(new PropertySetSearchFilterItemListMutator(createPersonStructLocalID()), resultFetcher);
	}

	/**
	 * @return the {@link StructLocalID} that will be used to get the list of StructFields that will
	 *         be presented for search in the dynamic filter-provider.
	 */
	protected static StructLocalID createPersonStructLocalID() {
		return StructLocalID.create(Organisation.DEV_ORGANISATION_ID, Person.class, Struct.DEFAULT_SCOPE, StructLocal.DEFAULT_SCOPE);
	}

}
