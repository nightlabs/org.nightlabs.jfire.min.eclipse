package org.nightlabs.jfire.base.ui.person.search;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.base.ui.config.IConfigModuleController;
import org.nightlabs.jfire.base.ui.person.search.config.PersonSearchGridLayoutConfig;
import org.nightlabs.jfire.base.ui.prop.search.config.AbstractPropertySetSearchPreferencePage;
import org.nightlabs.jfire.base.ui.prop.search.config.PropertySetSearchEditLayoutConfigModuleController;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

public abstract class AbstractPersonSearchEditLayoutPreferencePage extends AbstractPropertySetSearchPreferencePage {

	protected static class PersonSearchEditLayoutConfigModuleController extends PropertySetSearchEditLayoutConfigModuleController {

		public PersonSearchEditLayoutConfigModuleController(AbstractPersonSearchEditLayoutPreferencePage preferencePage) {
			super(preferencePage);
		}

		@Override
		public Class<? extends ConfigModule> getConfigModuleClass() {
			return PersonSearchConfigModule.class;
		}

		private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();

		@Override
		public Set<String> getConfigModuleFetchGroups() {
			if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
				CONFIG_MODULE_FETCH_GROUPS.addAll(super.getConfigModuleFetchGroups());
				CONFIG_MODULE_FETCH_GROUPS.add(PersonSearchConfigModule.FETCH_GROUP_QUICK_SEARCH_ENTRY);
				CONFIG_MODULE_FETCH_GROUPS.add(PropertySetSearchEditLayoutConfigModule.FETCH_GROUP_RESULT_VIEWER_CONFIGURATIONS);
				CONFIG_MODULE_FETCH_GROUPS.add(PropertySetViewerConfiguration.FETCH_GROUP_CONFIG_DATA);
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
}