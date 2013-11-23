/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutConfigModuleController;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutEntry;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

/**
 * ConfigModulePreferencePageController that is used for PreferencePages that define a layout for
 * the property-set search. These pages themselves define the id and class of the ConfigModule
 * therefore this controller delegates to these methods. This controller also defines the
 * fetch-groups necesary to edit a {@link PropertySetSearchEditLayoutConfigModule}, i.e. its
 * GridLayout for the search-fields and the virtual fetch-group for the result-viewer configuration.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
public class PropertySetSearchEditLayoutConfigModuleController extends AbstractEditLayoutConfigModuleController {

	/**
	 * Create a new {@link PropertySetSearchEditLayoutConfigModuleController} for the given preference-page.
	 * 
	 * @param preferencePage The preference-page to create the controller for.
	 */
	public PropertySetSearchEditLayoutConfigModuleController(AbstractPropertySetSearchPreferencePage preferencePage) {
		super(preferencePage);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method declaration only limits the result to config-module classes extending {@link PropertySetEditLayoutConfigModule}
	 * and delegates to the preference-page. 
	 * </p>
	 */
	@Override
	public Class<? extends PropertySetEditLayoutConfigModule> getConfigModuleClass() {
		return getPreferencePage().getConfigModuleClass();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to the preference-page
	 * </p>
	 */
	@Override
	public String getConfigModuleID() {
		return getPreferencePage().getConfigModuleID();
	}

	/**
	 * @return The preference-page casted to {@link AbstractPropertySetSearchPreferencePage}.
	 */
	@Override
	public AbstractPropertySetSearchPreferencePage getPreferencePage() {
		return (AbstractPropertySetSearchPreferencePage) super.getPreferencePage();
	}
	
	private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();

	/**
	 * @return The fetch-groups necessary to edit an {@link PropertySetSearchEditLayoutConfigModule}
	 *         , i.e. its GridLayout for the search-fields and the virtual fetch-group for its
	 *         result-viewer configurations.
	 */
	@Override
	public Set<String> getConfigModuleFetchGroups() {
		if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
			CONFIG_MODULE_FETCH_GROUPS.addAll(super.getConfigModuleFetchGroups());
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetEditLayoutEntry.FETCH_GROUP_STRUCT_FIELDS);
			CONFIG_MODULE_FETCH_GROUPS.add(StructField.FETCH_GROUP_NAME);
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetSearchEditLayoutConfigModule.FETCH_GROUP_QUICK_SEARCH_ENTRY);
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetSearchEditLayoutConfigModule.FETCH_GROUP_RESULT_VIEWER_CONFIGURATIONS);
			CONFIG_MODULE_FETCH_GROUPS.add(PropertySetViewerConfiguration.FETCH_GROUP_CONFIG_DATA);
		}
		return CONFIG_MODULE_FETCH_GROUPS;
	}
}