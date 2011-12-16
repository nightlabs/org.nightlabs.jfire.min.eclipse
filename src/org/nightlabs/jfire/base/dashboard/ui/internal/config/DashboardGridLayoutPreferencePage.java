/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.config;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.config.AbstractConfigModuleController;
import org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage;
import org.nightlabs.jfire.base.ui.config.IConfigModuleController;
import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutPreferencePage;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;

/**
 * @author abieber
 *
 */
public class DashboardGridLayoutPreferencePage extends
		AbstractEditLayoutPreferencePage {

	private static class Controller extends AbstractConfigModuleController {
		
		private static final Set<String> CONFIG_MODULE_FETCH_GROUPS = new HashSet<String>();
		
		public Controller(AbstractConfigModulePreferencePage preferencePage) {
			super(preferencePage);
		}

		@Override
		public Class<? extends ConfigModule> getConfigModuleClass() {
			return DashboardLayoutConfigModule.class;
		}

		@Override
		public Set<String> getConfigModuleFetchGroups() {
			if (CONFIG_MODULE_FETCH_GROUPS.isEmpty()) {
				CONFIG_MODULE_FETCH_GROUPS.addAll(super.getCommonConfigModuleFetchGroups());
				CONFIG_MODULE_FETCH_GROUPS.add(AbstractEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT);
				CONFIG_MODULE_FETCH_GROUPS.add(AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES);
				CONFIG_MODULE_FETCH_GROUPS.add(AbstractEditLayoutEntry.FETCH_GROUP_GRID_DATA);
			}
			return CONFIG_MODULE_FETCH_GROUPS;
		}
	}
	
	/**
	 * 
	 */
	public DashboardGridLayoutPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param useTabFolder
	 */
	public DashboardGridLayoutPreferencePage(boolean useTabFolder) {
		super(useTabFolder);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutPreferencePage#createConfigModuleGridLayoutConfig()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected IGridLayoutConfig createConfigModuleGridLayoutConfig() {
		return new DashboardGridLayoutConfig((DashboardLayoutConfigModule) getConfigModuleController().getConfigModule());
	}

	@Override
	public Controller getConfigModuleController() {
		return (Controller) super.getConfigModuleController();
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutPreferencePage#getUseCaseDescription()
	 */
	@Override
	public String getUseCaseDescription() {
		return Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.config.DashboardGridLayoutPreferencePage.useCaseDescription"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage#createConfigModuleController()
	 */
	@Override
	protected IConfigModuleController createConfigModuleController() {
		return new Controller(this);
	}

}
