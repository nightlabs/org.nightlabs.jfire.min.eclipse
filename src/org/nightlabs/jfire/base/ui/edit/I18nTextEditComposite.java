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

package org.nightlabs.jfire.base.ui.edit;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditorMultiLine;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.i18n.I18nText;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class I18nTextEditComposite
extends AbstractInlineEditComposite {

	private I18nTextEditor i18nTextEditor;
	private int lineCount;
	
	/**
	 * Assumes to have a parent composite with GridLaout and
	 * adds it own GridData.
	 * @param editor
	 * @param parent
	 * @param style
	 */
	public I18nTextEditComposite(Composite parent, int style, int lineCount) {
		super(parent, style);
		
		this.lineCount = lineCount;
		
//		GridLayout layout = new GridLayout();
//		setLayout(layout);
//		layout.horizontalSpacing = 2;
//		layout.verticalSpacing = 0;
//		layout.marginHeight = 2;
//		layout.marginWidth = 2;
//		GridData gridData = new GridData(GridData.FILL_BOTH);
//		setLayoutData(gridData);
//
//		fieldName = new Label(this, SWT.NONE);
//		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
//		nameData.grabExcessHorizontalSpace = true;
//		fieldName.setLayoutData(nameData);
//
		createEditor();
	}
	
	private void createEditor() {
//		I18nTextStructField field = (I18nTextStructField) getEditor().getStructField();
		if (lineCount > 1)
			i18nTextEditor = new I18nTextEditorMultiLine(this, null, null, lineCount);
		else
			i18nTextEditor = new I18nTextEditor(this);
		
		i18nTextEditor.setI18nText(null, EditMode.BUFFERED);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		i18nTextEditor.setEditable(true);
		if (i18nTextEditor instanceof Composite) {
			((Composite)i18nTextEditor).setEnabled(true);
			((Composite)i18nTextEditor).setLayoutData(textData);
		}
		
		i18nTextEditor.addModifyListener(getSwtModifyListener());
	}
	
	public void setInput(I18nText input) {
		i18nTextEditor.getI18nText().copyFrom(input);
		i18nTextEditor.refresh();
	}

//	/**
//	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractInlineDataFieldEditComposite#refresh()
//	 */
//	@Override
//	public void _refresh() {
//		if (i18nTextEditor != null)
//			i18nTextEditor.dispose();
//
//		createEditor(modifyListener);
//
////		StructField field = getEditor().getStructField();
////		fieldName.setText(field.getName().getText());
//		i18nTextEditor.getI18nText().copyFrom(getEditor().getDataField().getI18nText());
//		i18nTextEditor.refresh();
//		// TODO set the text fields maximum line count to the one given by the struct field
//		// ((TextStructField)editor.getDataField().getStructField()).getLineCount();
//	}
	
	public void updateFieldText(I18nText fieldText) {
		i18nTextEditor.getI18nText().copyTo(fieldText);
	}
	
	@Override
	public void dispose() {
		i18nTextEditor.removeModifyListener(getSwtModifyListener());
		super.dispose();
	}
}
