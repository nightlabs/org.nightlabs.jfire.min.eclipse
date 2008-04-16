package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QueryFilterFactory<Q extends AbstractSearchQuery>
	extends IExecutableExtension, Comparable<QueryFilterFactory<Q>>
{
	/**
	 * Creates the filter composite with all the given parameters.
	 *  
	 * @param parent the parent composite to create the filter into.
	 * @param style the style to use.
	 * @param layoutMode the LayoutMode to use (see {@link XComposite.LayoutMode}).
	 * @param layoutDataMode the LayoutMode to use (see {@link XComposite.LayoutDataMode}).
	 * @param queryProvider the QueryProvider from which the filter shall retrieve the query and set/get
	 * 	its filter properties to/from.
	 * @return the filter composite that shall be displayed in the viewer for the registered
	 * 	{@link #getViewerBaseClass()}.
	 */
	AbstractQueryFilterComposite<Q> createQueryFilter(
		Composite parent, int style,
		LayoutMode layoutMode, LayoutDataMode layoutDataMode,
		QueryProvider<? super Q> queryProvider);
	
	/**
	 * Returns the title of the section the filter will be instantiated into.
	 * @return the title of the section the filter will be instantiated into.
	 */
	String getSectionTitle();
	
	/**
	 * Returns the base class of the viewer, e.g. for the DeliveryNoteEntryViewer it is DeliveryNote.class.
	 * The viewer will ask the registry to return all factories that are registered for his base class.
	 * @return the base class of the viewer.
	 */
	Class<?> getViewerBaseClass();
}
