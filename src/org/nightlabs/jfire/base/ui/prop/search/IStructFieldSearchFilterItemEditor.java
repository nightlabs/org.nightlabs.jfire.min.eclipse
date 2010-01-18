package org.nightlabs.jfire.base.ui.prop.search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.query.ui.search.ISearchFilterItemEditor;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.AbstractStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;

/**
 * Interfaces for editors for {@link IStructFieldSearchFilterItem}s. Such editors must provide
 * UI to acquire the information to be sought after and return a {@link IStructFieldSearchFilterItem}
 * containing this information thereafter.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public interface IStructFieldSearchFilterItemEditor
extends ISearchFilterItemEditor
{
	/**
	 * Returns the {@link AbstractStructFieldSearchFilterItem} instance containing the information
	 * acquired through the {@link Control} created in {@link #createControl(Composite, boolean)}.
	 * 
	 * @return the {@link AbstractStructFieldSearchFilterItem} containing the information of this editor.
	 */
	public IStructFieldSearchFilterItem getSearchFilterItem();
	
	/**
	 * Returns whether the user has entered data into this editor that constraints the search results.
	 */
	public boolean hasSearchConstraint();
	
	/**
	 * Returns whether this search filter item editor is able to search multiple {@link StructField}s
	 * <b>of the same type</b> in one go.<p>
	 * As an example, it might make sense to have an editor whose value is used to search within
	 * surname, lastname and company.
	 * 
	 * @return whether this search filter item editor is able to search multiple {@link StructField}s
	 * <b>of the same type</b> in one go.<p>
	 */
	public boolean canHandleMultipleFields();
}