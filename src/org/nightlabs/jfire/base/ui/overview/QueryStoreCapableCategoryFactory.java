package org.nightlabs.jfire.base.ui.overview;

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
