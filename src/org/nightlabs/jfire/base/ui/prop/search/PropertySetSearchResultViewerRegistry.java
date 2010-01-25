package org.nightlabs.jfire.base.ui.prop.search;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.extensionpoint.EPProcessorException;

public class PropertySetSearchResultViewerRegistry extends AbstractEPProcessor {
	
	private static final String EXTENSION_POINT_ID = "propertySetSearchResultViewer";
	private static final String PROPERTY_SET_SEARCH_RESULT_VIEWER_ELEMENT_NAME = "propertySetSearchResultViewer";
	private static final String IDENTIFIER_ATTRIBUTE_NAME = "identifier";
	private static final String VIEWER_FACTORY_ATTRIBUTE_NAME = "viewerFactory";
	
	private static PropertySetSearchResultViewerRegistry sharedInstance;
	

	private Map<String, IPropertySetSearchResultViewerFactory> factories = new HashMap<String, IPropertySetSearchResultViewerFactory>();
	
	protected PropertySetSearchResultViewerRegistry() {
	}
	
	public static PropertySetSearchResultViewerRegistry sharedInstance() {
		if (sharedInstance == null)	{
			synchronized(PropertySetSearchResultViewerRegistry.class)	{
				if (sharedInstance == null)	{
					sharedInstance = new PropertySetSearchResultViewerRegistry();
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
				
				IPropertySetSearchResultViewerFactory factory =
					(IPropertySetSearchResultViewerFactory) element.createExecutableExtension(VIEWER_FACTORY_ATTRIBUTE_NAME);
				
				factories.put(identifier, factory);
			}
		} catch (Exception e) {
			throw new EPProcessorException(e);
		}
	}
	
	public IPropertySetSearchResultViewer createResultViewer(String identifier) {
		return factories.get(identifier).createResultViewer();
	}
}
