package org.nightlabs.jfire.base.ui.overview.search;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.form.NightlabsFormsToolkit;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jdo.query.QueryMap;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jfire.base.ui.overview.AbstractEntryViewer;
import org.nightlabs.jfire.base.ui.overview.Entry;
import org.nightlabs.jfire.base.ui.overview.EntryViewer;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Base class for creating {@link EntryViewer}s which are responsible for searching
 * 
 * Subclasses must implement 4 Methods:
 * 
 * {@link #createResultComposite(Composite)} which creates a Composite
 * which displayes the result of the search
 * {@link #createSearchComposite(Composite)} which creates a Composite
 * which displayes the search criteria
 * {@link #displaySearchResult(Object)} must display the search result
 * passed to this method
 * {@link #getDefaultQuickSearchEntryFactory()} must return a
 * {@link QuickSearchEntry} which will be used by default
 * 
 * @param <R> the type of objects shown by my table.
 * @param <Q> the type of query I am insisting on. This tries to ensure some type safety, so that no
 * 	one can register an additional section from another plugin and add queries not related to a viewer
 * 	to bypass the safety barrier and view elements not intended to be seen by logged-in user.  
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class SearchEntryViewer<R, Q extends AbstractSearchQuery<? extends R>>
	extends AbstractEntryViewer
	implements QueryProvider<R, Q>
{
	public static final Logger logger = Logger.getLogger(SearchEntryViewer.class);
	
	public SearchEntryViewer(Entry entry) {
		super(entry);
		queryMap = new QueryMap<R, Q>();
	}
	
	/**
	 * A mapping from query type to all used queries.
	 * ? extends AbstractSearchQuery<? extends R>
	 */
	protected QueryMap<R, Q> queryMap;
	
	private XComposite searchWrapper = null;
	private SashForm sashform = null;
	private ToolItem searchItem = null;
	private ToolBar searchTextToolBar = null;
	
	public Composite createComposite(Composite parent)
	{
		sashform = new SashForm(parent, SWT.VERTICAL);
		IToolkit toolkit = new NightlabsFormsToolkit(Display.getDefault());
		
		searchWrapper = new XComposite(sashform, SWT.NONE, LayoutMode.TOP_BOTTOM_WRAPPER);
		searchWrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createToolBar(searchWrapper, toolkit);
		
		createAdvancedSearchSections(searchWrapper, toolkit);
		
		resultComposite = createResultComposite(sashform);
		
		if (parent.getLayout() instanceof GridLayout)
			sashform.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Form Look & Feel
		searchWrapper.setToolkit(toolkit);
		searchWrapper.adaptToToolkit();
		
		configureQuickSearchEntries(searchItem);
				
		// Context Menu
		menuManager = new MenuManager();
		Menu contextMenu = menuManager.createContextMenu(parent);
		resultComposite.setMenu(contextMenu);
				
		sashform.setWeights(calculateSashWeights(null));
		
		return sashform;
	}

	/**
	 * @param toolkit
	 */
	protected void createAdvancedSearchSections(Composite parent, IToolkit toolkit)
	{
		SortedSet<QueryFilterFactory> registeredComposites =
			QueryFilterCompositeRegistry.sharedInstance().getQueryFilterCompositesFor(getResultType());
		
		if (registeredComposites == null)
			return;
		
		Section advancedSearchSection;
		for (QueryFilterFactory factory : registeredComposites)
		{
			advancedSearchSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
			advancedSearchSection.setLayout(new GridLayout());
			advancedSearchSection.setText(factory.getSectionTitle());
			advancedSearchSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			advancedSearchSection.addExpansionListener(expansionListener);
			
			Button advancedSectionActiveButton = new Button(advancedSearchSection, SWT.CHECK);
			advancedSectionActiveButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite.activeButton.text")); //$NON-NLS-1$
			advancedSectionActiveButton.setSelection(false);
			
			advancedSearchSection.setTextClient(advancedSectionActiveButton);

			ScrolledComposite scrollComp = new ScrolledComposite(advancedSearchSection, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
			scrollComp.setExpandHorizontal(true);
			scrollComp.setExpandVertical(true);
			scrollComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			scrollComp.setLayout(XComposite.getLayout(LayoutMode.TOTAL_WRAPPER));
			AbstractQueryFilterComposite<? extends R, ? extends Q> filterComposite =
				factory.createQueryFilter(
					scrollComp, SWT.NONE, LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, this
					);
			filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			scrollComp.setContent(filterComposite);
			advancedSearchSection.setClient(scrollComp);
			scrollComp.setMinSize(filterComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			
			advancedSectionActiveButton.addSelectionListener( new ActiveButtonSelectionListener(advancedSearchSection, filterComposite) );
		}
	}
	
	public Composite getComposite() {
		if (sashform == null)
			throw new IllegalStateException("createComposite() was not called before getComposite()!"); //$NON-NLS-1$
		return sashform;
	}
		
	private ToolBarManager toolBarManager = null;
	public ToolBarManager getToolBarManager() {
		return toolBarManager;
	}
	
	private Spinner limit;
	
	/**
	 * creates the top toolbar including the search text, the search item where
	 * all the quickSearchEntries will be displayed, the limit spinner and
	 * an additional toolbar where custom actions can be added
	 * 
	 * @param searchComposite the parent Composite where the toolbar will be located in
	 * @param toolkit the toolkit to use
	 */
	protected void createToolBar(final Composite searchComposite, IToolkit toolkit)
	{
		XComposite toolBarWrapper = new XComposite(searchComposite, SWT.NONE,
				LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		toolBarWrapper.setToolkit(toolkit);
		toolkit.adapt(toolBarWrapper);
		
		searchTextToolBar = new ToolBar(toolBarWrapper, SWT.FLAT | SWT.WRAP | SWT.HORIZONTAL);
		searchTextToolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		XComposite wrapper = new XComposite(searchTextToolBar, SWT.NONE,
				LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.NONE, 2);
		GridLayout wrapperLayout = XComposite.getLayout(LayoutMode.TOP_BOTTOM_WRAPPER,
				wrapper.getGridLayout(), 2);
		wrapperLayout.marginTop = 3;
		wrapper.setLayout(wrapperLayout);
		wrapper.setToolkit(toolkit);
		Label searchLabel = new Label(wrapper, SWT.NONE);
		searchLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.searchLabel.text")); //$NON-NLS-1$
		searchText = new Text(wrapper, SWT.NONE);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchText.addSelectionListener(searchTextListener);
		searchText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent event)
			{
				QuickSearchEntry<?, ?> activeQuickSearchEntry = (QuickSearchEntry<?, ?>) activeMenuItem.getData();
				activeQuickSearchEntry.setSearchConditionValue(searchText.getText());
			}
		});
		ToolItem textItem = new ToolItem(searchTextToolBar, SWT.SEPARATOR);
		textItem.setControl(wrapper);
		textItem.setWidth(300);
		searchItem = new ToolItem(searchTextToolBar, SWT.DROP_DOWN);
		searchItem.setImage(SharedImages.SEARCH_16x16.createImage());
		searchItem.addSelectionListener(searchItemListener);
		wrapper.adaptToToolkit();
		
		XComposite rangeWrapper = new XComposite(searchTextToolBar, SWT.NONE,
				LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.NONE, 2);
		rangeWrapper.setToolkit(toolkit);
		new Label(rangeWrapper, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.limitLabel.text")); //$NON-NLS-1$
		limit = new Spinner(rangeWrapper, SWT.NONE);
		limit.setMinimum(0);
		limit.setMaximum(Integer.MAX_VALUE);
		limit.setSelection(0);
		ToolItem rangeItem = new ToolItem(searchTextToolBar, SWT.SEPARATOR);
		rangeItem.setControl(rangeWrapper);
		rangeItem.setWidth(400);
		rangeWrapper.adaptToToolkit();
		
		ToolBar toolBar = new ToolBar(toolBarWrapper, SWT.FLAT | SWT.WRAP | SWT.HORIZONTAL);
		toolBarManager = new ToolBarManager(toolBar);
		toolkit.adapt(toolBar);
		toolBar.setBackground( toolBarWrapper.getBackground() );
		searchTextToolBar.setBackground( toolBarWrapper.getBackground() );
//		toolBarWrapper.adaptToToolkit();
	}
		
	protected Text searchText = null;
	
	/**
	 * Implement this method for displaying the result of a search
	 * 
	 * @param parent the parent {@link Composite}
	 * @return a Composite which displays the result of a search
	 */
	public abstract Composite createResultComposite(Composite parent);
	
	protected abstract Class<R> getResultType();
	
	private Composite resultComposite;
	protected Composite getResultComposite() {
		return resultComposite;
	}
	
	/**
	 * performs a search with the current criteria
	 * 
	 * This is done by calling {@link QuickSearchEntry#search(ProgressMonitor)} of the
	 * current selected {@link QuickSearchEntry}
	 * 
	 * Furthermore the selected result ranges are set
	 * and after the search is done {@link #displaySearchResult(Object)} is called
	 */
	public void search()
	{
		new Job(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.JDOQuerySearchEntryViewer.job.name"))	//$NON-NLS-1$
		{
			@Override
			protected IStatus run(final ProgressMonitor monitor)
			{
				final Collection<R> result = doSearch(queryMap, monitor);
					Display.getDefault().syncExec(new Runnable()
					{
						public void run()
						{
							displaySearchResult(result);
						}
					});
//				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	
	protected abstract Collection<R> doSearch(QueryMap<R, ? extends Q> queryMap, ProgressMonitor monitor);

	/**
	 * will be called after the search of the current {@link QuickSearchEntry}
	 * {@link #searchEntryType} is done and the result should be displayed
	 * in the Composite returned by {@link #createResultComposite(Composite)}
	 * 
	 * @param result the search result to display
	 */
	public abstract void displaySearchResult(Object result);
	
	private MenuManager menuManager;
	public MenuManager getMenuManager() {
		return menuManager;
	}
	
	public ISelectionProvider getSelectionProvider() {
		if (resultComposite instanceof AbstractTableComposite) {
			AbstractTableComposite<?> tableComposite = (AbstractTableComposite<?>) resultComposite;
			return tableComposite.getTableViewer();
		}
		return null;
	}
	
	protected void configureSash(SashForm sashform) {
		int searchHeight = searchWrapper.getSize().y;
		int resultHeight = resultComposite.getSize().y;
		sashform.setWeights(new int[] {searchHeight, resultHeight});
	}
	
	public Composite createCategoryEntryComposite(Composite parent) {
		throw new UnsupportedOperationException("SearchEntryViewer does not support custom Composites" +
				"for the Category."); //$NON-NLS-1$
	}

	private SelectionListener searchItemListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
			if (e.detail != SWT.ARROW)
				search();
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	private SelectionListener searchTextListener = new SelectionAdapter()
	{
		@Override
		public void widgetDefaultSelected(SelectionEvent e)
		{
			search();
		}		
	};
	
	private IExpansionListener expansionListener = new ExpansionAdapter()
	{
		@Override
		public void expansionStateChanged(ExpansionEvent e)
		{
			final Section section = (Section) e.getSource();
			sashform.setWeights(calculateSashWeights(section));
			sashform.layout(true, true);
		}
	};
		
	/**
	 * @param expandedStateChangedSection the section that has changed its expanded state.
	 * @return the two weights for the search and the result composite.
	 */
	protected int[] calculateSashWeights(Section expandedStateChangedSection)
	{
		// Calculate the total space available for both composites (search comp and result comp) 
		int completeHeight;
		int searchHeight;
		if (expandedStateChangedSection == null)
		{
			// we need to initialise size values because these composites haven't been layed out correctly. 
			searchHeight = searchWrapper.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			// NOTE: we cannot compute the size of the sashform, since in the parent tree, the bounds/ available size are/is not known yet.
			completeHeight = sashform.getParent().getSize().y - sashform.getParent().getBorderWidth() - sashform.SASH_WIDTH;
		}
		else
		{
			completeHeight = sashform.getClientArea().height - sashform.SASH_WIDTH;
			searchHeight = searchWrapper.getSize().y;			
		}
		
		if (expandedStateChangedSection != null)
		{
			if (expandedStateChangedSection.isExpanded())
			{
				searchHeight += expandedStateChangedSection.getClient().getSize().y;				
			}
			else
			{
				searchHeight -= expandedStateChangedSection.getClient().getSize().y;				
			}
		}
		int resultHeight = completeHeight - searchHeight;
		return new int[] { searchHeight, resultHeight };
	}
	
	protected Menu quickSearchMenu;
	
	protected void configureQuickSearchEntries(final ToolItem searchItem)
	{
		if (getQuickSearchEntryFactories() == null || getQuickSearchEntryFactories().isEmpty())
			return;
		
		Collection<QuickSearchEntryFactory> quickSearchEntryFactories = getQuickSearchEntryFactories();
		quickSearchMenu = new Menu(RCPUtil.getActiveShell(), SWT.POP_UP);

		boolean isFirstElement = true;
		for (final QuickSearchEntryFactory quickSearchEntryFactory : quickSearchEntryFactories) {
			final MenuItem menuItem = new MenuItem(quickSearchMenu, SWT.CHECK);
			menuItem.setText(quickSearchEntryFactory.getName());
			menuItem.setImage(quickSearchEntryFactory.getImage());
			// FIXME: Wie kann man denn Factories aus einem ExtensionPoint erzeugen, die typisierte elemente liefern??? (marius)
			final QuickSearchEntry<R, ? extends Q> quickSearchEntry = quickSearchEntryFactory.createQuickSearchEntry();
			quickSearchEntry.setQueryProvider(this);
			menuItem.setData(quickSearchEntry);

			menuItem.addSelectionListener(dropDownMenuSelectionAdapter);
			
			// initialise the first query and set empty condition
			if (isFirstElement)
			{
				quickSearchEntry.setSearchConditionValue("");
			}
		}
		
		searchItem.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				if (event.detail == SWT.ARROW)
				{
					Rectangle rect = searchItem.getBounds();
					Point p = new Point(rect.x, rect.y + rect.height);
					p = searchItem.getParent().toDisplay(p);
					quickSearchMenu.setLocation(p.x, p.y);
					quickSearchMenu.setVisible(true);
				}
			}
		});

//		selectedQuickSearchEntry = firstEntry;
		activeMenuItem = quickSearchMenu.getItem(0);
		activeMenuItem.setSelection(true);
	}
	
	/**
	 * Pointer to the currently active MenuItem
	 */
	private MenuItem activeMenuItem = null;
	
	protected SelectionListener dropDownMenuSelectionAdapter = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			final MenuItem newSelectedItem = (MenuItem) e.getSource();
			// unset search condition of old selected element
			if (activeMenuItem != null)
			{
				QuickSearchEntry<?, ?> entry = (QuickSearchEntry<?, ?>) activeMenuItem.getData();
				entry.unsetSearchCondition();
			}
			
			// erase old selection
			for (MenuItem mi : newSelectedItem.getParent().getItems())
			{
				mi.setSelection(false);
			}

			// update selected search entry with possibly changed search text
			newSelectedItem.setSelection(true);
			QuickSearchEntry<?, ?> entry = (QuickSearchEntry<?, ?>) newSelectedItem.getData();
			entry.setSearchConditionValue(searchText.getText());
			
			// update last element pointer
			activeMenuItem = newSelectedItem;
			
			// start a new search
			search();
		}
	};
	
	/**
	 * Subclasses can return here their implementations of {@link QuickSearchEntryFactory}
	 * which can be used for searching
	 * 
	 * @return a List of {@link QuickSearchEntryFactory}s will can be used for quick searching
	 */
	protected Collection<QuickSearchEntryFactory> getQuickSearchEntryFactories()
	{
		Set<QuickSearchEntryFactory> factories =	
			QuickSearchEntryRegistry.sharedInstance().getFactories(this.getClass().getName());
		return factories;
	}
	
	protected class ActiveButtonSelectionListener
		extends SelectionAdapter
	{
		private Section correspondingSection;
		private AbstractQueryFilterComposite<? extends R,? extends Q> filterComposite;
		
		/**
		 * @param correspondingSection The section that corresponds to the button this listener is
		 * 	added to.
		 * @param filterComposite 
		 */
		public ActiveButtonSelectionListener(Section correspondingSection, AbstractQueryFilterComposite<? extends R,? extends Q> filterComposite)
		{
			assert correspondingSection != null;
			assert filterComposite != null;
			this.correspondingSection = correspondingSection;
			this.filterComposite = filterComposite;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			final Button b = (Button) e.getSource();
			final boolean active = b.getSelection();
			
			filterComposite.setActive(active);
			
			if (active != correspondingSection.isExpanded())
			{
				correspondingSection.setExpanded(active);
				sashform.setWeights(calculateSashWeights(correspondingSection));
			}
		}
	}
	
	// FIXME: What is this for?? Shouldn't the sections to the expansion themselves?
//	protected void expand(Section searchCriteriaSection) {
//		if (searchCriteriaSection.isExpanded())
//			return;
//		searchCriteriaSection.setExpanded(true);
//		doExpand(searchCriteriaSection);
//	}

//	public <ReqQuery extends AbstractSearchQuery<? extends R>> ReqQuery getQueryOfType(Class<ReqQuery> queryClass)
	@Override
	public <ReqQuery extends Q> ReqQuery getQueryOfType(Class<ReqQuery> queryClass)
	{
		return queryMap.getQueryOfType(queryClass);
	}
	
	public QueryCollection<R, Q> getManagedQueries()
	{
		return queryMap;
	}
	
	@Override
	public Class<? extends R> getBaseViewerClass()
	{
		return getResultType();
	}
}
