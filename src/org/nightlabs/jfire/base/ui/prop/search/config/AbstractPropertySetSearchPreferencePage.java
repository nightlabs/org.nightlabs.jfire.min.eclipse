package org.nightlabs.jfire.base.ui.prop.search.config;


import java.util.Collection;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.nightlabs.base.ui.composite.ComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.ui.layout.AbstractEditLayoutPreferencePage;
import org.nightlabs.jfire.base.ui.prop.search.PropertySetViewerRegistry;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewerFactory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.PersonTableViewerConfiguration;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;
import org.nightlabs.jfire.security.SecurityReflector;

public abstract class AbstractPropertySetSearchPreferencePage extends AbstractEditLayoutPreferencePage {
	
	private XComposite configControlWrapper;
	private Control currentViewerConfigurationControl;
	private ComboComposite<IPropertySetViewerFactory> viewerFactoryCombo;
	private IPropertySetViewerFactory<?> previouslySelectedFactory;

	protected AbstractPropertySetSearchPreferencePage() {
		super(true);
	}

	protected PropertySetSearchEditLayoutConfigModule getPropertySetSearchConfigModule() {
		return (PropertySetSearchEditLayoutConfigModule) getConfigModuleController().getConfigModule();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Adds another tab to the top level tab folder that allows to configure the result viewer.</p>
	 */
	@Override
	protected void createPreferencePage(Composite parent) {
		super.createPreferencePage(parent);
		
		TabFolder tabFolder = getTabFolder();
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Result viewer configuration");
		
		tabItem.setControl(createResultViewerConfigurationControl(tabFolder));
	}
	
	protected Control createResultViewerConfigurationControl(Composite parent) {
		XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA);
		
		viewerFactoryCombo = new ComboComposite<IPropertySetViewerFactory>(wrapper, SWT.NONE);
		viewerFactoryCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IPropertySetViewerFactory) element).getName();
			}
		});
		
		viewerFactoryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final IPropertySetViewerFactory selectedFactory = viewerFactoryCombo.getSelectedElement();
				getPropertySetSearchConfigModule().setResultViewerUiIdentifier(selectedFactory.getViewerIdentifier());
				
				if (previouslySelectedFactory != null) {
					getPropertySetSearchConfigModule().setResultViewerConfiguration(previouslySelectedFactory.getViewerIdentifier(), previouslySelectedFactory.getViewerConfiguration());
				}
				
				if (currentViewerConfigurationControl != null) {
					currentViewerConfigurationControl.dispose();
				}
				
				// get the current configuration from config module
				PropertySetViewerConfiguration resultViewerConfiguration =
					getPropertySetSearchConfigModule().getResultViewerConfiguration(selectedFactory.getViewerIdentifier());
				
				// Create a new config if non was registered yet.
				if (resultViewerConfiguration == null) {
					resultViewerConfiguration = new PersonTableViewerConfiguration(SecurityReflector.getUserDescriptor().getOrganisationID(), IDGenerator.nextID(PropertySetViewerConfiguration.class));
				}
				
				// create the configuration ui through the factory
				currentViewerConfigurationControl = selectedFactory.createViewerConfigurationControl(configControlWrapper, resultViewerConfiguration);
				configControlWrapper.layout();
				setConfigChanged(true);
				
				previouslySelectedFactory = selectedFactory;
			}
		});
		
		configControlWrapper = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		Class<? extends PropertySet> candidateClass = getPropertySetSearchConfigModule().getCandidateClass();
		Collection<IPropertySetViewerFactory> viewerFactories = PropertySetViewerRegistry.sharedInstance().getViewerFactories(candidateClass);
		
		viewerFactoryCombo.setInput(viewerFactories);
		
		return wrapper;
	}
	
	@Override
	public void updateConfigModule() {
		super.updateConfigModule();
		PropertySetSearchEditLayoutConfigModule configModule = getPropertySetSearchConfigModule();
		IPropertySetViewerFactory<?> selectedFactory = viewerFactoryCombo.getSelectedElement();
		getPropertySetSearchConfigModule().setResultViewerConfiguration(selectedFactory.getViewerIdentifier(), selectedFactory.getViewerConfiguration());
	}
}
