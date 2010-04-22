package org.nightlabs.jfire.base.ui.prop;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.id.StructFieldID;


/**
 * 
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DefaultPropertySetTableConfig implements IPropertySetTableConfig 
{
	private IStruct struct;
	private List<IPropertySetTableColumnDescriptor> columnDescriptors = new LinkedList<IPropertySetTableColumnDescriptor>();
	
	/**
	 * Default constructor.
	 */
	public DefaultPropertySetTableConfig() {
		super();
	}

	/**
	 * Create a new {@link DefaultPropertySetTableConfig} that will column-descriptors each with one
	 * of the given StructFieldIDs.
	 * 
	 * @param struct The struct the given StructFieldIDs are from.
	 * @param structFieldIDs The ids of the StructFields that should be displayed in the columns of
	 *            the table this config is attached to.
	 */
	public DefaultPropertySetTableConfig(IStruct struct, StructFieldID... structFieldIDs) {
		super();
		this.struct = struct;
		for (StructFieldID columnFieldID : structFieldIDs) {
			addDefaultColumnDescriptor(columnFieldID);
		}
	}

	/**
	 * Create a new {@link DefaultPropertySetTableConfig} that will column-descriptors where each
	 * one will have a list of StructFieldIDs to display.
	 * 
	 * @param struct The struct the given StructFieldIDs are from.
	 * @param columnStructFieldIDs The ids of the StructFields that should be displayed in the
	 *            columns of the table this config is attached to.
	 */
	public DefaultPropertySetTableConfig(IStruct struct, StructFieldID[]... columnStructFieldIDs) {
		super();
		this.struct = struct;
		for (StructFieldID[] columnFieldIDs : columnStructFieldIDs) {
			addDefaultColumnDescriptor(columnFieldIDs);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.IPropertySetTableConfig#getStruct()
	 */
	@Override
	public IStruct getStruct() {
		return struct;
	}
	
	/**
	 * Set the {@link IStruct} this config returns.
	 * @param struct
	 * @return this.
	 */
	public DefaultPropertySetTableConfig setStruct(IStruct struct) {
		this.struct = struct;
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.IPropertySetTableConfig#getColumnDescriptors()
	 */
	@Override
	public List<IPropertySetTableColumnDescriptor> getColumnDescriptors() {
		return Collections.unmodifiableList(columnDescriptors);
	}
	
	/**
	 * Add the given column-descriptor to the list of descriptors.
	 * 
	 * @param columnDescriptor The descriptor to add.
	 * @return this.
	 */
	public DefaultPropertySetTableConfig addColumnDescriptor(IPropertySetTableColumnDescriptor columnDescriptor) {
		columnDescriptors.add(columnDescriptor);
		return this;
	}

	/**
	 * Remove the given column-descriptor from the list of descriptors.
	 * 
	 * @param columnDescriptor The descriptor to remove.
	 * @return this.
	 */
	public DefaultPropertySetTableConfig removeColumnDescriptor(IPropertySetTableColumnDescriptor columnDescriptor) {
		columnDescriptors.remove(columnDescriptor);
		return this;
	}

	/**
	 * Add a column-descriptor to the list of descriptors that will have the given StructFieldIDs
	 * set (single or multiple StuctFieldIDs possible). This method will create a
	 * {@link DefaultPropertySetTableColumnDescriptor} with the default separator-strings for the
	 * column-header and column-data.
	 * 
	 * @param structFieldIDs The ids that should be set for the new descriptor.
	 * @return The added descriptor.
	 */
	public DefaultPropertySetTableColumnDescriptor addDefaultColumnDescriptor(StructFieldID... structFieldIDs) {
		DefaultPropertySetTableColumnDescriptor columnDescriptor = new DefaultPropertySetTableColumnDescriptor(structFieldIDs);
		addColumnDescriptor(columnDescriptor);
		return columnDescriptor;
	}
}
