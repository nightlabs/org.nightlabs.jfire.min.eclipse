package org.nightlabs.jfire.base.ui.overview;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.LoginStateListener;
import org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer;
import org.nightlabs.jfire.base.ui.querystore.BaseQueryStoreTableComposite;
import org.nightlabs.jfire.base.ui.querystore.SaveQueryCollectionAction.QueryStoreEditDialog;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryStoreCapableCategoryComposite
	extends XComposite
{
	protected SashForm sashForm;
	protected DefaultCategoryComposite elementListing;
	protected ToolBarSectionPart queryStoreSection;
	private Composite tableStack;
	private StackLayout tableStackLayout;
	private Category category;
	
	protected EditQueryStoreAction editQueryStoreAction;
	protected DeleteQueryStoreAction deleteQueryStoreAction;

	private Map<Entry, TableConfig> entry2TableMap = new HashMap<Entry, TableConfig>();
	
	/**
	 * Clears the tables and the initialisation flags of all tables when the logged-in user changes.
	 */
	private LoginStateListener loginStateListener = new LoginStateListener()
	{
		private UserID oldUserID;
		
		@Override
		public void loginStateChanged(int loginState, IAction action)
		{
			if (Login.LOGINSTATE_LOGGED_IN == loginState)
			{
				// if no user was ever set -> initialise it with current
				if (oldUserID == null)
				{
					oldUserID = SecurityReflector.getUserDescriptor().getUserObjectID();
					return;
				}
				
				UserID newUserID = SecurityReflector.getUserDescriptor().getUserObjectID();
				if (! newUserID.equals(oldUserID))
				{
					clearTables();
				}
			}
		}
	};

	/**
	 * Listener that puts the corresponding {@link BaseQueryStoreTableComposite} to the selected Entry
	 * on top and controls the visibility state of the section containing this table.
	 * Further it triggers the initial loading of the QueryStores for a table if it wasn't done
	 * before.
	 */
	private ISelectionChangedListener entrySelectionChangedListener = new ISelectionChangedListener()
	{
		@Override
		public void selectionChanged(SelectionChangedEvent event)
		{
			final ISelection selection = event.getSelection();
			if (selection.isEmpty())
				return;
			
			if (! (selection instanceof IStructuredSelection))
			{
				logger.warn("The entry changed listener expects IStructuredSelections not: " +
					selection.getClass().getName());
				return;
			}
			
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (! (structuredSelection.getFirstElement() instanceof Entry))
			{
				logger.error("The entry changed listener expects an Entry as selection element not: " +
					structuredSelection.getFirstElement().getClass().getName());
				return;
			}
			
			final Entry selectedEntry = (Entry) structuredSelection.getFirstElement();
			
			// bring corresponding table to the top
			TableConfig config = entry2TableMap.get(selectedEntry);
			if (config == null)
			{ 
				// if there is no SearchEntryViewer created by this entry -> we don't have table
				//  => disable the section.
				queryStoreSection.getSection().setVisible(false);
				sashForm.setMaximizedControl(elementListing);
			}
			else
			{
				final FilteredQueryStoreComposite tableWrapper = config.getFilteredTableComposite();
				final BaseQueryStoreTableComposite table = tableWrapper.getTable();
				
				// set the corresponding table to the top
				bringTableToTop( tableWrapper );
				
				// start a Job to initialise the table with the stored queries
				if (! config.isInitialised())
				{
					loadQueries(table, config.getResultType(), true);
					config.setInitialised(true);
				}
				
				// set new table to actions
				if (editQueryStoreAction != null)
				{					
					editQueryStoreAction.setQueryTable( table );
				}
				if (deleteQueryStoreAction != null)
				{
					deleteQueryStoreAction.setQueryTable( table );					
				}
				
				// make the section visible again if was invisible before 
				if (! queryStoreSection.getSection().isVisible())
				{
					queryStoreSection.getSection().setVisible(true);
					sashForm.setMaximizedControl(null);
				}
			}
		}
	};
	
	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(QueryStoreCapableCategoryComposite.class);
	
	/**
	 * @param parent
	 * @param style
	 */
	public QueryStoreCapableCategoryComposite(Composite parent, int style, Category category)
	{
		this(parent, style, category, LayoutDataMode.NONE);
	}
	
	/**
	 * @param parent
	 * @param style
	 * @param layoutDataMode
	 */
	public QueryStoreCapableCategoryComposite(Composite parent, int style, Category category, 
		LayoutDataMode layoutDataMode)
	{
		super(parent, style, layoutDataMode);
		assert category != null;
		this.category = category;
		setLayout( getLayout(LayoutMode.TOTAL_WRAPPER) );
		getToolkit(true); // ensures that an IToolkit is set and we're looking like a form.
		createUI(this);
		
		// FIXME: register loginStateListener to the the LSDViewPart somehow!!
		// FIXME: add a static explicit Listener to the server in order to filter retrieve and filter	newly created QueryStores.
	}
	
	protected void createUI(XComposite parent)
	{
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		elementListing = new DefaultCategoryComposite(sashForm, SWT.NONE, category,
			AbstractTableComposite.DEFAULT_STYLE_SINGLE);
		elementListing.getTableViewer().addSelectionChangedListener(entrySelectionChangedListener);
		
		IToolkit toolkit = getToolkit();
		final int sectionStyle = ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED;
		final String sectionTitle = "Stored Queries";
		if (toolkit != null && toolkit instanceof FormToolkit)
		{
			queryStoreSection = new ToolBarSectionPart((FormToolkit) toolkit, sashForm, sectionStyle,
				sectionTitle);
		}
		else
		{
			final FormToolkit formToolkit = new FormToolkit(getDisplay());
			queryStoreSection = new ToolBarSectionPart(formToolkit, sashForm, sectionStyle, sectionTitle);
			addDisposeListener(new DisposeListener()
			{
				@Override
				public void widgetDisposed(DisposeEvent e)
				{
					formToolkit.dispose();
				}
			});
		}
		
		tableStack = new Composite(queryStoreSection.getSection(), SWT.NONE);
		tableStackLayout = new StackLayout();
		tableStack.setLayout(tableStackLayout);
		
		queryStoreSection.getSection().setClient(tableStack);
		
		createTableStack(tableStack, getCategory().getEntries());
//		bringTableToTop(entry2TableMap.get(getCategory().getEntries().get(0)).getTable());
		// TODO: remove test
		Label tmp = new Label(tableStack, SWT.BORDER);
		tmp.setText("fuck that");
		tableStackLayout.topControl = tmp;
		tableStack.layout();
		
		editQueryStoreAction = new EditQueryStoreAction();
		queryStoreSection.getToolBarManager().add(editQueryStoreAction);
//	FIXME: If I delete something from the Database this weird foreign key exception occurs: Caused by: java.sql.BatchUpdateException: Cannot delete or update a parent row: a foreign key constraint fails (`JFire_chezfrancois_jfire_org/JFIREQUERYSTORE_BASEQUERYSTORE`, CONSTRAINT `JFIREQUERYSTORE_BASEQUERYSTORE_FK3` FOREIGN KEY (`NAME_ORGANISATION_ID_OID`, `NAME_QUERY_STORE_ID_OID`) REFERENCES `J)
//				 When this is cleared up just uncomment the following line to enabled the deletion of QueryStores. (marius)
//		deleteQueryStoreAction = new DeleteQueryStoreAction(queryStoreTables);
//		queryStoreSection.getToolBarManager().add(deleteQueryStoreAction);
		
		queryStoreSection.updateToolBarManager();
		
		if (toolkit != null)
		{
			adaptToToolkit();
		}
	}
	
	/**
	 * Creates a table for each given entry that creates a SearchEntryViewer.
	 * 
	 * @param tableStackWrapper the composite to create the Tables into.
	 * @param entries the list of entries to create tables for.  
	 */
	protected void createTableStack(Composite tableStackWrapper, List<Entry> entries)
	{
		entry2TableMap.clear();
		
		if (entries == null)
		{
			logger.warn("No registered Entries found for this category! Category: " +
				getCategory().getCategoryFactory().getName());
			
			return;
		}
		
		for (Entry entry : entries)
		{
			// if the entry doesn't use a SearchEntryViewer -> we cannot get to the resultTypeClass
			//  => We cannot prepare retrieve the correct type of QueryStores!
			EntryViewer viewer = entry.createEntryViewer();
			if (! (viewer instanceof SearchEntryViewer<?, ?>))
			{
				logger.warn("Cannot create the QueryStoreTable for the entry:" +
					entry.getClass().getName()+" !");
				entry2TableMap.put(entry, null);
				continue;
			}
			
			// create the table
			FilteredQueryStoreComposite table =	new FilteredQueryStoreComposite(tableStackWrapper, entry);
			
			// update mapping
			final SearchEntryViewer<?, ?> searchEntryViewer = (SearchEntryViewer<?, ?>) viewer;
			entry2TableMap.put(entry, new TableConfig(searchEntryViewer.getResultType(), table));
		}
	}
	
	/**
	 * Refreshes the DefaultCategory showing the entries and disposes and recreates the
	 * BaseQueryStoreTables.
	 * 
	 * @param entries the list of entries to display
	 */
	public void setInput(List<Entry> entries)
	{
		// set new input to the table showing the entries
		elementListing.setInput(entries);
		
		// dispose all old tables;
		for (Control child : tableStack.getChildren())
		{
			child.dispose();
		}
		
		// create new ones
		createTableStack(tableStack, entries);
	}
	
	/**
	 * Clears all created BaseQueryStoreTableComposites and resets the initialised value, so that
	 * if the user clicks again on an Entry, a new {@link FetchQueriesJob} is scheduled.
	 */
	protected void clearTables()
	{
		if (entry2TableMap == null)
			return;
		
		for (TableConfig config : entry2TableMap.values())
		{
			config.getFilteredTableComposite().getTable().setInput(null);
			config.setInitialised(false);
		}
	}
	
	/**
	 * Creates a new FetchQueriesJob and schedules it for processing.
	 * 
	 * @param table the table to fill the retrieved QueryStores into.
	 * @param resultType the result type of the QueryStores.
	 * @param allPublicAsWell whether all publicly available QueryStores shall be fetched as well.
	 */
	protected void loadQueries(BaseQueryStoreTableComposite table, Class<?> resultType,
		boolean allPublicAsWell)
	{
		new FetchQueriesJob(table, resultType, allPublicAsWell).schedule();
	}
	
	protected void bringTableToTop(FilteredQueryStoreComposite table)
	{
		if (table == null)
			return;
		
		tableStackLayout.topControl = table;
		tableStack.layout();
	}

	/**
	 * @return the category
	 */
	public Category getCategory()
	{
		return category;
	}
}

/**
 * A simple Job that retrieves the QueryStores that return the given <code>resultType</code> and are
 * owned by the current user and additionally all public QueryStores as well if
 * <code>publicAsWell == true</code>. <br />
 * Afterwards sets the returned Collection of QueryStores as input to the given
 * {@link BaseQueryStoreTableComposite}.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
class FetchQueriesJob
 extends Job
{
	private BaseQueryStoreTableComposite table;
	private Class<?> resultType;
	private boolean publicAsWell;
	
	public FetchQueriesJob(BaseQueryStoreTableComposite table, Class<?> resultType,
		boolean publicAsWell)
	{
		super("Fetching stored Queries for: " + resultType.getSimpleName());
		assert table != null;
		this.table = table;
		this.resultType = resultType;
		this.publicAsWell = publicAsWell;
		
		setPriority(LONG);
	}

	@Override
	protected IStatus run(ProgressMonitor monitor) throws Exception
	{
		final Collection<BaseQueryStore<?, ?>> stores = 
			QueryStoreDAO.sharedInstance().getQueryStoresByReturnType(
				resultType, publicAsWell, BaseQueryStoreTableComposite.FETCH_GROUP_BASE_QUERY_STORE,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
		
		table.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (table == null || table.isDisposed())
					return;
				
				table.setInput(stores);
			}
		});
		
		return Status.OK_STATUS;
	}
}

/**
 * Small wrapper that contains a button in checkbox-style for selecting whether only the user's
 * queries shall be shown and a {@link BaseQueryStoreTableComposite}. <br />
 * Additionally it registers a double click listener to open the SearchEntryViewer and set the 
 * QueryCollection of the Store double-clicked on.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
class FilteredQueryStoreComposite
	extends Composite
{
	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(FilteredQueryStoreComposite.class);
	
	private Button showPublicQueries;
	private BaseQueryStoreTableComposite table;
	private final Entry entry;
	private static ViewerFilter onlyMyQueriesFilter = new ViewerFilter() 
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
	
	/**
	 * Selection listener that uses the Entry's handleActivation to open or activate the corresponding
	 * OverviewEntryEditor. Then it gets the SearchEntryViewer and loads the QueryCollection clicked
	 * on.
	 */
	private SelectionListener doubleClickListener = new SelectionAdapter()
	{
		@Override
		public void widgetDefaultSelected(SelectionEvent e)
		{
			if (table.getFirstSelectedElement() == null)
				return;
			
			final IWorkbenchPart part = entry.handleActivation();
			if (part == null)
				return;
			
			if (! (part instanceof OverviewEntryEditor))
			{
				logger.warn("The activated part of the Entry being clicked on is not an " +
						"OverviewEntryEditor, but instead: " + part.getClass().getName()+ " !");
				return;
			}
			
			final OverviewEntryEditor editor = (OverviewEntryEditor) part;
			// These tables are only created for Entries with a SearchEntryViewer assigned!!
			//  see #createTableStack(Composite, List)
			final SearchEntryViewer<?, ?> searchEntryViewer =
				(SearchEntryViewer<?, ?>) editor.getEntryViewer();
			
			final BaseQueryStore<?, ?> store = table.getFirstSelectedElement();
			searchEntryViewer.getQueryProvider().loadQueries(store.getQueryCollection());
		}
	};
	
	
	public FilteredQueryStoreComposite(Composite parent, Entry entry)
	{
		super(parent, SWT.NONE);
		assert entry != null;
		this.entry = entry;
		GridLayout layout = XComposite.getLayout(LayoutMode.TOTAL_WRAPPER);
		layout.horizontalSpacing = 5;
		setLayout( layout );
		createUI(this);
	}
	
	private void createUI(Composite parent)
	{
		showPublicQueries = new Button(parent, SWT.CHECK);
		showPublicQueries.setText("Show public queries");
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		showPublicQueries.setLayoutData(gd);
		
		table = new BaseQueryStoreTableComposite(parent,
			AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER);
		table.getTableViewer().getTable().addSelectionListener(doubleClickListener);
		gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);
		
		showPublicQueries.setSelection(true);
		showPublicQueries.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if ( ((Button) e.getSource()).getSelection() )
				{
					table.getTableViewer().setFilters( new ViewerFilter[0] );
				}
				else
				{
					table.getTableViewer().setFilters( new ViewerFilter[] { onlyMyQueriesFilter } );
				}
			}
		});
	}

	/**
	 * @return the table
	 */
	public BaseQueryStoreTableComposite getTable()
	{
		return table;
	}
}

/**
 * Container class for initialisation information used in the job to fetch the stored QueryStores
 * from the server if necessary.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
class TableConfig
{
	private boolean initialised = false;
	private FilteredQueryStoreComposite table;
	private Class<?> resultType;
	
	/**
	 * @param resultType
	 */
	public TableConfig(Class<?> resultType, FilteredQueryStoreComposite table)
	{
		assert resultType != null;
		assert table != null;
		this.resultType = resultType;
		this.table = table;
	}

	/**
	 * @return the initialised
	 */
	public boolean isInitialised()
	{
		return initialised;
	}

	/**
	 * @param initialised the initialised to set
	 */
	public void setInitialised(boolean initialised)
	{
		this.initialised = initialised;
	}

	/**
	 * @return the resultType
	 */
	public Class<?> getResultType()
	{
		return resultType;
	}

	/**
	 * @return the table
	 */
	public FilteredQueryStoreComposite getFilteredTableComposite()
	{
		return table;
	}
	
}

class EditQueryStoreAction 
	extends Action 
{
	private BaseQueryStoreTableComposite queryTable;
	
	public EditQueryStoreAction()
	{
//		assert queryTable != null;
//		this.queryTable = queryTable;
		setId(EditQueryStoreAction.class.getName());
		setImageDescriptor(SharedImages.EDIT_16x16);
		setToolTipText("Edit Stored Query");
		setText("Edit");
	}
	
	@Override
	public void run() 
	{
		if (queryTable == null || queryTable.isDisposed())
			return;
		
		BaseQueryStore<?, ?> store = queryTable.getFirstSelectedElement();
		if (store == null)
			return;
		
		QueryStoreEditDialog dialog = new QueryStoreEditDialog(queryTable.getShell(), store);
		
		if (dialog.open() != Window.OK)
			return;
		
		Collection<BaseQueryStore<?, ?>> input = 
			(Collection<BaseQueryStore<?, ?>>) queryTable.getTableViewer().getInput();
		
		input.remove(store);
		
		store = QueryStoreDAO.sharedInstance().storeQueryStore(store, 
			BaseQueryStoreTableComposite.FETCH_GROUP_BASE_QUERY_STORE,
			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, true, new NullProgressMonitor()
		);
		
		input.add(store);
		queryTable.setInput(input);
	}

	/**
	 * @param queryTable the queryTable to set
	 */
	public void setQueryTable(BaseQueryStoreTableComposite queryTable)
	{
		this.queryTable = queryTable;
	}
}

class DeleteQueryStoreAction 
	extends Action 
{
	private BaseQueryStoreTableComposite queryTable;
	
	public DeleteQueryStoreAction()
	{
		setId(EditQueryStoreAction.class.getName());
		setImageDescriptor(SharedImages.DELETE_16x16);
		setToolTipText("Delete Stored Query");
		setText("Delete");
	}
	
	@Override
	public void run()
	{
		if (queryTable == null || queryTable.isDisposed())
			return;
		
		BaseQueryStore<?, ?> store = queryTable.getFirstSelectedElement();
		if (store == null)
			return;
		
		boolean removed = QueryStoreDAO.sharedInstance().removeQueryStore(
			store, new NullProgressMonitor());
		
		if (removed)
		{
			Collection<BaseQueryStore<?, ?>> input = 
				(Collection<BaseQueryStore<?, ?>>) queryTable.getTableViewer().getInput();
			
			input.remove(store);
			queryTable.setInput(input);
		}
	}
	
	/**
	 * @param queryTable the queryTable to set
	 */
	public void setQueryTable(BaseQueryStoreTableComposite queryTable)
	{
		this.queryTable = queryTable;
	}
}
