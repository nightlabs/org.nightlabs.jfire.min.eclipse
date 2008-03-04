package org.nightlabs.jfire.base.ui.overview.search;

import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QueryFilter<R, Q extends AbstractSearchQuery<? extends R>>
{
	void setActive(boolean active);

	void setQueryProvider(QueryProvider<R, Q> queryProvider);
}
