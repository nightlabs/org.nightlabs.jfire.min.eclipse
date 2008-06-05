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

package org.nightlabs.jfire.base.ui.person.edit.blockbased.special;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangeListener;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorFactory;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CommentDataBlockEditor extends AbstractDataBlockEditor
	implements
		ModifyListener,
		DataFieldEditor // FIXME: Why the hell does a composite implement this interface?!
{
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CommentDataBlockEditor.class);

	private Text textComment;
//	private Label labelTitle;
//	private Composite wrapper;
	
	public CommentDataBlockEditor(IStruct struct, DataBlock dataBlock, Composite parent, int style) {
		super(struct, dataBlock, parent, style);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout thisLayout = new GridLayout();
		thisLayout.horizontalSpacing = 2;
		thisLayout.verticalSpacing = 2;
		thisLayout.marginWidth = 0;
		thisLayout.marginHeight = 0;
		this.setLayout(thisLayout);
		refresh(struct, dataBlock);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor#refresh(org.nightlabs.jfire.prop.IStruct, org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	public void refresh(IStruct struct, DataBlock block) {
		this.dataBlock = block;
		try {
			commentData = (TextDataField)dataBlock.getDataField(PersonStruct.COMMENT_COMMENT);
			refresh();
		} catch (DataFieldNotFoundException e) {
			logger.error("DataField not found. ",e); //$NON-NLS-1$
			commentData = null;
		}
	}

	private boolean refreshing = false;

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent evt) {
		setChanged(true);
	}

// Commented - seems not to be needed anymore. Marc
//	/**
//	 * @see org.nightlabs.jfire.base.ui.person.edit.DataFieldEditor#getTargetDataType()
//	 */
//	public Class getTargetDataType() {
//		return TextDataField.class;
//	}
//
//	/**
//	 * @see org.nightlabs.jfire.base.ui.person.edit.DataFieldEditor#getEditorType()
//	 */
//	public String getEditorType() {
//		return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		if (textComment == null) {
//			labelTitle = new Label(parent,SWT.NONE);
//			labelTitle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			textComment = new Text(parent, SWT.MULTI | XComposite.getBorderStyle(parent) | SWT.V_SCROLL | SWT.H_SCROLL);
			GridData commentLData = new GridData();
			commentLData.grabExcessHorizontalSpace = true;
			commentLData.grabExcessVerticalSpace = true;
			commentLData.horizontalAlignment = GridData.FILL;
			commentLData.verticalAlignment = GridData.FILL;
			commentLData.heightHint = 100;
			textComment.setLayoutData(commentLData);
		}
		return this;
	}

	public DataFieldEditor getNewEditorInstance(IStruct struct, DataField data) {
		return new CommentDataBlockEditor(struct, dataBlock,getParent(),getStyle());
	}

	private TextDataField commentData;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#refresh()
	 */
	@Override
	public void refresh() {
		refreshing = true;
		try {
			createControl(this);
			if (commentData != null) {
				if (commentData.getText() == null)
					textComment.setText(""); //$NON-NLS-1$
				else
					textComment.setText(commentData.getText());
			}
		} finally {
			refreshing = false;
		}
	}

	private List fieldEditorChangeListener = new LinkedList();
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#addDataFieldEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangeListener)
	 */
	@Override
	public void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListener.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#removeDataFieldEditorChangedListener(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangeListener)
	 */
	@Override
	public void removeDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListener.remove(listener);
	}

	
	private boolean changed = false;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#setChanged(boolean)
	 */
	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (!refreshing)
			notifyChangeListeners(this);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor#notifyChangeListeners(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor)
	 */
	@Override
	protected synchronized void notifyChangeListeners(DataFieldEditor dataFieldEditor) {
		if (refreshing)
			return;
		// first notify fieldEditorChangeListener
		for (Iterator iter = fieldEditorChangeListener.iterator(); iter.hasNext();) {
			DataFieldEditorChangeListener listener = (DataFieldEditorChangeListener) iter.next();
			listener.dataFieldEditorChanged(this);
		}
		// then blockEditorChangeListener
		super.notifyChangeListeners(dataFieldEditor);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#isChanged()
	 */
	@Override
	public boolean isChanged() {
		return changed;
	}

	public static class Factory implements DataBlockEditorFactory {
		/* (non-Javadoc)
		 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorFactory#getProviderStructBlockID()
		 */
		@Override
		public StructBlockID getProviderStructBlockID() {
			return PersonStruct.COMMENT;
		}
		
		/* (non-Javadoc)
		 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockEditorFactory#createPropDataBlockEditor(org.nightlabs.jfire.prop.IStruct, org.nightlabs.jfire.prop.DataBlock, org.eclipse.swt.widgets.Composite, int)
		 */
		@Override
		public AbstractDataBlockEditor createPropDataBlockEditor(IStruct struct, DataBlock dataBlock, Composite parent, int style) {
			return new CommentDataBlockEditor(struct, dataBlock,parent,style);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getControl()
	 */
	@Override
	public Control getControl() {
		return this;
	}

	protected DataFieldEditorFactory factory;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#setPropDataFieldEditorFactory(org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorFactory)
	 */
	@Override
	public void setPropDataFieldEditorFactory(DataFieldEditorFactory factory) {
		this.factory = factory;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getPropDataFieldEditorFactory()
	 */
	@Override
	public DataFieldEditorFactory getPropDataFieldEditorFactory() {
		return factory;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#setData(org.nightlabs.jfire.prop.IStruct, org.nightlabs.jfire.prop.DataField)
	 */
	@Override
	public void setData(IStruct struct, DataField data) {
		commentData = (TextDataField)data;
		refresh();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getDataField()
	 */
	@Override
	public DataField getDataField() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getStructField()
	 */
	@Override
	public StructField getStructField() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor#updatePropertySet()
	 */
	@Override
	public void updatePropertySet() {
		commentData.setText(textComment.getText());
	}
}
