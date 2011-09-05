/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.nightlabs.base.ui.composite.AsyncInitComposite;
import org.nightlabs.base.ui.composite.AsyncInitListener;
import org.nightlabs.base.ui.composite.AsyncInitListenerList;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.query.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.ui.config.ConfigUtil;
import org.nightlabs.jfire.base.ui.prop.ILoadingStateListener;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewer;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewerFactory;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewerInputChangedListener;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;
import org.nightlabs.jfire.prop.dao.PropertySetDAO;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.ObjectCarrier;
import org.nightlabs.util.Util;

/**
 * A Composite that can be used as basis for searches for {@link PropertySet}s matching a set of
 * criteria or for objects that have a {@link PropertySet} as member.
 * <p>
 * The contents of this Composite are mostly configured by instances of
 * {@link PropertySetSearchEditLayoutConfigModule} that provide the configuration of the static
 * search part (see below) and the result-viewer-configuration. The concrete subclass of the
 * ConfigModule to use has to be provided in the constructor of this composite along with the
 * use-case for the ConfigModule. Note, that the use-case given in the constructor will be used to
 * build the cfModID of the ConfigModule (it will be prefixed with the client-type rcp).
 * </p>
 * <p>
 * The Composite constructs an upper area with a {@link TabFolder} for a static (simple) and a
 * dynamic (complex) search. Clients are responsible to create the static and dynamic filter
 * providers {@link #createStaticSearchFilterProvider(SearchResultFetcher)},
 * {@link #createDynamicSearchFilterProvider(SearchResultFetcher)}. To implement this methods there
 * are base-classes available. See {@link PropertySetSearchFilterProvider} and
 * {@link DynamicPropertySetSearchFilterProvider}.
 * </p>
 * <p>
 * Below the search filter TabFolder the Composite will create a so-called Button-bar, a simple
 * Composite that can be filled by clients with Widgets to trigger search or other custom actions (
 * {@link #getButtonBar()}).
 * </p>
 * <p>
 * Below the Button-bar the Composite will use the {@link PropertySetViewerRegistry} to build the
 * {@link IPropertySetViewer} configured in the ConfigModule for the given use-case. The viewer will
 * be available using the {@link #getResultViewer()} method.
 * </p>
 * <p>
 * As a default this composite will create a {@link SearchResultFetcher} that uses the
 * SearchFilterProvider and the Method
 * {@link PropertyManagerRemote#searchPropertySetIDs(PropSearchFilter)} in order to get the results
 * to display. If the {@link IPropertySetViewer} of this Composite implements
 * {@link SearchResultFetcher}, however, this implementation will be used as result-fetcher, when a
 * search is triggered.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @param <InputType> The InputType of the {@link IPropertySetViewer} used by this Composite.
 * @param <ElementType> The ElementType of the {@link IPropertySetViewer} used by this Composite.
 * @param <PropertySetType> The type of {@link PropertySet} the search should be performed for. This
 *            can be {@link PropertySet} itself or a subclass as well as as objects that are in an
 *            other way linked to a property set.
 */
public abstract class PropertySetSearchComposite<InputType, ElementType> extends XComposite implements AsyncInitComposite {

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PropertySetSearchComposite.class);

	public static final String[] FETCH_GROUPS_FULL_DATA = new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_FULL_DATA};

	private XComposite wrapper;
	private TabFolder filterProviderFolder;
	private XComposite buttonBar;
	private IPropertySetViewer<InputType, ElementType, PropertySetViewerConfiguration> resultViewer;
//	private PropertySetTable<PropertySetType> resultTable;
	private String earlySearchText;
	private XComposite resultLabelWrapper;
	private Label resultLabel;
	private ListenerList loadingStateListeners = new ListenerList();
	
	private Class<? extends PropertySetSearchEditLayoutConfigModule> configModuleClass;
	private String propertySetSearchUseCase;
	private boolean loadingCompleted = false;

	/**
	 * Create a new {@link PropertySetSearchComposite}.
	 *
	 * @param parent The parent to use.
	 * @param style The style to use.
	 * @param earlySearchText The initial search text (will be used to search right after )
	 * 		criteria and afterwards resolve the found objects via the cache.
	 * @param propertySetSearchUseCase String that references a use case for which a {@link PropertySetEditLayoutConfigModule}
	 * 		with the search configuration data has been registered.
	 */
	public PropertySetSearchComposite(
			Composite parent, int style, String earlySearchText, Class<? extends PropertySetSearchEditLayoutConfigModule> configModuleClass, String propertySetSearchUseCase) 
	{
		super(parent, style);
		this.earlySearchText = earlySearchText;
		this.propertySetSearchUseCase = propertySetSearchUseCase;
		this.configModuleClass = configModuleClass;
		init(this);
	}

	/**
	 * Interface for filter provider tabs
	 */
	private static interface FilterProviderTab {
		SearchFilterProvider getFilterProvider();
	}

	/**
	 * Static filter provider tab.
	 */
	private class StaticProviderTabItem implements FilterProviderTab {
		private TabItem tabItem;
		private SearchFilterProvider filterProvider;
		private Composite providerComposite;

		public StaticProviderTabItem(
				TabFolder parent,
				SearchResultFetcher resultFetcher
			)
		{
			tabItem = new TabItem(parent, SWT.NONE);
			filterProvider = createStaticSearchFilterProvider(resultFetcher);
			providerComposite = filterProvider.createComposite(parent);
			providerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			tabItem.setControl(providerComposite);
			tabItem.setData(this);
		}

		public TabItem getTabItem() {
			return tabItem;
		}
		public SearchFilterProvider getFilterProvider() {
			return filterProvider;
		}
	}

	private StaticProviderTabItem staticTab;

	/**
	 * Dynamic filter provider tab.
	 */
	private class DynamicProviderTabItem implements FilterProviderTab {
		private TabItem tabItem;
		private SearchFilterProvider filterProvider;
		private Composite providerComposite;

		public DynamicProviderTabItem(TabFolder parent, SearchResultFetcher resultFetcher) {
			tabItem = new TabItem(parent, SWT.NONE);
			filterProvider = createDynamicSearchFilterProvider(resultFetcher);
			providerComposite = filterProvider.createComposite(parent);
			providerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			tabItem.setControl(providerComposite);
			tabItem.setData(this);
		}
		public TabItem getTabItem() {
			return tabItem;
		}
		public SearchFilterProvider getFilterProvider() {
			return filterProvider;
		}
	}

	private DynamicProviderTabItem dynamicTab;

	/**
	 * The {@link SearchResultFetcher} for this Composite. This is either the result-viewer created
	 * for the composite (if it implements SearchResultFetcher), or otherwise an instance of
	 * {@link ResultFetcher}. that will search using the PropertyManagerRemote.
	 */
	private SearchResultFetcher resultFetcher;

	/**
	 * Default implementation of {@link SearchResultFetcher} that will be used if the
	 * {@link IPropertySetViewer} does not itself implement this interface.
	 * <p>
	 * This implementation uses {@link PropertyManagerRemote#searchPropertySetIDs(PropSearchFilter)}
	 * and the {@link PropertySetDAO}.
	 * </p>
	 */
	private class ResultFetcher implements SearchResultFetcher {
		public void searchTriggered(final SearchFilterProvider filterProvider) {
			Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.loadJob.title")) { //$NON-NLS-1$
				@SuppressWarnings("unchecked")
				@Override
				protected IStatus run(ProgressMonitor monitor) throws Exception {
					monitor.beginTask(getName(), 10);
					try {
						logger.debug("Search triggered, getting PersonManager"); //$NON-NLS-1$
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								resultViewer.setMessage(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.statusMessage.searching")); //$NON-NLS-1$
							}
						});

						PropertyManagerRemote propertyManager;
						try {
							propertyManager = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, Login.getLogin().getInitialContextProperties());
						} catch (LoginException e1) {
							throw new RuntimeException(e1);
						}

						logger.debug("Have PersonManager searching"); //$NON-NLS-1$
						final ObjectCarrier<PropSearchFilter> oc = new ObjectCarrier<PropSearchFilter>();
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								oc.setObject((PropSearchFilter) filterProvider.getSearchFilter());
							}
						});
						PropSearchFilter searchFilter = oc.getObject();

						try {
							long start = System.currentTimeMillis();
							final Set<PropertySetID> propIDs = new HashSet<PropertySetID>(propertyManager.searchPropertySetIDs(searchFilter));
							logger.debug("ID search for " + propIDs.size() + " entries took " + Util.getTimeDiffString(start)); //$NON-NLS-1$ //$NON-NLS-2$
							
							monitor.worked(1);
							
							getResultViewer().setInput((Collection<InputType>) propIDs, new SubProgressMonitor(monitor, 9));
						} catch (Exception e) {
							logger.error("Error searching person.",e); //$NON-NLS-1$
							throw new RuntimeException(e);
						}
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			loadJob.schedule();
		}
	}
	
	/**
	 * Dummy used to indicate that no result-viewer was found/configured.
	 */
	@SuppressWarnings("unchecked")
	private class ResultViewerMock implements IPropertySetViewer {
		private String resultViewerIdentifier;
		public ResultViewerMock(String resultViewerIdentifier) {
			this.resultViewerIdentifier = resultViewerIdentifier;
		}
		@Override
		public void addOpenListener(IOpenListener listener) {
		}
		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
		}
		@Override
		public Control createControl(Composite parent) {
			Label l = new Label(resultWrapper, SWT.WRAP);
			l.setLayoutData(new GridData(GridData.FILL_BOTH));
			l.setText("Could not find the IPropertySetViewerFactory configured as result viewer for this use-case. The viewer identifier is : " //$NON-NLS-1$
							+ resultViewerIdentifier);
			return l;
		}
		@Override
		public Collection getInput() {
			return Collections.emptySet();
		}
		@Override
		public Object getFirstSelectedElement() {
			return null;
		}
		@Override
		public Collection getSelectedElements() {
			return Collections.emptySet();
		}
		@Override
		public void loadDefaultConfiguration() {
		}
		@Override
		public void removeOpenListener(IOpenListener listener) {
		}
		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		}
		@Override
		public void setConfiguration(PropertySetViewerConfiguration config) {
		}
		@Override
		public void setInput(Collection input, ProgressMonitor monitor) {
		}
		@Override
		public void setMessage(String message) {
		}
		@Override
		public void addInputChangedListener(IPropertySetViewerInputChangedListener listener) {
		}
		@Override
		public void removeInputChangedListener(IPropertySetViewerInputChangedListener listener) {
		}
	}

	/**
	 * Performs a search with the current filter provider.
	 */
	public void performSearch() {
		final SearchFilterProvider filterProvider = ((FilterProviderTab) filterProviderFolder.getSelection()[0].getData())
				.getFilterProvider();
		resultFetcher.searchTriggered(filterProvider);
	}

	/**
	 * Creates the wrapping top-level {@link Composite}.
	 * This might be overridden by clients.
	 */
	protected Composite createWrapper(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		return wrapper;
	}

	/**
	 * Returns the wrapping top-level {@link Composite}.
	 * @return The wrapping top-level {@link Composite}.
	 */
	protected Composite getWrapper() {
		return wrapper;
	}

	/**
	 * Add a listener that is triggered when the UI of this {@link PropertySetSearchComposite} has been
	 * loaded completely. This method may only be called from the UI thread, otherwise an exception is thrown.
	 * @param listener The {@link ILoadingStateListener} to be added.
	 */
	public void addLoadingStateListener(final ILoadingStateListener listener) {
		if (Display.getCurrent() == null)
			throw new IllegalStateException("Thread mismatch. This method can only be called from the UI thread."); //$NON-NLS-1$

		loadingStateListeners.add(listener);

		if (loadingCompleted) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					listener.loadingCompleted();
				}
			});
		}
	}

	/**
	 * Removes the given listener. This method may only be called from the UI thread, otherwise an exception is thrown.
	 * @param listener The listener to be removed.
	 */
	public void removeLoadingStateListener(final ILoadingStateListener listener) {
		if (Display.getCurrent() == null)
			throw new IllegalStateException("Thread mismatch. This method can only be called from the UI thread."); //$NON-NLS-1$

		loadingStateListeners.remove(listener);
	}

	private XComposite topWrapper;

	private XComposite resultWrapper;

	private Label loadingLabel;

	public Composite getTopWrapper() {
		return topWrapper;
	}

	public Composite getResultWrapper() {
		return resultWrapper;
	}

	/**
	 * Creates the Composites children.
	 * @param parent The parent to use
	 * @return The wrapping top-level composite.
	 */
	protected Control init(Composite parent) {
		createWrapper(parent);
		loadingLabel = new Label(getWrapper(), SWT.NONE);
		loadingLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.loadingLabel.text")); //$NON-NLS-1$
		final Composite wrapper = getWrapper();
		final Display display = wrapper.getDisplay();

		Job loadCfModJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.loadCfModJob.title")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				final String cfModID = AbstractEditLayoutConfigModule.getCfModID(AbstractEditLayoutConfigModule.CLIENT_TYPE_RCP, getPropertySetSearchUseCase());
				final String[] fetchGroups = new String[] { FetchPlan.DEFAULT, PropertySetViewerConfiguration.FETCH_GROUP_CONFIG_DATA,
						AbstractEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT, PropertySetSearchEditLayoutConfigModule.FETCH_GROUP_RESULT_VIEWER_CONFIGURATIONS };
				final PropertySetSearchEditLayoutConfigModule personSearchConfigModule = ConfigUtil.getUserCfMod(configModuleClass,
						cfModID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (!wrapper.isDisposed()) {
							createSearchComposite(wrapper, personSearchConfigModule);

							initListeners.fireAsyncInitEvent(false);
						}
					}
				});

				return Status.OK_STATUS;
			}
		};

		loadCfModJob.schedule();

		return getWrapper();
	}
	
	private AsyncInitListenerList initListeners = new AsyncInitListenerList(this);
	
	@Override
	public void addAsyncInitListener(AsyncInitListener listener)
	{
		initListeners.add(listener);
	}
	
	@Override
	public void removeAsyncInitListener(AsyncInitListener listener)
	{
		initListeners.remove(listener);
	}

	/**
	 * Creates the actual contents of this Composite.
	 * 
	 * @param parent
	 * @param psCfMod
	 */
	@SuppressWarnings("unchecked")
	protected void createSearchComposite(Composite parent, PropertySetSearchEditLayoutConfigModule psCfMod) {
		if (loadingLabel != null)
			loadingLabel.dispose();

		SashForm sash = new SashForm(parent, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));
		topWrapper = new XComposite(sash, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		filterProviderFolder = new TabFolder(topWrapper, SWT.NONE);
		filterProviderFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		staticTab = new StaticProviderTabItem(filterProviderFolder, resultFetcher);
		staticTab.getTabItem().setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.staticTab.text")); //$NON-NLS-1$

		dynamicTab = new DynamicProviderTabItem(filterProviderFolder, resultFetcher);
		dynamicTab.getTabItem().setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.dynamicTab.text")); //$NON-NLS-1$

		buttonBar = new XComposite(topWrapper, SWT.NONE, XComposite.LayoutMode.LEFT_RIGHT_WRAPPER, XComposite.LayoutDataMode.GRID_DATA_HORIZONTAL);

		resultWrapper = new XComposite(sash, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		resultLabelWrapper = new XComposite(resultWrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		resultLabelWrapper.getGridData().verticalIndent = 5;
		resultLabelWrapper.getGridData().horizontalIndent = 5;
		resultLabelWrapper.getGridData().verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		resultLabelWrapper.getGridData().grabExcessVerticalSpace = false;
		resultLabel = new Label(resultLabelWrapper, SWT.NONE | SWT.WRAP);
		resultLabel.setLayoutData(new GridData());
		resultLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.resultLabel.text")); //$NON-NLS-1$

		// build the IPropertySetViewr for the results
		
		String resultViewerIdentifier = psCfMod.getResultViewerUiIdentifier();
		IPropertySetViewerFactory<InputType, ElementType, ? extends PropertySetViewerConfiguration> viewerFactory = PropertySetViewerRegistry.sharedInstance().getViewerFactory(resultViewerIdentifier);
		if (viewerFactory != null) {
			resultViewer = (IPropertySetViewer<InputType, ElementType, PropertySetViewerConfiguration>) viewerFactory.createViewer();
		}
		// If we were not able to create the result viewer we use the mock
		if (resultViewer == null) {
			resultViewer = new ResultViewerMock(resultViewerIdentifier);
		}
		
		resultViewer.setConfiguration((PropertySetViewerConfiguration) psCfMod.getResultViewerConfiguration(resultViewerIdentifier));
		
		resultViewer.addInputChangedListener(new IPropertySetViewerInputChangedListener() {
			@Override
			public void inputChanged(IPropertySetViewer<?, ?, ?> propertySetViewer) {
				setResultLabelText(propertySetViewer.getInput().size());
			}
		});

		// If the resultViewer created implements the SearchResultFetcher interface, 
		// we let the viewer fetch the results as needed

		Control resultViewerControl = resultViewer.createControl(resultWrapper);

		GridData tgd = new GridData(GridData.FILL_BOTH);
		tgd.heightHint = 300;
		resultViewerControl.setLayoutData(tgd);
		
		sash.setWeights(new int[] {3, 5});
		
		if (resultViewer instanceof SearchResultFetcher) {
			resultFetcher = (SearchResultFetcher) resultViewer;
		} else {
			//  As a fallback we provide a result-fetcher that will call
			//  PropertyManager#searchPropertySetIDs and resultViewer.setInput 
			resultFetcher = new ResultFetcher();
		}

		staticTab.getFilterProvider().setResultFetcher(resultFetcher);
		dynamicTab.getFilterProvider().setResultFetcher(resultFetcher);
		
		// If an early text was set apply it
		if (earlySearchText != null && !"".equals(earlySearchText)) { //$NON-NLS-1$
			resultFetcher.searchTriggered(staticTab.getFilterProvider());
		}
		
		loadingCompleted = true;

		// Trigger loading state listeners
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (Object listener : loadingStateListeners.getListeners())
					((ILoadingStateListener) listener).loadingCompleted();
			}
		});
	}

	private void setResultLabelText(final int itemCount) {
		if (!isDisposed() && !getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable(){
				@Override
				public void run()
				{
					if (!isDisposed()) {
						
						// int itemCount = resultViewer.getElements().size();
						// TODO: What is this, still needed?
//						if (resultTable.getElements().size() == 1 && resultTable.getElements().iterator().next()instanceof String) {
//							itemCount = 0;
//						}
						StringBuilder sb = new StringBuilder();
						sb.append(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.resultLabel.text")); //$NON-NLS-1$
						sb.append(" ("); //$NON-NLS-1$
						sb.append(itemCount);
						sb.append(" ");  //$NON-NLS-1$
						sb.append(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.label.searchResults")); //$NON-NLS-1$
						sb.append(")");  //$NON-NLS-1$

						resultLabel.setText(sb.toString());
						resultLabelWrapper.layout(new Control[]{resultLabel});
//						layout(true, true);
					}
				}
			});
		}
	}

	public void setQuickSearchText(String text) {
		earlySearchText = text;
	}

	public String getSearchText()
	{
		return earlySearchText;
	}

	/**
	 * Creates a Button in the given Composite that will trigger {@link #performSearch()}
	 * when selected.
	 * <p>
	 * This method is intended to be used by clients and is not called internally.
	 * </p>
	 * @param parent The parent to add the button to.
	 * @return The newly created Button.
	 */
	public Button createSearchButton(Composite parent) {
		Button searchButton = new Button(parent, SWT.PUSH);
		searchButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchComposite.searchButton.text")); //$NON-NLS-1$
		searchButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performSearch();
			}
		});
		return searchButton;
	}

	
	public IPropertySetViewer<InputType, ElementType, PropertySetViewerConfiguration> getResultViewer() {
		return resultViewer;
	}

	/**
	 * Get the Button-bar created for this Composite. This method blocks the UI thread as long
	 * as the contents of this composite are not completely loaded, but continues to run the event loop.
	 *
	 * @return The Button-bar created for this Composite.
	 */
	public Composite getButtonBar() {
		while (!isDisposed() && !loadingCompleted) {
//			if (!Display.getDefault().readAndDispatch())
//				Display.getDefault().sleep();
			if (!getDisplay().readAndDispatch())
				getDisplay().sleep();
		}

		return buttonBar;
	}

	protected String getPropertySetSearchUseCase() {
		return propertySetSearchUseCase;
	}

	public Class<? extends PropertySetSearchEditLayoutConfigModule> getConfigModuleClass() {
		return configModuleClass;
	}

	/**
	 * Create the static (simple) {@link SearchFilterProvider} that should be used in the first Tab.
	 * <p>
	 * A Base-class for the implementation of this method can be found in
	 * {@link PropertySetSearchFilterProvider}. (Btw. This class is also configured using instances
	 * of {@link PropertySetSearchEditLayoutConfigModule}).
	 * </p>
	 * 
	 * @param resultFetcher The {@link SearchResultFetcher} for the new provider.
	 * @return The newly created {@link SearchFilterProvider}.
	 */
	protected abstract SearchFilterProvider createStaticSearchFilterProvider(SearchResultFetcher resultFetcher);

	/**
	 * Create the dynamic (complex) {@link SearchFilterProvider} that should be used in the second
	 * Tab.
	 * <p>
	 * A base-class for the implementation of this method can be found in
	 * {@link DynamicPropertySetSearchFilterProvider}.
	 * </p>
	 * 
	 * @param resultFetcher The {@link SearchResultFetcher} for the new provider.
	 * @return The newly created {@link SearchFilterProvider}.
	 */
	protected abstract SearchFilterProvider createDynamicSearchFilterProvider(SearchResultFetcher resultFetcher);




}
