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

package org.nightlabs.jfire.base.ui.prop.edit;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.StructField;

/**
 * Abstract base class for all  {@link DataFieldEditor} s with implementations for the listener stuff and other
 * common things for all field editors.<br/>
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class AbstractDataFieldEditor<F extends DataField> implements DataFieldEditor<F>, ModifyListener
{
	private StructField<F> structField;
	private IStruct struct;

	public AbstractDataFieldEditor()
	{
//		this.refStruct = refStruct;
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract Control createControl(Composite parent);

	/**
	 * Extendors should use this field to store the data field they are currently handeling.
	 */
	private F dataField;

	/**
	 * Not intended to be overridden.<br/>
	 * Subclasses should overwrite {@link #setDataField()} to react on changes.
	 *
	 * @see #setDataField()
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#setData(StructField)
	 */
	public void setData(IStruct struct, F data) {
		refreshing = true;
		this.struct = struct;
		try  {
			setDataField(data);
		} finally {
			refreshing = false;
		}
	}

	public F getDataField() {
		return dataField;
	}

	/**
	 * Subclasses can do things when data changes here.
	 * @param dataField
	 */
	protected void setDataField(F dataField) {
		this.dataField = dataField;
		setChanged(false);
	}

	/**
	 * Subclasses should perfom refreshing <b>here<b> and not override
	 * {@link #refresh(DataField)}
	 */
	public abstract void doRefresh();

	private boolean refreshing = false;

	/**
	 * Not intended to be overridden.
	 *
	 * @see #doRefresh(DataField)
	 */
	public final void refresh() {
		refreshing = true;
		try {
			doRefresh();
		} finally {
			refreshing = false;
		}
	}

	private Collection<DataFieldEditorChangeListener> changeListener = new LinkedList<DataFieldEditorChangeListener>();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#addDataFieldEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangeListener)
	 */
	@Override
	public synchronized void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		changeListener.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#removeDataFieldEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangeListener)
	 */
	@Override
	public synchronized void removeDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		changeListener.remove(listener);
	}

	protected synchronized void notifyChangeListeners() {
		// TODO: Rewrite to noitfy listener asynchronously
		for (DataFieldEditorChangeListener listener : changeListener)
			listener.dataFieldEditorChanged(this);
	}

	private boolean changed;

	/**
	 * Sets the changed state of this editor.
	 * @see  org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#setChanged(boolean)
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (!refreshing) {
			if (changed) {
				notifyChangeListeners();
			}
		}
	}

	/**
	 * Checks if this editors value has changed.
	 * @see  org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#isChanged()
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Returns the PropStructField this editor is associated with.
	 *
	 * @return
	 */
	public StructField<F> getStructField() {
		if (structField == null) {
			if (dataField != null) {
				try {
					structField = (StructField<F>) struct.getStructField(
							dataField.getStructBlockOrganisationID(), dataField.getStructBlockID(),
							dataField.getStructFieldOrganisationID(), dataField.getStructFieldID()
						);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (structField == null)
			throw new IllegalStateException("The StructField can only be retrieved if the Editor has already been assigned a DataField."); //$NON-NLS-1$

		return structField;
	}

	protected DataFieldEditorFactory<F> factory;

	/**
	 *
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getPropDataFieldEditorFactory()
	 */
	public DataFieldEditorFactory<F> getPropDataFieldEditorFactory() {
		return factory;
	}

	/**
	 *
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#setPropDataFieldEditorFactory(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorFactory)
	 */
	public void setPropDataFieldEditorFactory(DataFieldEditorFactory<F> factory) {
		this.factory = factory;
	}

	protected IStruct getStruct() {
		return struct;
	}

	@Override
	public void modifyData() {
		notifyChangeListeners();
	}

	private org.eclipse.swt.events.ModifyListener swtModifyListener = new org.eclipse.swt.events.ModifyListener(){
		@Override
		public void modifyText(ModifyEvent e) {
			modifyData();
		}
	};

	/**
	 * This method returns a {@link org.eclipse.swt.events.ModifyListener} that can be used for SWT text widgets
	 * and delegates to the method {@link #modifyData()} of the PropertyFramework {@link ModifyListener} interface.
	 * @return
	 */
	protected org.eclipse.swt.events.ModifyListener getSwtModifyListener() {
		return swtModifyListener;
	}
}