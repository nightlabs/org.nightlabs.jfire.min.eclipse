/**
 * 
 */
package org.nightlabs.jfire.base.ui.overview.search;

import java.util.SortedSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.DefaultQueryProvider;
import org.nightlabs.jdo.query.QueryCollection;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class QueryFilterDialog 
extends ResizableTitleAreaDialog 
{
	private String scope;
	private QueryCollection<? extends AbstractSearchQuery> queryCollection;
//	private Set<AbstractQueryFilterComposite> filterComposites;
	
	/**
	 * @param parentShell
	 */
	public QueryFilterDialog(Shell parentShell, String scope, 
			QueryCollection<? extends AbstractSearchQuery> queryCollection) 
	{
		super(parentShell, null);
		this.scope = scope;
		this.queryCollection = queryCollection;
	}

	@Override
	protected Control createDialogArea(Composite parent) 
	{
		Composite wrapper = new XComposite(parent, SWT.NONE);
		Class<?> targetType = queryCollection.getResultClass();
		SortedSet<QueryFilterFactory> factories = QueryFilterFactoryRegistry.sharedInstance().getQueryFilterCompositesFor(
				scope, targetType);
		if (factories != null) {
			for (QueryFilterFactory factory : factories) {
				AbstractQueryFilterComposite filterComp = factory.createQueryFilter(wrapper, SWT.NONE, 
						LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA, new DefaultQueryProvider(targetType));
//				filterComposites.add(filterComp);
				filterComp.getQueryProvider().loadQueries(queryCollection);
			}
		}
		return wrapper;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();
		getShell().setText("Search Criteria");		
		setTitle("Search Criteria");
		setMessage("Search with the given criteria");
	}
	
}
