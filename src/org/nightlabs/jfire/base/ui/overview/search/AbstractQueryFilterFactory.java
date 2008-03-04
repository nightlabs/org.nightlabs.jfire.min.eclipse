package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Status;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractQueryFilterFactory<R, Q extends AbstractSearchQuery<R>>
	implements QueryFilterFactory<R, Q>
{
	private Class<R> viewerBaseClass;
	private String sectionTitle;
	
	/**
	 * Default implementation compares factories according to their section titles.
	 */
	@Override
	public int compareTo(QueryFilterFactory<R, Q> other)
	{
		return getSectionTitle().compareTo(other.getSectionTitle());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException
	{
		if (AbstractEPProcessor.checkString(
			config.getAttribute(QueryFilterCompositeRegistry.ATTRIBUTE_ELEMENT_CLASS))
			)
		{
			try
			{
				final String viewerBaseClassName = 
					config.getAttribute(QueryFilterCompositeRegistry.ATTRIBUTE_ELEMENT_CLASS);
				
				viewerBaseClass = JFireBasePlugin.getDefault().getBundle().loadClass(viewerBaseClassName);
			}
			catch (InvalidRegistryObjectException e)
			{
				throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
					"Invalid registry object!", e));
			}
			catch (ClassNotFoundException e)
			{
				throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
					"Could not find Class:" + viewerBaseClass, e));
			}
		}
		else
		{
			// TODO: how to get to the plugin that defined this invalid extension??
			throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
				"A viewer base class has to be defined, but an empty string was found! config:"+config.getName()));			
		}
		
		if (AbstractEPProcessor.checkString(
			config.getAttribute(QueryFilterCompositeRegistry.ATTRIBUTE_SECTION_TITLE))
			)
		{
			sectionTitle = config.getAttribute(QueryFilterCompositeRegistry.ATTRIBUTE_SECTION_TITLE);
		}
		else
		{
			// TODO: how to get to the plugin that defined this invalid extension??
			throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
				"No section title set! config:"+config.getName()));			
		}
	}
	
	@Override
	public String getSectionTitle()
	{
		return sectionTitle;
	}

	@Override
	public Class<R> getViewerBaseClass()
	{
		return viewerBaseClass;
	}
}
