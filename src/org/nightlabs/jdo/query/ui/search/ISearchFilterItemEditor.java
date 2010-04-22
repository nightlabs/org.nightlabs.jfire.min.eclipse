package org.nightlabs.jdo.query.ui.search;

import java.util.EnumSet;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.ISearchFilterItem;
import org.nightlabs.jdo.search.MatchType;

/**
 * Interfaces for editors for {@link ISearchFilterItem}s. Such editors must provide
 * UI to acquire the information to be sought after and return a {@link ISearchFilterItem}
 * containing this information thereafter.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public interface ISearchFilterItemEditor
{
	/**
	 * Returns the UI control of this editor able to acquire the information
	 * for the search filter item returned in {@link #getSearchFilterItem()}.
	 * 
	 * @param parent The parent of the composite to be created.
	 * @param showTitle Whether this editor should show a title-text.
	 * @return the UI control of this editor.
	 */
	public Control createControl(Composite parent, boolean showTitle);
	
	/**
	 * Returns the {@link ISearchFilterItem} instance containing the information
	 * acquired through the {@link Control} created in {@link #createControl(Composite, boolean)}.
	 * 
	 * @return the {@link ISearchFilterItem} containing the information of this editor.
	 */
	public ISearchFilterItem getSearchFilterItem();

	/**
	 * Returns an {@link EnumSet} containing all {@link MatchType}s that are supported by the
	 * {@link ISearchFilterItem} returned in {@link #getSearchFilterItem()}.
	 * 
	 * @return All {@link MatchType}s that are supported by the {@link ISearchFilterItem} returned
	 *         in {@link #getSearchFilterItem()}
	 */
	public EnumSet<MatchType> getSupportedMatchTypes();
}
