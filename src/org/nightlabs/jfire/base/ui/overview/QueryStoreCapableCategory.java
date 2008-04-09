package org.nightlabs.jfire.base.ui.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryStoreCapableCategory
	extends DefaultCategory
{
	private QueryStoreCapableCategoryComposite categoryComposite;
	
	public QueryStoreCapableCategory(CategoryFactory categoryFactory)
	{
		super(categoryFactory);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.Category#createComposite(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Composite createComposite(Composite composite)
	{
		categoryComposite = new QueryStoreCapableCategoryComposite(composite, SWT.NONE, this); 
		return categoryComposite;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.Category#getComposite()
	 */
	@Override
	public Composite getComposite()
	{
		return categoryComposite;
	}

	@Override
	protected void updateCategoryComposite()
	{
		if (categoryComposite == null)
			return;
		
		if (Display.getCurrent() == null)
		{
			categoryComposite.getDisplay().asyncExec(new Runnable ()
			{
				@Override
				public void run()
				{
					doUpdateComposite();
				}
			});
		}
		else
		{
			doUpdateComposite();
		}
	}
	
	protected void doUpdateComposite()
	{
		categoryComposite.setInput(getEntries());
	}
}
