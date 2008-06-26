/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangeListener;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataBlockEditorComposite extends Composite implements IDataBlockEditorComposite {

	private DataBlockEditor dataBlockEditor;
	private IStruct struct;
	private DataBlock dataBlock;
	
	protected AbstractDataBlockEditorComposite(DataBlockEditor dataBlockEditor, Composite parent, int style) {
		super(parent,style);
		this.dataBlockEditor = dataBlockEditor;
		this.struct = dataBlockEditor.getStruct();
		this.dataBlock = dataBlockEditor.getDataBlock();
	}

	@Override
	public final void refresh(IStruct struct, DataBlock dataBlock) {
		this.struct = struct;
		this.dataBlock = dataBlock;
	}
	
	protected abstract void doRefresh();

	private DataFieldEditorChangeListener fieldEditorChangeListener = new DataFieldEditorChangeListener() {
		@Override
		public void dataFieldEditorChanged(DataFieldEditor<? extends DataField> editor) {
			notifyChangeListeners(editor);

			List<ValidationResult> validationResults = getDataBlock().validate(getStruct());
			if (getValidationResultManager() != null)
				getValidationResultManager().setValidationResults(validationResults);
		}
	};
	
	/**
	 * key: String DataField.getPropRelativePK<br/>
	 * value: DataFieldEditor fieldEditor
	 */
	private Map<String, DataFieldEditor<DataField>> fieldEditors = new HashMap<String, DataFieldEditor<DataField>>();


	protected void addFieldEditor(DataField dataField, DataFieldEditor<DataField> fieldEditor) {
		addFieldEditor(dataField, fieldEditor, true);
	}

	protected void addFieldEditor(DataField dataField, DataFieldEditor<DataField> fieldEditor, boolean addListener) {
		fieldEditors.put(dataField.getPropRelativePK(), fieldEditor);
		fieldEditor.addDataFieldEditorChangedListener(fieldEditorChangeListener);
	}

	protected DataFieldEditor<DataField> getFieldEditor(DataField dataField) {
		return fieldEditors.get(dataField.getPropRelativePK());
	}

	protected boolean hasFieldEditorFor(DataField dataField) {
		return fieldEditors.containsKey(dataField.getPropRelativePK());
	}

	private ListenerList fieldEditorChangeListeners = new ListenerList();
	
	public void addDataFieldEditorChangeListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListeners.add(listener);
	}
	
	public void removeDataFieldEditorChangeListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListeners.remove(listener);
	}
	
	protected synchronized void notifyChangeListeners(DataFieldEditor<? extends DataField> dataFieldEditor) {
		Object[] listeners = fieldEditorChangeListeners.getListeners();
		for (Object listener : listeners) {
			if (listener instanceof DataFieldEditorChangeListener)
				((DataFieldEditorChangeListener) listener).dataFieldEditorChanged(dataFieldEditor);
		}
	}

	public Map<String, Integer> getStructFieldDisplayOrder() {
		// TODO re-enable this
		//return AbstractPropStructOrderConfigModule.sharedInstance().structFieldDisplayOrder();
		List<StructField<? extends DataField>> fields = getStruct().getStructBlock(getDataBlock().getDataBlockGroup()).getStructFields();
		Map<String, Integer> fieldOrdering = new HashMap<String, Integer>(fields.size());
		int index = 0;
		for (StructField<? extends DataField> field : fields) {
			fieldOrdering.put(field.getPrimaryKey(), index);
			index++;
		}

		return fieldOrdering;
	}

	@SuppressWarnings("unchecked")
	public Iterator<DataField> getOrderedPropDataFieldsIterator() {
		List<DataField> result = new LinkedList<DataField>();
		Map<String, Integer> structFieldOrder = getStructFieldDisplayOrder();
		for (Iterator<DataField> it = getDataBlock().getDataFields().iterator(); it.hasNext(); ) {
			DataField dataField = it.next();
			if (structFieldOrder.containsKey(dataField.getStructFieldPK())) {
				Integer index = structFieldOrder.get(dataField.getStructFieldPK());
				dataField.setPriority(index.intValue());
			}
			result.add(dataField);
		}
		Collections.sort(result);
		return result.iterator();
	}

	protected IStruct getStruct() {
		return struct;
	}

	@Override
	public void dispose() {
		for (DataFieldEditor<? extends DataField> editor : fieldEditors.values()) {
			editor.removeDataFieldEditorChangedListener(fieldEditorChangeListener);
		}
		fieldEditors.clear();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#updatePropertySet()
	 */
	public void updatePropertySet() {
		for (DataFieldEditor<? extends DataField> editor : fieldEditors.values()) {
			editor.updatePropertySet();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#getDataBlock()
	 */
	public DataBlock getDataBlock() {
		return dataBlock;
	}
	
	public DataBlockEditor getDataBlockEditor() {
		return dataBlockEditor;
	}

	private IValidationResultManager validationResultManager;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#setValidationResultManager(org.nightlabs.jfire.base.ui.prop.edit.blockbased.IValidationResultManager)
	 */
	public void setValidationResultManager(IValidationResultManager validationResultManager) {
		this.validationResultManager = validationResultManager;
	}

	public IValidationResultManager getValidationResultManager() {
		return validationResultManager;
	}
}