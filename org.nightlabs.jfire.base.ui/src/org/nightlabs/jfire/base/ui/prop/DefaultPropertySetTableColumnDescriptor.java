package org.nightlabs.jfire.base.ui.prop;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DefaultPropertySetTableColumnDescriptor implements IPropertySetTableColumnDescriptor {

	private String columDataSeparator = ", "; //$NON-NLS-1$
	private String columHeaderSeparator = ", "; //$NON-NLS-1$
	private List<StructFieldID> structFieldIDs = new LinkedList<StructFieldID>();
	private int columnWeight = 1;

	/**
	 * Create a new {@link DefaultPropertySetTableColumnDescriptor} for the given StructFieldIDs.
	 * 
	 * @param structFieldIDs The IDs of the StructFields of which the data should be displayed in
	 *            this column.
	 */
	public DefaultPropertySetTableColumnDescriptor(StructFieldID... structFieldIDs) {
		for (StructFieldID structFieldID : structFieldIDs) {
			addStructFieldID(structFieldID);
		}
	}
	
	/**
	 * Create a new {@link DefaultPropertySetTableColumnDescriptor} for the given StructFieldIDs.
	 * 
	 * @param columnHeaderSeparator The separator for the column header.
	 * @param columnDataSeparator The separator for the column data.
	 * @param structFieldIDs The IDs of the StructFields of which the data should be displayed in
	 *            this column.
	 */
	public DefaultPropertySetTableColumnDescriptor(String columnHeaderSeparator, String columnDataSeparator,
			StructFieldID... structFieldIDs) {
		this(structFieldIDs);
		this.columDataSeparator = columnDataSeparator;
		this.columHeaderSeparator = columnHeaderSeparator;
	}
	

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.IPropertySetTableColumnDescriptor#createColumnLayoutData()
	 */
	@Override
	public ColumnLayoutData createColumnLayoutData() {
		return new ColumnWeightData(columnWeight);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.IPropertySetTableColumnDescriptor#getColumDataSeparator()
	 */
	@Override
	public String getColumDataSeparator() {
		return columDataSeparator;
	}
	
	/**
	 * @param columDataSeparator The separator used for the column data when multiple StructFields are displayed.
	 * @return this.
	 */
	public DefaultPropertySetTableColumnDescriptor setColumDataSeparator(String columDataSeparator) {
		this.columDataSeparator = columDataSeparator;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.IPropertySetTableColumnDescriptor#getColumHeaderSeparator()
	 */
	@Override
	public String getColumHeaderSeparator() {
		return columHeaderSeparator;
	}
	
	/**
	 * @param columHeaderSeparator The separator used for the column header when multiple StructFields are displayed.
	 * @return this.
	 */
	public DefaultPropertySetTableColumnDescriptor setColumHeaderSeparator(String columHeaderSeparator) {
		this.columHeaderSeparator = columHeaderSeparator;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.IPropertySetTableColumnDescriptor#getColumnStructFieldIDs()
	 */
	@Override
	public List<StructFieldID> getColumnStructFieldIDs() {
		return Collections.unmodifiableList(structFieldIDs);
	}
	
	/**
	 * @param structFieldID The {@link StructFieldID} to add.
	 * @return this.
	 */
	public DefaultPropertySetTableColumnDescriptor addStructFieldID(StructFieldID structFieldID) {
		structFieldIDs.add(structFieldID);
		return this;
	}

	/**
	 * @param structFieldID The {@link StructFieldID} to remove.
	 * @return this.
	 */
	public DefaultPropertySetTableColumnDescriptor removeStructFieldID(StructFieldID structFieldID) {
		structFieldIDs.remove(structFieldID);
		return this;
	}

	/**
	 * @return The weight of this column comared to the other ones. this will be used in
	 *         {@link #createColumnLayoutData()}.
	 */
	public int getColumnWeight() {
		return columnWeight;
	}

	/**
	 * @param columnWeight The weight of this column comared to the other ones. this will be used in
	 *            {@link #createColumnLayoutData()}.
	 */
	public void setColumnWeight(int columnWeight) {
		this.columnWeight = columnWeight;
	}
}
