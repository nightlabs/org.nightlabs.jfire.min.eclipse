package org.nightlabs.jfire.base.ui.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite;

/**
 * Abstract base class of a composite which returns a {@link AbstractJDOQuery}
 * which can be used for searching
 * 
 * Furthermore it supports an active state
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class JDOQueryComposite<R, Q extends AbstractSearchQuery<R>>
	extends XComposite
{
	public JDOQueryComposite(AbstractQueryFilterComposite<R, Q> filterComposite, int style,
		LayoutMode layoutMode, LayoutDataMode layoutDataMode)
	{
		super(filterComposite, style, layoutMode, layoutDataMode);
	}

	public JDOQueryComposite(AbstractQueryFilterComposite<R, Q> filterComposite, int style)
	{
		super(filterComposite, style);
	}
	
	@SuppressWarnings("unchecked")
	protected AbstractQueryFilterComposite<R, Q> getFilterComposite()
	{
		return (AbstractQueryFilterComposite<R, Q>) super.getParent();
	}
	
//	/**
//	 * return a {@link AbstractJDOQuery} which can be used for search
//	 * @return the {@link AbstractJDOQuery} to use for search
//	 */
//	public abstract AbstractJDOQuery getJDOQuery();
	
	/**
	 * creates the content of the Composite
	 * @param parent the parent Composite
	 */
	protected abstract void createComposite(Composite parent);
	
//	private boolean active = true;
	public boolean isActive() {
		return isEnabled();
	}
	
	/**
	 * 
	 * @return
	 */
	protected Q getQuery()
	{
		return getFilterComposite().getQuery();
	}
	
	public void setActive(boolean active) {
//		this.active = active;
		setEnabled(active);
		if (active)
		{
			resetSearchQueryValues();
		}
		else
		{
			unsetSearchQueryValues();
		}
	}

	protected abstract void resetSearchQueryValues();
	
	protected abstract void unsetSearchQueryValues();
	
}
