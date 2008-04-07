package org.nightlabs.jfire.base.ui.overview.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.eclipse.swt.widgets.Control;
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
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.DefaultQueryProvider;
import org.nightlabs.jdo.query.QueryCollection;
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
{
	public static final Logger logger = Logger.getLogger(SearchEntryViewer.class);
	
	public SearchEntryViewer(Entry entry) {
		super(entry);
		queryProvider = new DefaultQueryProvider<R, Q>(getResultType());
	}
	
	/**
	 * The element creating and providing the queries required by the UI. 
	 */
	protected QueryProvider<R, Q> queryProvider;
	
	private ScrolledComposite scrollableSearchWrapper;
	private XComposite toolbarAndAdvancedSearchWrapper = null;
	private SashForm sashform = null;
	private ToolItem searchItem = null;
	private ToolBar searchTextToolBar = null;
	
	public Composite createComposite(Composite parent)
	{
		sashform = new SashForm(parent, SWT.VERTICAL);
		IToolkit toolkit = XComposite.retrieveToolkit(parent);
		
		scrollableSearchWrapper = new ScrolledComposite(sashform, SWT.V_SCROLL);
		scrollableSearchWrapper.setExpandHorizontal(true);
		scrollableSearchWrapper.setExpandVertical(true);

		toolbarAndAdvancedSearchWrapper = new XComposite(scrollableSearchWrapper, SWT.NONE,
			LayoutMode.ORDINARY_WRAPPER);
		
		Control toolbar = createToolBar(toolbarAndAdvancedSearchWrapper, toolkit);
		toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createQuickSearchEntries(searchItem);
		createAdvancedSearchSections(toolbarAndAdvancedSearchWrapper, toolkit);
		scrollableSearchWrapper.setContent(toolbarAndAdvancedSearchWrapper);
		// TODO: Even though the min size is set for the width and the height, the scrollableSearchWrapper only shows scrollbars when not able to show the full height... why??? (marius)
		// When we have found a way to make the horizontal scrollbar visible, then we should add a resize listener to the scrollableSearchWrapper and set the new MinSize as long as it changes.
		scrollableSearchWrapper.setMinHeight(toolbarAndAdvancedSearchWrapper.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		scrollableSearchWrapper.setMinWidth(500);
		resultComposite = createResultComposite(sashform);
		
		// Form Look & Feel
		toolbarAndAdvancedSearchWrapper.setToolkit(toolkit);
		toolbarAndAdvancedSearchWrapper.adaptToToolkit();
		if (resultComposite instanceof XComposite)
		{
			final XComposite resultXComposite = (XComposite) resultComposite;
			resultXComposite.setToolkit(toolkit);
			resultXComposite.adaptToToolkit();
			
			if (resultXComposite instanceof AbstractTableComposite<?>)
			{
				// TODO: I don't know why, but since I changed the layout to the WeightedTableLayout, no scrollbar is shown for the table as well... wtf! (marius) 
				final AbstractTableComposite<?> tableComp = (AbstractTableComposite<?>) resultXComposite;
				final GridData tableData = (GridData) tableComp.getTableViewer().getTable().getLayoutData();
				tableData.minimumWidth = 500;
				tableData.minimumHeight = 200;
			}
		}
		else if (toolkit != null)
		{
			toolkit.adapt(resultComposite);
		}
		
		// Context Menu
		menuManager = new MenuManager();
		Menu contextMenu = menuManager.createContextMenu(parent);
		resultComposite.setMenu(contextMenu);
				
		sashform.setWeights(calculateSashWeights(null));
		
		return sashform;
	}

	/**
	 * List of all sections containing advanced search information.
	 */
	private List<Section> advancedSearchSections;
	
	/**
	 * @param parent the Composite to create the sections into.
	 * @param toolkit the toolkit to use for section creation, etc.
	 */
	protected void createAdvancedSearchSections(Composite parent, IToolkit toolkit)
	{
		SortedSet<QueryFilterFactory> queryFilterFactories = getQueryFilterFactories();
		
		if (queryFilterFactories == null || queryFilterFactories.isEmpty())
		{
			advancedSearchSections = Collections.emptyList();
			return;
		}
		
		advancedSearchSections = new ArrayList<Section>(queryFilterFactories.size());
		
		Section advancedSearchSection;
		Button advancedSectionActiveButton;
		final int sectionStyle = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE 
			| ExpandableComposite.CLIENT_INDENT;
		
		for (QueryFilterFactory factory : queryFilterFactories)
		{
			if (toolkit != null)
			{
				advancedSearchSection = toolkit.createSection(parent, sectionStyle);
			}
			else
			{
				advancedSearchSection = new Section(parent, sectionStyle);
			}
			advancedSearchSection.setLayout(new GridLayout());
			advancedSearchSection.setText(factory.getSectionTitle());
			advancedSearchSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			advancedSearchSection.addExpansionListener(expansionListener);
			
			advancedSectionActiveButton = new Button(advancedSearchSection, SWT.CHECK);
			advancedSectionActiveButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite.activeButton.text")); //$NON-NLS-1$
			advancedSectionActiveButton.setSelection(false);
			
			advancedSearchSection.setTextClient(advancedSectionActiveButton);

			AbstractQueryFilterComposite<? extends R, ? extends Q> filterComposite =
				factory.createQueryFilter(
					advancedSearchSection, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER,
					LayoutDataMode.GRID_DATA_HORIZONTAL, queryProvider
					);
			filterComposite.setSectionButtonActiveStateManager(
				new ActiveStateButtonManager(advancedSectionActiveButton) );
			advancedSearchSection.setClient(filterComposite);
			
			advancedSectionActiveButton.addSelectionListener(
				new ActiveButtonSelectionListener(advancedSearchSection, filterComposite) );
			advancedSearchSections.add(advancedSearchSection);
		}
	}
	
	/**
	 * Returns the SortedSet of QueryFilterFactories that will be used to create the advanced search
	 * sections.
	 * 
	 * @return the SortedSet of QueryFilterFactories that will be used to create the advanced search
	 * sections.
	 */
	protected SortedSet<QueryFilterFactory> getQueryFilterFactories()
	{
		return QueryFilterFactoryRegistry.sharedInstance().getQueryFilterCompositesFor(getResultType());
	}

	/**
	 * @return The sashform containing the search part (toolbar & advanced search sections) and the
	 * 	result composite (displaying the found elements).
	 */
	public Composite getComposite()
	{
		if (sashform == null)
			throw new IllegalStateException("createComposite() was not called before getComposite()!"); //$NON-NLS-1$
		
		return sashform;
	}
		
	private ToolBarManager toolBarManager = null;
	
	/**
	 * @return the ToolBarManager used for the search tool bar with the quick search entries.
	 */
	public ToolBarManager getToolBarManager()
	{
		return toolBarManager;
	}
	
	protected Text searchText = null;
	
	private Spinner limit;
	
	/**
	 * creates the top toolbar including the search text, the search item where
	 * all the quickSearchEntries will be displayed, the limit spinner and
	 * an additional toolbar where custom actions can be added
	 * 
	 * @param searchComposite the parent Composite where the toolbar will be located in
	 * @param toolkit the toolkit to use
	 * @return the control representing the toolbar in the header of this viewer.
	 */
	protected Control createToolBar(final XComposite searchComposite, IToolkit toolkit)
	{
		XComposite toolBarWrapper = new XComposite(searchComposite, SWT.NONE,
				LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE, 5);
			
		int toolbarStyle = SWT.WRAP | SWT.HORIZONTAL;
		if (toolkit != null)
		{
			toolbarStyle |= SWT.FLAT;
		}
		int borderStyle = searchComposite.getBorderStyle();
		
		XComposite quickSeachTextComp = new XComposite(toolBarWrapper, SWT.NONE,
				LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.NONE, 2);
		GridLayout gridLayout = quickSeachTextComp.getGridLayout();
		gridLayout.marginLeft = gridLayout.marginWidth;
		gridLayout.marginWidth = 0;
		gridLayout.marginRight = 0;
		Label searchLabel = new Label(quickSeachTextComp, SWT.NONE);
		searchLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.searchLabel.text")); //$NON-NLS-1$
		searchText = new Text(quickSeachTextComp, borderStyle);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = 200;
		searchText.setLayoutData(gridData);
		searchText.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				search();
			}		
		});
		searchText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent event)
			{
				if (activeMenuItem == null)
					return;
				
				QuickSearchEntry<?, ?> activeQuickSearchEntry = (QuickSearchEntry<?, ?>) activeMenuItem.getData();
				activeQuickSearchEntry.setSearchConditionValue(searchText.getText());
			}
		});
		
		searchTextToolBar = new ToolBar(toolBarWrapper, toolbarStyle);
		
		searchItem = new ToolItem(searchTextToolBar, SWT.DROP_DOWN);
		searchItem.setImage(SharedImages.SEARCH_16x16.createImage());
		searchItem.addSelectionListener(searchItemListener);
		
		XComposite rangeWrapper = new XComposite(toolBarWrapper, SWT.NONE,
				LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.NONE, 2);
		Label limitLabel = new Label(rangeWrapper, SWT.NONE);
		limitLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.limitLabel.text")); //$NON-NLS-1$
		limitLabel.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.limitLabel.tooltip")); //$NON-NLS-1$
		
		limit = new Spinner(rangeWrapper, borderStyle);
		limit.setMinimum(0);
		limit.setMaximum(Integer.MAX_VALUE);
		limit.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.limitLabel.tooltip")); //$NON-NLS-1$
		limit.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				queryProvider.getManagedQueries().setToExclude( limit.getSelection() );
			}
		});
		limit.setSelection(25);
		queryProvider.getManagedQueries().setToExclude(25);
			
		Label spacerLabel = new Label(toolBarWrapper, SWT.NONE);
		spacerLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		ToolBar toolBar = new ToolBar(toolBarWrapper, toolbarStyle);
		GridData actionToolBarData = new GridData(SWT.END, SWT.FILL, false, true);
		actionToolBarData.minimumWidth = 200;
		toolBar.setLayoutData(actionToolBarData);
		toolBarManager = new ToolBarManager(toolBar);
		if (toolkit != null)
		{
			toolBarWrapper.setToolkit(toolkit);
			toolBarWrapper.adaptToToolkit();
			quickSeachTextComp.setToolkit(toolkit);
			quickSeachTextComp.adaptToToolkit();
			rangeWrapper.setToolkit(toolkit);
			rangeWrapper.adaptToToolkit();
			toolkit.adapt(toolBar);
		}
		
		return toolBarWrapper;
	}
		
	/**
	 * Implement this method for displaying the result of a search
	 * 
	 * @param parent the parent {@link Composite}
	 * @return a Composite which displays the result of a search
	 */
	public abstract Composite createResultComposite(Composite parent);
	
	public abstract Class<R> getResultType();
	
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
		new Job(Messages.getString("org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer.searchJob.name"))	//$NON-NLS-1$
		{
			@Override
			protected IStatus run(final ProgressMonitor monitor)
			{
				final Collection<R> result = doSearch(queryProvider.getManagedQueries(), monitor);
					Display.getDefault().syncExec(new Runnable()
					{
						public void run()
						{
							displaySearchResult(result);
						}
					});
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	
	/**
	 * The actual search should be done here. It is advised to do it via the DAOs.
	 * 
	 * @param queryMap the {@link QueryCollection} containing all queries managed by this viewer.
	 * @param monitor the monitor to show the progress.
	 * @return a collection of all elements matching the cascaded queries of the <code>queryMap</code>.
	 */
	protected abstract Collection<R> doSearch(QueryCollection<R, ? extends Q> queryMap, ProgressMonitor monitor);

	/**
	 * will be called after the search of the current {@link QuickSearchEntry}
	 * {@link #searchEntryType} is done and the result should be displayed
	 * in the Composite returned by {@link #createResultComposite(Composite)}
	 * 
	 * @param result the search result to display
	 */
	protected abstract void displaySearchResult(Object result);
	
	private MenuManager menuManager;
	public MenuManager getMenuManager() {
		return menuManager;
	}
	
	public ISelectionProvider getSelectionProvider()
	{
		if (resultComposite instanceof AbstractTableComposite)
		{
			AbstractTableComposite<?> tableComposite = (AbstractTableComposite<?>) resultComposite;
			return tableComposite.getTableViewer();
		}
		return null;
	}
	
	private SelectionListener searchItemListener = new SelectionAdapter()
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (e.detail != SWT.ARROW)
				search();
		}
	};
	
	/**
	 * Listener used to adapt the sash weights to the new size of the search composite and
	 * sets the minimum size of the search composite so that the scrollbars will be shown correctly.
	 */
	protected IExpansionListener expansionListener = new ExpansionAdapter()
	{
		@Override
		public void expansionStateChanged(ExpansionEvent e)
		{
			final Section section = (Section) e.getSource();
			sashform.setWeights(calculateSashWeights(section));
			// This is not enough since the returned size is not the actual one if the searchWrapper is
			// completely visible (e.g. when it grows too big for the sash itself, then the weights are
			// not changed and the searchWrapper's returned size is only its visible part) 
//			scrollableSearchWrapper.setMinHeight(scrollableSearchWrapper.getSize().y);
			
			// We need the -1 otherwise scrollbars are sometimes visible. Another magical number...
			scrollableSearchWrapper.setMinHeight(calculateSearchAreaHeight()-1);
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
			searchHeight = scrollableSearchWrapper.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			if (searchHeight < 0)
			{
				searchHeight = 100;
			}
			// NOTE: we cannot compute the size of the sashform, since in the parent tree, the bounds/ available size are/is not known yet.
//			completeHeight = sashform.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT).y - sashform.SASH_WIDTH;
			completeHeight = sashform.getParent().getSize().y - sashform.getParent().getBorderWidth() - sashform.SASH_WIDTH;
			if (completeHeight <= 0)
			{
				completeHeight = 5 * searchHeight;
			}
		}
		else
		{
//			scrollableSearchWrapper.layout(true, true);
			completeHeight = sashform.getClientArea().height - sashform.SASH_WIDTH;
			searchHeight = calculateSearchAreaHeight();
//			searchHeight = scrollableSearchWrapper.getSize().y; // computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}
		
		
		// only move the sash if after the adjustment of the sash, the result composite 
		// still has some height left (at least 50 pixels).
		if (searchHeight + 50 > completeHeight)
		{
			return sashform.getWeights();
		}
		
		int resultHeight = completeHeight - searchHeight;
		return new int[] { searchHeight, resultHeight };
	}

	/**
	 * @return the complete height of the search area.
	 */
	private int calculateSearchAreaHeight()
	{
		int searchHeight = toolBarManager.getControl().getSize().y;
		
		// spacing used as a buffer for minimum area heights
		final int verticalSpacing;
		if (toolbarAndAdvancedSearchWrapper.getLayout() instanceof GridLayout)
		{
			GridLayout gridLayout = (GridLayout) toolbarAndAdvancedSearchWrapper.getLayout();
			verticalSpacing = gridLayout.verticalSpacing;
		}
		else
		{
			verticalSpacing = 10;
		}

		for (Section section : advancedSearchSections)
		{
			searchHeight += section.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}
		
		// add the spacing in between the sections
		searchHeight += advancedSearchSections.size() * verticalSpacing;
		searchHeight += advancedSearchSections.size() * 5; //magical spacing needed due to section spacings.
		return searchHeight;
	}
	
	/**
	 * Reference to the menu containing all quick search items.
	 */
	protected Menu quickSearchMenu;
	
	/**
	 * Pointer to the currently active MenuItem
	 */
	private MenuItem activeMenuItem = null;
	
	/**
	 * Gets all registered {@link QuickSearchEntryFactory}s and adds the entries created by these 
	 * factories to the {@link #quickSearchMenu}.
	 * 
	 * @param searchItem the ToolBar item that acts as the button to trigger the drop down list of
	 * 	quick search entries.
	 */
	protected void createQuickSearchEntries(final ToolItem searchItem)
	{
		if (getQuickSearchEntryFactories() == null || getQuickSearchEntryFactories().isEmpty())
			return;
		
		Collection<QuickSearchEntryFactory> quickSearchEntryFactories = getQuickSearchEntryFactories();
		quickSearchMenu = new Menu(RCPUtil.getActiveShell(), SWT.POP_UP);

		for (final QuickSearchEntryFactory quickSearchEntryFactory : quickSearchEntryFactories) {
			final MenuItem menuItem = new MenuItem(quickSearchMenu, SWT.CHECK);
			menuItem.setText(quickSearchEntryFactory.getName());
			menuItem.setImage(quickSearchEntryFactory.getImage());
			// FIXME: Wie kann man denn Factories aus einem ExtensionPoint erzeugen, die typisierte elemente liefern??? (marius)
			final QuickSearchEntry<R, ? extends Q> quickSearchEntry = quickSearchEntryFactory.createQuickSearchEntry();
			quickSearchEntry.setQueryProvider(queryProvider);
			menuItem.setData(quickSearchEntry);

			menuItem.addSelectionListener(dropDownMenuSelectionAdapter);
			
			// initialise the first query and set empty condition
			if (quickSearchEntryFactory.isDefault())
			{
				if (activeMenuItem != null) // there is already an item declared as default.
				{
					logger.warn("There is already a quick search entry marked as default! This entry wit id="+
						quickSearchEntryFactory.getId() + " and with name=" +quickSearchEntryFactory.getName() +
					" is also declared as 'default'! This declaration is ignored.");
				}
				else
				{
					quickSearchEntry.setSearchConditionValue("");
					activeMenuItem = menuItem;
					activeMenuItem.setSelection(true);
				}
			}
		}
		
		if (activeMenuItem == null && quickSearchMenu.getItems().length > 0)
		{
			activeMenuItem = quickSearchMenu.getItem(0);
			activeMenuItem.setSelection(true);
			((QuickSearchEntry)activeMenuItem.getData()).setSearchConditionValue("");
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
	}
	
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
	protected SortedSet<QuickSearchEntryFactory> getQuickSearchEntryFactories()
	{
		return QuickSearchEntryRegistry.sharedInstance().getFactories(this.getClass().getName());
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
		public ActiveButtonSelectionListener(Section correspondingSection,
			AbstractQueryFilterComposite<? extends R,? extends Q> filterComposite)
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
				
				// This needs to be triggered manually, since the setExpanded(boolean) won't trigger the
				// notification of its listeners.
				final ExpansionEvent event = new ExpansionEvent(correspondingSection, active);
				expansionListener.expansionStateChanged(event);
			}
		}
	}
	
	/**
	 * Returns the collection of queries managed by this viewer.  
	 * @return the collection of queries managed by this viewer.
	 */
	public QueryCollection<R, Q> getManagedQueries()
	{
		return queryProvider.getManagedQueries();
	}

	/**
	 * @return the queryProvider
	 */
	public QueryProvider<R, Q> getQueryProvider()
	{
		return queryProvider;
	}
	
}
