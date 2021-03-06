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

import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;

/**
 * {@link DataFieldEditorFactory}s are used to register specific types
 * of {@link DataFieldEditor}s that edit their corresponding {@link DataField}.
 * The factory is responsible for creating the field editors and should be
 * registered as extension to the point <code>org.nightlabs.jfire.base.ui.propDataFieldEditorFactory</code>
 * <p>
 * Note that there exists an abstract base class that should be subclassed
 * rather than implementing this interface directly: {@link AbstractDataFieldEditorFactory}.
 * </p>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface DataFieldEditorFactory<F extends DataField> {
	/**
	 * Should return the subclass of {@link DataField}
	 * the {@link DataFieldEditor} this factory creates can modify.
	 * 
	 * @return The type of {@link DataField} edited by the field editor this factory creates.
	 */
	public Class<F> getPropDataFieldType();

	/**
	 * Returns the editor types the field editor can be used with.
	 * @return The editor types the field editor can be used with.
	 */
	public String[] getEditorTypes();

	/**
	 * Should return a new Instace of the editor for the supplied data.
	 * @param data
	 * @return
	 */
	public DataFieldEditor<F> createPropDataFieldEditor(IStruct struct, F data);
}
