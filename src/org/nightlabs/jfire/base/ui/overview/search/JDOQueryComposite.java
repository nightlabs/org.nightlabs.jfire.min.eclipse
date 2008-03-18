package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryEvent;

/**
 * Abstract base class of a composite which returns a {@link AbstractJDOQuery}
 * which can be used for searching
 * 
 * Furthermore it supports an active state
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
@Deprecated
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
	
	/**
	 * creates the content of the Composite
	 * @param parent the parent Composite
	 */
	protected abstract void createComposite(Composite parent);
	
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
	
	protected abstract void resetSearchQueryValues(Q query);
	
	protected abstract void unsetSearchQueryValues(Q query);
	
	protected boolean isUpdatingUI()
	{
		return getFilterComposite().isUpdatingUI();
	}
	
	protected abstract void updateUI(QueryEvent event);
	
	protected boolean uIChangedQuery()
	{
		return getFilterComposite().uIChangedQuery();
	}
	
	protected void setUIChangedQuery(boolean uiDidIt)
	{
		getFilterComposite().setUIChangedQuery(uiDidIt);
	}
	
	/**
	 * Delegates to the surrounding {@link AbstractQueryFilterComposite} and its
	 * {@link ActiveStateManager}.
	 * 
	 * @param active whether one of the filters contained in has gone <code>active</code>.
	 */
	protected void setSearchSectionActive(boolean active)
	{
		getFilterComposite().getSectionButtonActiveStateManager().setActive(active);
	}
	
	/**
	 * Returns a boolean whether the whole query of the given event changed.
	 *  
	 * @param event the event to check.
	 * @return a boolean whether the whole query of the given event changed.
	 */
	protected final boolean isWholeQueryChanged(QueryEvent event)
	{
		return AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(event.getPropertyName());
	}
}
