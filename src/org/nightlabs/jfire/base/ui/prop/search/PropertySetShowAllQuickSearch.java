package org.nightlabs.jfire.base.ui.prop.search;

import org.nightlabs.jdo.query.ui.search.SearchResultFetcher;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.search.TextStructFieldSearchFilterItem;

public abstract class PropertySetShowAllQuickSearch extends PropertySetQuickSearch {

	public PropertySetShowAllQuickSearch(SearchResultFetcher resultFetcher) {
		super("*", resultFetcher); //$NON-NLS-1$
	}
	
	@Override
	public SearchFilter getSearchFilter() {
		SearchFilter filter = getSearchFilter(false);
		filter.addSearchFilterItem(new TextStructFieldSearchFilterItem(PersonStruct.PERSONALDATA_NAME, MatchType.NOTEQUALS, "")); //$NON-NLS-1$
		return filter;
	}
}