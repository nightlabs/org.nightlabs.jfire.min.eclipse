package org.nightlabs.jfire.base.ui.overview.search;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.extensionpoint.EPProcessorException;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryFilterCompositeRegistry
	extends AbstractEPProcessor
{
	/**
	 * The logger used in this class.
	 */
//	private static final Logger logger = Logger.getLogger(QueryFilterCompositeRegistry.class);
	
	private static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.ui.queryFilterComposite";
	private static final String ELEMENT_NAME = "QueryFilter";
	public static final String ATTRIBUTE_ELEMENT_CLASS = "baseElementClass"; 
	public static final String ATTRIBUTE_QUERY_FILTER_FACTORY_CLASS = "queryFilterFactoryClass";
	public static final String ATTRIBUTE_SECTION_TITLE = "sectionTitle";
	
	private static QueryFilterCompositeRegistry sharedInstance = null;

	/**
	 * @return The shared Instance of this class.
	 */
	public static QueryFilterCompositeRegistry sharedInstance()
	{
		if (sharedInstance == null)
		{
			synchronized (QueryFilterCompositeRegistry.class)
			{
				if (sharedInstance == null)
					sharedInstance = new QueryFilterCompositeRegistry();
			}
		}
		return sharedInstance;
	}
	
	protected QueryFilterCompositeRegistry()
	{
		queryFilters = new HashMap<Class, SortedSet<QueryFilterFactory>>();
	}

	@Override
	public String getExtensionPointID()
	{
		return EXTENSION_POINT_ID;
	}
	
	private Map<Class, SortedSet<QueryFilterFactory>> queryFilters;
//	private Map<Class, List<QueryFilterFactory>> cachedQueryFiltersSortedByInheritance;
	
//	private Map<Class, List<AbstractQueryFilterComposite>> queryFilters;
	
	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception
	{
		if (! ELEMENT_NAME.equals(element.getName()))
		{
			throw new EPProcessorException("While Processing an element, the element name didn't match! given name=" 
				+ element.getName());
		}
		QueryFilterFactory factory;
		if (checkString(element.getAttribute(ATTRIBUTE_QUERY_FILTER_FACTORY_CLASS)))
		{
			try
			{
				factory = (QueryFilterFactory) element.createExecutableExtension(ATTRIBUTE_QUERY_FILTER_FACTORY_CLASS);
			}
			catch (CoreException e) {
				throw new EPProcessorException("Could't instantiate the given factory object!", e); 
			}
		}
		else
		{
			throw new EPProcessorException("the given factory class string is null or empty!");
		}
		
		// clear cache if some extension point is parsed lazily
//		if (cachedQueryFiltersSortedByInheritance != null)
//		{
//			cachedQueryFiltersSortedByInheritance = null;
//		}
		
		SortedSet<QueryFilterFactory> registeredComposites = queryFilters.get(factory.getViewerBaseClass());
		if (registeredComposites == null)
		{
			registeredComposites = new TreeSet<QueryFilterFactory>();
		}
		
		registeredComposites.add(factory);
		queryFilters.put(factory.getViewerBaseClass(), registeredComposites);
	}
	
	/**
	 * Returns the list of registered 
	 * @return
	 */
	public Map<Class, SortedSet<QueryFilterFactory>> getQueryFilterComposites()
	{
		checkProcessing();
		return queryFilters;
	}

	/**
	 * 
	 * @param baseElementType
	 * @return
	 */
	public SortedSet<QueryFilterFactory> getQueryFilterCompositesFor(Class baseElementType)
	{
		checkProcessing();
		return getQueryFilterComposites().get(baseElementType);
	}
}
