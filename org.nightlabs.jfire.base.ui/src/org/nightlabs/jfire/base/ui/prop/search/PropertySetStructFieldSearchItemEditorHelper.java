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

package org.nightlabs.jfire.base.ui.prop.search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.ISearchFilterItem;
import org.nightlabs.jfire.prop.StructField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class PropertySetStructFieldSearchItemEditorHelper implements
		PropertySetSearchFilterItemEditorHelper {

	
	private StructField structField;
	
	
	protected PropertySetStructFieldSearchItemEditorHelper() {
		super();
	}
	
	/**
	 * Constructs a new StructFieldSearchItemEditorHelper
	 * and calls {@link #init(AbstractPersonStructField)}.
	 * 
	 * @param structField
	 */
	public PropertySetStructFieldSearchItemEditorHelper(StructField _personStructField) {
		super();
		init(_personStructField);
	}
	
	
	public void init(StructField personStructField) {
		this.structField = personStructField;
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract Control getControl(Composite parent);

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public abstract ISearchFilterItem getSearchFilterItem();

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#newInstance()
	 */
	public PropertySetSearchFilterItemEditorHelper newInstance() {
		PropertySetStructFieldSearchItemEditorHelper result;
		try {
			result = this.getClass().newInstance();
		} catch (Throwable t) {
			IllegalStateException ill = new IllegalStateException("Error instatiating new StructFieldSearchItemEditorHelper "+this); //$NON-NLS-1$
			ill.initCause(t);
			throw ill;
		}
		result.init(this.structField);
		return result;
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#getDisplayName()
	 */
	public String getDisplayName() {
		return structField.getName().getText();
	}

	protected StructField getStructField() {
		return structField;
	}
}
