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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedEvent;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataBlockEditor implements DataBlockEditor {

	private static final Logger logger = Logger.getLogger(AbstractBlockBasedEditor.class);
	
	private IStruct struct;
	protected DataBlock dataBlock;

	protected AbstractDataBlockEditor() {
	}

	private ListenerList dataBlockEditorListeners = new ListenerList();
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#addDataBlockEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener)
	 */
	public synchronized void addDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		dataBlockEditorListeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditor#removeDataBlockEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener)
	 */
	public synchronized void removeDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		dataBlockEditorListeners.add(listener);
	}
	protected synchronized void notifyChangeListeners(DataFieldEditor<? extends DataField> dataFieldEditor) {
		Object[] listeners = dataBlockEditorListeners.getListeners();
		DataBlockEditorChangedEvent changedEvent = new DataBlockEditorChangedEvent(this, dataFieldEditor);
		for (Object listener : listeners) {
			if (listener instanceof DataBlockEditorChangedListener)
				((DataBlockEditorChangedListener) listener).dataBlockEditorChanged(changedEvent);
		}
	}

	private DataFieldEditorChangedListener fieldEditorChangeListener = new DataFieldEditorChangedListener() {
		@Override
		public void dataFieldEditorChanged(DataFieldEditorChangedEvent changedEvent) {
			notifyChangeListeners(changedEvent.getDataFieldEditor());
			
			validateDataBlock();
		}
	};	

//	@Override
//	public IStruct getStruct() {
//		return struct;
//	}

	@Override
	public void updatePropertySet() {
		getDataBlockEditorComposite().updatePropertySet();
	}

	@Override
	public DataBlock getDataBlock() {
		return dataBlock;
	}
	
	@Override
	public IStruct getStruct() {
		return struct;
	}

	private IValidationResultManager validationResultManager;

	@Override
	public void setValidationResultManager(IValidationResultManager validationResultManager) {
		this.validationResultManager = validationResultManager;
		getDataBlockEditorComposite().setValidationResultManager(validationResultManager);
		validateDataBlock();
	}
	
	public IValidationResultManager getValidationResultManager() {
		return validationResultManager;
	}
	
	protected void validateDataBlock() {
		if (getDataBlock() != null) {
			List<ValidationResult> validationResults = getDataBlock().validate(getStruct());
			if (getValidationResultManager() != null)
				getValidationResultManager().setValidationResults(validationResults);
		} else
			logger.warn(this.getClass().getName() + ".validateDataBlock() called before setData()"); 
	}
	
	@Override
	public void setData(IStruct struct, DataBlock dataBlock) {
		this.struct = struct;
		this.dataBlock = dataBlock;
		if (dataBlockEditorComposite != null)
			dataBlockEditorComposite.refresh(struct, dataBlock);
	}
	
	private IDataBlockEditorComposite dataBlockEditorComposite;
	
	@Override
	public Control createControl(Composite parent) {
		if (dataBlockEditorComposite != null)
			throw new IllegalStateException("The control for this DataBlockEditor was already created");
		dataBlockEditorComposite = createEditorComposite(parent);
		dataBlockEditorComposite.addDataFieldEditorChangeListener(fieldEditorChangeListener);
		dataBlockEditorComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent evt) {
				dataBlockEditorComposite.removeDataFieldEditorChangeListener(fieldEditorChangeListener);
			}
		});
		if (!(dataBlockEditorComposite instanceof Control))
			throw new IllegalStateException(this.getClass() + " is not implemented correctly, it did not return a " + Control.class.getName() + " in createEditorComposite()");
		
		return (Control) dataBlockEditorComposite;
	}
	
	private IDataBlockEditorComposite getDataBlockEditorComposite() {
		if (dataBlockEditorComposite == null)
			throw new IllegalStateException("The control of this DataBlockEditor was not created yet, however this implementation relies on it in order to function");
		return dataBlockEditorComposite;
	}
	
	@Override
	public Control getControl() {
		return (Control) getDataBlockEditorComposite();
	}
	
	protected abstract IDataBlockEditorComposite createEditorComposite(Composite parent);
}