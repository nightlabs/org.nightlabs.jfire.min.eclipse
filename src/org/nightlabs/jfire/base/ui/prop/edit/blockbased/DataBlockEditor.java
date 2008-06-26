package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * Implementations of this interface are used to display the {@link DataField}s
 * of a {@link DataBlock} stored in a {@link PropertySet}.
 * <p>
 * Factories creating instances of this interface are registered
 * per {@link StructBlockID} using the extension-point <code>org.nightlabs.jfire.base.ui.specialisedDataBlockEditor</code>.
 * Note that if a {@link DataBlock} should be displayed/edited for 
 * which no {@link DataBlockEditorFactory} was registered, the 
 * framework will automatically use the {@link GenericDataBlockEditor}.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface DataBlockEditor {

	public Control createControl(Composite parent);
	public Control getControl();
	
	void setData(IStruct struct, DataBlock block);
	
	IStruct getStruct();
	DataBlock getDataBlock();
	
	/**
	 * Default implementation of updateProp() iterates through all
	 * DataFieldEditor s added by {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * and calls their updateProp method.<br/>
	 * Implementors might override if no registered PropDataFieldEditors are used.
	 */
	void updatePropertySet();

	void addDataBlockEditorChangedListener(DataBlockEditorChangedListener listener);
	void removeDataBlockEditorChangedListener(DataBlockEditorChangedListener listener);	

	void setValidationResultManager(IValidationResultManager validationResultManager);
}