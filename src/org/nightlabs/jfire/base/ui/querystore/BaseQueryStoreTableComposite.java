package org.nightlabs.jfire.base.ui.querystore;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.jfire.query.store.BaseQueryStore;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class BaseQueryStoreTableComposite
	extends AbstractTableComposite<BaseQueryStore<?, ?>>
{

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 * @param viewerStyle
	 */
	public BaseQueryStoreTableComposite(Composite parent, int style, boolean initTable,
		int viewerStyle)
	{
		super(parent, style, initTable, viewerStyle);
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	protected BaseQueryStoreTableComposite(Composite parent, int style, boolean initTable)
	{
		super(parent, style, initTable);
	}

	/**
	 * @param parent
	 * @param viewerStyle
	 */
	public BaseQueryStoreTableComposite(Composite parent, int viewerStyle)
	{
		this(parent, SWT.NONE, true, viewerStyle);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table)
	{
		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Query Name");
		viewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (! (element instanceof BaseQueryStore<?, ?>))
					return "";
				
				final BaseQueryStore<?, ?> store = (BaseQueryStore<?, ?>) element;
				return store.getName().getText();
			}
		});
		
		viewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Creator");
		viewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (! (element instanceof BaseQueryStore<?, ?>))
					return "";
				
				final BaseQueryStore<?, ?> store = (BaseQueryStore<?, ?>) element;
				return store.getOwner().getName();
			}
		});
		
		table.setLayout(new WeightedTableLayout(new int[] { 2, 1 }));
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#setTableProvider(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer)
	{
		tableViewer.setContentProvider(new TableContentProvider());
	}

}
