package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.base.ui.config.IConfigModuleController;
import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutConfigModuleController;
import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutPreferencePage;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2;

public abstract class AbstractPersonSearchEditLayoutPreferencePage extends AbstractEditLayoutPreferencePage {
	
	protected static class PersonSearchEditLayoutConfigModuleController extends AbstractEditLayoutConfigModuleController {

		public PersonSearchEditLayoutConfigModuleController(AbstractPersonSearchEditLayoutPreferencePage preferencePage) {
			super(preferencePage);
		}

		@Override
		public Class<? extends ConfigModule> getConfigModuleClass() {
			return PersonSearchConfigModule.class;
		}

		@Override
		public String getConfigModuleID() {
			return ((AbstractPersonSearchEditLayoutPreferencePage) getPreferencePage()).getConfigModuleID();
		}
		
		private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();
		
		@Override
		public Set<String> getConfigModuleFetchGroups() {
			if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
				CONFIG_MODULE_FETCH_GROUPS.addAll(super.getConfigModuleFetchGroups());
				CONFIG_MODULE_FETCH_GROUPS.add(PropertySetFieldBasedEditLayoutEntry2.FETCH_GROUP_STRUCT_FIELDS);
				CONFIG_MODULE_FETCH_GROUPS.add(PersonSearchConfigModule.FETCH_GROUP_QUICK_SEARCH_ENTRY);
				CONFIG_MODULE_FETCH_GROUPS.add(StructField.FETCH_GROUP_NAME);
			}
			return CONFIG_MODULE_FETCH_GROUPS;
		}
	}
	
	
	protected AbstractPersonSearchEditLayoutPreferencePage() {
		super();
	}

	@Override
	protected IGridLayoutConfig createConfigModuleGridLayoutConfig() {
		return new PersonSearchGridLayoutConfig(getPersonSearchConfigModule());
	}

	@Override
	protected IConfigModuleController createConfigModuleController() {
		return new PersonSearchEditLayoutConfigModuleController(this);
	}
	
	public PersonSearchConfigModule getPersonSearchConfigModule() {
		return (PersonSearchConfigModule) getConfigModuleController().getConfigModule();
	}
	
	@Override
	protected abstract String getConfigModuleID();
}
