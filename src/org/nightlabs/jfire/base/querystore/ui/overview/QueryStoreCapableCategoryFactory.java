package org.nightlabs.jfire.base.querystore.ui.overview;

import org.nightlabs.jfire.base.ui.overview.Category;
import org.nightlabs.jfire.base.ui.overview.DefaultCategoryFactory;

/**
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryStoreCapableCategoryFactory
	extends DefaultCategoryFactory
{
	@Override
	public Category createCategory()
	{
		return new QueryStoreCapableCategory(this);
	}
}
