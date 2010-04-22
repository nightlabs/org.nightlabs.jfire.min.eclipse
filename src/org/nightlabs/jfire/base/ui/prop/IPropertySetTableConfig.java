package org.nightlabs.jfire.base.ui.prop;

import java.util.List;

import org.nightlabs.jfire.prop.IStruct;

/**
 * Implementations of this interface are used to configure the columns of a {@link PropertySetTable}.
 * 
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IPropertySetTableConfig {
	IStruct getStruct();
	/**
	 * Returns the {@link IPropertySetTableColumnDescriptor}s of this config. The descriptors define
	 * which StructFields should be displayed in which column.
	 * 
	 * @return The list of column-descriptors for a certain {@link PropertySetTable}.
	 */
	List<IPropertySetTableColumnDescriptor> getColumnDescriptors();
}
