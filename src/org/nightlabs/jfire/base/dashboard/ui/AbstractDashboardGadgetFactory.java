/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author abieber
 *
 */
public abstract class AbstractDashboardGadgetFactory implements
		IDashboardGadgetFactory {

	
	private String dashBoardGadgetType;
	private String name;
	private String description;
	
	/**
	 * 
	 */
	public AbstractDashboardGadgetFactory() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory#getDashboardGadgetType()
	 */
	@Override
	public String getDashboardGadgetType() {
		return dashBoardGadgetType;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		String dashBoardGadgetTypeAttr = config.getAttribute("dashboardGadgetType");
		if (dashBoardGadgetTypeAttr == null || dashBoardGadgetTypeAttr.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, "org.nightlabs.jfire.base.dashboard.ui", "Attribute dashboardGadgetType is invalid"));
		this.dashBoardGadgetType = dashBoardGadgetTypeAttr;
		
		name = config.getAttribute("name");
		if (name == null || name.isEmpty())
			name = dashBoardGadgetType;
		
		description = config.getAttribute("description");
		if (description == null || description.isEmpty())
			description = "";
	}

}
