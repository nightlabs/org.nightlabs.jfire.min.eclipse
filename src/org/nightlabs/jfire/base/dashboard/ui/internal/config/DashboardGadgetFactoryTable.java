/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.config;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;

/**
 * @author abieber
 *
 */
public class DashboardGadgetFactoryTable extends AbstractTableComposite<IDashboardGadgetFactory> {

	/**
	 * @param parent
	 * @param style
	 */
	public DashboardGadgetFactoryTable(Composite parent, int style) {
		super(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		TableColumn col = new TableColumn(table, SWT.LEFT);
		col.setText("Name");
//		table.setHeaderVisible(false);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100));
		table.setLayout(layout);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#setTableProvider(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider() {
			
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 0) {
					return ((IDashboardGadgetFactory) element).getName();
				}
				return "";
			}
		});
	}

}
