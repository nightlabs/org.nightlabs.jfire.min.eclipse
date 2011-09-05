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

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;

/**
 * Default {@link IDataBlockGroupEditor} uses a
 * {@link DataBlockGroupEditorComposite} to which it delegates all methods to
 * implement the interface.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class DataBlockGroupEditor implements IDataBlockGroupEditor
{
	/**
	 * Factory that creates {@link DataBlockGroupEditor}
	 */
	public static class Factory implements IDataBlockGroupEditorFactory {

		@Override
		public IDataBlockGroupEditor createDataBlockGroupEditor() {
			return new DataBlockGroupEditor();
		}
		
	}
	
	private DataBlockGroupEditorComposite editorComposite;

	@Override
	public final Composite createControl(Composite parent) {
		editorComposite = createDataBlockGroupEditorComposite(parent);
		return editorComposite;
	}

	/**
	 * Creates the {@link DataBlockGroupEditorComposite} this editor delegates all methods to.
	 * <p>
	 * Override this method to create a custom subclass of {@link DataBlockGroupEditorComposite} here.
	 * </p>
	 * @param parent The parent to create the Composite for.
	 * @return A new {@link DataBlockGroupEditorComposite}.
	 */
	protected DataBlockGroupEditorComposite createDataBlockGroupEditorComposite(
			Composite parent) {
		return new DataBlockGroupEditorComposite(parent, this);
	}

	/**
	 * @return The {@link DataBlockGroupEditorComposite} created in {@link #createDataBlockGroupEditorComposite(Composite)}.
	 */
	protected DataBlockGroupEditorComposite getEditorComposite() {
		return editorComposite;
	}
	
	@Override
	public DataBlockGroup getDataBlockGroup() {
		checkComposite();
		return editorComposite.getDataBlockGroup();
	}

	@Override
	public IStruct getStruct() {
		checkComposite();
		return editorComposite.getStruct();
	}

	@Override
	public void refresh(IStruct struct, DataBlockGroup blockGroup) {
		checkComposite();
		editorComposite.refresh(struct, blockGroup);
	}

	@Override
	public void setValidationResultHandler(IValidationResultHandler validationResultHandler) {
		checkComposite();
		editorComposite.setValidationResultHandler(validationResultHandler);
	}

	@Override
	public void updatePropertySet() {
		checkComposite();
		editorComposite.updatePropertySet();
	}

	@Override
	public void addDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		checkComposite();
		editorComposite.addDataBlockEditorChangedListener(listener);
	}

	@Override
	public void removeDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		checkComposite();
		editorComposite.removeDataBlockEditorChangedListener(listener);
	}
	
	@Override
	public void addDataBlockGroupEditorChangedListener(IDataBlockGroupEditorChangedListener listener) {
		checkComposite();
		editorComposite.addDataBlockEditorGroupChangedListener(listener);
	}

	@Override
	public void removeDataBlockGroupEditorChangedListener(IDataBlockGroupEditorChangedListener listener) {
		checkComposite();
		editorComposite.removeDataBlockEditorGroupChangedListener(listener);
	}

	private void checkComposite() {
		if (editorComposite == null) {
			throw new IllegalStateException(
					"This DataBlockGroupEditor works only if its control was created. (editorComposite == null!"); //$NON-NLS-1$
		}
	}

}
