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

import java.util.Arrays;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.ComboComposite;
import org.nightlabs.jdo.search.ISearchFilterItem;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.search.TextStructFieldSearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextStructFieldSearchItemEditorHelper
		extends PropertySetStructFieldSearchItemEditorHelper {
	
	public static class Factory implements PropertySetSearchFilterItemEditorHelperFactory<TextStructFieldSearchItemEditorHelper> {
		public TextStructFieldSearchItemEditorHelper createHelper() {
			return new TextStructFieldSearchItemEditorHelper();
		}
	}

	private Composite helperComposite;
	private ComboComposite<MatchType> comboMatchType;
	private Text textNeedle;

	/**
	 * 
	 */
	public TextStructFieldSearchItemEditorHelper() {
		super();
	}

	/**
	 * @param personStructField
	 */
	public TextStructFieldSearchItemEditorHelper(
			StructField structField) {
		super(structField);
	}

//	protected class MatchTypeOrderEntry {
//		MatchType matchType;
//		String displayName;
//		public MatchTypeOrderEntry(MatchType matchType, String displayName) {
//			this.matchType = matchType;
//			this.displayName = displayName;
//		}
//	}
//	private MatchTypeOrderEntry[] matchTypeOrder = new MatchTypeOrderEntry[7];
//	private MatchTypeOrderEntry setMatchTypeOrderEntry(int idx, MatchType matchType) {
//		String displayName = matchType.getLocalisedName();
//		MatchTypeOrderEntry result = new MatchTypeOrderEntry(matchType, displayName);
//		matchTypeOrder[idx] = result;
//		return result;
//	}
	
	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control getControl(Composite parent) {
		if (helperComposite == null) {
			helperComposite = new Composite(parent,SWT.NONE);
			GridLayout wrapperLayout = new GridLayout();
			wrapperLayout.numColumns = 2;
			wrapperLayout.makeColumnsEqualWidth = true;
			helperComposite.setLayout(wrapperLayout);
			helperComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			comboMatchType = new ComboComposite<MatchType>(helperComposite,SWT.READ_ONLY, new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((MatchType) element).getLocalisedName();
				}
			});
			
			comboMatchType.setInput(Arrays.asList(MatchType.values()));
//			comboMatchType.add(setMatchTypeOrderEntry(0, MatchType.MATCHTYPE_CONTAINS).displayName);
//			comboMatchType.add(setMatchTypeOrderEntry(1, MatchType.MATCHTYPE_NOTCONTAINS).displayName);
//			comboMatchType.add(setMatchTypeOrderEntry(2, MatchType.MATCHTYPE_BEGINSWITH).displayName);
//			comboMatchType.add(setMatchTypeOrderEntry(3, MatchType.MATCHTYPE_ENDSWITH).displayName);
//			comboMatchType.add(setMatchTypeOrderEntry(4, MatchType.MATCHTYPE_EQUALS).displayName);
//			comboMatchType.add(setMatchTypeOrderEntry(5, MatchType.MATCHTYPE_MATCHES).displayName);
//			comboMatchType.add(setMatchTypeOrderEntry(6, MatchType.MATCHTYPE_NOTEQUALS).displayName);
			
			GridData gdCombo = new GridData();
			gdCombo.grabExcessHorizontalSpace = true;
			gdCombo.horizontalAlignment = GridData.FILL;
			comboMatchType.setLayoutData(gdCombo);
			comboMatchType.setSelection(MatchType.CONTAINS);
			
			textNeedle = new Text(helperComposite,SWT.BORDER);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			textNeedle.setLayoutData(gd);
		}
			
		return helperComposite;
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	@Override
	public ISearchFilterItem getSearchFilterItem() {
		StructFieldID id = StructFieldID.create(
			personStructField.getStructBlockOrganisationID(),
			personStructField.getStructBlockID(),
			personStructField.getStructFieldOrganisationID(),
			personStructField.getStructFieldID()
		);
		MatchType matchType = comboMatchType.getSelectedElement(); // matchTypeOrder[comboMatchType.getSelectionIndex()].matchType;
		String needle = textNeedle.getText();
		TextStructFieldSearchFilterItem result = new TextStructFieldSearchFilterItem(
			id,
			matchType,
			needle
		);
		return result;
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditorHelper#close()
	 */
	public void close() {
	}

}
