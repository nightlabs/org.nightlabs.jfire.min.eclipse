/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorLayoutData;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.prop.DataField;

public class GenericDataBlockEditorComposite extends AbstractDataBlockEditorComposite {

	private static final Logger logger = Logger.getLogger(GenericDataBlockEditorComposite.class);
	
	/**
	 * Assumes to have a parent with GridLayout.
	 * Adds its controls to the parent.
	 * 
	 * @param parent Should be a ExpandableDataBlockGroupEditor
	 * @param style SWT-style for the container-GenericDataBlockEditorComposite
	 * @param columnHint A hint for the column count the Editor should use
	 */
	public GenericDataBlockEditorComposite(
			DataBlockEditor editor,
			Composite parent,
			int style,
			int columnHint
	) {
		super(editor, parent, style);
		// set grid data for this
		GridData thisData = new GridData(GridData.FILL_BOTH);
//		thisData.grabExcessHorizontalSpace = true;
		this.setLayoutData(thisData);

		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = columnHint;
		thisLayout.makeColumnsEqualWidth = true;
		thisLayout.marginWidth = 0;
		thisLayout.marginHeight = 0;
		setLayout(thisLayout);
		createFieldEditors();
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.IDataBlockEditorComposite#createFieldEditors()
	 */
	public void createFieldEditors() {
		for (Iterator<DataField> it = getOrderedPropDataFieldsIterator(); it.hasNext(); ) {
			DataField dataField = it.next();
			if (!hasFieldEditorFor(dataField)) {
				DataFieldEditor<DataField> fieldEditor;
				try {
					fieldEditor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
							getStruct(), ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE,
							null, dataField
					);
				} catch (DataFieldEditorNotFoundException e) {
					// could not find editor for class log the error
					logger.error("Editor not found for one field, continuing",e); //$NON-NLS-1$
					continue;
				}
				addFieldEditor(dataField, fieldEditor,true);
				// add the field editor
				fieldEditor.createControl(this);
				DataFieldEditorLayoutData layoutData = fieldEditor.getLayoutData();
				if (layoutData != null) {
					fieldEditor.getControl().setLayoutData(DataFieldEditorLayoutData.toGridData(layoutData));
				} else {
					fieldEditor.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				}
			}

			DataFieldEditor fieldEditor = getFieldEditor(dataField);
			if (getStruct() != null)
				fieldEditor.setData(getStruct(), dataField);
			fieldEditor.refresh();
		}
	}
}