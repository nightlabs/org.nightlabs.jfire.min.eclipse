package org.nightlabs.jfire.base.ui.querystore;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.util.JFaceUtil;
import org.nightlabs.jfire.base.ui.querystore.BaseQueryStoreActiveTableComposite.BaseQueryStoreColumnLabelProvider;
import org.nightlabs.jfire.query.store.BaseQueryStore;

/**
 * Simple Table for BaseQueryStores with 3 columns (name, public, owner). This table does not update
 * itself to database changes like {@link BaseQueryStoreActiveTableComposite}.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class BaseQueryStoreInActiveTableComposite
	extends AbstractTableComposite<BaseQueryStore>
{

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 * @param viewerStyle
	 */
	public BaseQueryStoreInActiveTableComposite(Composite parent, int style, boolean initTable, int viewerStyle)
	{
		super(parent, style, initTable, viewerStyle);
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	public BaseQueryStoreInActiveTableComposite(Composite parent, int style, boolean initTable)
	{
		super(parent, style, initTable);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public BaseQueryStoreInActiveTableComposite(Composite parent, int style)
	{
		super(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(final TableViewer tableViewer, Table table)
	{
		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		viewerColumn.getColumn().setText("Query Name");
		viewerColumn.setLabelProvider(new BaseQueryStoreActiveTableComposite.BaseQueryStoreColumnLabelProvider()
		{
			@Override
			public String doGetText(BaseQueryStore<?, ?> store)
			{
				return store.getName().getText();
			}
		});
		
		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		viewerColumn.getColumn().setText("public");
		viewerColumn.setLabelProvider(new BaseQueryStoreColumnLabelProvider()
		{
			@Override
			public Image getImage(Object element)
			{
				if (! (element instanceof BaseQueryStore<?, ?>))	
					return super.getImage(element);

				final BaseQueryStore<?, ?> store = (BaseQueryStore<?, ?>) element;
				return JFaceUtil.getCheckBoxImage(tableViewer, store.isPubliclyAvailable()); 
			}
			
			@Override
			public String getToolTipText(Object element)
			{
				return "Whether the stored Query is publicly available for all users.";
			}

			@Override
			public String doGetText(BaseQueryStore<?, ?> store)
			{
				return "";
			}
		});
		
		viewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Creator");
		viewerColumn.setLabelProvider(new BaseQueryStoreColumnLabelProvider()
		{
			@Override
			public String doGetText(BaseQueryStore<?, ?> store)
			{
				return store.getOwner().getName();
			}
		});

		ColumnViewerToolTipSupport.enableFor(getTableViewer());
		
		final int checkImageWidth = JFaceUtil.getCheckBoxImage(tableViewer, true).getBounds().width;
		tableViewer.getTable().setLayout(new WeightedTableLayout(
			new int[] { 4, -1, 3 }, new int[] {-1, checkImageWidth, -1})
		);		
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
