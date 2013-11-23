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
	
	/**
	 * Returns the data that the user entered in a string representation. Every editor must be able
	 * to restore the user input when the return value of this method is fed into {@link #setInput(String)}.
	 * 
	 * @return the data that the user entered in a string representation.
	 */
	public String getInput();
	
	/**
	 * Loads the user input given as string obtained by {@link #getInput()}.
	 * 
	 * @param input The input to load.
	 */
	public void setInput(String input);
	
	/**
	 * Adds a listener that is triggered when the search is triggered from this editor i.e. by pressing the enter key.
	 * 
	 * @param listener The listener to be added.
	 */
	public void addSearchTriggerListener(ISearchTriggerListener listener);
	
	/**
	 * Removes the given listener.
	 * 
	 * @param listener The listener to be removed.
	 */
	public void removeSearchTriggerListener(ISearchTriggerListener listener);
}