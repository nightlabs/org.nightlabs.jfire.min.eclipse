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
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedEvent;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * Base class for implementations of {@link IDataBlockEditorComposite}.
 * It has no abstract methods, but is still abstract as it is intended
 * to be subclassed and configured/filled by creating {@link DataFieldEditor}s
 * for 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataBlockEditorComposite extends Composite implements IDataBlockEditorComposite {

	private DataBlockEditor dataBlockEditor;
	private IStruct struct;
	private DataBlock dataBlock;
	
	/**
	 * Create a new {@link AbstractBlockBasedEditor}. Use this super constructor from the subclass.
	 * 
	 * @param dataBlockEditor The {@link DataBlockEditor} this is created for.
	 * @param parent The parent {@link Composite} to use.
	 * @param style The style to apply to the new editor composite.
	 */
	protected AbstractDataBlockEditorComposite(DataBlockEditor dataBlockEditor, Composite parent, int style) {
		super(parent,style);
		this.dataBlockEditor = dataBlockEditor;
		this.struct = dataBlockEditor.getStruct();
		this.dataBlock = dataBlockEditor.getDataBlock();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Implemented by iterating the {@link DataFieldEditor}s that were added by {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * and calling their {@link DataFieldEditor#setData(IStruct, DataField)} 
	 * </p>
	 */
	@Override
	public final void refresh(IStruct struct, DataBlock dataBlock) {
		this.struct = struct;
		this.dataBlock = dataBlock;
		for (DataFieldEditor<DataField> fieldEditor : fieldEditors.values()) {
			try {
				fieldEditor.setData(struct, dataBlock.getDataField(fieldEditor.getStructField().getStructFieldIDObj()));
			} catch (DataFieldNotFoundException e) {
				throw new RuntimeException("Could not find correct DataField: ", e);
			}
		}
	}

	/**
	 * Added to all {@link DataFieldEditor}s added with {@link #addFieldEditor(DataField, DataFieldEditor)}.
	 */
	private DataFieldEditorChangedListener fieldEditorChangeListener = new DataFieldEditorChangedListener() {
		@Override
		public void dataFieldEditorChanged(DataFieldEditorChangedEvent changedEvent) {
			notifyChangeListeners(changedEvent.getDataFieldEditor());

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

	/**
	 * Adds the given {@link DataFieldEditor} for the given {@link DataField} to the editors
	 * known to this {@link Composite}. The {@link DataFieldEditor}s added here will be used
	 * to implement the {@link #refresh(IStruct, DataBlock)} and {@link #updatePropertySet()}
	 * methods of the {@link IDataBlockEditorComposite} interface.
	 * 
	 * @param dataField The {@link DataField} the given {@link DataFieldEditor} is for.
	 * @param fieldEditor The {@link DataFieldEditor} to add.
	 */
	protected void addFieldEditor(DataField dataField, DataFieldEditor<DataField> fieldEditor) {
		addFieldEditor(dataField, fieldEditor, true);
	}

	/**
	 * Adds the given {@link DataFieldEditor} for the given {@link DataField} to the editors
	 * known to this {@link Composite}. The {@link DataFieldEditor}s added here will be used
	 * to implement the {@link #refresh(IStruct, DataBlock)} and {@link #updatePropertySet()}
	 * methods of the {@link IDataBlockEditorComposite} interface.
	 * 
	 * @param dataField The {@link DataField} the given {@link DataFieldEditor} is for.
	 * @param fieldEditor The {@link DataFieldEditor} to add.
	 * @param addListener Whether to add a {@link DataFieldEditorChangedListener} to the given
	 *                    {@link DataFieldEditor} that will cause all changes in the {@link DataFieldEditor}
	 *                    to be notified to listeners to this Composite.
	 */
	protected void addFieldEditor(DataField dataField, DataFieldEditor<DataField> fieldEditor, boolean addListener) {
		fieldEditors.put(dataField.getPropRelativePK(), fieldEditor);
		if (addListener)
			fieldEditor.addDataFieldEditorChangedListener(fieldEditorChangeListener);
	}
	
	/**
	 * Get the {@link DataFieldEditor} that was registered with {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * to be editing the given {@link DataField}.
	 * 
	 * @param dataField The {@link DataField} to search the {@link DataFieldEditor} for.
	 * @return The {@link DataFieldEditor} editing the given {@link DataField} or 
	 *         <code>null</code> if none could be found.
	 */
	protected DataFieldEditor<DataField> getFieldEditor(DataField dataField) {
		return fieldEditors.get(dataField.getPropRelativePK());
	}
	
	/**
	 * Checks whether a {@link DataFieldEditor} was registerd with {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * to be editing the given {@link DataField}.
	 * 
	 * @param dataField The {@link DataField} to search the {@link DataFieldEditor} for.
	 * @return Whether a {@link DataFieldEditor} was registered for the given {@link DataField}.
	 */
	protected boolean hasFieldEditorFor(DataField dataField) {
		return fieldEditors.containsKey(dataField.getPropRelativePK());
	}

	private ListenerList fieldEditorChangeListeners = new ListenerList();

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockEditorComposite#addDataFieldEditorChangeListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener)
	 */
	@Override
	public void addDataFieldEditorChangeListener(DataFieldEditorChangedListener listener) {
		fieldEditorChangeListeners.add(listener);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockEditorComposite#removeDataFieldEditorChangeListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener)
	 */
	@Override
	public void removeDataFieldEditorChangeListener(DataFieldEditorChangedListener listener) {
		fieldEditorChangeListeners.remove(listener);
	}
	
	/**
	 * Notifies the listener to this {@link Composite} of a change in the given {@link DataFieldEditor}.
	 * @param dataFieldEditor The {@link DataFieldEditor} whose data changed.
	 */
	protected synchronized void notifyChangeListeners(DataFieldEditor<? extends DataField> dataFieldEditor) {
		Object[] listeners = fieldEditorChangeListeners.getListeners();
		DataFieldEditorChangedEvent evt = new DataFieldEditorChangedEvent(dataFieldEditor);
		for (Object listener : listeners) {
			if (listener instanceof DataFieldEditorChangedListener)
				((DataFieldEditorChangedListener) listener).dataFieldEditorChanged(evt);
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * Implemented by iterating the {@link DataFieldEditor}s registered with {@link #addFieldEditor(DataField, DataFieldEditor)}
	 * and calling their {@link DataFieldEditor#updatePropertySet()} method.
	 * </p>
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