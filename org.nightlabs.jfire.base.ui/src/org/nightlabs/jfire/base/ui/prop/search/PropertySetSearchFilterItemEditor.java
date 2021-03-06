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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.query.ui.search.SearchFilterItemEditor;
import org.nightlabs.jdo.search.ISearchFilterItem;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Concrete SearchFilterItemEditor that is capable of building one {@link ISearchFilterItem}. It
 * therefore maintains a list of {@link PropertySetStructFieldSearchItemEditorHelper}s and presents
 * a user a combo will all available. The selected editor-helper is used to build the filter-item.
 * <p>
 * The structure to search in is defined by the StructLocal you
 * set for this editor {@link #setStructLocalID(StructLocalID)}.
 * </p>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetSearchFilterItemEditor extends SearchFilterItemEditor implements SelectionListener{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PropertySetSearchFilterItemEditor.class);

	private XComposite wrapper;
	private List<PropertySetStructFieldSearchItemEditorHelper> searchFieldList;
	private Combo comboSearchField;
	private StructLocalID structLocalID;

	/**
	 * Create a new {@link PropertySetSearchFilterItemEditor}.
	 * 
	 * @param structLocalID The id of the StructLocal to get the list of StructFields from that the
	 *            user will be able to search in.
	 */
	public PropertySetSearchFilterItemEditor(StructLocalID structLocalID) {
		this.structLocalID = structLocalID;
	}
	
	/**
	 * Create a new {@link PropertySetSearchFilterItemEditor}.
	 */ 
	public PropertySetSearchFilterItemEditor() {
	}
	
	/**
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterItemEditor#getControl(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	public Control getControl(Composite parent) {
		if (wrapper == null) {
			wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
			GridLayout wrapperLayout = (GridLayout)wrapper.getLayout();
			wrapperLayout.numColumns = 2;
			wrapperLayout.makeColumnsEqualWidth = false;

			comboSearchField = new Combo(wrapper, SWT.DROP_DOWN | SWT.READ_ONLY);
			GridData gdCombo = new GridData();
			gdCombo.grabExcessHorizontalSpace = false;
			gdCombo.horizontalAlignment = GridData.FILL;
			comboSearchField.setLayoutData(gdCombo);

			comboSearchField.addSelectionListener(this);
			
			fillSearchFieldCombo();
		}
		return wrapper;
	}

	public void fillSearchFieldCombo() {
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.PropertySetSearchFilterItemEditor.fillSearchFieldCombo.job.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				if (searchFieldList == null) {
					try {
						searchFieldList = buildSearchFieldList(monitor);
					}
					catch (Throwable t) {
						searchFieldList = null;
						throw new RuntimeException(t);
					}
				}
				comboSearchField.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (comboSearchField.isDisposed())
							return;

						for (int i = 0; i<searchFieldList.size()-1; i++) {
							PropertySetSearchFilterItemEditorHelper helper = searchFieldList.get(i);
							comboSearchField.add(helper.getDisplayName());
						}
						comboSearchField.select(0);
						onComboChange();
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	/**
	 * Builds a list of {@link PropertySetSearchFilterItemEditorHelper}s that are used to build the
	 * contents of the search field combo and the right part of the editor. The
	 * {@link StructLocalID} this editor was constructed with is used to get a StructLocal, better
	 * all StructFields, the user can search in.
	 * 
	 * @return The list of {@link PropertySetSearchFilterItemEditorHelper} for all StructFields in
	 *         the StructLocal where an field-editor can be created using the
	 *         {@link StructFieldSearchFilterEditorRegistry}.
	 */
	@SuppressWarnings("unchecked")
	protected List<PropertySetStructFieldSearchItemEditorHelper> buildSearchFieldList(ProgressMonitor monitor) {
		List<PropertySetStructFieldSearchItemEditorHelper> helperList = new ArrayList<PropertySetStructFieldSearchItemEditorHelper>();
		if (getStructLocalID() == null) {
			throw new IllegalStateException("This PropertySetSearchFilterItemEditor does not have a StructLocalID set it therefore does not know about its structure. Please set the StructLocalID first."); //$NON-NLS-1$
		}
		IStruct struct = StructLocalDAO.sharedInstance().getStructLocal(getStructLocalID(), monitor);
		for (StructBlock structBlock : struct.getStructBlocks()) {
			for (StructField<?> structField : structBlock.getStructFields()) {
				if (StructFieldSearchFilterEditorRegistry.sharedInstance().hasEditor((Class<? extends StructField<?>>) structField.getClass()))
					helperList.add(new PropertySetStructFieldSearchItemEditorManager(structField));
			}
		}
		return helperList;
	}

	/**
	 * Delegates to the current PropertySetSearchFilterItemEditorHelper.
	 *
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterItemEditor#getSearchFilterItem()
	 */
	@Override
	public ISearchFilterItem getSearchFilterItem() {
		return getCurrentHelper().getSearchFilterItem();
	}


	private PropertySetSearchFilterItemEditorHelper lastHelper;
	private int lastIdx = -1;

	private PropertySetSearchFilterItemEditorHelper getCurrentHelper() {
		int idx = comboSearchField.getSelectionIndex();
		if ((idx < 0) || (idx >= searchFieldList.size()))
			throw new ArrayIndexOutOfBoundsException("Selection index of search field combo is out of range of searchFieldList.S"); //$NON-NLS-1$
		return searchFieldList.get(idx);
	}

	private void onComboChange() {
		int idx = comboSearchField.getSelectionIndex();
		if (idx == lastIdx)
			return;
		if (idx < 0)
			return;
		PropertySetSearchFilterItemEditorHelper helper = getCurrentHelper();
		String lastInput = null;
		if (lastHelper != null) {
			lastInput = lastHelper.getInput();
			lastHelper.close();
			try {
				lastHelper.getControl(null).dispose();
			} catch (Throwable t) {
				logger.error("Error disposing helper control.",t); //$NON-NLS-1$
			}
		}
 		helper.getControl(wrapper);
 		helper.setInput(lastInput);
		wrapper.layout();
		lastIdx = idx;
		lastHelper = helper;
	}
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent evt) {
		if (evt.getSource().equals(comboSearchField)) {
			onComboChange();
		}
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	/**
	 * @see org.nightlabs.jdo.query.ui.search.SearchFilterItemEditor#close()
	 */
	@Override
	public void close() {
		comboSearchField.removeSelectionListener(this);
	}

	@Override
	public Control createControl(Composite parent, boolean showTitle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setStructLocalID(StructLocalID structLocalID) {
		this.structLocalID = structLocalID;
	}
	
	public StructLocalID getStructLocalID() {
		return structLocalID;
	}
}
