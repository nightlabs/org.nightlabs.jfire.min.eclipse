/**
 * 
 */
package org.nightlabs.jdo.ui.search;

/**
 * {@link SearchFilterProvider} that accepts a
 * String to do an early search with the user
 * being required to trigger it.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface EarlySearchFilterProvider extends SearchFilterProvider {

	void setEarlySearchText(String earlySearchText);
}
