package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.nightlabs.base.ui.composite.ListComposite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite;
import org.nightlabs.clientui.ui.layout.IGridLayoutConfig;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jfire.base.ui.config.IConfigModuleController;
import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutPreferencePage;
import org.nightlabs.jfire.base.ui.person.search.config.StructFieldSearchGridLayoutConfig;
import org.nightlabs.jfire.base.ui.prop.search.PropertySetViewerRegistry;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewerFactory;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.search.config.StructFieldSearchEditLayoutEntry;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

/**
 * Base-class for the implementation of a PreferencePage that edits a config-module of type
 * {@link PropertySetSearchEditLayoutConfigModule}. This class will do most of the work, subclasses
 * will only have to define the concrete sub-class of the config-module to use as well as the
 * cfModID to use (which defines the client-type and use-case for that configuration). Additionally
 * sub-classes have to provide some descriptive Strings in {@link #getUseCaseDescription()} and
 * {@link #getLayoutConfigTabText()} (<- note that this method is not abstract but has to be
 * implemented).
 * <p>
 * A {@link StructFieldSearchGridLayoutConfig} will be used to implement the configuration of the
 * search-fields in its first tab. (see {@link #createConfigModuleGridLayoutConfig()})
 * </p>
 * <p>
 * A second tab will be added that will be used to configure the result-viewer for the concrete
 * use-case. This tab will use the {@link PropertySetViewerRegistry} to find appropriate
 * {@link IPropertySetViewerFactory}s that will create the result-viewer configuration and visualize
 * its editing.
 * </p>
 * 
 * @author Tobias Langner
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@SuppressWarnings("unchecked")
public abstract class AbstractPropertySetSearchPreferencePage extends AbstractEditLayoutPreferencePage {

	/**
	 * Dialog used to select appropriate {@link IPropertySetViewerFactory}s for the current
	 * use-case.
	 */
	private class ViewerFactoryDialog extends ResizableTitleAreaDialog {

		public ViewerFactoryDialog(Shell shell) {
			super(shell, null);
		}

		private IPropertySetViewerFactory selectedFactory;

		@Override
		protected Control createDialogArea(Composite parent) {
			setMessage("Selet a type of result-viewer to add to the configuration");
			final ListComposite<IPropertySetViewerFactory> viewerFactoryList = new ListComposite<IPropertySetViewerFactory>(parent,
					SWT.BORDER);
			viewerFactoryList.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					selectedFactory = viewerFactoryList.getSelectedElement();
					if (selectedFactory != null) {
						close();
					}
				}
			});
			viewerFactoryList.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent arg0) {
					selectedFactory = viewerFactoryList.getSelectedElement();
				}
			});
			viewerFactoryList.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((IPropertySetViewerFactory) element).getDescription();
				}
			});

			viewerFactoryList.setInput(getAddableFactories());
			return viewerFactoryList;
		}

		@Override
		protected void okPressed() {
			if (selectedFactory != null) {
				super.okPressed();
			}
		}

		public IPropertySetViewerFactory getSelectedFactory() {
			return selectedFactory;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Choose result-viewer type");
		}
	}

	private XComposite configControlWrapper;
	/**
	 * Control for the current viewer-config, disposed and re-creaed when changing viewer-configs
	 */
	private Control currentViewerConfigurationControl;
	/**
	 * Combo showing all viewer-configurations currently in the config-module 
	 */
	private XComboComposite<IPropertySetViewerFactory> viewerFactoryCombo;
	/**
	 * Remembered to apply the current editing-state when viewer-configs are changed 
	 */
	private IPropertySetViewerFactory previouslySelectedFactory;

	/**
	 * All IPropertySetViewerFactories loaded from the registry. The list of the the ones configured
	 * for the config-module is taken from the config-module itself.
	 */
	private Map<String, IPropertySetViewerFactory> viewerFactories;
	/**
	 * Those factories out of all possible factories that are configured for the config-module of
	 * this page
	 */
	private Collection<IPropertySetViewerFactory> selectedFactories;

	/**
	 * Create a new {@link AbstractPropertySetSearchPreferencePage}.
	 */
	protected AbstractPropertySetSearchPreferencePage() {
		super(true);
	}

	/**
	 * Sub-classes have to return the concrete sub-class of
	 * {@link PropertySetEditLayoutConfigModule} the operate on. This defines which ConfigModule is
	 * downloaded an thus which search-types and viewer-types are valid.
	 * 
	 * @return The concrete sub-class of the {@link PropertySetEditLayoutConfigModule} to use.
	 */
	protected abstract Class<? extends PropertySetEditLayoutConfigModule> getConfigModuleClass();

	/**
	 * Sub-classes have to return a String made up from a client-type and use-case for the
	 * config-module to download. A convenience method for implemenations is
	 * {@link AbstractEditLayoutConfigModule#getCfModID(String, String)}. This defines exactly
	 * (besides the config-module class) which config-module is downloaded for this preference-page.
	 * 
	 * @return The cfModID-String defining the client-type and use-case for the preference-page to
	 *         download.
	 */
	protected abstract String getConfigModuleID();

	/**
	 * @return The config-module of this pages controller casted to {@link PropertySetSearchEditLayoutConfigModule}.
	 */
	protected PropertySetSearchEditLayoutConfigModule getPropertySetSearchConfigModule() {
		return (PropertySetSearchEditLayoutConfigModule) getConfigModuleController().getConfigModule();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Adds another tab to the top level tab folder that allows to configure the result viewer.
	 * </p>
	 */
	@Override
	protected void createPreferencePage(Composite parent) {
		super.createPreferencePage(parent);

		TabFolder tabFolder = getTabFolder();
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Result viewer configuration");

		tabItem.setControl(createResultViewerConfigurationControl(tabFolder));
	}

	/**
	 * Creates the ui to edit the result-viewer configuration. This is the combo for switching the
	 * viewer-types, the buttons to add and remove configurations and a place-holder where the ui
	 * for the viewer-types selected in the combo will be place into.
	 * 
	 * @param parent The parent to add the control to.
	 * @return The newly created ui.
	 */
	protected Control createResultViewerConfigurationControl(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA);

		XComposite headerRow = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		headerRow.getGridLayout().numColumns = 3;
		headerRow.getGridLayout().makeColumnsEqualWidth = false;
		viewerFactoryCombo = new XComboComposite<IPropertySetViewerFactory>(headerRow, SWT.READ_ONLY);
		viewerFactoryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		viewerFactoryCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IPropertySetViewerFactory) element).getName();
			}
		});
		viewerFactoryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				viewerFactorySelected();
			}
		});

		Button addConfigurationButton = new Button(headerRow, SWT.PUSH);
		addConfigurationButton.setText("+");
		addConfigurationButton.setToolTipText("Add a configuration for a result viewer.");
		addConfigurationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPropertySetViewerFactory factory = null;
				if (getAddableFactories().size() > 0) {
					ViewerFactoryDialog dlg = new ViewerFactoryDialog(e.display.getActiveShell());
					if (dlg.open() == Window.OK) {
						factory = dlg.getSelectedFactory();
					}
				}
				if (factory != null) {
					// Create a new config if non was registered yet.
					factory.createViewerConfiguration();
					getPropertySetSearchConfigModule().setResultViewerConfiguration(factory.getViewerIdentifier(),
							factory.createViewerConfiguration());

					updatePreferencePage();
				}
			}
		});

		Button removeConfigurationButton = new Button(headerRow, SWT.PUSH);
		removeConfigurationButton.setToolTipText("Remove the current result viewer configuration.");
		removeConfigurationButton.setText("-");
		removeConfigurationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (viewerFactoryCombo.getSelectedElement() != null) {
					// We prevent removing the last entry.
					if (getPropertySetSearchConfigModule().getResultViewerConfigurationIDs().size() > 1) {
						getPropertySetSearchConfigModule().removeResultViewerConfiguration(
								viewerFactoryCombo.getSelectedElement().getViewerIdentifier());
						updatePreferencePage();
					}
				}
			}
		});

		configControlWrapper = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);

		viewerFactories = new TreeMap<String, IPropertySetViewerFactory>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				Map<String, IPropertySetViewerFactory> tmp = new HashMap<String, IPropertySetViewerFactory>(viewerFactories);
				IPropertySetViewerFactory f1 = tmp.get(o1);
				IPropertySetViewerFactory f2 = tmp.get(o2);
				if (f1 == null || f2 == null) {
					return 0;
				}
				return f1.getName().compareTo(f2.getName());
			}
		});
		Class<? extends PropSearchFilter> filterClass = getPropertySetSearchConfigModule().getFilterClass();
		Collection<IPropertySetViewerFactory> factories = PropertySetViewerRegistry.sharedInstance().getViewerFactories(filterClass);
		for (IPropertySetViewerFactory factory : factories) {
			viewerFactories.put(factory.getViewerIdentifier(), factory);
		}

		// viewerFactoryCombo.setInput(viewerFactories);

		return wrapper;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Applies the search-configuration by using the super-implementation and then uses the ui for
	 * configuring the result-viewer and applies this configuration.
	 * </p>
	 */
	@Override
	public void updateConfigModule() {
		super.updateConfigModule();
		PropertySetSearchEditLayoutConfigModule configModule = getPropertySetSearchConfigModule();
		IPropertySetViewerFactory selectedFactory = viewerFactoryCombo.getSelectedElement();
		if (previouslySelectedFactory != null) {
			getPropertySetSearchConfigModule().setResultViewerConfiguration(previouslySelectedFactory.getViewerIdentifier(),
					previouslySelectedFactory.getViewerConfiguration());
		}
		getPropertySetSearchConfigModule().setResultViewerConfiguration(selectedFactory.getViewerIdentifier(),
				selectedFactory.getViewerConfiguration());
		configModule.setResultViewerUiIdentifier(selectedFactory.getViewerIdentifier());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Uses the super-implementation to apply the search-configuration and the ui created for the
	 * second tab to apply the result-viewer configuration.
	 * </p>
	 */
	@Override
	protected void updatePreferencePage() {
		super.updatePreferencePage();
		PropertySetSearchEditLayoutConfigModule configModule = getPropertySetSearchConfigModule();

		Set<String> resultViewerConfigurationIDs = configModule.getResultViewerConfigurationIDs();
		selectedFactories = new ArrayList<IPropertySetViewerFactory>(resultViewerConfigurationIDs.size());
		for (String resultViewerIdentifier : resultViewerConfigurationIDs) {
			IPropertySetViewerFactory viewerFactory = viewerFactories.get(resultViewerIdentifier);
			if (viewerFactory != null) {
				selectedFactories.add(viewerFactory);
			}
		}
		viewerFactoryCombo.setInput(selectedFactories);

		String viewerUiIdentifier = configModule.getResultViewerUiIdentifier();
		if (viewerUiIdentifier != null) {
			IPropertySetViewerFactory viewerFactory = viewerFactories.get(viewerUiIdentifier);
			if (viewerFactory != null) {
				viewerFactoryCombo.setSelection(viewerFactory);
				viewerFactorySelected();
			}
		}
	}

	/**
	 * Called when the selection of the viewer-type combo changes and will re-create the ui using
	 * the selected {@link IPropertySetViewerFactory}.
	 */
	private void viewerFactorySelected() {
		final IPropertySetViewerFactory selectedFactory = viewerFactoryCombo.getSelectedElement();
		
		if (previouslySelectedFactory != null) {
			getPropertySetSearchConfigModule().setResultViewerConfiguration(previouslySelectedFactory.getViewerIdentifier(),
					previouslySelectedFactory.getViewerConfiguration());
		}

		if (currentViewerConfigurationControl != null) {
			currentViewerConfigurationControl.dispose();
		}
		
		if (selectedFactory != null) {
			getPropertySetSearchConfigModule().setResultViewerUiIdentifier(selectedFactory.getViewerIdentifier());

			// get the current configuration from config module
			PropertySetViewerConfiguration resultViewerConfiguration = getPropertySetSearchConfigModule().getResultViewerConfiguration(
					selectedFactory.getViewerIdentifier());

			// Create a new config if non was registered yet.
			if (resultViewerConfiguration == null) {
				resultViewerConfiguration = selectedFactory.createViewerConfiguration();
			}

			// create the configuration ui through the factory
			currentViewerConfigurationControl = selectedFactory.createViewerConfigurationControl(configControlWrapper,
					resultViewerConfiguration);
			configControlWrapper.layout();
			setConfigChanged(true);

			previouslySelectedFactory = selectedFactory;
		}
	}

	/**
	 * @return All {@link IPropertySetViewerFactory}s applicable for the current config-module and
	 *         not yet added to it.
	 */
	private Collection<IPropertySetViewerFactory> getAddableFactories() {
		Map<String, IPropertySetViewerFactory> factories = new HashMap<String, IPropertySetViewerFactory>(viewerFactories);
		for (String viewerFactoryID : getPropertySetSearchConfigModule().getResultViewerConfigurationIDs()) {
			factories.remove(viewerFactoryID);
		}
		return factories.values();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Implemented using a {@link StructFieldSearchGridLayoutConfig}.
	 * </p>
	 */
	@Override
	protected IGridLayoutConfig createConfigModuleGridLayoutConfig() {
		return new StructFieldSearchGridLayoutConfig(getConfigModule());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a {@link PropertySetSearchEditLayoutConfigModuleController}.
	 * </p>
	 */
	@Override
	protected IConfigModuleController createConfigModuleController() {
		return new PropertySetSearchEditLayoutConfigModuleController(this);
	}

	/**
	 * @return The config-module of this pages controller casted to {@link PropertySetSearchEditLayoutConfigModule}.
	 */
	public PropertySetSearchEditLayoutConfigModule getConfigModule() {
		return (PropertySetSearchEditLayoutConfigModule) getConfigModuleController().getConfigModule();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a composite that allows the user to set one {@link StructFieldSearchEditLayoutEntry}
	 * as the quickSearchEntry.
	 * </p>
	 */
	protected Composite createFooterComposite(Composite wrapper, final GridLayoutConfigComposite gridLayoutConfigComposite) {
		XComposite comp = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		Button button = new Button(comp, SWT.PUSH);
		button.setText("Set as quick search");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final IGridLayoutConfig currentGridLayoutConfig = getCurrentGridLayoutConfig();
				if (StructFieldSearchGridLayoutConfig.class.isAssignableFrom(currentGridLayoutConfig.getClass())) {
					StructFieldSearchEditLayoutEntry layoutEntry = ((StructFieldSearchGridLayoutConfig) currentGridLayoutConfig)
							.getSearchEntryForGridDataEntry(gridLayoutConfigComposite.getSelectedGridDataEntry());

					if (layoutEntry.getEntryType().equals(EditLayoutEntry.ENTRY_TYPE_SEPARATOR)) {
						MessageDialog.openError(getShell(), "Cannot select separator as quick search item",
								"You cannot select a separator as quick search item.");
						return;
					}

					getConfigModule().setQuickSearchEntry(layoutEntry);
					gridLayoutConfigComposite.refreshEntryTable();

					setConfigChanged(true);
				}
			}
		});
		return comp;
	}

}
