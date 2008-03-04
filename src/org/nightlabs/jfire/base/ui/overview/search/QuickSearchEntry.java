package org.nightlabs.jfire.base.ui.overview.search;

import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * Interface which defines an QuickSearch which can be used
 * e.g. in the Overview to perform a quick search with only a search text
 * in a special context defined by this QuickSearchEntry
 * 
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QuickSearchEntry<R, Q extends AbstractSearchQuery<? extends R>>
{
//	/**
//	 * returns the text to search for
//	 * @return the text to search for
//	 */
//	String getSearchText();
	
	/**
	 * sets the search text
	 * @param searchText the search text to set
	 */
	void setSearchConditionValue(String searchText);
	
	void unsetSearchCondition();
	
//	void resetSearchCondition();
	
	void setQueryProvider(QueryProvider<R, ? super Q> queryProvider);
		
//	/**
//	 * performs the search with the given search text returned
//	 * by {@link #getSearchText()}
//	 * 
//	 * @return the result of the search
//	 */
//	Object search(ProgressMonitor monitor);
	
//	/**
//	 * sets the range for the result
//	 * 
//	 * @param minInclude the minimum range which is included
//	 * @param maxExclude the maximum range which is excluded
//	 */
//	void setResultRange(long minInclude, long maxExclude);
//	
//	/**
//	 * returns the minimum range which is included
//	 * @return the minimum range which is included
//	 */
//	long getMinIncludeRange();
//	
//	/**
//	 * returns the maximum range which is excluded
//	 * @return the maximum range which is excluded
//	 */
//	long getMaxExcludeRange();
	
	/**
	 * return the factory which created this instance
	 * @return the factory which created this instance
	 */
	QuickSearchEntryFactory<R, Q> getFactory();
}
