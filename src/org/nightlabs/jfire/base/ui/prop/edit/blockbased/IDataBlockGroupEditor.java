package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;

import com.sun.xml.internal.ws.api.PropertySet;

/**
 * Interface for graphical Editors of a {@link DataBlockGroup}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface IDataBlockGroupEditor {
	
	/**
	 * Create the UI for this editor.
	 * 
	 * @param parent The parent Composite.
	 * @return A new instance of the Composite of this editor. 
	 */
	Composite createControl(Composite parent);
	
	/**
	 * Refresh this editor with the data of the given {@link DataBlockGroup}.
	 * 
	 * @param struct The structure the DataBlockGroup is build upon.
	 * @param blockGroup The data to display/edit.
	 */
	void refresh(IStruct struct, DataBlockGroup blockGroup);
	
	/**
	 * Add a listener for changes to the data of a DataBlock in this editor this editor.
	 *  
	 * @param listener The listener to add.
	 */
	void addDataBlockEditorChangedListener(DataBlockEditorChangedListener listener);
	
	/**
	 * Remove the given listener from the list of listeners for changes to the data of a DataBlock in this editor.
	 * 
	 * @param listener The listener to remove.
	 */
	void removeDataBlockEditorChangedListener(DataBlockEditorChangedListener listener);

	/**
	 * Add a listener to changes of the structure of the DataBlockGroup.
	 * 
	 * @param listener The listener to add.
	 */
	void addDataBlockGroupEditorChangedListener(IDataBlockGroupEditorChangedListener listener);
	
	/**
	 * Remove the given listener from the list of listeners to changes of the structure of the DataBlockGroup.
	 * 
	 * @param listener The listener to remove.
	 */
	void removeDataBlockGroupEditorChangedListener(IDataBlockGroupEditorChangedListener listener);
	
	/**
	 * Update the {@link PropertySet} of the {@link DataBlockGroup} with the data entered by the user.
	 */
	void updatePropertySet();
	
	/**
	 * @return The structure of this editor.
	 */
	IStruct getStruct();

	/**
	 * @return The {@link DataBlockGroup} currently edited.
	 */
	DataBlockGroup getDataBlockGroup();
	
	/**
	 * @param validationResultHandler The {@link IValidationResultHandler} to use.
	 */
	void setValidationResultHandler(IValidationResultHandler validationResultHandler);
	

}