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


	/**
	 * @param parent
	 * @param style
	 */
	public DataBlockGroupEditor(
			IStruct struct,
			DataBlockGroup blockGroup,
			Composite parent,
			IValidationResultManager validationResultManager
	) {
		super(parent, SWT.NONE);
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

	private List<AbstractDataBlockEditor> dataBlockEditors = new LinkedList<AbstractDataBlockEditor>();

	public void refresh(IStruct struct, DataBlockGroup blockGroup) {
		this.blockGroup = blockGroup;
		this.struct = struct;
		createDataBlockEditors(struct, content);
		scrolledComposite.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		assert(dataBlockEditors.size() == blockGroup.getDataBlocks().size());

		Iterator<AbstractDataBlockEditor> editorIt = dataBlockEditors.iterator();
		Iterator<DataBlock> blockIt = blockGroup.getDataBlocks().iterator();

		while (editorIt.hasNext()) {
			AbstractDataBlockEditor dataBlockEditor = editorIt.next();
			dataBlockEditor.setValidationResultManager(validationResultManager);
			DataBlock block = blockIt.next();
			dataBlockEditor.refresh(struct, block);
		}

//		for (int i=0; i<dataBlockEditors.size(); i++){
//			AbstractDataBlockEditor dataBlockEditor = (AbstractDataBlockEditor)dataBlockEditors.get(i);
//			try {
//				dataBlockEditor.refresh(struct, blockGroup.getDataBlock(i));
//			} catch (DataBlockNotFoundException e) {
//				IllegalStateException ill = new IllegalStateException("No datablock found on pos "+i); //$NON-NLS-1$
//				ill.initCause(e);
//				throw ill;
//			}
//		}
		content.layout(true, true);
//		scrolledComposite.layout(true, true);
	}

//	private SelectionListener addListener = new SelectionListener() {
//		public void widgetDefaultSelected(SelectionEvent e) {}
//		public void widgetSelected(SelectionEvent e) {
//			Button addButton = (Button) e.widget;
//			int index = ((Integer) addButton.getData()) + 1;
//			try {
//				blockGroup.addDataBlock(struct, index).explode(struct);
//			} catch (DataBlockUniqueException e1) {
//				e1.printStackTrace();
//			}
//			refresh(struct, blockGroup);
//		}
//	};
//
//	private SelectionListener removeListener = new SelectionListener() {
//		public void widgetDefaultSelected(SelectionEvent e) {}
//		public void widgetSelected(SelectionEvent e) {
//			Button removeButton = (Button) e.widget;
//			try {
//				blockGroup.removeDataBlock(blockGroup.getBlockByIndex((Integer) removeButton.getData()));
//			} catch (DataBlockRemovalException e1) {
//				e1.printStackTrace();
//			}
//			refresh(struct, blockGroup);
//		}
//	};

	protected void createDataBlockEditors(final IStruct _struct, Composite wrapperComp) {
		for (Composite comp : blockComposites) {
			comp.dispose();
		}
		dataBlockEditors.clear();
		blockComposites.clear();

		List<DataBlock> dataBlocks = blockGroup.getDataBlocks();
		for (int i = 0; i < dataBlocks.size(); i++) {
			DataBlock dataBlock = dataBlocks.get(i);
			Composite wrapper = new XComposite(wrapperComp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
			blockComposites.add(wrapper);

			if (i > 0) {
				Label sep = new Label(wrapper, SWT.SEPARATOR | SWT.HORIZONTAL);
				GridData gd = new GridData(SWT.HORIZONTAL | SWT.FILL);
				gd.horizontalSpan = 2;
				sep.setLayoutData(gd);
			}

			AbstractDataBlockEditor blockEditor = DataBlockEditorFactoryRegistry.sharedInstance().createDataBlockEditor(
					_struct,
					dataBlock,
					wrapper,
					SWT.NONE,
					2
			);
			blockEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
//			GridData gd = (GridData) blockEditor.getLayoutData();
//			gd.verticalAlignment = SWT.BEGINNING;
//			blockEditor.setLayoutData(gd);
			blockEditor.addDataBlockEditorChangedListener(this);
			dataBlockEditors.add(blockEditor);

			if (! _struct.getStructBlock(blockGroup).isUnique()) {
				AddOrRemoveDataBlockGroupComposite manager = new AddOrRemoveDataBlockGroupComposite(wrapper, dataBlock, i);
				manager.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
				manager.setListener(new AddOrRemoveDataBlockGroupComposite.Listener() {
					public void addDataBlock(int index) {
						try {
							for (AbstractDataBlockEditor editor : dataBlockEditors)
								editor.updatePropertySet();

							blockGroup.addDataBlock(_struct.getStructBlock(blockGroup), index);
							refresh(struct, blockGroup);
						} catch (DataBlockUniqueException e) {
							e.printStackTrace();
						}
					}
					public void removeDataBlock(DataBlock block) {
						try {
							for (AbstractDataBlockEditor editor : dataBlockEditors)
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

	private ListenerList changeListener = new ListenerList();
	public synchronized void addPropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}

	public synchronized void removePropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}

	protected synchronized void notifyChangeListeners(AbstractDataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		Object[] listeners = changeListener.getListeners();
		for (Object listener : listeners) {
			((DataBlockEditorChangedListener) listener).dataBlockEditorChanged(dataBlockEditor,dataFieldEditor);
		}
	}

	/**
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorChangedListener#dataBlockEditorChanged(org.nightlabs.jfire.base.admin.ui.widgets.prop.edit.AbstractDataBlockEditor, org.nightlabs.jfire.base.admin.ui.widgets.prop.edit.AbstractPropDataFieldEditor)
	 */
	public void dataBlockEditorChanged(AbstractDataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		notifyChangeListeners(dataBlockEditor,dataFieldEditor);
	}

	public void updatePropertySet() {
		for (AbstractDataBlockEditor blockEditor : dataBlockEditors) {
			blockEditor.updatePropertySet();
		}
	}

	public IStruct getStruct() {
		return struct;
	}

}
