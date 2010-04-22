package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.LabeledText;
import org.nightlabs.base.ui.composite.ListComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jfire.base.ui.prop.structedit.StructBlockNode;
import org.nightlabs.jfire.base.ui.prop.structedit.StructFieldNode;
import org.nightlabs.jfire.base.ui.prop.structedit.StructTreeComposite;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerColumnDescriptor;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PropertySetTableViewerColumnDescriptorComposite extends XComposite {

	class StructDialog extends ResizableTitleAreaDialog {

		private StructField<?> selectedStructField;
		private StructTreeComposite treeComposite;


		public StructDialog(Shell shell) {
			super(shell, null);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Select struct field");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			setTitle("Select struct field for column");
			treeComposite = new StructTreeComposite(parent, true, null);
			treeComposite.setInput(
					StructLocalDAO.sharedInstance().getStructLocal(
							StructLocalID.create(
									Organisation.DEV_ORGANISATION_ID,
									Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE
							),
							new NullProgressMonitor()
					)
			);
			treeComposite.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent arg0) {
					StructFieldNode node = treeComposite.getStructFieldNode();
					selectedStructField = node != null ? node.getField() : null;
					if (selectedStructField != null) {
						boolean alreadyContained = selectedStructFieldList.getElements().contains(selectedStructField);
						if (alreadyContained) {
							setMessage("Struct field is already contained", IMessageProvider.INFORMATION);
						}
						boolean canBeDisplayed = II18nTextDataField.class.isAssignableFrom(selectedStructField.getDataFieldClass());
						if (!canBeDisplayed) {
							setMessage("Data of this type can't be displayed in a table", IMessageProvider.ERROR);
						}
						boolean enabled = !alreadyContained && canBeDisplayed;
						if (enabled) {
							setMessage(null);
						}
						setOKButtonEnabled(enabled);
					}
				}
			});
			treeComposite.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent arg0) {
					if (treeComposite.getSelection() instanceof ITreeSelection) {
						ITreeSelection selection = (ITreeSelection) treeComposite.getSelection();
						if (selection.getFirstElement() instanceof StructFieldNode) {
							StructFieldNode node = (StructFieldNode) selection.getFirstElement();
							selectedStructField = node != null ? node.getField() : null;
							if (selectedStructField != null && !(selectedStructFieldList.getElements().contains(selectedStructField))) {
								okPressed();
							}
						}
						if (selection.getFirstElement() instanceof StructBlockNode) {
							StructBlockNode node = (StructBlockNode) selection.getFirstElement();
							if (treeComposite.getTreeViewer().getExpandedState(node)) {
								// TODO In the case a node is already expanded when opening this dialog it collapses
								// AND expands again when trying to collapse it. After a while of repeating this step
								// it finally remains in collapsed state.
								treeComposite.getTreeViewer().collapseToLevel(node, TreeViewer.ALL_LEVELS);
							} else {
								treeComposite.getTreeViewer().expandToLevel(node, TreeViewer.ALL_LEVELS);
							}
						}
					}
				}
			});
			return super.createDialogArea(parent);
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			setOKButtonEnabled(false);
		}

		protected void setOKButtonEnabled(boolean value) {
			getButton(IDialogConstants.OK_ID).setEnabled(value);
		}

		public StructField<?> getSelectedStructField() {
			return selectedStructField;
		}
	}
		

	
	@SuppressWarnings("unchecked")
	private ListComposite<StructField> selectedStructFieldList;
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
//	private StructLocal structLocal;
	
	private PropertySetTableViewerColumnDescriptor columnDescriptor;
	private LabeledText columnHeaderSeparator;
	private LabeledText columnDataSeparator;
	private LabeledText columnWeight;	
	
	/**
	 * @param parent
	 * @param style
	 */
	@SuppressWarnings("unchecked")
	public PropertySetTableViewerColumnDescriptorComposite(Composite parent, StructLocalID structLocalID) {
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		Label label = new Label(this, SWT.WRAP);
		label.setText("Select the fields whose values you want to be displayed in the column.");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		XComposite tableButtonWrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		tableButtonWrapper.getGridLayout().makeColumnsEqualWidth = false;
				
		selectedStructFieldList = new ListComposite<StructField>(tableButtonWrapper, SWT.NONE);
		selectedStructFieldList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				
			}
		});
		XComposite buttonWrapper = new XComposite(tableButtonWrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
		buttonWrapper.getGridData().verticalAlignment = SWT.BEGINNING;
		
		selectedStructFieldList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((StructField) element).getName().getText();
			}
		});
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		
		addButton = new Button(buttonWrapper, SWT.PUSH);
		addButton.setLayoutData(gd);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructDialog dialog = new StructDialog(addButton.getShell());
				if (dialog.open() == Dialog.OK) {
					selectedStructFieldList.addElement(dialog.getSelectedStructField());
				}
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		removeButton = new Button(buttonWrapper, SWT.PUSH);
		removeButton.setLayoutData(gd);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedStructFieldList.removeSelected();
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		upButton = new Button(buttonWrapper, SWT.PUSH);
		upButton.setLayoutData(gd);
		upButton.setText("Up");
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = selectedStructFieldList.getSelectionIndex();
				Collections.swap(selectedStructFieldList.getElements(), selectionIndex, selectionIndex-1);
				selectedStructFieldList.refresh();
				selectedStructFieldList.setSelection(selectionIndex-1);
				updateButtonStates();
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		downButton = new Button(buttonWrapper, SWT.PUSH);
		downButton.setLayoutData(gd);
		downButton.setText("Down");
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = selectedStructFieldList.getSelectionIndex();
				Collections.swap(selectedStructFieldList.getElements(), selectionIndex, selectionIndex+1);
				selectedStructFieldList.refresh();
				selectedStructFieldList.setSelection(selectionIndex+1);
				updateButtonStates();
			}
		});
		
		columnHeaderSeparator = new LabeledText(this, "Column header separator");
		columnDataSeparator = new LabeledText(this, "Column data separator");
		columnWeight = new LabeledText(this, "Column weight");
		
//		structLocal = StructLocalDAO.sharedInstance().getStructLocal(structLocalID, new NullProgressMonitor());
	}
	
	@SuppressWarnings("unchecked")
	public void setColumnDescriptor(PropertySetTableViewerColumnDescriptor columnDescriptor) {
		this.columnDescriptor = columnDescriptor;
		selectedStructFieldList.setInput(new LinkedList<StructField>(columnDescriptor.getStructFields()));
		selectedStructFieldList.refresh();
		if (columnDescriptor.getColumnHeaderSeparator() == null) {
			columnDescriptor.setColumnHeaderSeparator(", ");
		}
		columnHeaderSeparator.setText(columnDescriptor.getColumnHeaderSeparator());
		if (columnDescriptor.getColumnDataSeparator() == null) {
			columnDescriptor.setColumnDataSeparator(", ");
		}
		columnDataSeparator.setText(columnDescriptor.getColumnDataSeparator());
		if (columnDescriptor.getColumnWeight() <= 0) {
			columnDescriptor.setColumnWeight(1);
		}
		columnWeight.setText(String.valueOf(columnDescriptor.getColumnWeight()));
	}
	
	protected void updateButtonStates() {
		removeButton.setEnabled(selectedStructFieldList.getSelectedElement() != null);
		upButton.setEnabled(selectedStructFieldList.getSelectedElement() != null && selectedStructFieldList.getSelectionIndex() != 0);
		downButton.setEnabled(selectedStructFieldList.getSelectedElement() != null && selectedStructFieldList.getSelectionIndex() != selectedStructFieldList.getElements().size()-1);
	}

	@SuppressWarnings("unchecked")
	public PropertySetTableViewerColumnDescriptor updateColumnDescriptor() {
		if (columnDescriptor != null && !selectedStructFieldList.isDisposed()) {
			columnDescriptor.setStructFields(new ArrayList<StructField>(selectedStructFieldList.getElements()));
			columnDescriptor.setColumnHeaderSeparator(columnHeaderSeparator.getText());
			columnDescriptor.setColumnDataSeparator(columnDataSeparator.getText());
			try {
				columnDescriptor.setColumnWeight(Integer.parseInt(columnWeight.getText()));
			} catch (NumberFormatException e) {
				columnWeight.setText("1");
				columnDescriptor.setColumnWeight(1);
			}
		}
		return columnDescriptor;
	}

}
