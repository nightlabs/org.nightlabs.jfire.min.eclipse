/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutConfigModuleController;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2;

public abstract class PropertySetSearchEditLayoutConfigModuleController extends AbstractEditLayoutConfigModuleController {

	public PropertySetSearchEditLayoutConfigModuleController(AbstractPropertySetSearchPreferencePage preferencePage) {
		super(preferencePage);
	}

	@Override
	public String getConfigModuleID() {
		return ((AbstractPropertySetSearchPreferencePage) getPreferencePage()).getConfigModuleID();
	}
	
	private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();
	
	@Override
	public Set<String> getConfigModuleFetchGroups() {
		if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
			CONFIG_MODULE_FETCH_GROUPS.addAll(super.getConfigModuleFetchGroups());
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetFieldBasedEditLayoutEntry2.FETCH_GROUP_STRUCT_FIELDS);
			CONFIG_MODULE_FETCH_GROUPS.add(StructField.FETCH_GROUP_NAME);
		}
		return CONFIG_MODULE_FETCH_GROUPS;
	}
	
	@Override
	public abstract Class<? extends ConfigModule> getConfigModuleClass();
}