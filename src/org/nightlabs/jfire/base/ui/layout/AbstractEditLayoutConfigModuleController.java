/**
 * 
 */
package org.nightlabs.jfire.base.ui.layout;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.base.ui.config.AbstractConfigModuleController;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule2;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public abstract class AbstractEditLayoutConfigModuleController extends
		AbstractConfigModuleController {

	public AbstractEditLayoutConfigModuleController(AbstractEditLayoutPreferencePage preferencePage) {
		super(preferencePage);
	}

	@Override
	public Class<? extends ConfigModule> getConfigModuleClass() {
		return PropertySetFieldBasedEditLayoutConfigModule2.class;
	}

	private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();

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
