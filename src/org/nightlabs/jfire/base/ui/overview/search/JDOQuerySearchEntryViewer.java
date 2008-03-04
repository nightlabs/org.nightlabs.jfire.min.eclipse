package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.base.ui.overview.Entry;

/**
 * Base Implementation of a {@link SearchEntryViewer} which is designed
 * to work with an implementation of {@link AbstractQueryFilterComposite} as
 * Composite returned by {@link #createSearchComposite(org.eclipse.swt.widgets.Composite)}
 * and an implementation of {@link AbstractTableComposite} as Composite returned
 * by {@link #createResultComposite(org.eclipse.swt.widgets.Composite)}
 * 
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class JDOQuerySearchEntryViewer<R, Q extends AbstractSearchQuery<R>>
	extends SearchEntryViewer<R, Q>
{
	public JDOQuerySearchEntryViewer(Entry entry) {
		super(entry);
	}

	/**
	 * takes the given result and calls setInput(Object input)
	 * with it
	 */
	@Override
	public void displaySearchResult(Object result)
	{
		if (getListComposite() != null) {
			getListComposite().getTableViewer().setInput(result);
		}
	}

//	@Override
//	public QuickSearchEntryFactory getQuickSearchEntryFactory() {
//		return new AdvancedQuickSearchEntryFactory();
//	}

	@Override
	public Composite createResultComposite(Composite parent) {
		AbstractTableComposite<R> tableComposite = createListComposite(parent);
		addResultTableListeners(tableComposite);
		return tableComposite;
	}

//	@Override
//	public Composite createSearchComposite(Composite parent) {
//		return new Composite(parent, SWT.NONE);
////		return createFilterComposite(parent);
//	}

	/**
	 * creates an {@link AbstractQueryFilterComposite} which is used as search composite
	 * this Method is called by {@link SearchEntryViewer#createSearchComposite(Composite)
	 * 
	 * @param parent the parent Composite
	 * @return the {@link AbstractQueryFilterComposite} which is used as search composite
	 */
//	public abstract AbstractQueryFilterComposite createFilterComposite(Composite parent);
	
	/**
	 * creates an {@link AbstractTableComposite} which is used as result composite
	 * this Method is called by {@link SearchEntryViewer#createResultComposite(Composite)}
	 * 
	 * @param parent the parent Composite
	 * @return the {@link AbstractTableComposite} which is used as result composite
	 */
	public abstract AbstractTableComposite<R> createListComposite(Composite parent);
	
	/**
	 * This method is called by {@link #createSearchComposite(Composite)} with
	 * the table created by {@link #createListComposite(Composite)}.
	 * <p>
	 * This implementation does nothing, but subclass may add listeners (for doubleclick etc.)
	 * to the table here.
	 * </p>
	 * @param tableComposite
	 */
	protected void addResultTableListeners(AbstractTableComposite<R>  tableComposite) {
	}
	
	/**
	 * returns the AbstractTableComposite created by {@link #createListComposite(Composite)}
	 * @return the AbstractTableComposite created by {@link #createListComposite(Composite)}
	 */
	@SuppressWarnings("unchecked")
	public AbstractTableComposite<R> getListComposite() {
		return (AbstractTableComposite<R>) getResultComposite();
	}

//	/**
//	 * returns the {@link AbstractQueryFilterComposite} created by {@link #createFilterComposite(Composite)}
//	 * @return the {@link AbstractQueryFilterComposite} created by {@link #createFilterComposite(Composite)}
//	 */
//	public AbstractQueryFilterComposite getFilterComposite() {
//		return (AbstractQueryFilterComposite) getSearchComposite();
//	}
	
//	/**
//	 * can be overridden by inheritance to optimise their search results
//	 * by default this method does nothing
//	 * 
//	 * @param result the search result to optimise
//	 */
//	protected void optimizeSearchResults(Object result) {
//		
//	}
	
//	@SuppressWarnings("unchecked")
//	/**
//	 * return the result of the given queries
//	 * 
//	 * @param queries a collection of {@link JDOQuery}s
//	 * @param monitor the {@link IProgressMonitor} to display the progress
//	 * @return the result of the queries
//	 */
//	protected abstract Object getQueryResult(Collection<? extends AbstractJDOQuery> queries, ProgressMonitor monitor); // TODO need to add generic type info for JDOQuery - maybe add type info to the SearchEntryViewer already. Should be consequent. 

//	protected class AdvancedQuickSearchEntryFactory
//	extends AbstractQuickSearchEntryFactory
//	{
//		@Override
//		public String getName() {
//			return Messages.getString("org.nightlabs.jfire.base.ui.overview.search.JDOQuerySearchEntryViewer.advancedEntry.name"); //$NON-NLS-1$
//		}
//
//		public QuickSearchEntry createQuickSearchEntry() {
//			return new AdvancedQuickSearchEntryType(this);
//		}
//	}
	
//	/**
//	 * Implementation of an {@link AbstractQuickSearchEntry} which
//	 * takes the queries returned from the {@link AbstractQueryFilterComposite}
//	 * which should be returned by {@link SearchEntryViewer#createSearchComposite(org.eclipse.swt.widgets.Composite)}
//	 * and use these for searching
//	 * 
//	 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
//	 */
//	private class AdvancedQuickSearchEntryType
//	extends AbstractQuickSearchEntry
//	{
//		public AdvancedQuickSearchEntryType(QuickSearchEntryFactory factory) {
//			super(factory);
//		}
//		
//		public Object search(ProgressMonitor monitor)
//		{
//			Display.getDefault().syncExec(new Runnable(){
//				public void run() {
//					getListComposite().getTableViewer().setInput(new String[] {Messages.getString("org.nightlabs.jfire.base.ui.overview.search.JDOQuerySearchEntryViewer.applySearch.listComposite_loading")}); //$NON-NLS-1$
//				}
//			});
//			
//			final List<AbstractJDOQuery<?>> queries = new ArrayList<AbstractJDOQuery<?>>(2);
//			Display.getDefault().syncExec(new Runnable() {
//				public void run() {
//					queries.addAll(getFilterComposite().getJDOQueries());
//				}
//			});
//			
//			for (AbstractJDOQuery<?> query : queries) {
//				query.setFromInclude(getMinIncludeRange());
//				query.setToExclude(getMaxExcludeRange());
//			}
//			
//			final Object result = getQueryResult(queries, monitor);
//			optimizeSearchResults(result);
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
//					getListComposite().getTableViewer().setInput(result);
//				}
//			});
//			
//			return result;
//		}
//	}
		
}
