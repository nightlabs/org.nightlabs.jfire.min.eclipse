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
//	private ScrolledComposite scrollComp = null;
	private ToolItem searchItem = null;
	private ToolBar searchTextToolBar = null;
//	private Section searchCriteriaSection = null;
	
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
				
//		configureSash(sashform);
		sashform.layout(true, true);
		sashform.setWeights(calculateSashWeights(null));
		sashform.layout(true, true);
		
//		searchEntryType = getDefaultQuickSearchEntryFactory().createQuickSearchEntry();
		
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
			configureSection(advancedSearchSection, factory);
//			advancedSearchSection.setLayout(new GridLayout());
//			advancedSearchSection.setText(factory.getSectionTitle());
//			advancedSearchSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			advancedSearchSection.addExpansionListener(expansionListener);
			
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
		}
		
//		searchCriteriaSection = toolkit.createSection(searchWrapper, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
//		searchCriteriaSection.setLayout(new GridLayout());
//		searchCriteriaSection.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.searchCriteriaSection.text")); //$NON-NLS-1$
//		searchCriteriaSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		searchCriteriaSection.addExpansionListener(expansionListener);
//		configureSection(searchCriteriaSection);
		
//		scrollComp = new ScrolledComposite(searchCriteriaSection, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
//		scrollComp.setExpandHorizontal(true);
//		scrollComp.setExpandVertical(true);
//		scrollComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		scrollComp.setLayout(new GridLayout());
//		searchComposite = createSearchComposite(scrollComp);
//		searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		scrollComp.setContent(searchComposite);
//		searchCriteriaSection.setClient(scrollComp);
//		scrollComp.setMinSize(searchComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
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
	
//	/**
//	 * Implement this method for displaying the search criteria
//	 * 
//	 * @param parent the parent {@link Composite}
//	 * @return a Composite which displays the search criteria
//	 */
//	public abstract Composite createSearchComposite(Composite parent);
	
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
//				final QuickSearchEntry entry = getActiveQuickSearchEntry();
//				
//				if (entry != null) {
//					final Object result = entry.search(monitor);
//					if (result instanceof AbstractJDOQuery)
//						throw new IllegalStateException("QuickSearchEntry.search(...) of class " + entry.getClass().getName() + " is implemented incorrectly! It should return a result - not a query!");

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
	
//	private QuickSearchEntry selectedQuickSearchEntry;
	
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
//			final Button activeButton = (Button) section.getTextClient();
//			activeButton.setSelection(section.isExpanded());
//			doExpand(section);
			sashform.setWeights(calculateSashWeights(section));
			sashform.layout(true, true);
		}
	};
		
//	protected void doExpand(Section searchCriteriaSection)
//	{
//		if (initalSearchHeight == -1) {
//			initalSearchHeight = searchWrapper.getSize().y;
//		}
//		int completeHeight = sashform.getSize().y;
//		int searchHeight = initalSearchHeight;
//		if (searchCriteriaSection.isExpanded())
//		{
//			searchHeight += searchCriteriaSection.getSize().y;
//		}
//		int resultHeight = completeHeight - searchHeight;
//		sashform.setWeights(new int[] { searchHeight, resultHeight });
//		sashform.layout(true, true);
////		RCPUtil.setControlEnabledRecursive(searchTextToolBar, !advancedSectionActiveButton.getSelection());
////		RCPUtil.setControlEnabledRecursive(searchComposite, advancedSectionActiveButton.getSelection());
//	}
	
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
	
//	/**
//	 * Subclasses can return here their implementations of {@link QuickSearchEntryFactory}
//	 * which can be used for searching
//	 *
//	 * @return a List of {@link QuickSearchEntryFactory}s will can be used for quick searching
//	 */
//	protected List<QuickSearchEntryFactory> getQuickSearchEntryFactories() {
//		return Collections.EMPTY_LIST;
//	}
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
	
//	protected QuickSearchEntry getActiveQuickSearchEntry() {
//		final QuickSearchEntry[] activeEntry = new QuickSearchEntry[1];
//		Display.getDefault().syncExec(new Runnable() {
//			@Override
//			public void run() {
//				QuickSearchEntry entry;
//				if (advancedSectionActiveButton.getSelection())
//					entry = getQuickSearchEntryFactory().createQuickSearchEntry();
//				else {
//					entry = selectedQuickSearchEntry;					
//					entry.setSearchConditionValue(searchText.getText());					
//				}
//				
//				if (limit.getSelection() > 0)
//					entry.setResultRange(0, limit.getSelection());
//				else
//					entry.setResultRange(0, Long.MAX_VALUE);
//				
//				activeEntry[0] = entry;
//			}
//		});
//		
//		return activeEntry[0];
//	}
	
//	/**
//	 * Subclasses must implement this method to return at least onejop
//	 * {@link QuickSearchEntryFactory} which will be used for searching by default
//	 * 
//	 * @return the {@link QuickSearchEntryFactory} which is used by this implementation by default
//	 */
//	protected abstract QuickSearchEntryFactory getDefaultQuickSearchEntryFactory();
	
	/**
	 * Subclasses must implement this method to return the {@link QuickSearchEntryFactory} that
	 * should be used when searching with the advanced search section.
	 * @param factory 
	 * 
	 * @return The {@link QuickSearchEntryFactory} which is used when searching through the advanced section.
	 */
	// FIXME: This shouldn't be a factory but the Factories from the registry that define the advanced search options.
//	protected abstract QuickSearchEntryFactory getQuickSearchEntryFactory();
	
//	/**
//	 * Subclasses my override this method to return the default search entry factory
//	 * by default the {@link QuickSearchEntryFactory} returned by
//	 * {@link #getAdvancedQuickSearchEntryFactory()} is used
//	 */
//	protected QuickSearchEntryFactory getDefaultSearchEntryFactory() {
//		return getAdvancedQuickSearchEntryFactory();
//	}
		
	protected void configureSection(final Section section, QueryFilterFactory<?,?> factory)
	{
		Button advancedSectionActiveButton = new Button(section, SWT.CHECK);
		advancedSectionActiveButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite.activeButton.text")); //$NON-NLS-1$
		advancedSectionActiveButton.setSelection(false);
//		advancedSectionActiveButton.setEnabled(false);
		section.setLayout(new GridLayout());
		section.setText(factory.getSectionTitle());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.addExpansionListener(expansionListener);

		advancedSectionActiveButton.addSelectionListener( new ActiveButtonSelectionListener(section) );
//			new SelectionAdapter()
//		{
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				final Button b = (Button) e.getSource();
////				section.setExpanded(b.getSelection());
////				doExpand();
////				RCPUtil.setControlEnabledRecursive(searchTextToolBar, !b.getSelection());
////				RCPUtil.setControlEnabledRecursive(searchComposite, b.getSelection());
////				searchTextToolBar.setEnabled(!b.getSelection());
//			}
//		});
		section.setTextClient(advancedSectionActiveButton);
	}

	protected class ActiveButtonSelectionListener
		extends SelectionAdapter
	{
		private Section correspondingSection;
		
		/**
		 * @param correspondingSection The section that corresponds to the button this listener is
		 * 	added to.
		 */
		public ActiveButtonSelectionListener(Section correspondingSection)
		{
			assert correspondingSection != null;
			this.correspondingSection = correspondingSection;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			final Button b = (Button) e.getSource();
			if (b.getSelection() != correspondingSection.isExpanded())
			{
				correspondingSection.setExpanded(b.getSelection());
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
}
