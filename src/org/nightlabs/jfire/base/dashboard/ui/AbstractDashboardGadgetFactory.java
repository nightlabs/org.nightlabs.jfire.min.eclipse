package org.nightlabs.jfire.base.dashboard.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Use this as base class for implementing {@link IDashboardGadgetFactory}. It
 * will initialize {@link #getDashboardGadgetType() dashboardGadgetType},
 * {@link #getName() name} and {@link #getDescription() description} from the
 * extension-element in the plugin.xml.
 * 
 * @author abieber
 */
public abstract class AbstractDashboardGadgetFactory implements IDashboardGadgetFactory {

	
	private String dashBoardGadgetType;
	private String name;
	private String description;
	
	public AbstractDashboardGadgetFactory() {
	}

	@Override
	public String getDashboardGadgetType() {
		return dashBoardGadgetType;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		String dashBoardGadgetTypeAttr = config.getAttribute("dashboardGadgetType"); //$NON-NLS-1$
		if (dashBoardGadgetTypeAttr == null || dashBoardGadgetTypeAttr.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, "org.nightlabs.jfire.base.dashboard.ui", "Attribute dashboardGadgetType is invalid")); //$NON-NLS-1$ //$NON-NLS-2$
		this.dashBoardGadgetType = dashBoardGadgetTypeAttr;
		
		name = config.getAttribute("name"); //$NON-NLS-1$
		if (name == null || name.isEmpty())
			name = dashBoardGadgetType;
		
		description = config.getAttribute("description"); //$NON-NLS-1$
		if (description == null || description.isEmpty())
			description = ""; //$NON-NLS-1$
	}

}
