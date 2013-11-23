package org.nightlabs.jfire.base.ui.prop.view;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

/**
 * Implementations of this interface represent factories to create {@link IPropertySetViewer}s.
 * Additionally implementations of this interface are used to create ui that enables the user to
 * edit the configuration of an {@link IPropertySetViewer}.
 * <p>
 * The different implementations (for different types of {@link IPropertySetViewer}s) are registered
 * as extensions to the extension-point <code>org.nightlabs.jfire.base.ui.propertySetViewer</code>.
 * </p>
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IPropertySetViewerFactory<InputType, ElementType, ConfigType extends PropertySetViewerConfiguration> {
	/**
	 * Create an return instance of the result viewer.
	 * @return the created instance.
	 */
	IPropertySetViewer<InputType, ElementType, ConfigType> createViewer();

	/**
	 * Return a collection of the subclasses of {@link PropSearchFilter} whose result the viewer
	 * created in {@link #createViewer()} can handle.
	 * 
	 * @return A collection of the subclasses of {@link PropSearchFilter} whose result the viewer
	 *         created in {@link #createViewer()} can handle.
	 */
	Collection<Class<? extends PropSearchFilter>> getSupportedFilterClasses();
	
	/**
	 * Returns the name of the underlying viewer.
	 * 
	 * @return  the name of the underlying viewer.
	 */
	String getName();
	
	/**
	 * Returns the description of the underlying viewer.
	 * @return the description of the underlying viewer.
	 */
	String getDescription();

	/**
	 * Returns a unique identifier for the underlying viewer.
	 * @return a unique identifier for the underlying viewer.
	 */
	String getViewerIdentifier();

	/**
	 * Called to create a <b>new</b> {@link PropertySetViewerConfiguration} that can be used to
	 * configure an instance of {@link IPropertySetViewer} of the type that this factory creates.
	 * 
	 * @return A <b>new</b> instance of a {@link PropertySetViewerConfiguration} that can be used to
	 *         configure an instance of {@link IPropertySetViewer} of the type that this factory
	 *         creates.
	 */
	PropertySetViewerConfiguration createViewerConfiguration();
	
	/**
	 * Returns a control to configure the property set viewer.
	 * 
	 * The data configured here must be returned in the method {@link #getViewerConfiguration()}.
	 * @param parent the parent of the control
	 * @param configuration the current configuration if any or <code>null</code> otherwise.
	 * @return a control to configure the property set viewer.
	 */
	Control createViewerConfigurationControl(Composite parent, ConfigType configuration);
	
	/**
	 * Returns the configuration of the viewer configured in the control returned by {@link #createViewerConfigurationControl(Composite)}.
	 * @return the configuration of the viewer.
	 */
	PropertySetViewerConfiguration getViewerConfiguration();
}
