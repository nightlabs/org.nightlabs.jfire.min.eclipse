///* *****************************************************************************
// * JFire - it's hot - Free ERP System - http://jfire.org                       *
// * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
// *                                                                             *
// * This library is free software; you can redistribute it and/or               *
// * modify it under the terms of the GNU Lesser General Public                  *
// * License as published by the Free Software Foundation; either                *
// * version 2.1 of the License, or (at your option) any later version.          *
// *                                                                             *
// * This library is distributed in the hope that it will be useful,             *
// * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
// * Lesser General Public License for more details.                             *
// *                                                                             *
// * You should have received a copy of the GNU Lesser General Public            *
// * License along with this library; if not, write to the                       *
// *     Free Software Foundation, Inc.,                                         *
// *     51 Franklin St, Fifth Floor,                                            *
// *     Boston, MA  02110-1301  USA                                             *
// *                                                                             *
// * Or get it online :                                                          *
// *     http://opensource.org/licenses/lgpl-license.php                         *
// *                                                                             *
// *                                                                             *
// ******************************************************************************/
//
//package org.nightlabs.jfire.base.ui.prop.edit;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Label;
//import org.nightlabs.base.ui.composite.XComposite;
//import org.nightlabs.jfire.prop.DataField;
//
///**
// * Abstract base composite for composites that are supposed to edit a single {@link DataField} in an <b>inline</b> style,
// * that means they consist of a label for the respective data field and a single input element like a textbox. Extending this
// * class makes it easy to create different composites that look similar according to insets and spacing.
// *
// * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
// * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
// */
//public abstract class AbstractInlineDataFieldEditComposite<Editor extends DataFieldEditor<? extends DataField>>
//extends XComposite
//{
//	private Label title;
//
//	/**
//	 * @see Composite#Composite(org.eclipse.swt.widgets.Composite, int)
//	 */
//	public AbstractInlineDataFieldEditComposite(Composite parent, int style, Editor editor) {
//		super(parent, style);
//		setLayout(getDefaultLayout());
//		// FIXME: titles used within TextDataFieldComposite always indent some pixels. Why? Marc
//		title = new Label(this, SWT.NONE);
//		title.setLayoutData(createTitleLayoutData());
//	}
//
//	protected Object createTitleLayoutData() {
//		GridData nameData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
////		nameData.grabExcessHorizontalSpace = true;
//		return nameData;
//	}
//
//	/**
//	 * Creates a standard {@link GridLayout} for DataFieldEditComposites.
//	 * @return a standard {@link GridLayout} to be used in DataFieldEditors
//	 */
//	protected GridLayout getDefaultLayout() {
//		GridLayout layout = new GridLayout();
//		layout.horizontalSpacing = 0;
//// TODO: this is a quickfix for the Formtoolkit Boarderpainter, which paints to the outside of the elements -> there needs to be space in the enclosing composite for the borders
//		layout.verticalSpacing = 4;
//		layout.marginHeight = 0;
//		// removed the marginWidth... be tight! Marc
//		//layout.marginWidth = 2;
//		layout.marginWidth = 0;
//		return layout;
//	}
//
////	protected final getInputControlLayoutData() {
////
////	}
//}
