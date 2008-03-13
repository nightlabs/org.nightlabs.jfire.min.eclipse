package org.nightlabs.jfire.base.ui.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryEvent;
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
	/**
	 * The logger used in this class.
	 */
//	private static final Logger logger = Logger.getLogger(JDOQueryComposite.class);
	
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
			resetSearchQueryValues(getQuery());
		}
		else
		{
			unsetSearchQueryValues(getQuery());
		}
	}

	protected abstract void resetSearchQueryValues(Q query);
	
	protected abstract void unsetSearchQueryValues(Q query);
	
	private boolean updatingUI = false;
	protected boolean isUpdatingUI()
	{
		return updatingUI;
	}
	
	public void updateUI(QueryEvent event)
	{
		updatingUI = true;
		doUpdateUI(event);
		updatingUI = false;
	}
	
	protected abstract void doUpdateUI(QueryEvent event);
	
	/**
	 * Computes two flags indicating whether the new Query is <code>null</code> (first one) and 
	 * whether the whole query changed (second one). This can be used in {@link #doUpdateUI(QueryEvent)}
	 * to handle the distinguishing of the possible cases more easily.
	 *  
	 * @param event the event to check.
	 * @return boolean[2]; [0] = new query is <code>null</code>, [1] = whole query changed.
	 */
	protected final boolean isWholeQueryChanged(QueryEvent event)
	{
		return AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(event.getPropertyName());
//		boolean allChanged = false;
//		boolean newQueryIsNull = false;
//		
//		if (AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(event.getPropertyName()))
//		{
//			allChanged = true;
//		}
//		if (event.getChangedQuery() == null)
//		{
//			if (allChanged == false)
//			{
//				logger.warn("Received an event that set the changedQuery == null, but no 'Everything changed' flag is set!", new Exception());
//				allChanged = true;
//			}
//				
//			newQueryIsNull = true;
//		}
//
//		return new boolean[] { allChanged, newQueryIsNull };
	}
}
