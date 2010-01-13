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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.edit.IEntryEditor;
import org.nightlabs.jfire.base.ui.edit.TextEditComposite;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.structfield.TextStructField;

/**
 * Represents an editor for {@link TextDataField} within a
 * block based ExpandableBlocksEditor.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class TextDataFieldEditor extends AbstractDataFieldEditor<TextDataField> implements DataFieldEditor<TextDataField> {
	
	private TextEditComposite textEditComposite;

	public TextDataFieldEditor(IStruct struct, TextDataField data) {
		super(struct, data);
	}

	public static class Factory extends AbstractDataFieldEditorFactory<TextDataField> {
		@Override
		public Class<TextDataField> getPropDataFieldType() {
			return TextDataField.class;
		}
		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}
		@Override
		public DataFieldEditor<TextDataField> createPropDataFieldEditor(IStruct struct, TextDataField data) {
			return new TextDataFieldEditor(struct, data);
		}
	}

	@Override
	public Control createControl(Composite parent) {
		if (textEditComposite == null) {
			textEditComposite = createTextEditComposite(parent);
			textEditComposite.addModificationListener(getModifyListener());
		}
		
		refresh();
		
		return textEditComposite;
	}
	
	/**
	 * Creates the composite to edit the text entry with the given <code>parent</code>.<br/>
	 * Subclasses may override this method if they want to create a {@link TextEditComposite} with different properties.
	 * 
	 * @param parent The parent of the composite to be created.
	 * @return The composite to edit the text entry.
	 */
	protected TextEditComposite createTextEditComposite(Composite parent) {
		return new TextEditComposite(parent, SWT.NONE, getLineCount());
	}

	@Override
	public Control getControl() {
		return textEditComposite;
	}

	@Override
	public void updatePropertySet() {
		getDataField().setText(textEditComposite.getContent());
	}

	@Override
	public void doRefresh() {
		if (textEditComposite != null) {
			if (getStructField() != null && getDataField() != null) {
				textEditComposite.setTitle("&" + getStructField().getName().getText()); //$NON-NLS-1$
				textEditComposite.setContent(getDataField().getText());
			}
		}
	}
	
//	@Override
//	protected void handleManagedBy(String managedBy) {
//		super.handleManagedBy(managedBy);
		// The super method disables the whole composite thus making it impossible to select (and copy to clipboard) some text.
		// Thus, we make the text read-only.
//		textEditComposite.setEnabled(managedBy == null, String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.TextDataFieldComposite.managedBy.tooltip"), managedBy)); //$NON-NLS-1$
//		fieldText.setEditable(managedBy == null);
//		if (managedBy != null)
//			setToolTipText(String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.TextDataFieldComposite.managedBy.tooltip"), managedBy));
//		else
//			setToolTipText(null);
//	}

//	private boolean isMultiLine() {
//		StructField<?> structField = getStructField();
//		if (structField instanceof TextStructField) {
//			return ((TextStructField) structField).getLineCount() > 1;
//		}
//		return false;
//	}

	private int getLineCount() {
		StructField<?> structField = getStructField();
		if (structField instanceof TextStructField) {
			return Math.max(((TextStructField) structField).getLineCount(), 1);
		}
		return 1;
	}

	@Override
	protected IEntryEditor getEntryViewer() {
		return textEditComposite;
	}
}
