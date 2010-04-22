package org.nightlabs.jfire.base.ui.prop;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Interface used to configure the displayed StructFields of a column in a {@link PropertySetTable}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IPropertySetTableColumnDescriptor {
	
	/**
	 * @return The String used to separate the names of the different StructFields in the header of
	 *         this column when multiple StructFields are displayed.
	 */
	String getColumHeaderSeparator();

	/**
	 * @return The String used to separate the values of each row in this column when the data of
	 *         multiple StructFields are displayed.
	 */
	String getColumDataSeparator();
	
	/**
	 * Create a {@link ColumnLayoutData} that will be used for this column.
	 * @return A <b>new</b> {@link ColumnLayoutData} for this column.
	 */
	ColumnLayoutData createColumnLayoutData();

	/**
	 * @return The list of StructFields of which the data of the PropertySet should be displayed in
	 *         each row within this column.
	 */
	List<StructFieldID> getColumnStructFieldIDs();
}
