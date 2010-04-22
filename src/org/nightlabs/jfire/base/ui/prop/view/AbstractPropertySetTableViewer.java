package org.nightlabs.jfire.base.ui.prop.view;

import java.util.Collection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.PropertySetTable;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

/**
 * BaseClass that can be used for implementations of {@link IPropertySetViewer} that utilize a
 * {@link PropertySetTable}. It then implements all the delegate methods to the table and other
 * common methods of {@link IPropertySetViewer}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @param <InputType> See {@link IPropertySetViewer}
 * @param <TableInputType> The type of that will be set to the PropertySetTable on
 *            {@link #setInput(Collection, org.nightlabs.progress.ProgressMonitor)}, this might
 *            differ from the InputType of this viewer.
 * @param <SelectionType> See {@link IPropertySetViewer}
 * @param <ConfigType> See {@link IPropertySetViewer}
 */
public abstract class AbstractPropertySetTableViewer<InputType, TableInputType, SelectionType, ConfigType extends PropertySetViewerConfiguration>
		implements IPropertySetViewer<InputType, SelectionType, ConfigType> {

	private PropertySetTable<TableInputType, SelectionType> propertySetTable;
	private ConfigType configuration;
	private ListenerList contentChangeListner = new ListenerList();
	private Collection<InputType> input;

	/**
	 * Called to create the {@link PropertySetTable} used by this viewer. Only after this method was
	 * called the implementations of the delegate-methods will not yield
	 * {@link NullPointerException} s.
	 * 
	 * @param parent The parent to add the new table to.
	 * @return A new {@link PropertySetTable} that should be used by this viewer.
	 */
	protected abstract PropertySetTable<TableInputType, SelectionType> createPropertySetTable(Composite parent);

	/**
	 * {@inheritDoc}
	 * <p>
	 * Re-declared to constrain the return type to a PropertySetTable.
	 * </p>
	 */
	@Override
	public PropertySetTable<TableInputType, SelectionType> createControl(Composite parent) {
		propertySetTable = createPropertySetTable(parent);
		return propertySetTable;
	}

	@Override
	public void setConfiguration(ConfigType config) {
		this.configuration = config;
	}

	/**
	 * @return The {@link PropertySetTable} created in {@link #createControl(Composite)}.
	 */
	public PropertySetTable<TableInputType, SelectionType> getPropertySetTable() {
		return propertySetTable;
	}

	/**
	 * @return The configuration set in {@link #setConfiguration(PropertySetViewerConfiguration)}
	 */
	public ConfigType getConfiguration() {
		return configuration;
	}

	@Override
	public void loadDefaultConfiguration() {
	}

	@Override
	public void addOpenListener(IOpenListener listener) {
		propertySetTable.getTableViewer().addOpenListener(listener);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		propertySetTable.addSelectionChangedListener(listener);
	}

	@Override
	public SelectionType getFirstSelectedElement() {
		return propertySetTable.getFirstSelectedElement();
	}

	@Override
	public Collection<SelectionType> getSelectedElements() {
		return propertySetTable.getSelectedElements();
	}

	@Override
	public void removeOpenListener(IOpenListener listener) {
		propertySetTable.getTableViewer().removeOpenListener(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		propertySetTable.removeSelectionChangedListener(listener);
	}

	@Override
	public void setMessage(String message) {
		propertySetTable.setLoadingMessage(message);
	}

	@Override
	public void addInputChangedListener(IPropertySetViewerInputChangedListener listener) {
		contentChangeListner.add(listener);
	}

	@Override
	public void removeInputChangedListener(IPropertySetViewerInputChangedListener listener) {
		contentChangeListner.remove(listener);
	}

	/**
	 * Can be used internally to set the input. Note that the input is also set in
	 * {@link #notifiyContentChangedListeners(Collection)}.
	 * 
	 * @param input The input to set.
	 */
	protected void setInput(Collection<InputType> input) {
		this.input = input;
	}

	@Override
	public Collection<InputType> getInput() {
		return input;
	}

	/**
	 * Notifies all contentChangeListners and sets the input of this viewer in order to implement
	 * {@link #getInput()}.
	 */
	protected void notifiyContentChangedListeners(Collection<InputType> input) {
		setInput(input);
		Object[] listeners = contentChangeListner.getListeners();
		for (Object listener : listeners) {
			if (listener instanceof IPropertySetViewerInputChangedListener) {
				((IPropertySetViewerInputChangedListener) listener).inputChanged(this);
			}
		}
	}
}
