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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.exception.DataBlockRemovalException;
import org.nightlabs.jfire.prop.exception.DataBlockUniqueException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class DataBlockGroupEditor
extends XComposite
implements DataBlockEditorChangedListener
{
	private DataBlockGroup blockGroup;

	private IStruct struct;

	private List<Composite> blockComposites = new LinkedList<Composite>();

	private IValidationResultManager validationResultManager;

	public DataBlockGroupEditor(
			IStruct struct,
			DataBlockGroup blockGroup,
			Composite parent,
			IValidationResultManager validationResultManager
	) {
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		this.blockGroup = blockGroup;

		scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		content = new XComposite(scrolledComposite, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		scrolledComposite.setContent(content);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.validationResultManager = validationResultManager;

		refresh(struct, blockGroup);
	}

	private ScrolledComposite scrolledComposite;
	private XComposite content;

	private List<DataBlockEditor> dataBlockEditors = new LinkedList<DataBlockEditor>();

	public void refresh(IStruct struct, DataBlockGroup blockGroup) {
		this.blockGroup = blockGroup;
		this.struct = struct;
		createDataBlockEditors(struct, content);
		scrolledComposite.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		assert(dataBlockEditors.size() == blockGroup.getDataBlocks().size());

		Iterator<DataBlockEditor> editorIt = dataBlockEditors.iterator();
//		Iterator<DataBlock> blockIt = blockGroup.getDataBlocks().iterator();

		while (editorIt.hasNext()) {
			DataBlockEditor dataBlockEditor = editorIt.next();
//			dataBlockEditor.setValidationResultManager(validationResultManager);
//			DataBlock block = blockIt.next();
//			dataBlockEditor.setData(struct, block);
		}
		content.layout(true, true);
	}

	
	protected void createDataBlockEditors(final IStruct _struct, Composite wrapperComp) {
		if (blockGroup.getDataBlocks().size() == dataBlockEditors.size()) {
			updateBlockEditors(_struct, wrapperComp);
		} else {
			reCreateDataBlockEditors(_struct, wrapperComp);
		}
	}
	
	protected void reCreateDataBlockEditors(final IStruct _struct, Composite wrapperComp) {
		for (Composite comp : blockComposites) {
			comp.dispose();
		}
		dataBlockEditors.clear();
		blockComposites.clear();

		List<DataBlock> dataBlocks = blockGroup.getDataBlocks();
		for (int i = 0; i < dataBlocks.size(); i++) {
			DataBlock dataBlock = dataBlocks.get(i);
			Composite wrapper = new XComposite(wrapperComp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
			blockComposites.add(wrapper);

			if (i > 0) {
				Label sep = new Label(wrapper, SWT.SEPARATOR | SWT.HORIZONTAL);
				GridData gd = new GridData(SWT.HORIZONTAL | SWT.FILL);
				gd.horizontalSpan = 2;
				sep.setLayoutData(gd);
			}

			DataBlockEditor blockEditor = DataBlockEditorFactoryRegistry.sharedInstance().createDataBlockEditor(
					_struct,
					dataBlock
			);
			blockEditor.setData(_struct, dataBlock);
			Control blockEditorControl = blockEditor.createControl(wrapper);
			blockEditorControl.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING));
			
			blockEditor.addDataBlockEditorChangedListener(this);
			blockEditor.setValidationResultManager(validationResultManager);
			dataBlockEditors.add(blockEditor);

			if (! _struct.getStructBlock(blockGroup).isUnique()) {
				AddOrRemoveDataBlockGroupComposite manager = new AddOrRemoveDataBlockGroupComposite(wrapper, dataBlock, i);
				manager.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
				manager.setListener(new AddOrRemoveDataBlockGroupComposite.Listener() {
					public void addDataBlock(int index) {
						try {
							for (DataBlockEditor editor : dataBlockEditors)
								editor.updatePropertySet();

							blockGroup.addDataBlock(_struct.getStructBlock(blockGroup), index);
							refresh(struct, blockGroup);
						} catch (DataBlockUniqueException e) {
							e.printStackTrace();
						}
					}
					public void removeDataBlock(DataBlock block) {
						try {
							for (DataBlockEditor editor : dataBlockEditors)
								editor.updatePropertySet();

							blockGroup.removeDataBlock(block);
							refresh(struct, blockGroup);
						} catch (DataBlockRemovalException e) {
							e.printStackTrace();
						}
					}
				});

				if (blockGroup.getDataBlocks().size() == 1) {
					manager.getRemoveButton().setEnabled(false);
				}
			}
		}
	}
	
	protected void updateBlockEditors(final IStruct _struct, Composite wrapperComp) {
		List<DataBlock> dataBlocks = blockGroup.getDataBlocks();
		for (int i = 0; i < dataBlocks.size(); i++) {
			DataBlock dataBlock = dataBlocks.get(i);
			dataBlockEditors.get(i).setData(_struct, dataBlock);
		}
	}
	
	private ListenerList changeListener = new ListenerList();
	public synchronized void addPropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}

	public synchronized void removePropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}

	protected synchronized void notifyChangeListeners(DataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		Object[] listeners = changeListener.getListeners();
		for (Object listener : listeners) {
			((DataBlockEditorChangedListener) listener).dataBlockEditorChanged(dataBlockEditor,dataFieldEditor);
		}
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener#dataBlockEditorChanged(org.nightlabs.jfire.base.admin.ui.widgets.prop.edit.AbstractDataBlockEditor, org.nightlabs.jfire.base.admin.ui.widgets.prop.edit.AbstractPropDataFieldEditor)
	 */
	public void dataBlockEditorChanged(DataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		notifyChangeListeners(dataBlockEditor,dataFieldEditor);
	}

	public void updatePropertySet() {
		for (DataBlockEditor blockEditor : dataBlockEditors) {
			blockEditor.updatePropertySet();
		}
	}

	public IStruct getStruct() {
		return struct;
	}

}
