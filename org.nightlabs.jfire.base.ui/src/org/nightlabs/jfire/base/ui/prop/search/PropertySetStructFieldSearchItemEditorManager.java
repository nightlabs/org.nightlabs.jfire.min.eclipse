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
 * A concrete StructFieldSearchItemEditorHelper that
 * serves as a manager for other StructFieldSearchItemEditorHelper.
 * It searches for StructFieldSearchItemEditorHelper in
 * the PropertySetSearchFilterItemEditorHelperRegistry liked to
 * a class of PersonStructFields.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetStructFieldSearchItemEditorManager extends
		PropertySetStructFieldSearchItemEditorHelper {

	/**
	 * 
	 */
	public PropertySetStructFieldSearchItemEditorManager() {
		super();
	}

	/**
	 * @param personStructField
	 */
	public PropertySetStructFieldSearchItemEditorManager(
			StructField personStructField) {
		super(personStructField);
	}

	
	private PropertySetSearchFilterItemEditorHelper helper;
	private Control helperControl;
	
	/**
	 * This searches for the right helper,
	 * gets and remembers a new instance of it
	 * and the Control it returned.
	 * 
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control getControl(Composite parent) {
		if (helper != null)
			if (helperControl != null && !helperControl.isDisposed())
				return helperControl;
			
//		PropertySetSearchFilterItemEditorHelperRegistry registry = PropertySetSearchFilterItemEditorHelperRegistry.sharedInstance();
		StructFieldSearchFilterEditorRegistry registry = StructFieldSearchFilterEditorRegistry.sharedInstance();
		if (getStructField() == null)
			throw new IllegalStateException("Member personStructField is null. init(personStructField) might not have been called."); //$NON-NLS-1$
		
		final IStructFieldSearchFilterItemEditor filterItemEditor = registry.createSearchFilterItemEditor(getStructField(), null);
		helper = new PropertySetStructFieldSearchItemEditorHelper() {
			@Override
			public ISearchFilterItem getSearchFilterItem() {
				return filterItemEditor.getSearchFilterItem();
			}

			@Override
			public Control getControl(Composite parent) {
				return filterItemEditor.createControl(parent, false);
			}

			@Override
			public void close() {
			}
			
			public String getInput() {
				return filterItemEditor.getInput();
			};
			
			public void setInput(String input) {
				filterItemEditor.setInput(input);
			};
		};
		
		if (helper instanceof PropertySetStructFieldSearchItemEditorHelper)
			((PropertySetStructFieldSearchItemEditorHelper)helper).init(getStructField());
		helperControl = helper.getControl(parent);
		return helperControl;
	}

	/**
	 * Delegates to the helper from the registry.
	 * @see #getControl(Composite)
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	@Override
	public ISearchFilterItem getSearchFilterItem() {
		if (helper == null)
			throw new IllegalStateException("SearchItemEditorHelper is null and can not be asked for the SearchFilterItem"); //$NON-NLS-1$
		
		return helper.getSearchFilterItem();
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#close()
	 */
	public void close() {
	}

	@Override
	public String getInput() {
		if (helper != null)
			return helper.getInput();
		return null;
	}

	@Override
	public void setInput(String input) {
		if (helper != null)
			helper.setInput(input);
	}

}
