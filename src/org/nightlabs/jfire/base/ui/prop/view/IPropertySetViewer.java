package org.nightlabs.jfire.base.ui.prop.view;

import java.util.Collection;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Interface for viewers of a collection of property sets.
 * 
 * @param ConfigType The type of the {@link PropertySetViewerConfiguration} for this
 *            {@link IPropertySetViewer}.
 * 
 * @param InputType This is the type this viewer understands as input-type. For most viewers this
 *            will be {@link PropertySetID} if the viewer displays PropertySets. However viewers can
 *            be registered that might display objects that are not instances of PropertySet but are
 *            linked to a property-set (like LegalEntities or Products) then the viewer might also
 *            display properties of that objects along with StructField values. Another scenario
 *            might be viewers registered to take the output-values of a SearchFilter as InputType.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @see PropertySetSearchEditLayoutConfigModule
 */
public interface IPropertySetViewer<InputType, ElementType, ConfigType extends PropertySetViewerConfiguration> {

	/**
	 * Sets the elements that should be displayed in this viewer.
	 * <p>
	 * Is likely to be called on a worker-thread.
	 * </p>
	 * 
	 * @param input the input that should be displayed by this viewer.
	 * @param monitor The monitor to report progress when the implementation needs to first fetch
	 *            more information for display.
	 */
	void setInput(Collection<InputType> input, ProgressMonitor monitor);

	/**
	 * Get all elements set as input for this viewer.
	 * 
	 * @return All elements set as input for this viewer.
	 */
	Collection<InputType> getInput();
	
	/**
	 * Sets the configuration of this result viewer.
	 * 
	 * @param config The configuration to be set.
	 */
	void setConfiguration(ConfigType config);

	/**
	 * Creates and returns the control of this result viewer.
	 * 
	 * @param parent The parent of the control.
	 * @return the created control.
	 */
	Control createControl(Composite parent);

	/**
	 * Loads a default configuration for this viewer.
	 */
	void loadDefaultConfiguration();

	/**
	 * Set a message that this viewer should display to the user.
	 * 
	 * @param message The message to show.
	 */
	void setMessage(String message);

	/**
	 * Add a listener for selection changes to this viewer.
	 * 
	 * @param listener The listener to add.
	 */
	void addSelectionChangedListener(ISelectionChangedListener listener);

	/**
	 * Remove the given listener from the list of listeners to selection changes.
	 * 
	 * @param listener The listener to remove.
	 */
	void removeSelectionChangedListener(ISelectionChangedListener listener);

	/**
	 * Add a listener that will be notified on double-clicks and other open-events.
	 * 
	 * @param listener The listener to add.
	 */
	void addOpenListener(IOpenListener listener);

	/**
	 * Remove the given open-listener.
	 * 
	 * @param listener The listener to remove.
	 */
	void removeOpenListener(IOpenListener listener);

	/**
	 * Get the currently selected elements.
	 * 
	 * @return The currently selected elements, might be an empty collection.
	 */
	Collection<ElementType> getSelectedElements();

	/**
	 * Get the fist currently selected element.
	 * 
	 * @return The fist currently selected element, or <code>null</code> if none is selected.
	 */
	ElementType getFirstSelectedElement();

	/**
	 * Add a listener that will be notified when the input of this viewer has changed, i.e. when
	 * {@link #setInput(Collection, ProgressMonitor)} was called.
	 * 
	 * @param listener The listener to add.
	 */
	void addInputChangedListener(IPropertySetViewerInputChangedListener listener);

	/**
	 * Remove the given input-changed listener.
	 * 
	 * @param listener The listener to remove.
	 */
	void removeInputChangedListener(IPropertySetViewerInputChangedListener listener);
}
