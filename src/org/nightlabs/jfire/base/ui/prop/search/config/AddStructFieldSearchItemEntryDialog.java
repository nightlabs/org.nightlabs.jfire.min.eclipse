package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.ComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.tree.AbstractTreeComposite;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog;
import org.nightlabs.jfire.base.ui.prop.config.PropertySetFieldBasedEditLayoutConfigModuleController;
import org.nightlabs.jfire.base.ui.prop.search.IStructFieldSearchFilterItemEditor;
import org.nightlabs.jfire.base.ui.prop.search.StructFieldSearchFilterEditorRegistry;
import org.nightlabs.jfire.base.ui.prop.structedit.StructFieldNode;
import org.nightlabs.jfire.base.ui.prop.structedit.StructTreeComposite;
import org.nightlabs.jfire.base.ui.prop.structedit.TreeNode;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.layout.EditLayoutEntry;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry2;

/**
 * Dialog used internally to add a new StructField or Separator entry to a {@link PropertySetFieldBasedEditLayoutConfigModuleController}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class AddStructFieldSearchItemEntryDialog extends ResizableTitleAreaDialog {

	private String entryType;
	
	@SuppressWarnings("unchecked")
	private Set<StructField> structFields = Collections.EMPTY_SET;
	
//	private Set<StructFieldID> ignoreFields;
	private Map<StructField, String> ignoreFields;
	private StructLocal structLocal;
	
	private ComboComposite<MatchType> matchTypeCombo;
	private MatchType matchType;
	
	private StackLayout stackLayout;
	private Composite stackLayoutComposite;
	
	private Composite singleSelectionStructTreeComposite;
	private StructTreeComposite singleSelectionStructTree;
	
	private Composite multiSelectionStructTreeComposite;
	private StructTreeComposite multiSelectionStructTree;
	
	private String multiSelectionErrorMessage;
	private String singleSelectionErrorMessage;

	/**
	 * Construct a new {@link AddStructFieldEntryDialog}.
	 *
	 * @param shell The parent shell.
	 * @param resourceBundle The resource bundle to get initial sizes from.
	 * @param ignoreFields All ids that should not be addable along with a description why.
	 * @param structLocal The StructLocal to select fields from.
	 */
	public AddStructFieldSearchItemEntryDialog(Shell shell, ResourceBundle resourceBundle, Map<StructField, String> ignoreFields, StructLocal structLocal) {
		super(shell, resourceBundle);
		this.ignoreFields = ignoreFields;
		this.structLocal = structLocal;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		XComposite comp = new XComposite(parent, SWT.NONE, LayoutDataMode.GRID_DATA);
		XComposite radioGroup = new XComposite(comp, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 3);
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
		structFieldReferenceType.setSelection(true);
		
		Button combinedStructFieldReference = new Button(radioGroup, SWT.RADIO);
		combinedStructFieldReference.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				combinedStructFieldReferenceSelected();
			}
		});
		combinedStructFieldReference.setText("Multi-struct field reference");
		
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog.dialog.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.AddStructFieldEntryDialog.dialog.message")); //$NON-NLS-1$
//		parent.getDisplay().asyncExec(new Runnable() {
//			public void run() {
				setOKButtonEnabled(false);
//			}
//		});
		
		stackLayoutComposite = new XComposite(comp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		stackLayout = new StackLayout();
		stackLayoutComposite.setLayout(stackLayout);
		
		createSingleSelectionStructTree(stackLayoutComposite);
		createMultiSelectionStructTree(stackLayoutComposite);
		
		// Make struct tree visible
		stackLayout.topControl = singleSelectionStructTreeComposite;
		stackLayoutComposite.layout();
		
		XComposite wrapper = new XComposite(comp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		new Label(wrapper, SWT.NONE).setText("Match type:");
		matchTypeCombo = new ComboComposite<MatchType>(wrapper, SWT.NONE);
		matchTypeCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				matchType = matchTypeCombo.getSelectedElement();
			}
		});
		
		structFieldReferenceSelected();
		updateMatchTypeCombo();
		
		return comp;
	}
	
	private void createSingleSelectionStructTree(Composite parent) {
		singleSelectionStructTreeComposite = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		singleSelectionStructTree = new StructTreeComposite(singleSelectionStructTreeComposite, true, null);
		singleSelectionStructTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				StructFieldNode node = singleSelectionStructTree.getStructFieldNode();
				
				structFields = Collections.EMPTY_SET;
				
				if (node != null) {
					final StructField field = node.getField();
					if (ignoreFields.containsKey(field)) {
						singleSelectionErrorMessage = ignoreFields.get(field);
					}	else {
						structFields = Collections.singleton(field);
					}
					
					if (!StructFieldSearchFilterEditorRegistry.sharedInstance().hasEditor(field.getClass())) {
						singleSelectionErrorMessage = "Fields of this type cannot be added because no editor is available.";
					}
					
				} else {
					singleSelectionErrorMessage = "The selected item is no field.";
				}
				
				updateUI();
			}
		});
		singleSelectionStructTree.setInput(structLocal);
	}
	
	private void createMultiSelectionStructTree(Composite parent) {
		multiSelectionStructTreeComposite = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		multiSelectionStructTree = new StructTreeComposite(multiSelectionStructTreeComposite, true, null, AbstractTreeComposite.DEFAULT_STYLE_MULTI);
		multiSelectionStructTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				Set<TreeNode> selectedElements = multiSelectionStructTree.getSelectedElements();
				
				multiSelectionErrorMessage = null;
				
				if (!selectedElements.isEmpty()) {
					structFields = new HashSet<StructField>();
					
					for (TreeNode node : selectedElements) {
						if (node != null && StructFieldNode.class.isAssignableFrom(node.getClass())) {
							StructField<?> field = ((StructFieldNode) node).getField();
							structFields.add(field);
						} else {
							multiSelectionErrorMessage = "At least one of the selected items is no field.";
							break;
						}
					}
					
				// if there has been no error so far
					if (multiSelectionErrorMessage == null) {
						Class<? extends StructField<?>> fieldClass = (Class<? extends StructField<?>>) structFields.iterator().next().getClass();
						for (StructField<?> field : structFields) {
							
							if (ignoreFields.containsKey(field)) {
								multiSelectionErrorMessage = ignoreFields.get(field);
								break;
							}
							
							if (!field.getClass().equals(fieldClass)) {
								multiSelectionErrorMessage = "All selected fields have to be of the same type.";
								break;
							}
						}
						
						// if there has been no error so far
						if (multiSelectionErrorMessage == null) {
							if (!StructFieldSearchFilterEditorRegistry.sharedInstance().hasEditor(fieldClass)) {
								multiSelectionErrorMessage = "Fields of this type cannot be added because no editor is available.";
							}
						}
					}
				}
				
				if (multiSelectionErrorMessage != null) {
					structFields = new HashSet<StructField>();
				}
				
				updateUI();
			}
		});
		multiSelectionStructTree.setInput(structLocal);
	}
	
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

	protected void updateUI() {
		updateMatchTypeCombo();
		updateErrorMessage();
	}

	protected void separatorSelected() {
		entryType = EditLayoutEntry.ENTRY_TYPE_SEPARATOR;
		singleSelectionStructTreeComposite.setEnabled(false);
		multiSelectionStructTreeComposite.setEnabled(false);
		matchTypeCombo.setEnabled(false);
		updateUI();
	}

	protected void structFieldReferenceSelected() {
		entryType = PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_STRUCT_FIELD_REFERENCE;
		stackLayout.topControl = singleSelectionStructTreeComposite;
		stackLayoutComposite.layout();
		singleSelectionStructTreeComposite.setEnabled(true);
		matchTypeCombo.setEnabled(true);
		updateUI();
	}
	
	protected void combinedStructFieldReferenceSelected() {
		entryType = PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_MULTI_STRUCT_FIELD_REFERENCE;
		stackLayout.topControl = multiSelectionStructTreeComposite;
		stackLayoutComposite.layout();
		multiSelectionStructTreeComposite.setEnabled(true);
		matchTypeCombo.setEnabled(true);
		
		updateUI();
	}
	
	protected StructTreeComposite getSingleSelectionStructTree() {
		return singleSelectionStructTree;
	}

	/**
	 * @return The selected type.
	 */
	public String getEntryType() {
		return entryType;
	}
	
	@SuppressWarnings("unchecked")
	public Set<StructField> getStructFields() {
		return Collections.unmodifiableSet(structFields);
	}
	
	protected void updateMatchTypeCombo() {
		if (matchTypeCombo == null)
			return;
		
		if (getStructFields() == null || getStructFields().isEmpty()) {
			matchTypeCombo.setInput(Collections.EMPTY_LIST);
		} else {
			StructField<?> field = getStructFields().iterator().next();
			IStructFieldSearchFilterItemEditor editor = StructFieldSearchFilterEditorRegistry.sharedInstance().createSearchFilterItemEditor(field, null);
			if (editor != null) {
				matchTypeCombo.setInput(editor.getSupportedMatchTypes());
				matchTypeCombo.setSelection(0);
				matchType = matchTypeCombo.getSelectedElement();
			} else {
				matchTypeCombo.setInput(Collections.EMPTY_LIST);
			}
		}
	}
	
	protected void updateErrorMessage() {
		String errorMessage = null;
		
		if (entryType.equals(PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_STRUCT_FIELD_REFERENCE)) {
			errorMessage = singleSelectionErrorMessage;
		} else if (entryType.equals(PropertySetFieldBasedEditLayoutEntry2.ENTRY_TYPE_MULTI_STRUCT_FIELD_REFERENCE)) {
			errorMessage = multiSelectionErrorMessage;
		}
		
		setErrorMessage(errorMessage);
		setOKButtonEnabled(errorMessage == null);
		matchTypeCombo.setEnabled(errorMessage == null);
	}
	
	public MatchType getMatchType() {
		return matchType;
	}
}
