package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QueryFilterFactory<R, Q extends AbstractSearchQuery<R>>
	extends IExecutableExtension, Comparable<QueryFilterFactory<R, Q>>
{
	AbstractQueryFilterComposite<R, Q> createQueryFilter(
		Composite parent, int style,
		LayoutMode layoutMode, LayoutDataMode layoutDataMode,
		QueryProvider<R, ? super Q> queryProvider);
	
	String getSectionTitle();
	
	Class<R> getViewerBaseClass();
}
