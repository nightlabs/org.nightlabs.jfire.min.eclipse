package org.nightlabs.jfire.base.ui.overview;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer;
import org.nightlabs.jfire.base.ui.querystore.BaseQueryStoreActiveTableComposite;
import org.nightlabs.jfire.base.ui.querystore.QueryStoreEditDialog;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * This is the composite that is embedded in an {@link QueryStoreCapableCategory} and consists of a
 * {@link DefaultCategoryComposite} and a stack of tables (one for each entry that supports the
 * following prerequisites) separated by a sash.
 * 
 * <p>For this Composite and hence the QueryStoreCapableCategory to work properly two prerequisites
 * 		have to be met:
 * 	<ul>
 * 		<li>The {@link EntryViewer} created by the Entry should be a {@link SearchEntryViewer}, 
 * 				otherwise no Table with stored QuerieStores will be created.</li>
 * 		<li>In order for the loading of the stored QueryStores to work, the IWorkbenchPart opened by
 * 				calling {@link Entry#handleActivation()} has to be an {@link OverviewEntryEditor}.</li>
 * 	</ul>
 * </p>
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
	
	protected LoadQueryStoreAction loadQueryStoreAction;
	protected EditQueryStoreAction editQueryStoreAction;
	protected DeleteQueryStoreAction deleteQueryStoreAction;

	private Map<Entry, FilteredQueryStoreComposite> entry2TableMap =
		new HashMap<Entry, FilteredQueryStoreComposite>();
	
	/**
	 * Listener that puts the corresponding {@link BaseQueryStoreActiveTableComposite} to the selected Entry
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
			FilteredQueryStoreComposite filteredTableComp = entry2TableMap.get(selectedEntry);
			if (filteredTableComp == null)
			{ 
				// if there is no SearchEntryViewer created by this entry -> we don't have table
				//  => disable the section.
				queryStoreSection.getSection().setVisible(false);
				sashForm.setMaximizedControl(elementListing);
			}
			else
			{
				final BaseQueryStoreActiveTableComposite table = filteredTableComp.getTable();
				
				// set the corresponding table to the top
				bringTableToTop( filteredTableComp );
				
				// trigger the BaseQueryStoreActiveController to fetch the input data if not already done.
				if (! filteredTableComp.isInitialised())
				{ 
					// only set input if not already initialised, afterwards the controller notifies and sets the table input itself
					table.load();
					filteredTableComp.setInitialised(true);
				}
				
				// set new table to actions
				if (loadQueryStoreAction != null)
				{
					loadQueryStoreAction.setQueryTable(filteredTableComp);
				}
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
		bringTableToTop( entry2TableMap.get(getCategory().getEntries().get(0)) );
		
		loadQueryStoreAction = new LoadQueryStoreAction();
		editQueryStoreAction = new EditQueryStoreAction();
		deleteQueryStoreAction = new DeleteQueryStoreAction();
		queryStoreSection.getToolBarManager().add(loadQueryStoreAction);
		queryStoreSection.getToolBarManager().add(editQueryStoreAction);
		queryStoreSection.getToolBarManager().add(deleteQueryStoreAction);
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
			
			final SearchEntryViewer<?, ?> searchEntryViewer = (SearchEntryViewer<?, ?>) viewer;
			
			// create the table
			FilteredQueryStoreComposite table =	new FilteredQueryStoreComposite(tableStackWrapper, entry,
				searchEntryViewer.getResultType());
			
			// update mapping
			entry2TableMap.put(entry, table);
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
 * Small wrapper that contains a button in checkbox-style for selecting whether only the user's
 * queries shall be shown and a {@link BaseQueryStoreActiveTableComposite}. <br />
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
	private BaseQueryStoreActiveTableComposite table;
	private final Entry entry;
	
	private boolean initialised = false;
	private Class<?> resultType;
	
	/**
	 * Simple Filter filters out all QueryStores not owned by the current user.
	 */
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
	
	public FilteredQueryStoreComposite(Composite parent, Entry entry, Class<?> resultType)
	{
		super(parent, SWT.NONE);
		assert entry != null;
		this.entry = entry;
		assert resultType != null;
		this.resultType = resultType;
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
		
		// modified table to only show the name and the owner + description in the tooltip.
		table = new BaseQueryStoreActiveTableComposite(parent,
			AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER, resultType)
		{
			@Override
			protected void createTableColumns(TableViewer tableViewer, Table table)
			{
				createNameColumn(tableViewer);
				createOwnerColumn(tableViewer);
			}
			
			@Override
			protected void setTableLayout(TableViewer tableViewer)
			{
				tableViewer.getTable().setLayout( new WeightedTableLayout(new int[] { 3, 1 }) );		
			}
		};
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
	public BaseQueryStoreActiveTableComposite getTable()
	{
		return table;
	}
	
	/**
	 * @return the entry
	 */
	public Entry getEntry()
	{
		return entry;
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
}

class EditQueryStoreAction 
	extends Action 
{
	private BaseQueryStoreActiveTableComposite queryTable;
	
	public EditQueryStoreAction()
	{
		setId(EditQueryStoreAction.class.getName());
		setImageDescriptor(SharedImages.EDIT_16x16);
		setToolTipText("Edit Stored Query");
		setText("Edit");
	}
	
	@SuppressWarnings("unchecked")
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
			BaseQueryStoreActiveTableComposite.FETCH_GROUP_BASE_QUERY_STORE,
			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, true, new NullProgressMonitor()
		);
		
		input.add(store);
		queryTable.setInput(input);
	}

	/**
	 * @param queryTable the queryTable to set
	 */
	public void setQueryTable(BaseQueryStoreActiveTableComposite queryTable)
	{
		this.queryTable = queryTable;
	}
}

class DeleteQueryStoreAction 
	extends Action 
{
	private BaseQueryStoreActiveTableComposite queryTable;
	
	public DeleteQueryStoreAction()
	{
		setId(EditQueryStoreAction.class.getName());
		setImageDescriptor(SharedImages.DELETE_16x16);
		setToolTipText("Delete Stored Query");
		setText("Delete");
	}
	
	@SuppressWarnings("unchecked")
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
	public void setQueryTable(BaseQueryStoreActiveTableComposite queryTable)
	{
		this.queryTable = queryTable;
	}
}

class LoadQueryStoreAction 
	extends Action
{
	private FilteredQueryStoreComposite filteredQueryComp;
	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(LoadQueryStoreAction.class);

	private static final String imagePath = "icons/overview/Overview-Load.16x16.png";
	
	public LoadQueryStoreAction()
	{
		setId(EditQueryStoreAction.class.getName());
		ImageDescriptor imageDesc = JFireBasePlugin.getImageDescriptor(imagePath);
		setImageDescriptor(imageDesc);
		setToolTipText("Load Stored Query");
		setText("Load");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		if (filteredQueryComp == null || filteredQueryComp.isDisposed())
			return;

		BaseQueryStore<?, ?> store = filteredQueryComp.getTable().getFirstSelectedElement();
		if (store == null)
			return;

		final IWorkbenchPart part = filteredQueryComp.getEntry().handleActivation();
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
		
		searchEntryViewer.getQueryProvider().loadQueries(store.getQueryCollection());
	}

	/**
	 * @param queryTable the queryTable to set
	 */
	public void setQueryTable(FilteredQueryStoreComposite filteredQueryComp)
	{
		this.filteredQueryComp = filteredQueryComp;
	}
}
