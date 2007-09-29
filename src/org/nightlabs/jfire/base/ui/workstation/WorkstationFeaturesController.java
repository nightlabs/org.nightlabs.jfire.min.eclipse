/**
 * 
 */
package org.nightlabs.jfire.base.ui.workstation;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.ui.config.AbstractConfigModuleController;
import org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage;
import org.nightlabs.jfire.workstation.WorkstationFeaturesCfMod;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class WorkstationFeaturesController extends
		AbstractConfigModuleController {

	/**
	 * @param preferencePage
	 */
	public WorkstationFeaturesController(
			AbstractConfigModulePreferencePage preferencePage) {
		super(preferencePage);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.AbstractConfigModuleController#getConfigModuleClass()
	 */
	@Implement
	public Class getConfigModuleClass() {
		return WorkstationFeaturesCfMod.class;
	}

	private static final Set<String> fetchGroups = new HashSet<String>();
	
	@Implement
	public Set<String> getConfigModuleFetchGroups() {
		if (fetchGroups.isEmpty()) {
			fetchGroups.addAll(getCommonConfigModuleFetchGroups());
			fetchGroups.add(WorkstationFeaturesCfMod.FETCH_GROUP_THIS_FEATURES);			
		}
		
		return fetchGroups;
	}
}
