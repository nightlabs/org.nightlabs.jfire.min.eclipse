package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.clientui.ui.layout.IGridDataEntry;
import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.config.StructFieldSearchEditLayoutEntry;
import org.nightlabs.progress.ProgressMonitor;

/**
 * {@link IGridLayoutConfig} operating on a {@link PersonSearchConfigModule}.
 */
public class PersonSearchGridLayoutConfig implements IGridLayoutConfig {
	
	private PersonSearchConfigModule cfMod;

	private List<IGridDataEntry> entries;
	private Map<IGridDataEntry,StructFieldSearchEditLayoutEntry> entriesMap;
	private Map<StructFieldSearchEditLayoutEntry, IGridDataEntry> createdEntriesMap;
	
	private Job loadStructLocalJob;
	private StructLocal structLocal;
	
	public PersonSearchGridLayoutConfig(PersonSearchConfigModule cfMod) {
		this.cfMod = cfMod;
		
		loadStructLocalJob = new Job("Loading StructLocal") {

			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				StructLocalID structLocalID = StructLocalID.create(Organisation.DEV_ORGANISATION_ID, Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE);
				structLocal = StructLocalDAO.sharedInstance().getStructLocal(structLocalID, monitor);
				return Status.OK_STATUS;
			}
		};
		loadStructLocalJob.schedule();
	}

	@Override
	public IGridDataEntry addGridDataEntry() {
		try {
			loadStructLocalJob.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		Map<StructField, String> ignoreFields = new HashMap<StructField, String>();
		for (IGridDataEntry gdEntry : getGridDataEntries()) {
			StructFieldSearchEditLayoutEntry entry = entriesMap.get(gdEntry);
			if (entry != null && ( PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_STRUCT_FIELD_REFERENCE.equals(entry.getEntryType())
				                     || PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_MULTI_STRUCT_FIELD_REFERENCE.equals(entry.getEntryType()) ))
			{
				for (StructField field : entry.getStructFields()) {
					ignoreFields.put(field, "This field cannot be added because it has already been assigned.");
				}
			}
		}
		
//		for (StructField<?> structField : structLocal.getStructFields()) {
//			if (!StructFieldSearchFilterEditorRegistry.sharedInstance().hasEditor(structField.getClass())) {
//				ignoreFields.put(structField, "Fields of this type cannot be added because no editor is available.");
//			}
//		}

//		AddStructFieldEntryDialog dlg = new AddStructFieldEntryDialog(RCPUtil.getActiveShell(), null, ignoreIDs, structLocal);
		AddStructFieldSearchItemEntryDialog dlg = new AddStructFieldSearchItemEntryDialog(RCPUtil.getActiveShell(), null, ignoreFields, structLocal);
		if (dlg.open() != Window.OK) {
			return null;
		}
		StructFieldSearchEditLayoutEntry layoutEntry = getConfigModule().createEditLayoutEntry(dlg.getEntryType());
		layoutEntry.setGridData(new GridData(IDGenerator.nextID(GridData.class)));
		layoutEntry.setStructFields(dlg.getStructFields());
		layoutEntry.setMatchType(dlg.getMatchType());
		
		getConfigModule().addEditLayoutEntry(layoutEntry);
		IGridDataEntry gdEntry = createGridDataEntry(layoutEntry);
		clearCache();
		return gdEntry;
	}
	
	@Override
	public List<IGridDataEntry> getGridDataEntries() {
		if (entries == null) {
			entries = new LinkedList<IGridDataEntry>();
			if (entriesMap == null) {
				entriesMap = new HashMap<IGridDataEntry, StructFieldSearchEditLayoutEntry>();
				createdEntriesMap = new HashMap<StructFieldSearchEditLayoutEntry, IGridDataEntry>();
			}
			
			for (final StructFieldSearchEditLayoutEntry entry : getConfigModule().getStructFieldSearchEditLayoutEntries()) {
				IGridDataEntry gdEntry = createdEntriesMap.get(entry);
				if (gdEntry == null) {
					gdEntry = createGridDataEntry(entry);
				}
				entries.add(gdEntry);
			}
		}
		return entries;
	}
	
	private IGridDataEntry createGridDataEntry(final StructFieldSearchEditLayoutEntry entry) {
		IGridDataEntry gdEntry = new IGridDataEntry() {
			@Override
			public GridData getGridData() {
				return entry.getGridData();
			}

			@Override
			public String getName() {
				return getGridDataEntryName(entry);
			}
		};
		
		createdEntriesMap.put(entry, gdEntry);
		entriesMap.put(gdEntry, entry);

		return gdEntry;
	}
	
	protected String getGridDataEntryName(StructFieldSearchEditLayoutEntry entry) {
		if (entry.getEntryType().equals(EditLayoutEntry.ENTRY_TYPE_SEPARATOR)) {
			return "Separator"; //$NON-NLS-1$
		}
		try {
			loadStructLocalJob.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		String name = "";
		for (StructField field : entry.getStructFields()) {
			name += field.getName().getText() + ", ";
		}
		
		name = name.substring(0, name.length()-2) + " [" + entry.getMatchType().getLocalisedName() + "]";
		
		return name;
	}
	
	@Override
	public GridLayout getGridLayout() {
		return getConfigModule().getGridLayout();
	}

	@Override
	public boolean moveEntryDown(IGridDataEntry gridDataEntry) {
		if (entriesMap == null)
			return false;
		StructFieldSearchEditLayoutEntry entry = entriesMap.get(gridDataEntry);
		clearCache();
		if (entry == null)
			return false;
		return getConfigModule().moveEditLayoutEntryDown(entry);
	}

	@Override
	public boolean moveEntryUp(IGridDataEntry gridDataEntry) {
		if (entriesMap == null)
			return false;
		StructFieldSearchEditLayoutEntry entry = entriesMap.get(gridDataEntry);
		clearCache();
		if (entry == null)
			return false;
		return getConfigModule().moveEditLayoutEntryUp(entry);
	}

	@Override
	public void removeGridDataEntry(IGridDataEntry gridDataEntry) {
		if (entriesMap == null)
			return;
		StructFieldSearchEditLayoutEntry entry = entriesMap.get(gridDataEntry);
		clearCache();
		if (entry == null)
			return;
		getConfigModule().removeEditLayoutEntry(entry);
	}

	protected void clearCache() {
		entries = null;
	}
	
	public PersonSearchConfigModule getConfigModule() {
		return cfMod;
	}
	
	public StructFieldSearchEditLayoutEntry getSearchEntryForGridDataEntry(IGridDataEntry entry) {
		return entriesMap.get(entry);
	}
}