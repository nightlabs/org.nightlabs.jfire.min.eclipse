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
import org.nightlabs.jdo.query.DefaultQueryProvider;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class QueryFilterDialog 
extends ResizableTitleAreaDialog 
{
	private String scope;
	private Class<?> targetType;
	
	/**
	 * @param parentShell
	 */
	public QueryFilterDialog(Shell parentShell, String scope, Class<?> targetType) {
		super(parentShell, null);
		this.scope = scope;
		this.targetType = targetType;
	}

	@Override
	protected Control createDialogArea(Composite parent) 
	{
		Composite wrapper = new XComposite(parent, SWT.NONE);
		SortedSet<QueryFilterFactory> factories = QueryFilterFactoryRegistry.sharedInstance().getQueryFilterCompositesFor(
				scope, targetType);
		if (factories != null) {
			for (QueryFilterFactory factory : factories) {
				factory.createQueryFilter(wrapper, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, 
						LayoutDataMode.GRID_DATA, new DefaultQueryProvider(targetType));
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
