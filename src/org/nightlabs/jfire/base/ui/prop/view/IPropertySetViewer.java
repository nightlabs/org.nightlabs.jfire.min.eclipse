package org.nightlabs.jfire.base.ui.prop.view;


import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

/**
 * Interface for viewers of a collection of property sets.
 * 
 * @param C The type of the {@link PropertySetViewerConfiguration} for this {@link IPropertySetViewer}.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @see PropertySetSearchEditLayoutConfigModule
 */
public interface IPropertySetViewer<C extends PropertySetViewerConfiguration> {
	
	/**
	 * Sets the property sets that should be displayed in this viewer.
	 * 
	 * @param propertySets the property sets that should be displayed.
	 */
	public void setPropertySets(Collection<PropertySet> propertySets);
	
	/**
	 * Sets the configuration of this result viewer.
	 * 
	 * @param config The configuration to be set.
	 */
	public void setConfiguration(C config);
	
	/**
	 * Creates and returns the control of this result viewer.
	 * @param parent The parent of the control.
	 * @return the created control.
	 */
	public Control createControl(Composite parent);
	
	/**
	 * Loads a default configuration for this viewer.
	 */
	public void loadDefaultConfiguration();
}
