package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.jfire.prop.StructField;

public interface StructFieldEditor<F extends StructField> extends StructPartEditor<F>
{
	/**
	 * In this method, editors should validate their input and returning a boolean indicating the result of the validation.
	 * @return
	 */
	public boolean validateInput();
	
	/**
	 * Saves all data of the struct field in order to be able to reset it when calling {@link #restoreData()}
	 */
	public void saveData();
	
	/**
	 * Resets all input data of this editor to the data saved by {@link #saveData()}.
	 */
	public void restoreData();

	
	public String getErrorMessage();
}
