package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;

/**
 * Factory for {@link IStructFieldSearchFilterItemEditor}s. Instances of this factory are kept in
 * the {@link StructFieldSearchFilterEditorRegistry} and used to create new
 * {@link IStructFieldSearchFilterItemEditor}s.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
public interface IStructFieldSearchFilterItemEditorFactory {

	/**
	 * Create new instance of the {@link IStructFieldSearchFilterItemEditor} this factory creates.
	 * 
	 * @param <T> The type of DataField of the StructField.
	 * @param structFields The set of structFields the editor should create an
	 *            {@link SearchFilterItem} for.
	 * @param matchType The match-type the {@link SearchFilterItem} should use for the search.
	 * @return A <b>new instance</b> of the {@link IStructFieldSearchFilterItemEditor} this factory
	 *         creates.
	 */
	<T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields,
			MatchType matchType);
}
