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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.events.ModifyEvent;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.StructField;

/**
 * Abstract base class for all {@link DataFieldEditor}s with implementations for the listener stuff and other
 * common things for all field editors.<br/>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class AbstractDataFieldEditor<F extends DataField> implements DataFieldEditor<F>
{
	private StructField<F> structField;
	private IStruct struct;

	private F dataField;
	protected DataFieldEditorFactory<F> factory;

	private boolean refreshing;
	private boolean changed;
	
	private org.eclipse.swt.events.ModifyListener swtModifyListener = new org.eclipse.swt.events.ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!refreshing) {
//				notifyChangeListeners();
				setChanged(true);
			}
		}
	};

	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyData() {
			if (!refreshing) {
//				notifyChangeListeners();
				setChanged(true);
			}
		}
	};

	public AbstractDataFieldEditor(IStruct struct, F data)
	{
		this.struct = struct;
		setDataField(data);
	}

	/**
	 * Not intended to be overridden.<br/>
	 * Subclasses should overwrite {@link #setDataField()} to react on changes.
	 *
	 * @see #setDataField()
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#setData(StructField)
	 */
	@Override
	public void setData(IStruct struct, F data) {
		if (refreshing) // Added this, because otherwise refreshing might be set "false" too early. If this is incorrect, then refreshing must be changed to an int and become a (recursion-)counter. Marco.
			return;

		ignoreModifyEvents(true);
		this.struct = struct;
		try  {
			setDataField(data);
		} finally {
			ignoreModifyEvents(false);
		}
		if (getControl() != null && !getControl().isDisposed()) {
			refresh();
		}
	}
	
	

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getDataField()
	 */
	@Override
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

	/**
	 * Not intended to be overridden.
	 * @see #doRefresh(DataField)
	 */
	@Override
	public final void refresh() {
		if (refreshing) // Added this, because otherwise refreshing might be set "false" too early. If this is incorrect, then refreshing must be changed to an int and become a (recursion-)counter. Marco.
			return;

		ignoreModifyEvents(true);
		try {
//			getDataFieldComposite().setContent(getDataField().getData());
			
//			setDataField(getDataField());
			doRefresh();
			handleManagedBy(getDataField().getManagedBy());
			getEntryViewer().setTitle(getStructField().getName().getText());
		} finally {
			ignoreModifyEvents(false);
		}
	}
	
	protected void handleManagedBy(String managedBy) {
//		Composite composite = getEditComposite();
//
//		for (Control child : composite.getChildren()) {
//			if (title != child)
//				child.setEnabled(managedBy == null);
//		}
//		if (managedBy != null)
//			composite.setToolTipText(String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite.managedBy.tooltip"), managedBy)); //$NON-NLS-1$
//		else
//			composite.setToolTipText(null);
		if (getEntryViewer() != null)
			getEntryViewer().setEnabledState(managedBy == null, String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldComposite.managedBy.tooltip"), managedBy)); //$NON-NLS-1$
	}
	
	/**
	 * Returns the {@link IEntryEditor} that is used to display the value of the
	 * data field.
	 * @return the {@link IEntryEditor} that is used to display the value of the
	 * data field.
	 */
	protected abstract IEntryEditor getEntryViewer();

	private ListenerList changeListener = new ListenerList();

	@Override
	public synchronized void addDataFieldEditorChangedListener(DataFieldEditorChangedListener listener) {
		changeListener.add(listener);
	}

	@Override
	public synchronized void removeDataFieldEditorChangedListener(DataFieldEditorChangedListener listener) {
		changeListener.remove(listener);
	}

	protected synchronized void notifyChangeListeners() {
		// TODO: Rewrite to noitfy listener asynchronously
		DataFieldEditorChangedEvent evt = new DataFieldEditorChangedEvent(this);
		Object[] listeners = changeListener.getListeners();
		for (Object listener : listeners) {
			((DataFieldEditorChangedListener) listener).dataFieldEditorChanged(evt);
		}
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (!refreshing) {
			if (changed) {
				notifyChangeListeners();
			}
		}
	}

	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
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

	@Override
	public DataFieldEditorFactory<F> getPropDataFieldEditorFactory() {
		return factory;
	}

	@Override
	public void setPropDataFieldEditorFactory(DataFieldEditorFactory<F> factory) {
		this.factory = factory;
	}

	/**
	 * Get the struct.
	 * @return the struct
	 */
	public IStruct getStruct() {
		return struct;
	}

	/**
	 * This method returns a {@link org.eclipse.swt.events.ModifyListener} that can be used for SWT text widgets.
	 * The listener will notify the {@link ModifyListener}s of this {@link DataFieldEditor} of the change.
	 * @return A modify listener.
	 */
	protected org.eclipse.swt.events.ModifyListener getSwtModifyListener() {
		return swtModifyListener;
	}

	/**
	 * This method returns a {@link ModifyListener} that can be used if {@link ModifyListener}s of this {@link DataFieldEditor}
	 * should be notified of some other change in the data fields.
	 * @return A {@link ModifyListener}.
	 */
	protected ModifyListener getModifyListener() {
		return modifyListener;
	}
	
	/**
	 * Sets whether modification events triggered by the ModifyListeners returned by either {@link #getModifyListener()} or
	 * {@link #getSwtModifyListener()} should be ignored or not.<br />
	 * This method is intended to be used when the contents of the {@link DataFieldEditor} are set programmatically and
	 * therefore no modification events should be triggered.<br />
	 * <b>WARNING:</b> Don't forget to call {@link #ignoreModifyEvents(boolean)} with <code>false</code> when you're done with updating.
	 * 
	 * @param ignore Boolean indicating whether modification events should be ignored or not.
	 */
	protected void ignoreModifyEvents(boolean ignore) {
		this.refreshing = ignore;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation returns a {@link DataFieldEditorLayoutData}
	 * that is configured to fill horizontally.
	 * </p>
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getLayoutData()
	 */
	@Override
	public DataFieldEditorLayoutData getLayoutData() {
		return new DataFieldEditorLayoutData(DataFieldEditorLayoutData.FILL_HORIZONTAL);
	}
}