package org.nightlabs.jfire.base.ui.overview.search;

import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * Abstract base class for {@link QuickSearchEntry}. This class enforces the used query in the
 * backend to have all of its members defined in a static inner class called "FieldName". <br>
 * Then only implement {@link #getModifiedQueryFieldName()} to return the corresponding field name
 * that is changed by this QuickSearchEntry.
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
	private long minInclude = 0;
	private long maxExclude = Long.MAX_VALUE;

	public AbstractQuickSearchEntry(QuickSearchEntryFactory<Q> factory, Class<Q> queryType) {
		super();
		this.factory = factory;

		assert queryType != null;
		this.queryType = queryType;
	}

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

	protected abstract String getModifiedQueryFieldName();

	@Override
	public void setSearchConditionValue(String searchText)
	{
		final Q query = getQueryOfType(queryType);
//		query.setFieldEnabled(getModifiedQueryFieldName(), true);
		query.setFieldEnabled(getModifiedQueryFieldName(), !searchText.isEmpty());
		doSetSearchConditionValue(query, searchText);
	}

	protected void doSetSearchConditionValue(Q query, String value)
	{
		query.setFieldValue(getModifiedQueryFieldName(), value);
	}

	@Override
	public void unsetSearchCondition()
	{
		final Q query = getQueryOfType(queryType);
		query.setFieldEnabled(getModifiedQueryFieldName(), false);
		doUnsetSearchConditionValue(getQueryOfType(queryType));
	}

	protected void doUnsetSearchConditionValue(Q query)
	{
		query.setFieldValue(getModifiedQueryFieldName(), null);
	}

}
