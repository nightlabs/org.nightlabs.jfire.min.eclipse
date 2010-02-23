package org.nightlabs.jfire.base.ui.prop.view;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

/**
 * Implementations of this interface represent factories to create {@link IPropertySetViewer}s.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public interface IPropertySetViewerFactory<C extends PropertySetViewerConfiguration> {
	/**
	 * Create an return instance of the result viewer.
	 * @return the created instance.
	 */
	public IPropertySetViewer<C> createViewer();
	
	/**
	 * Return a collection of the subclasses of {@link PropertySet} that the viewer created in {@link #createViewer()} can handle.
	 * @return a collection of the subclasses of {@link PropertySet} that the viewer created in {@link #createViewer()} can handle.
	 */
	public Collection<Class<? extends PropertySet>> getSupportedCandidateClasses();
	
	/**
	 * Returns the name of the underlying viewer.
	 * 
	 * @return  the name of the underlying viewer.
	 */
	public String getName();
	
	/**
	 * Returns the description of the underlying viewer.
	 * @return the description of the underlying viewer.
	 */
	public String getDescription();

	/**
	 * Returns a unique identifier for the underlying viewer.
	 * @return a unique identifier for the underlying viewer.
	 */
	public String getViewerIdentifier();
	
	/**
	 * Returns a control to configure the property set viewer.
	 * 
	 * The data configured here must be returned in the method {@link #getViewerConfiguration()}.
	 * @param parent the parent of the control
	 * @param configuration the current configuration if any or <code>null</code> otherwise.
	 * @return a control to configure the property set viewer.
	 */
	public Control createViewerConfigurationControl(Composite parent, C configuration);
	
	/**
	 * Returns the configuration of the viewer configured in the control returned by {@link #createViewerConfigurationControl(Composite)}.
	 * @return the configuration of the viewer.
	 */
	public PropertySetViewerConfiguration getViewerConfiguration();
}
