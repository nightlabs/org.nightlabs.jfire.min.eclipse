package org.nightlabs.jfire.base.ui.querystore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.util.JFaceUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.NullProgressMonitor;

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
		registerJDOListener();
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	protected BaseQueryStoreTableComposite(Composite parent, int style, boolean initTable)
	{
		this(parent, style, initTable, AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER);
	}

	/**
	 * @param parent
	 * @param viewerStyle
	 */
	public BaseQueryStoreTableComposite(Composite parent, int viewerStyle)
	{
		this(parent, SWT.NONE, true, viewerStyle);
	}

	protected void registerJDOListener()
	{
		JDOLifecycleManager.sharedInstance().addNotificationListener(BaseQueryStore.class, implicitUpdateListener);
		
		addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent event)
			{
				JDOLifecycleManager.sharedInstance().removeNotificationListener(
					BaseQueryStore.class, implicitUpdateListener);
			}
		});		
	}
	
	public static final String[] FETCH_GROUP_BASE_QUERY_STORE = new String[] {
		FetchPlan.DEFAULT, BaseQueryStore.FETCH_GROUP_OWNER
	};
	
	private NotificationListener implicitUpdateListener = new NotificationAdapterJob()
	{
		public void notify(org.nightlabs.notification.NotificationEvent notificationEvent)
		{
			Map<QueryStoreID, BaseQueryStore<?, ?>> shownElements = getIdsOfPresentedElements();
			Set<QueryStoreID> keySet = shownElements.keySet();
			Collection<BaseQueryStore<?, ?>> oldInput =
				(Collection<BaseQueryStore<?, ?>>) getTableViewer().getInput();
			
			final List<BaseQueryStore<?, ?>> newInput;
			if (oldInput == null)
			{
				newInput = Collections.emptyList();
			}
			else
			{
				newInput =	new ArrayList<BaseQueryStore<?,?>>(	oldInput );				
			}
			
			for (Iterator<DirtyObjectID> it = notificationEvent.getSubjects().iterator(); it.hasNext();)
			{
				DirtyObjectID dirtyObjectID = it.next();
				final QueryStoreID storeID = (QueryStoreID) dirtyObjectID.getObjectID();
				
				if (! keySet.contains(storeID))
					continue;

				switch (dirtyObjectID.getLifecycleState())
				{
					case DIRTY: // get changed object and replace old one with it.
						BaseQueryStore<?, ?> newStore =	QueryStoreDAO.sharedInstance().getQueryStore(
								storeID, FETCH_GROUP_BASE_QUERY_STORE,
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
						
						// remove old element from input set and set new one to the previous position
						int index = newInput.indexOf(shownElements.get(storeID));
						newInput.remove(shownElements.get(storeID));
						newInput.add(index, newStore);
						break;
						
					case DELETED:	// - remove the object from the new input
						BaseQueryStore<?, ?> deletedStore = shownElements.get(storeID);
						newInput.remove(deletedStore);
						break;
						
					default:
						break;
				}
			}
			
			// only update UI if the old and new input differ
//			if (! getTableViewer().getInput().equals(newInput))
			getTable().getDisplay().asyncExec(new Runnable() 
			{
				@Override
				public void run()
				{
					getTableViewer().setInput(newInput);
				}
			});
		}
	};

	/**
	 * Helper method that returns a set of all QueryStoreIDs of all the elements last set as input.
	 * @return a set of all QueryStoreIDs of all the elements last set as input.
	 */
	protected Map<QueryStoreID, BaseQueryStore<?, ?>> getIdsOfPresentedElements()
	{
		Map<QueryStoreID, BaseQueryStore<?, ?>> idsOfInput =
			new HashMap<QueryStoreID, BaseQueryStore<?,?>>();
		
		Collection<BaseQueryStore<?, ?>> oldInput =
			(Collection<BaseQueryStore<?, ?>>) getTableViewer().getInput();
		
		if (oldInput != null)
		{
			for (BaseQueryStore<?, ?> store : oldInput)
			{
				idsOfInput.put((QueryStoreID) JDOHelper.getObjectId(store), store);
			}
		}
		
		return idsOfInput;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(final TableViewer tableViewer, Table table)
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
		
		final int checkImageWidth = JFaceUtil.getCheckBoxImage(tableViewer, true).getBounds().width;
		table.setLayout(new WeightedTableLayout(new int[] { 4, -1, 3 }, new int[] {-1, checkImageWidth, -1}));
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
