package org.nightlabs.jfire.base.ui.person.search;

import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.ListComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.tree.AbstractTreeComposite;
import org.nightlabs.jfire.base.ui.prop.structedit.StructTreeComposite;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.view.PersonTableViewerConfiguration;
import org.nightlabs.progress.NullProgressMonitor;

public class PersonTableViewerConfigurationComposite extends XComposite {

	private StructTreeComposite structTree;
	private ListComposite<StructField> selectedStructFieldList;
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	
	private PersonTableViewerConfiguration configuration;

	public PersonTableViewerConfigurationComposite(Composite parent) {
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Select the fields whose values you want to be displayed in the result table.");
		
		XComposite wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		XComposite tableWrapper = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		tableWrapper.getGridLayout().makeColumnsEqualWidth = true;
		XComposite buttonWrapper = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);

		structTree = new StructTreeComposite(tableWrapper, true, null, AbstractTreeComposite.DEFAULT_STYLE_SINGLE);
		structTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				updateButtonStates();
			}
		});
		
		selectedStructFieldList = new ListComposite<StructField>(tableWrapper, SWT.NONE);
		selectedStructFieldList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				updateButtonStates();
			}
		});
		
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
				StructField sf = structTree.getStructFieldNode().getField();
				selectedStructFieldList.addElement(sf);
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
		
		buttonWrapper.layout();
		wrapper.layout();
		
		StructLocalID structLocalID = StructLocalID.create(Organisation.DEV_ORGANISATION_ID, Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE);
		StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(structLocalID, new NullProgressMonitor());
		
		structTree.setInput(structLocal);
	}
	
	public void setViewerConfiguration(PersonTableViewerConfiguration config) {
		this.configuration = config;
		selectedStructFieldList.setInput(new LinkedList<StructField>(configuration.getDisplayedStructFields()));
	}
	
	protected void updateButtonStates() {
		addButton.setEnabled(structTree.getStructFieldNode() != null);
		removeButton.setEnabled(selectedStructFieldList.getSelectedElement() != null);
		upButton.setEnabled(selectedStructFieldList.getSelectedElement() != null && selectedStructFieldList.getSelectionIndex() != 0);
		downButton.setEnabled(selectedStructFieldList.getSelectedElement() != null && selectedStructFieldList.getSelectionIndex() != selectedStructFieldList.getElements().size()-1);
	}

	public PersonTableViewerConfiguration getViewerConfiguration() {
		configuration.setDisplayedStructFields(selectedStructFieldList.getElements());
		return configuration;
	}

}
