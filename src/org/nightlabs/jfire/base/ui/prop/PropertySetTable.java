/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.prop;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Table Composite that displays a configurable set of {@link StructField}s of a list of
 * {@link PropertySet}s.
 * <p>
 * The {@link StructFieldID}s to display are part of the {@link IPropertySetTableConfig} that has to
 * be returned on {@link #getPropertySetTableConfig()}. For technical reasons this config is queried
 * only in the constructor and can't be changed within the lifetime of one {@link PropertySetTable}.
 * </p>
 * 
 * @param <InputType> The type of elements in the collection that can be set as input for this table.
 *            The table will have an {@link ArrayContentProvider} so Arrays of Collections of this
 *            type are supported.
 * 
 * @param <SelectionType> The type that this table treats as selection-type (i.e. element-type) for
 *            the table. Implementors have to provide a conversion from the InputType to the
 *            SelectionType (see {@link #convertInputElement(Object)}).
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
public abstract class PropertySetTable<InputType, SelectionType>
extends AbstractTableComposite<SelectionType> {

	private class LabelProvider extends TableLabelProvider {
		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			PropertySet propertySet = getPropertySetFromElement((InputType) element);
			if (propertySet != null) {
				return getStructFieldText(propertySet, columnIndex);
			} else {
				if (element instanceof String && columnIndex == 0) {
					return String.valueOf(element);
				}
			}
			return ""; //$NON-NLS-1$
		}
	}

	private IPropertySetTableConfig config;
	private List<ColumnLayoutData> columnLayoutDatas = new LinkedList<ColumnLayoutData>();

	/**
	 * Create a new {@link PropertySetTable}. The table will be multi-selection-capable and will
	 * have a border.
	 * 
	 * @param parent The parent to add the new table to.
	 * @param style The style of the {@link AbstractTableComposite} surrounding the table.
	 */
	public PropertySetTable(Composite parent, int style) {
		this(parent, style, AbstractTableComposite.DEFAULT_STYLE_MULTI_BORDER);
	}

	/**
	 * Create a new PropertySetTable that allows for a non-default style of the table-viewer.
	 * 
	 * @param parent The parent to add the new table to.
	 * @param style The style of the {@link AbstractTableComposite} surrounding the table.
	 * @param viewerStyle The style to create the {@link TableViewer} with.
	 */
	public PropertySetTable(Composite parent, int style, int viewerStyle) {
		super(parent, style, false, viewerStyle);
		this.config = getPropertySetTableConfig();
		if (this.config == null) {
			throw new IllegalStateException("The config of this PropertySetTable is null, the method getPropertySetTableConfig() was not implemented correctly."); //$NON-NLS-1$
		}
		if (this.config.getColumnDescriptors() == null) {
			throw new IllegalStateException("The config of this PropertySetTable returns null for its column-descriptors, this is not supported."); //$NON-NLS-1$
		}
		initTable();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates the StructField columns ({@link #createStructFieldColumns(TableViewer, Table)}) and
	 * all additional columns ({@link #createAdditionalColumns(TableViewer, Table)}) and applies a
	 * table layout {@link #applyTableLayout(Table)}.
	 * </p>
	 */
	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		createStructFieldColumns(tableViewer, table);
		createAdditionalColumns(tableViewer, table);
		applyTableLayout(table);
	}

	/**
	 * Called when the columns of this table are created and provides a hook for sub-classes to add
	 * columns to the table that do not display contents of DataFields for the configured StructFields.
	 * <p>
	 * It is recommended to use the {@link TableViewerColumn} construct to create a new column and
	 * assign it an own LabelProvider. However, setting the tables global LabelProvider should also
	 * work as the columns showing the DataField content have an own TableViewerColumn with a
	 * separate LabelProvider.
	 * </p>
	 * <p>
	 * Note that a {@link ColumnLayoutData} has to be provided for all additional columns, too, or
	 * otherwise the layout of the table will be wrong. Provide the {@link ColumnLayoutData} using
	 * {@link #addColumnLayoutData(ColumnLayoutData)} or
	 * {@link #addColumnLayoutData(int, ColumnLayoutData)} depending on whether you decided to
	 * simply add the columns or place them at a certain index.
	 * </p>
	 * <p>
	 * The default implementation does nothing
	 * </p>
	 * 
	 * @param tableViewer The {@link TableViewer} to add the columns to.
	 * @param table The {@link Table} to add the columns to.
	 */
	protected void createAdditionalColumns(TableViewer tableViewer, Table table) {
		// Nothing done in base implementation
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Set a default {@link ArrayContentProvider} and
	 * the internal {@link LabelProvider}.
	 * </p>
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());
	}

	/**
	 * Creates the StructFieldColumns columns for this table.
	 * This method is called by {@link #createTableColumns(TableViewer, Table)}
	 * if not overridden or can be used in custom implementations.
	 *
	 * @param tableViewer The {@link TableViewer} of this table.
	 * @param table The {@link Table} of this table.
	 */
	protected void createStructFieldColumns(TableViewer tableViewer, Table table) {
		for (int i = 0; i < config.getColumnDescriptors().size(); i++) {
			IPropertySetTableColumnDescriptor columnDescriptor = config.getColumnDescriptors().get(i);
			StringBuilder columnText = new StringBuilder();
			for (Iterator<StructFieldID> it = columnDescriptor.getColumnStructFieldIDs().iterator(); it.hasNext();) {
				StructFieldID structFieldID = it.next();
				try {
					columnText.append(config.getStruct().getStructField(structFieldID).getName().getText());
				} catch (PropertyException e) {
					throw new RuntimeException(e);
				}
				if (it.hasNext()) {
					columnText.append(columnDescriptor.getColumHeaderSeparator());
				}
			}
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText(columnText.toString().trim());
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, column);
			final int structFieldColumnIndex = i;
			viewerColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					PropertySet propertySet = getPropertySetFromElement((InputType) element);
					if (propertySet != null) {
						return getStructFieldText(propertySet, structFieldColumnIndex);
					} else {
						if (element instanceof String && structFieldColumnIndex == 0) {
							return String.valueOf(element);
						}
					}
					return ""; //$NON-NLS-1$
				}
			});
			
			addColumnLayoutData(columnDescriptor.createColumnLayoutData());
		}
	}

	/**
	 * Sets the given {@link ColumnLayoutData} that should layout the 'next' layout, i.e. it will be
	 * added to the end of the list of the already defined {@link ColumnLayoutData}s.
	 * <p>
	 * Note that calling this method should only be done from
	 * {@link #createAdditionalColumns(TableViewer, Table)} as this is a defined moment where
	 * layout-data can be contributed.
	 * </p>
	 * 
	 * @param columnLayoutData The {@link ColumnLayoutData} to add.
	 */
	protected void addColumnLayoutData(ColumnLayoutData columnLayoutData) {
		addColumnLayoutData(columnLayoutDatas.size(), columnLayoutData);
	}

	/**
	 * Sets the given {@link ColumnLayoutData} that should layout the column with the given index.
	 * Note that, the length of the list of already defined {@link ColumnLayoutData}-definitions has
	 * to meet: <code>length <= index</code>
	 * <p>
	 * Note that calling this method should only be done from
	 * {@link #createAdditionalColumns(TableViewer, Table)} as this is a defined moment where
	 * layout-data can be contributed.
	 * </p>
	 * 
	 * @param columnLayoutData The {@link ColumnLayoutData} to add.
	 */
	protected void addColumnLayoutData(int index, ColumnLayoutData columnLayoutData) {
		columnLayoutDatas.add(index, columnLayoutData);
	}

	/**
	 * Takes all {@link ColumnLayoutData}s defined using
	 * {@link #addColumnLayoutData(ColumnLayoutData)} and
	 * {@link #addColumnLayoutData(int, ColumnLayoutData)} and creates a TableLayout using this
	 * definition. 
	 * 
	 * @param table The {@link Table} to layout.
	 */
	protected void applyTableLayout(final Table table) {
		applyTableLayout(table, true);
	}

	/**
	 * Used as workaround for setting the table-layout twice and relayouting in an asynExec, because
	 * the layout layouts only once and will not distribute over the complete width of the table
	 * (after resize).
	 */
	private void applyTableLayout(final Table table, boolean useWorkaround) {
		TableLayout tableLayout = new TableLayout();
		for (ColumnLayoutData layoutData : columnLayoutDatas) {
			tableLayout.addColumnData(layoutData);
		}
		table.setLayout(tableLayout);
		if (useWorkaround) {
			table.getDisplay().asyncExec(new Runnable() {
				public void run() {
					applyTableLayout(table, false);
					table.layout(true, true);
				}
			});
		}
	}

	/**
	 * This method may be overridden to extract the {@link PropertySet}
	 * from a single table element (element from the collection set as input).
	 * It is used to create the column-text for all columns that display DataField content.
	 * <p>
	 * The method should return <code>null</code> if it can not extract
	 * a PropertySet from the given element.
	 * </p>
	 * <p>
	 * The default implementation returns the element if it is an
	 * instance of {@link PropertySet}.
	 * </p>
	 *
	 * @param element The element to extract the {@link PropertySet} from.
	 * @return The {@link PropertySet} extracted from the given Element.
	 */
	protected PropertySet getPropertySetFromElement(InputType inputElement) {
		if (inputElement instanceof PropertySet)
			return (PropertySet) inputElement;
		return null;
	}

	/**
	 * Used by the {@link LabelProvider} of the columns that display contents of DataFields.
	 * Currently this method is capable of returning a result for all DataFields that implement
	 * {@link II18nTextDataField}.
	 * 
	 * @param propertySet The {@link PropertySet} to get the field value from.
	 * @param columnIdx The index of the {@link StructFieldID} to get. (Array passed in the
	 *            constructor).
	 * @return The String representation of the {@link StructField} value for the given
	 *         {@link PropertySet}.
	 */
	protected String getStructFieldText(PropertySet propertySet, int columnIdx) {
		if (columnIdx >= 0 && columnIdx < config.getColumnDescriptors().size()) {
			IPropertySetTableColumnDescriptor columnDescriptor = config.getColumnDescriptors().get(columnIdx);
			List<StructFieldID> fieldIDs = columnDescriptor.getColumnStructFieldIDs();
			StringBuilder text = new StringBuilder();
			for (Iterator<StructFieldID> it = fieldIDs.iterator(); it.hasNext();) {
				StructFieldID fieldID = it.next();
				DataField dataField = propertySet.getPersistentDataFieldByIndex(fieldID, 0);
				boolean fieldAdded = false;
				if (dataField != null && dataField instanceof II18nTextDataField) {
					String fieldValue = ((II18nTextDataField) dataField).getI18nText().getText();
					fieldAdded = fieldValue != null && !fieldValue.isEmpty();
					if (fieldAdded) {
						text.append(fieldValue);
					}
				}
				if (it.hasNext() && fieldAdded) {
					text.append(columnDescriptor.getColumDataSeparator());
				}
			}
			return text.toString();
		} else
			return ""; //$NON-NLS-1$
	}

	/**
	 * Return the configuration of the columns of this table. The configuration defines which
	 * StructFields will be displayed in which column and the weights of these columns in the table.
	 * This method is called once in the constructor and there is no way to change the config once
	 * it was applied.
	 * 
	 * @return The {@link IPropertySetTableConfig} of this table.
	 */
	protected abstract IPropertySetTableConfig getPropertySetTableConfig();

	/**
	 * Called for every selection element to provide the instance of SelectionType for an InputType.
	 * <p>
	 * If <code>null</code> is returned here, the given inputElement will not be represented in the
	 * selection of this table when queried by the methods of {@link AbstractTableComposite}.
	 * </p>
	 * 
	 * @param inputElement The element form the input collection to get the SelectionType for.
	 * @return The instance of SelectionType corresponding to the given inputElemen, or
	 *         <code>null</code>.
	 */
	protected abstract SelectionType convertInputElement(InputType inputElement);

	/**
	 * Calls {@link #convertInputElement(Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected SelectionType getSelectionObject(Object element) {
		return convertInputElement((InputType) element);
	}
}
