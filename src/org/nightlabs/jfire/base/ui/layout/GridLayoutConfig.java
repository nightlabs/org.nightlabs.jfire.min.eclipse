package org.nightlabs.jfire.base.ui.layout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.clientui.ui.layout.IGridDataEntry;
import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;

/**
 * {@link IGridLayoutConfig} operating on a {@link AbstractEditLayoutConfigModule}
 */
public abstract class GridLayoutConfig implements IGridLayoutConfig {

	private AbstractEditLayoutConfigModule<?, AbstractEditLayoutEntry<?>> cfMod;
	
	private List<IGridDataEntry> entries;
	private Map<IGridDataEntry,AbstractEditLayoutEntry<?>> entriesMap;
	private Map<AbstractEditLayoutEntry<?>,IGridDataEntry> createdEntriesMap;

	@SuppressWarnings("unchecked")
	public GridLayoutConfig(AbstractEditLayoutConfigModule cfMod) {
		this.cfMod = cfMod;
	}

	@Override
	public IGridDataEntry addGridDataEntry() {
//		try {
//			loadStructLocalJob.join();
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//
//		Set<StructFieldID> ignoreIDs = new HashSet<StructFieldID>();
//		for (IGridDataEntry gdEntry : getGridDataEntries()) {
//			AbstractEditLayoutEntry<O> entry = entriesMap.get(gdEntry);
//			if (entry != null && AbstractEditLayoutEntry.ENTRY_TYPE_STRUCT_FIELD_REFERENCE.equals(entry.getEntryType())) {
//				if (entry.getStructFieldID() != null)
//					ignoreIDs.add(entry.getStructFieldID());
//			}
//		}
//
//		AddStructFieldEntryDialog dlg = new AddStructFieldEntryDialog(getShell(), null, ignoreIDs, structLocal);
//		if (dlg.open() != Window.OK) {
//			return null;
//		}
//		AbstractEditLayoutEntry<O> layoutEntry = cfMod.createEditLayoutEntry(dlg.getEntryType());
		
		AbstractEditLayoutEntry<?> layoutEntry = addEntry();
		
		layoutEntry.setGridData(new GridData(IDGenerator.nextID(GridData.class)));
		
		cfMod.addEditLayoutEntry(layoutEntry);
		IGridDataEntry gdEntry = createGridDataEntry(layoutEntry);
		clearCache();
		return gdEntry;
	}
	
	protected abstract AbstractEditLayoutEntry<?> addEntry();
	
	protected abstract IGridDataEntry createGridDataEntry(final AbstractEditLayoutEntry<?> entry);
	
	protected List<IGridDataEntry> createGridDataEntries() {
		for (final AbstractEditLayoutEntry<?> entry : cfMod.getEditLayoutEntries()) {
			IGridDataEntry gdEntry = createdEntriesMap.get(entry);
			if (gdEntry == null) {
				gdEntry = createGridDataEntry(entry);
				
				createdEntriesMap.put(entry, gdEntry);
				entriesMap.put(gdEntry, entry);
			}
			entries.add(gdEntry);
		}
		
		return entries;
	}

//	private IGridDataEntry createGridDataEntry(final AbstractEditLayoutEntry<O> entry) {
//		IGridDataEntry gdEntry = new IGridDataEntry() {
//			@Override
//			public GridData getGridData() {
//				return entry.getGridData();
//			}
//
//			@Override
//			public I18nText getName() {
//				if (entry.getStructFieldID() == null) {
//					return new StaticI18nText("Separator"); //$NON-NLS-1$
//				}
//				try {
//					loadStructLocalJob.join();
//				} catch (InterruptedException e) {
//					throw new RuntimeException(e);
//				}
//				StructField<?> field = null;
//				try {
//					field = structLocal.getStructField(entry.getStructFieldID());
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//				return field.getName();
//			}
//		};
//
//		return gdEntry;
//	}

	@Override
	public List<IGridDataEntry> getGridDataEntries() {
		if (entries == null) {
			entries = new LinkedList<IGridDataEntry>();
			if (entriesMap == null) {
				entriesMap = new HashMap<IGridDataEntry, AbstractEditLayoutEntry<?>>();
				createdEntriesMap = new HashMap<AbstractEditLayoutEntry<?>, IGridDataEntry>();
			}

			
		}
		return entries;
	}

	@Override
	public GridLayout getGridLayout() {
		return cfMod.getGridLayout();
	}

	@Override
	public boolean moveEntryDown(IGridDataEntry gridDataEntry) {
		if (entriesMap == null)
			return false;
		AbstractEditLayoutEntry<?> entry = entriesMap.get(gridDataEntry);
		clearCache();
		if (entry == null)
			return false;
		return cfMod.moveEditLayoutEntryDown(entry);
	}

	@Override
	public boolean moveEntryUp(IGridDataEntry gridDataEntry) {
		if (entriesMap == null)
			return false;
		AbstractEditLayoutEntry<?> entry = entriesMap.get(gridDataEntry);
		clearCache();
		if (entry == null)
			return false;
		return cfMod.moveEditLayoutEntryUp(entry);
	}

	@Override
	public void removeGridDataEntry(IGridDataEntry gridDataEntry) {
		if (entriesMap == null)
			return;
		AbstractEditLayoutEntry<?> entry = entriesMap.get(gridDataEntry);
		clearCache();
		if (entry == null)
			return;
		cfMod.removeEditLayoutEntry(entry);
	}

	protected void clearCache() {
		entries = null;
	}
	
	public AbstractEditLayoutConfigModule<?, AbstractEditLayoutEntry<?>> getConfigModule() {
		return cfMod;
	}
}