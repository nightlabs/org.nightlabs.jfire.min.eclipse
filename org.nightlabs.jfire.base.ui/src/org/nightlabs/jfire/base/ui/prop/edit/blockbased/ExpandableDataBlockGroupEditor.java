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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class ExpandableDataBlockGroupEditor
extends ExpandableComposite
{
	public void dataBlockEditorChanged(DataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
	}

	
	private IDataBlockGroupEditor blockGroupEditor;
//	/**
//	 * The class whose properties are to be edited.
//	 */
//	private Class<?> linkClass;

	/**
	 * @param parent
	 * @param style
	 * @param linkClass The class whose properties are to be edited
	 */
	public ExpandableDataBlockGroupEditor(
			IStruct struct,
			DataBlockGroup blockGroup,
			Composite parent,
			IValidationResultHandler validationResultHandler
	) {
		super(parent, SWT.NONE);
		blockGroupEditor = DataBlockGroupEditorFactoryRegistry.sharedInstance().createDataBlockGroupEditor(); 
		Composite editorControl = blockGroupEditor.createControl(this, blockGroup);
		blockGroupEditor.setValidationResultHandler(validationResultHandler);
		blockGroupEditor.refresh(blockGroup);
		
		GridLayout thisLayout = new GridLayout();
		thisLayout.verticalSpacing = 0;
		thisLayout.horizontalSpacing= 0;
		setLayout(thisLayout);

		//		TableWrapData thisLayoutData = new TableWrapData();
		GridData thisLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		setLayoutData(thisLayoutData);
		setClient(editorControl);
		// TODO: genauer abchecken, was hier läuft :-)
//		StructProvider provider = (StructProvider)PropStructProviderRegistry.sharedInstance().getPropStructProvider(linkClass);

//		IStruct structure = provider.getStruct();
//		IStruct structure = StructLocalDAO.sharedInstance().getStructLocal(linkClass.getName());
		if (blockGroup.getStructBlock(struct).getName().getText() == null)
			setText(blockGroup.getStructBlock(struct).getStructBlockID());
		else
			setText(blockGroup.getStructBlock(struct).getName().getText());

		addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if (ExpandableDataBlockGroupEditor.this.owner != null)
					ExpandableDataBlockGroupEditor.this.owner.reflow(true);
			}
		});

	}

	public void updateProp() {
		blockGroupEditor.updatePropertySet();
	}

	public synchronized void addPropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		blockGroupEditor.addDataBlockEditorChangedListener(listener);
	}
	public synchronized void removePropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		blockGroupEditor.removeDataBlockEditorChangedListener(listener);
	}

	public synchronized void addPropDataBlockGroupEditorChangedListener(IDataBlockGroupEditorChangedListener listener) {
		blockGroupEditor.addDataBlockGroupEditorChangedListener(listener);
	}
	public synchronized void removePropDataBlockGroupEditorChangedListener(IDataBlockGroupEditorChangedListener listener) {
		blockGroupEditor.removeDataBlockGroupEditorChangedListener(listener);
	}
	
	public void refresh(DataBlockGroup blockGroup) {
	}

	private ScrolledForm owner = null;

	public void setOwner(ScrolledForm owner) {
		this.owner = owner;
	}

}
