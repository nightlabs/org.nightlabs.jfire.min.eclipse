package org.nightlabs.jfire.base.ui.prop.config;

import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.JDOHelper;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.prop.structedit.StructFieldNode;
import org.nightlabs.jfire.base.ui.prop.structedit.StructTreeComposite;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutEntry;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Dialog used internally to add a new StructField or Separator entry to a {@link PropertySetFieldBasedEditLayoutConfigModuleController}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class AddStructFieldEntryDialog extends ResizableTitleAreaDialog {

	private String entryType;
	private StructTreeComposite structTree;
	private StructFieldID structFieldID;
	private StructField structField;
//	private Set<StructFieldID> ignoreIDs;
	private Map<StructFieldID, String> ignoreIDs;
	private StructLocal structLocal;

	/**
	 * Construct a new {@link AddStructFieldEntryDialog}.
	 *
	 * @param shell The parent shell.
	 * @param resourceBundle The resource bundle to get initial sizes from.
	 * @param ignoreIDs All ids that should not be addable.
	 * @param structLocal The StructLocal to select fields from.
	 */
	public AddStructFieldEntryDialog(
			Shell shell, ResourceBundle resourceBundle,
			Map<StructFieldID, String> ignoreIDs, StructLocal structLocal) {
		super(shell, resourceBundle);
		this.ignoreIDs = ignoreIDs;
		this.structLocal = structLocal;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		XComposite comp = new XComposite(parent, SWT.NONE, LayoutDataMode.GRID_DATA_HORIZONTAL);
		XComposite radioGroup = new XComposite(comp, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		Button separatorType = new Button(radioGroup, SWT.RADIO);
		separatorType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				separatorSelected();
			}
		});
		separatorType.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog.button.separator")); //$NON-NLS-1$

		Button structFieldReferenceType = new Button(radioGroup, SWT.RADIO);
		structFieldReferenceType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				structFieldReferenceSelected();
			}
		});
		structFieldReferenceType.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog.button.structFieldReference")); //$NON-NLS-1$
		structTree = new StructTreeComposite(comp, true, null);
		structTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				StructFieldNode node = structTree.getStructFieldNode();
				structField = node != null ? node.getField() : null;
				structFieldID = (StructFieldID) (node != null ? JDOHelper.getObjectId(node.getField()) : null);
				updateOKButtonEnabled();
			}
		});
		structTree.setInput(structLocal);
		structFieldReferenceType.setSelection(true);
		structFieldReferenceSelected();
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog.dialog.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog.dialog.message")); //$NON-NLS-1$
		comp.getDisplay().asyncExec(new Runnable() {
			public void run() {
				setOKButtonEnabled(false);
			}
		});
		
		createAdditionalUI(comp);
		
		return comp;
	}
	
	/**
	 * Extendors can use this method to create additional UI below the struct tree.
	 * 
	 * @param parent The parent to be used.
	 */
	protected void createAdditionalUI(Composite parent) {}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog.window.title")); //$NON-NLS-1$
	}

	protected void setOKButtonEnabled(boolean value) {
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null)
			button.setEnabled(value);
	}

	protected void updateOKButtonEnabled() {
		setOKButtonEnabled(
				EditLayoutEntry.ENTRY_TYPE_SEPARATOR.equals(entryType) || (
						structFieldID != null &&
						!(ignoreIDs != null ? ignoreIDs.containsKey(structFieldID) : false)
				)
			);
		
		if (ignoreIDs.containsKey(structFieldID)) {
			String message = ignoreIDs.get(structFieldID);
			setErrorMessage(message);
		} else {
			setErrorMessage(null);
		}
	}

	protected void separatorSelected() {
		entryType = EditLayoutEntry.ENTRY_TYPE_SEPARATOR;
		structTree.setEnabled(false);
		updateOKButtonEnabled();
	}

	protected void structFieldReferenceSelected() {
		entryType = PropertySetEditLayoutEntry.ENTRY_TYPE_STRUCT_FIELD_REFERENCE;
		structTree.setEnabled(true);
		updateOKButtonEnabled();
	}
	
	protected StructTreeComposite getStructTree() {
		return structTree;
	}

	/**
	 * @return The selected type.
	 */
	public String getEntryType() {
		return entryType;
	}

	/**
	 * @return The selected {@link StructFieldID}.
	 */
	public StructFieldID getStructFieldID() {
		return structFieldID;
	}
	
	/**
	 * @return The selected {@link StructField}.
	 */
	public StructField getStructField() {
		return structField;
	}
}
