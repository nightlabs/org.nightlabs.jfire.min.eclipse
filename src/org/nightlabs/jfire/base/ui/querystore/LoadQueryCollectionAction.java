package org.nightlabs.jfire.base.ui.querystore;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.action.WorkbenchPartAction;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.dialog.CenteredTitleDialog;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.overview.OverviewEntryEditor;
import org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class LoadQueryCollectionAction
	extends WorkbenchPartAction
{
	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(LoadQueryCollectionAction.class);
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
	 */
	@Override
	public boolean calculateEnabled()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
	 */
	@Override
	public boolean calculateVisible()
	{
		return true;
	}

	private static final String[] FETCH_GROUPS_QUERYSTORES = new String[] {
		FetchPlan.DEFAULT, BaseQueryStore.FETCH_GROUP_OWNER  
	};
	
	@Override
	public void run()
	{
		if (! (getActivePart() instanceof OverviewEntryEditor))
		{
			logger.warn("The load QueryCollection action is called from outside an OverviewEntryEditor." +
					"This is not intended! ActivePart=" + getActivePart().getClass().getName(),
					new Exception()
					);
			return;
		}

		final OverviewEntryEditor editor = (OverviewEntryEditor) getActivePart();
		
		if (! (editor.getEntryViewer() instanceof SearchEntryViewer))
		{
			logger.error("This Action will only work with subclasses of SearchEntryViewer, since they" +
					"know what kind of objects their queries will return!", new Exception());
			
			return;
		}
		
		final SearchEntryViewer<?, ?> viewer = (SearchEntryViewer<?, ?>) editor.getEntryViewer();
		
		final Class<?> resultType = viewer.getResultType();
		
		Job fetchStoredQueries = new Job("Fetching stored Query configurations...")
		{
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				final Collection<BaseQueryStore<?, ?>> storedQueryCollections = 
					QueryStoreDAO.sharedInstance().getQueryStoresByReturnType(
						resultType, true, FETCH_GROUPS_QUERYSTORES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				
				viewer.getComposite().getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						chooseQueryCollection(storedQueryCollections, viewer);
					}
				});
				return Status.OK_STATUS;
			}
		};
		fetchStoredQueries.setUser(true);
		fetchStoredQueries.setPriority(org.eclipse.core.runtime.jobs.Job.LONG);
		fetchStoredQueries.schedule();
	}
	
	protected void chooseQueryCollection(Collection<BaseQueryStore<?, ?>> storedQueries,
		SearchEntryViewer<?, ?> viewer)
	{
		LoadQueryStoreDialog dialog = new LoadQueryStoreDialog(viewer.getComposite().getShell(), storedQueries);
		dialog.open();
		if (dialog.getSelectedQueryStore() == null)
			return;
		
		viewer.getQueryProvider().loadQueries(dialog.getSelectedQueryStore().getQueryCollection());
	}
	
	/**
	 * Simple Dialog showing all given BaseQueryStores and returns the one selected. Additionally it
	 * can filter all QueryStores for the ones that were created by the current user.
	 * 
	 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
	 */
	public static class LoadQueryStoreDialog extends CenteredTitleDialog
	{
		private BaseQueryStore<?, ?> selectedQueryConfiguration;
		private Button showAdditionalPublicQueriesButton;
		private BaseQueryStoreTableComposite resultTable; 
		private Collection<BaseQueryStore<?, ?>> queries;
		private ViewerFilter onlyMyQueriesFilter = new ViewerFilter()
		{
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				if (! (element instanceof BaseQueryStore<?, ?>))
				return false;
				
				final BaseQueryStore<?, ?> store = (BaseQueryStore<?, ?>) element;
				if (! store.getOwnerID().equals(SecurityReflector.getUserDescriptor().getUserObjectID()))
					return false;
				
				return true;
			}
		};
		
		public LoadQueryStoreDialog(Shell parentShell, Collection<BaseQueryStore<?, ?>> queries)
		{
			super(parentShell);
			assert queries != null;
			this.queries = queries;
		}
		
		@Override
		protected int getShellStyle()
		{
			return super.getShellStyle() | SWT.RESIZE;
		}
		
		@Override
		protected void configureShell(Shell newShell)
		{
			super.configureShell(newShell);
			newShell.setText("Query Selection");
		}
		
		@Override
		protected Control createDialogArea(Composite parent)
		{
			XComposite wrapper = new XComposite((Composite) super.createDialogArea(parent), SWT.NONE,
				LayoutDataMode.GRID_DATA);
			showAdditionalPublicQueriesButton = new Button(wrapper, SWT.CHECK);
			showAdditionalPublicQueriesButton.setText("Show publicly available queries.");
			showAdditionalPublicQueriesButton.setSelection(true);
			showAdditionalPublicQueriesButton.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					final boolean onlyMyQueries = ! ((Button) e.getSource()).getSelection();
					if (onlyMyQueries)
					{
						resultTable.getTableViewer().setFilters(new ViewerFilter[] { onlyMyQueriesFilter });						
					}
					else
					{
						resultTable.getTableViewer().setFilters(new ViewerFilter[0]);
//						resultTable.refresh();
					}
				}
			});
			GridData buttonData = new GridData();
			buttonData.horizontalAlignment = SWT.RIGHT;
			showAdditionalPublicQueriesButton.setLayoutData(buttonData);
			
			resultTable = new BaseQueryStoreTableComposite(wrapper,
				AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER);
			resultTable.addDoubleClickListener(new IDoubleClickListener()
			{
				@Override
				public void doubleClick(DoubleClickEvent event)
				{
					okPressed();
				}
			});
			resultTable.setInput(queries);
			
			setTitle("Select Query to load.");
			setMessage("Please select the Query you like to be loaded.");
			return wrapper;
		}
		
		@Override
		protected void okPressed()
		{
			selectedQueryConfiguration = resultTable.getFirstSelectedElement();
			super.okPressed();
		}
		
		public BaseQueryStore<?, ?> getSelectedQueryStore()
		{
			return selectedQueryConfiguration;
		}
	}
	
}
