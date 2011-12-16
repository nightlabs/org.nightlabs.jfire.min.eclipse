package org.nightlabs.jfire.base.dashboard.ui.internal.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.clientui.ui.layout.IGridDataEntry;
import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;

/**
 * 
 */

/**
 * @author abieber
 *
 */
public class DashboardGridLayoutConfig<T> implements IGridLayoutConfig {

	private DashboardLayoutConfigModule<T> configModule;
	
	private List<IGridDataEntry> entries;
	private Map<IGridDataEntry, DashboardGadgetLayoutEntry<T>> entryMap;
	
	/**
	 * 
	 */
	public DashboardGridLayoutConfig(DashboardLayoutConfigModule<T> configModule) {
		this.configModule = configModule;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.clientui.ui.layout.IGridLayoutConfig#addGridDataEntry()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IGridDataEntry addGridDataEntry() {
		
		AddDashboardGadgetWizard wiz = new AddDashboardGadgetWizard(configModule);
		WizardDialog dlg = new WizardDialog(RCPUtil.getActiveShell(), wiz);
		if (dlg.open() == Window.OK) {
			DashboardGadgetLayoutEntry<T> layoutEntry = (DashboardGadgetLayoutEntry<T>) wiz.getLayoutEntry();
			configModule.addEditLayoutEntry(layoutEntry);
			createIndex(true);
			return createGridDataEntry(layoutEntry);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.clientui.ui.layout.IGridLayoutConfig#getGridDataEntries()
	 */
	@Override
	public List<IGridDataEntry> getGridDataEntries() {
		createIndex(false);
		return entries;
	}

	private void createIndex(boolean force) {
		if (entryMap == null || entries == null || force) {
			entries = new LinkedList<IGridDataEntry>();
			List<DashboardGadgetLayoutEntry<T>> editLayoutEntries = configModule.getEditLayoutEntries();
			entryMap = new HashMap<IGridDataEntry, DashboardGadgetLayoutEntry<T>>();
			for (DashboardGadgetLayoutEntry<T> entry : editLayoutEntries) {
				IGridDataEntry gridDataEntry = createGridDataEntry(entry);
				entryMap.put(gridDataEntry, entry);
				entries.add(gridDataEntry);
			}
			
		}
	}
	
	private IGridDataEntry createGridDataEntry(final DashboardGadgetLayoutEntry<T> layoutEntry) {
		return new IGridDataEntry() {
			
			@Override
			public String getName() {
				return layoutEntry.getName();
			}
			
			@Override
			public GridData getGridData() {
				return layoutEntry.getGridData();
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.clientui.ui.layout.IGridLayoutConfig#getGridLayout()
	 */
	@Override
	public GridLayout getGridLayout() {
		return configModule.getGridLayout();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.clientui.ui.layout.IGridLayoutConfig#moveEntryDown(org.nightlabs.clientui.ui.layout.IGridDataEntry)
	 */
	@Override
	public boolean moveEntryDown(IGridDataEntry gridDataEntry) {
		createIndex(false);
		DashboardGadgetLayoutEntry<T> gadgetLayoutEntry = entryMap.get(gridDataEntry);
		if (gadgetLayoutEntry != null) {
			try {
				return configModule.moveEditLayoutEntryDown(gadgetLayoutEntry);
			} finally {
				createIndex(true);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.clientui.ui.layout.IGridLayoutConfig#moveEntryUp(org.nightlabs.clientui.ui.layout.IGridDataEntry)
	 */
	@Override
	public boolean moveEntryUp(IGridDataEntry gridDataEntry) {
		createIndex(false);
		DashboardGadgetLayoutEntry<T> gadgetLayoutEntry = entryMap.get(gridDataEntry);
		if (gadgetLayoutEntry != null) {
			try {
				return configModule.moveEditLayoutEntryUp(gadgetLayoutEntry);
			} finally {
				createIndex(true);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.clientui.ui.layout.IGridLayoutConfig#removeGridDataEntry(org.nightlabs.clientui.ui.layout.IGridDataEntry)
	 */
	@Override
	public void removeGridDataEntry(IGridDataEntry gridDataEntry) {
		createIndex(false);
		DashboardGadgetLayoutEntry<T> gadgetLayoutEntry = entryMap.get(gridDataEntry);
		if (gadgetLayoutEntry != null) {
			configModule.removeEditLayoutEntry(gadgetLayoutEntry);
			createIndex(true);
		}
	}

}
