package org.nightlabs.jfire.base.ui.overview.search;

import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * Abstract base class for {@link QuickSearchEntry}
 * 
 * @param <R> the type of result this entry will yield after its usage.
 * @param <Q> the type of Query needed for me to set my filter properties.
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractQuickSearchEntry<Q extends AbstractSearchQuery>
	implements QuickSearchEntry<Q>
{
	private QuickSearchEntryFactory<Q> factory = null;
	private QueryProvider<? super Q> queryProvider = null;
	protected Class<Q> queryType; 
//	private String lastSearchConditionText = null;
	private long minInclude = 0;
	private long maxExclude = Long.MAX_VALUE;
	
	public AbstractQuickSearchEntry(QuickSearchEntryFactory<Q> factory, Class<Q> queryType) {
		super();
		this.factory = factory;
		
		assert queryType != null;
		this.queryType = queryType;
	}
		
//	public String getSearchText() {
//		return searchText;
//	}
//	public void setSearchConditionValue(String searchText) {
//		this.searchText = searchText;
//	}
				
	public void setResultRange(long minInclude, long maxExclude) {
		this.minInclude = minInclude;
		this.maxExclude = maxExclude;
	}
	
	public long getMinIncludeRange() {
		return minInclude;
	}
	
	public long getMaxExcludeRange() {
		return maxExclude;
	}
	
	public QuickSearchEntryFactory<Q> getFactory() {
		return factory;
	}

	@Override
	public void setQueryProvider(QueryProvider<? super Q> queryProvider)
	{
		this.queryProvider = queryProvider;
	}
	
	protected QueryProvider<? super Q> getQueryProvider()
	{
		return queryProvider;
	}
	
	protected Q getQueryOfType(Class<Q> queryType)
	{
		return getQueryProvider().getQueryOfType(queryType);
	}
	
	@Override
	public void setSearchConditionValue(String searchText)
	{
//		lastSearchConditionText = searchText;
		doSetSearchConditionValue(getQueryOfType(queryType), searchText);
	}
	
	protected abstract void doSetSearchConditionValue(Q query, String value);
	
	@Override
	public void unsetSearchCondition()
	{
		doUnsetSearchConditionValue(getQueryOfType(queryType));
	}
	
	protected abstract void doUnsetSearchConditionValue(Q query);
	
//	@Override
//	public void resetSearchCondition()
//	{
//		doResetSearchCondition(getQueryOfType(queryType), lastSearchConditionText);
//	}
//
//	protected abstract void doResetSearchCondition(Q query, String lastValue);
}
