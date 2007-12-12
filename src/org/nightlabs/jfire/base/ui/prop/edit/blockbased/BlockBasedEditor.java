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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.composite.groupedcontent.GroupedContentComposite;
import org.nightlabs.base.ui.composite.groupedcontent.GroupedContentProvider;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor
 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.EditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class BlockBasedEditor extends AbstractBlockBasedEditor {
	
	public static final String EDITORTYPE_BLOCK_BASED = "block-based"; //$NON-NLS-1$
	
	private GroupedContentComposite groupedContentComposite;
	private XComposite displayNameComp;
	private Text displayNameText;
	private Button autogenerateNameCheckbox;
	private boolean showDisplayNameComposite;
	
	private class ContentProvider implements GroupedContentProvider {
		private DataBlockGroupEditor groupEditor;
		private DataBlockGroup blockGroup;
		private IStruct struct;
		
		public ContentProvider(DataBlockGroup blockGroup, IStruct struct) {
			this.blockGroup = blockGroup;
			this.struct = struct;
		}
		
		public Image getGroupIcon() {
			return null;
		}
		public String getGroupTitle() {
			//return blockGroup.getStructBlock(getPropStructure()).getID();
			return blockGroup.getStructBlock(struct).getName().getText();
		}
		public Composite createGroupContent(Composite parent) {
			groupEditor = new DataBlockGroupEditor(struct, blockGroup, parent);
			if (changeListenerProxy.getChangeListenerProxy() != null)
				groupEditor.addPropDataBlockEditorChangedListener(changeListenerProxy.getChangeListenerProxy());
			return groupEditor;
		}
		public void refresh(DataBlockGroup blockGroup) {
			if (groupEditor != null) {
				groupEditor.refresh(struct, blockGroup);
			}
			this.blockGroup = blockGroup;
		}
		public void updateProp() {
			if (groupEditor != null) {
				groupEditor.updatePropertySet();
			}
		}
	}
	
	/**
	 * Creates a new {@link BlockBasedEditor}.
	 * @param showDisplayNameComp Indicates whether a composite to edit the display name settings of the managed property set should be displayed.
	 */
	public BlockBasedEditor(boolean showDisplayNameComp) {
		this(null, null, showDisplayNameComp);
	}
	
	/**
	 * Creates a new {@link BlockBasedEditor}.
	 * @param propSet The {@link PropertySet} to be managed.
	 * @param propStruct The {@link IStruct} of the {@link PropertySet} to be managed.
	 * @param showDisplayNameComp Indicates whether a composite to edit the display name settings of the managed property set should be displayed.
	 */
	public BlockBasedEditor(PropertySet propSet, IStruct propStruct, boolean showDisplayNameComp) {
		super(propSet, propStruct);
		this.showDisplayNameComposite = showDisplayNameComp;
	}
	
	private Map<String, ContentProvider> groupContentProvider = new HashMap<String, ContentProvider>();
	
	
	private boolean refreshing = false;
	/**
	 * Refreshes the UI-Representation of the given Property.
	 * 
	 * @param changeListener
	 */
	@Override
	public void refreshControl() {
		Display.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					refreshing = true;
					if (groupedContentComposite == null || groupedContentComposite.isDisposed())
						return;
					
					refreshDisplayNameComp();						
					
					if (!propertySet.isInflated())
						propertySet.inflate(getPropStructure(new NullProgressMonitor()));
					
					// get the ordered dataBlocks
					for (Iterator<DataBlockGroup> it = BlockBasedEditor.this.getOrderedDataBlockGroupsIterator(); it.hasNext(); ) {
						DataBlockGroup blockGroup = it.next();
						if (shouldDisplayStructBlock(blockGroup)) {
							if (!groupContentProvider.containsKey(blockGroup.getStructBlockKey())) {
								ContentProvider contentProvider = new ContentProvider(blockGroup, propertySet.getStructure());
								groupContentProvider.put(blockGroup.getStructBlockKey(),contentProvider);
								groupedContentComposite.addGroupedContentProvider(contentProvider);
							}
							else {			
								ContentProvider contentProvider = groupContentProvider.get(blockGroup.getStructBlockKey());								
								contentProvider.refresh(blockGroup);
							}
						} // if (shouldDisplayStructBlock(blockGroup)) {
					}		
					groupedContentComposite.layout();
					refreshing = false;
				}
			}
		);
	}

//	private DataBlockEditorChangedListener changeListener;
	
	public Control createControl(Composite parent, DataBlockEditorChangedListener changeListener, boolean refresh) {
		setChangeListener(changeListener);
		return createControl(parent, refresh);
	}
	
	private class ChangeListenerProxy implements DataBlockEditorChangedListener {
		private DataBlockEditorChangedListener changeListener;
		
		public void dataBlockEditorChanged(AbstractDataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
			if (!refreshing) {
				updatePropertySet();
				refreshDisplayNameComp();
				
				if (changeListener != null)
					changeListener.dataBlockEditorChanged(dataBlockEditor, dataFieldEditor);
			}
		}
		
		public void setChangeListener(DataBlockEditorChangedListener changeListener) {
			this.changeListener = changeListener;
		}
		
		public ChangeListenerProxy getChangeListenerProxy() {
			return changeListenerProxy;
		}
	}	
	private ChangeListenerProxy changeListenerProxy = new ChangeListenerProxy();
	private DisplayNameChangedListener displayNameChangedListener;
	
	/**
	 * @param changeListener The changeListener to set.
	 */
	public void setChangeListener(final DataBlockEditorChangedListener changeListener) {
		changeListenerProxy.setChangeListener(changeListener);
	}
	
	public void setDisplayNameChangedListener(DisplayNameChangedListener displayNameChangedListener) {
		this.displayNameChangedListener = displayNameChangedListener;
	}
	
	protected void fireDataBlockEditorChangedEvent(AbstractDataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		changeListenerProxy.dataBlockEditorChanged(dataBlockEditor, dataFieldEditor);
		
		if (!refreshing)
			refreshControl();
	}

	public Control createControl(Composite parent, boolean refresh) {
		if (groupedContentComposite == null) {
			if (showDisplayNameComposite) {
				displayNameComp = new XComposite(parent, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
				displayNameComp.getGridLayout().numColumns = 2;
				
				Label label = new Label(displayNameComp, SWT.NONE);
				label.setText("Name");
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 2;
				label.setLayoutData(gd);
				
				displayNameText = new Text(displayNameComp, XComposite.getBorderStyle(displayNameComp));
				displayNameText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						if (refreshing)
							return;
						propertySet.setDisplayName(displayNameText.getText());
						if (displayNameChangedListener != null)
							displayNameChangedListener.displayNameChanged(displayNameText.getText());
					}
				});				
				displayNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				autogenerateNameCheckbox = new Button(displayNameComp, SWT.CHECK);
				autogenerateNameCheckbox.setText("Autogenerate");
				
				autogenerateNameCheckbox.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						updatePropertySet();
						refreshDisplayNameComp();
					}
					public void widgetDefaultSelected(SelectionEvent e) {}
				});
			}
			
			groupedContentComposite = new GroupedContentComposite(parent, SWT.NONE, true);
			groupedContentComposite.setGroupTitle("propTail"); //$NON-NLS-1$
		}
		if (refresh)
			refreshControl();
		return groupedContentComposite;
	}

	public void disposeControl() {
		if (groupedContentComposite != null && !groupedContentComposite.isDisposed())
				groupedContentComposite.dispose();
		
		if (displayNameComp != null && !displayNameComp.isDisposed())
			displayNameComp.dispose();
		
		groupedContentComposite = null;
		displayNameComp = null;
	}

	public void updatePropertySet() {
		for (ContentProvider contentProvider : groupContentProvider.values()) {
			contentProvider.updateProp();
		}
		if (displayNameComp != null) {
			String displayName = autogenerateNameCheckbox.getSelection() ? null : displayNameText.getText();			
			getPropertySet().setAutoGenerateDisplayName(autogenerateNameCheckbox.getSelection());
			getPropertySet().setDisplayName(displayName);
		}
	}

	private void refreshDisplayNameComp() {
		refreshing = true;
		try {
			if (displayNameComp != null) {
				String oldDisplayNameText = displayNameText.getText();
				if (propertySet.getDisplayName() != null)
					displayNameText.setText(propertySet.getDisplayName());
				autogenerateNameCheckbox.setSelection(propertySet.isAutoGenerateDisplayName());
				displayNameText.setEnabled(!autogenerateNameCheckbox.getSelection());
				if (!displayNameText.getText().equals(oldDisplayNameText)) {
					if (displayNameChangedListener != null)
						displayNameChangedListener.displayNameChanged(displayNameText.getText());
				}
			}
		} finally {
			refreshing = false;
		}
	}
}
