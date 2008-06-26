package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.events.DisposeListener;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangeListener;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.IStruct;

public interface IDataBlockEditorComposite {

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor#refresh(org.nightlabs.jfire.prop.IStruct, org.nightlabs.jfire.prop.DataBlock)
	 */
	void refresh(IStruct struct, DataBlock dataBlock);
	
	void updatePropertySet();
	
	void setValidationResultManager(IValidationResultManager validationResultManager);

	void addDataFieldEditorChangeListener(DataFieldEditorChangeListener listener);
	
	void removeDataFieldEditorChangeListener(DataFieldEditorChangeListener listener);
	
	void addDisposeListener(DisposeListener listener);

}