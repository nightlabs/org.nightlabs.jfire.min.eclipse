/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.eclipse.extension.AbstractEPProcessor;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;
import org.slf4j.LoggerFactory;

/**
 * @author abieber
 *
 */
public class DashboardGadgetRegistry extends AbstractEPProcessor {

	public static final String EXTENSION_POINT_ID = DashboardGadgetRegistry.class.getPackage().getName() + ".dashboardGadgetFactory"; //$NON-NLS-1$
	
	private Map<String, IDashboardGadgetFactory> factories = new HashMap<String, IDashboardGadgetFactory>();
	
	private static DashboardGadgetRegistry sharedInstance;
	
	public static DashboardGadgetRegistry sharedInstance() {
		if (sharedInstance == null) {
			synchronized (DashboardGadgetRegistry.class) {
				if (sharedInstance == null) {
					sharedInstance = new DashboardGadgetRegistry();
					sharedInstance.process();
				}
			}
		}
		return sharedInstance;
	}
	
	/**
	 * 
	 */
	public DashboardGadgetRegistry() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.extension.AbstractEPProcessor#getExtensionPointID()
	 */
	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.extension.AbstractEPProcessor#processElement(org.eclipse.core.runtime.IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception {
		IDashboardGadgetFactory factory = (IDashboardGadgetFactory) element.createExecutableExtension("class"); //$NON-NLS-1$
		if (!factories.containsKey(factory.getDashboardGadgetType())) {
			factories.put(factory.getDashboardGadgetType(), factory);
		} else {
			LoggerFactory.getLogger(DashboardGadgetRegistry.class).warn("There already exists a IDashboardGadgetFactory for the type {}: {}. We ignore all new factories, in this case {}", new Object[] {factory.getDashboardGadgetType(), factories.get(factory.getDashboardGadgetType()), factory}); //$NON-NLS-1$
		}
		
	}
	
	public Collection<IDashboardGadgetFactory> getFactories() {
		return Collections.unmodifiableCollection(factories.values());
	}
	
	public IDashboardGadgetFactory getFactory(String dashboardGadgetType) {
		return factories.get(dashboardGadgetType);
	}

}
