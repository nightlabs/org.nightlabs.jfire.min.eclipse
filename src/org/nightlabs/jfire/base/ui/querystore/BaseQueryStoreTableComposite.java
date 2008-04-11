package org.nightlabs.jfire.base.ui.querystore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.util.JFaceUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedEvent;
import org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectController;
import org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectTableComposite;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.jfire.query.store.jdo.filter.BaseQueryStoreLifecycleFilter;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
@SuppressWarnings("unchecked")
public class BaseQueryStoreTableComposite
	extends ActiveJDOObjectTableComposite<QueryStoreID, BaseQueryStore>
{
	private Class<?> resultType;

	/**
	 * @param parent
	 * @param viewerStyle
	 */
	public BaseQueryStoreTableComposite(Composite parent, int viewerStyle, Class<?> resultType)
	{
		super(parent, SWT.NONE, viewerStyle);
		assert resultType != null;
		this.resultType = resultType;
	}

	public static final String[] FETCH_GROUP_BASE_QUERY_STORE = new String[] {
		FetchPlan.DEFAULT, BaseQueryStore.FETCH_GROUP_OWNER
	};
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(final TableViewer tableViewer, Table table)
	{
		createNameColumn(tableViewer);
		createPublicAvailableColumn(tableViewer);
		createOwnerColumn(tableViewer);
		setTableLayout(tableViewer);		
	}
	
	protected void setTableLayout(TableViewer tableViewer)
	{
		final int checkImageWidth = JFaceUtil.getCheckBoxImage(tableViewer, true).getBounds().width;
		tableViewer.getTable().setLayout(new WeightedTableLayout(
			new int[] { 4, -1, 3 }, new int[] {-1, checkImageWidth, -1})
		);		
	}

	/**
	 * @param tableViewer
	 */
	private TableViewerColumn createOwnerColumn(final TableViewer tableViewer)
	{
		TableViewerColumn viewerColumn;
		viewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Creator");
		viewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (! (element instanceof BaseQueryStore<?, ?>))
					return super.getText(element);
				
				final BaseQueryStore<?, ?> store = (BaseQueryStore<?, ?>) element;
				return store.getOwner().getName();
			}
		});
		
		return viewerColumn;
	}

	/**
	 * @param tableViewer
	 */
	private TableViewerColumn createPublicAvailableColumn(final TableViewer tableViewer)
	{
		TableViewerColumn viewerColumn;
		viewerColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
		viewerColumn.getColumn().setText("public");
		viewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return "";
			}
			
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
		});
		return viewerColumn;
	}

	/**
	 * @param tableViewer
	 */
	protected TableViewerColumn createNameColumn(final TableViewer tableViewer)
	{
		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Query Name");
		viewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (! (element instanceof BaseQueryStore<?, ?>))
					return super.getText(element);
				
				final BaseQueryStore<?, ?> store = (BaseQueryStore<?, ?>) element;
				return store.getName().getText();
			}
		});
		return viewerColumn;
	}

	@Override
	protected ActiveJDOObjectController<QueryStoreID, BaseQueryStore> createActiveJDOObjectController()
	{
		return new BaseQueryStoreActiveController(resultType);
	}

	@Override
	protected ITableLabelProvider createLabelProvider()
	{
		return null;
	}

}

/**
 * Migrated to use ActiveJDOObjectController that are used for each table and filter only for
 * QueryStores with the correct result type. 
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
@SuppressWarnings("unchecked")
class BaseQueryStoreActiveController
	extends ActiveJDOObjectController<QueryStoreID, BaseQueryStore>
{
	private Class<?> resultType;

	public BaseQueryStoreActiveController(Class<?> resultType)
	{
		assert resultType != null;
		this.resultType = resultType;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends BaseQueryStore> getJDOObjectClass()
	{
//	damn sun java compiler does not accept this cast (in version 1.6.0; since 1.6.03 this is ok)!! (marius)
//		return (Class<? extends BaseQueryStore<?, ?>>) BaseQueryStore.class;
		return BaseQueryStore.class;
	}

	@Override
	protected Collection<BaseQueryStore> retrieveJDOObjects(Set<QueryStoreID> objectIDs,
		ProgressMonitor monitor)
	{
		return (Collection<BaseQueryStore>) QueryStoreDAO.sharedInstance().getQueryStores(objectIDs, 
			BaseQueryStoreTableComposite.FETCH_GROUP_BASE_QUERY_STORE, 
			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	@Override
	protected Collection<BaseQueryStore> retrieveJDOObjects(ProgressMonitor monitor)
	{
		return (Collection<BaseQueryStore>) QueryStoreDAO.sharedInstance().getQueryStoresByReturnType(resultType, true, 
			BaseQueryStoreTableComposite.FETCH_GROUP_BASE_QUERY_STORE, 
			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	@Override
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new BaseQueryStoreLifecycleFilter(
			SecurityReflector.getUserDescriptor().getUserObjectID(), resultType, true,
			new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}
	
	@Override
	protected void sortJDOObjects(List<BaseQueryStore> objects)
	{
	}
	
	@Override
	protected void onJDOObjectsChanged(
		JDOObjectsChangedEvent<QueryStoreID, BaseQueryStore> event)
	{
	}
}