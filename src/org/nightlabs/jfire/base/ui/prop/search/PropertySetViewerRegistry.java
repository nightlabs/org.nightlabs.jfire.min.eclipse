package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewer;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewerFactory;
import org.nightlabs.jfire.prop.PropertySet;

public class PropertySetViewerRegistry extends AbstractEPProcessor {
	
	private static final String EXTENSION_POINT_ID = JFireBasePlugin.class.getPackage().getName() + ".propertySetViewer";
	private static final String PROPERTY_SET_SEARCH_RESULT_VIEWER_ELEMENT_NAME = "propertySetViewer";
	private static final String IDENTIFIER_ATTRIBUTE_NAME = "identifier";
	private static final String VIEWER_FACTORY_ATTRIBUTE_NAME = "viewerFactory";
	
	private static PropertySetViewerRegistry sharedInstance;
	

	private Map<String, IPropertySetViewerFactory> factories = new HashMap<String, IPropertySetViewerFactory>();
	
	protected PropertySetViewerRegistry() {
	}
	
	public static PropertySetViewerRegistry sharedInstance() {
		if (sharedInstance == null)	{
			synchronized(PropertySetViewerRegistry.class)	{
				if (sharedInstance == null)	{
					sharedInstance = new PropertySetViewerRegistry();
				}
			}
		}
		return sharedInstance;
	}

	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception {
		try {
			if (element.getName().equals(PROPERTY_SET_SEARCH_RESULT_VIEWER_ELEMENT_NAME)) {
				String identifier = element.getAttribute(IDENTIFIER_ATTRIBUTE_NAME);
				
				IPropertySetViewerFactory factory =
					(IPropertySetViewerFactory) element.createExecutableExtension(VIEWER_FACTORY_ATTRIBUTE_NAME);
				
				factories.put(identifier, factory);
			}
		} catch (Exception e) {
			throw new EPProcessorException(e);
		}
	}
	
	public IPropertySetViewer createResultViewer(String identifier) {
		checkProcessing();
		return factories.get(identifier).createViewer();
	}
	
	/**
	 * Returns the factories of all {@link IPropertySetViewer}s that support the given candidateClass, i.e. are able to display instances of it.
	 * @param candidateClass The candidate class.
	 * @return the factories of all {@link IPropertySetViewer}s that support the given candidateClass, i.e. are able to display instances of it.
	 */
	public Collection<IPropertySetViewerFactory> getViewerFactories(Class<? extends PropertySet> candidateClass) {
		checkProcessing();
		
		Collection<IPropertySetViewerFactory> viewerFactories = new LinkedList<IPropertySetViewerFactory>();
		
		for (IPropertySetViewerFactory factory : factories.values()) {
			if (factory.getSupportedCandidateClasses().contains(candidateClass))
				viewerFactories.add(factory);
		}
		
		return viewerFactories;
	}
}