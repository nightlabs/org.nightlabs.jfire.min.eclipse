/**
 * 
 */
package org.nightlabs.jfire.base.ui.layout;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.base.ui.config.AbstractConfigModuleController;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;

/**
 * Base controller-class to use when editing config-modules that define a GridLayout (and optionally
 * more). It constrains the config-module class to subclasses of
 * {@link AbstractEditLayoutConfigModule} and provides default fetch-groups to work with such a
 * config-module.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AbstractEditLayoutConfigModuleController extends
		AbstractConfigModuleController {

	/**
	 * @see AbstractConfigModuleController
	 * @param preferencePage The PreferencePage this controller is for.
	 */
	public AbstractEditLayoutConfigModuleController(AbstractEditLayoutPreferencePage preferencePage) {
		super(preferencePage);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Re-declared to constrain the possible result-classes to subclasses of
	 * {@link AbstractEditLayoutConfigModule} as this controller should only be used for such
	 * config-modules.
	 * </p>
	 */
	@Override
	public abstract Class<? extends AbstractEditLayoutConfigModule> getConfigModuleClass();
	
	private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the fetchgroups needed to edit a {@link AbstractEditLayoutConfigModule}, i.e. the
	 * fetch-groups for the GridLayout and the GridData-entries of such a config-module.
	 * </p>
	 */
	@Override
	public Set<String> getConfigModuleFetchGroups() {
		if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
			CONFIG_MODULE_FETCH_GROUPS.addAll(getCommonConfigModuleFetchGroups());
			CONFIG_MODULE_FETCH_GROUPS.add(AbstractEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT);
			CONFIG_MODULE_FETCH_GROUPS.add(AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES);
			CONFIG_MODULE_FETCH_GROUPS.add(AbstractEditLayoutEntry.FETCH_GROUP_GRID_DATA);
		}
		return CONFIG_MODULE_FETCH_GROUPS;
	}

}
