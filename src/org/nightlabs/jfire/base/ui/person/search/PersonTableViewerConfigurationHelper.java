package org.nightlabs.jfire.base.ui.person.search;

import java.util.List;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.prop.DefaultPropertySetTableColumnDescriptor;
import org.nightlabs.jfire.base.ui.prop.DefaultPropertySetTableConfig;
import org.nightlabs.jfire.base.ui.prop.IPropertySetTableConfig;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerConfiguration;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerColumnDescriptor;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * Helper that can be used to create an {@link IPropertySetTableConfig} from a {@link PropertySetTableViewerConfiguration}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PersonTableViewerConfigurationHelper {

	/**
	 * Static helper, not to be instantiated.
	 */
	private PersonTableViewerConfigurationHelper() {
		// Nothing to do
	}
	
	/**
	 * Creates a new {@link IPropertySetTableConfig} based on the given {@link PropertySetTableViewerConfiguration}.
	 * 
	 * @param configuration The configuration to convert.
	 * @return A new {@link IPropertySetTableConfig} based on the given {@link PropertySetTableViewerConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	public static IPropertySetTableConfig createPropertySetTableConfig(PropertySetTableViewerConfiguration configuration) {
		DefaultPropertySetTableConfig tableConfig = new DefaultPropertySetTableConfig();
		for (PropertySetTableViewerColumnDescriptor columnDescriptor : configuration.getColumnDescriptors()) {
			List<StructField> structFields = columnDescriptor.getStructFields();
			List<StructFieldID> structFieldIDs = NLJDOHelper.getObjectIDList(structFields);
			DefaultPropertySetTableColumnDescriptor colDescriptor = tableConfig.addDefaultColumnDescriptor(structFieldIDs
					.toArray(new StructFieldID[structFields.size()]));
			colDescriptor.setColumDataSeparator(columnDescriptor.getColumnDataSeparator());
			colDescriptor.setColumHeaderSeparator(columnDescriptor.getColumnHeaderSeparator());
			colDescriptor.setColumnWeight(columnDescriptor.getColumnWeight());
		}
		tableConfig.setStruct(StructLocalDAO.sharedInstance().getStructLocal(StructLocalID.create(
				Organisation.DEV_ORGANISATION_ID,
				Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE
		), new NullProgressMonitor()));
		return tableConfig;
	}	

}
