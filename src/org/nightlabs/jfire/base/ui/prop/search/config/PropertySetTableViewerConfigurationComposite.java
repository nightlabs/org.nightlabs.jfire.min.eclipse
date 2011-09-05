package org.nightlabs.jfire.base.ui.prop.search.config;

import java.util.Collections;
import java.util.Iterator;
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
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerColumnDescriptor;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerConfiguration;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * Master-detail composite to configure the columns displayed in a PropertySetTable.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PropertySetTableViewerConfigurationComposite extends XComposite {

	private ListComposite<PropertySetTableViewerColumnDescriptor> columnDescriptorList;
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	
	private PropertySetTableViewerColumnDescriptorComposite columnDescriptorComposite;
	private PropertySetTableViewerConfiguration configuration;
	
	public PropertySetTableViewerConfigurationComposite(Composite parent) {
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		
		XComposite wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		wrapper.getGridLayout().makeColumnsEqualWidth = true;
		XComposite columnDescriptorWrapper = new XComposite(wrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		
		Label label = new Label(columnDescriptorWrapper, SWT.WRAP);
		label.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.config.PropertySetTableViewerConfigurationComposite.columnTableLabel.text")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		XComposite tableButtonWrapper = new XComposite(columnDescriptorWrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		tableButtonWrapper.getGridLayout().makeColumnsEqualWidth = false;
		
		columnDescriptorList = new ListComposite<PropertySetTableViewerColumnDescriptor>(tableButtonWrapper, SWT.NONE);
		columnDescriptorList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				updateButtonStates();
			}
		});
		XComposite buttonWrapper = new XComposite(tableButtonWrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
		buttonWrapper.getGridData().verticalAlignment = SWT.BEGINNING;
		
		columnDescriptorList.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				if (element instanceof PropertySetTableViewerColumnDescriptor) {
					PropertySetTableViewerColumnDescriptor descriptor = ((PropertySetTableViewerColumnDescriptor) element);
					StringBuilder sb = new StringBuilder();
					for (Iterator<StructField> it = descriptor.getStructFields().iterator(); it.hasNext();) {
						StructField structField = it.next();
						sb.append(structField.getName().getText());
						if (it.hasNext())
							sb.append(descriptor.getColumnHeaderSeparator());
					}
					if (sb.length() == 0)
						sb.append(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.config.PropertySetTableViewerConfigurationComposite.columnDescriptorList.noFieldsYet")); //$NON-NLS-1$
					return sb.toString();
				}
				return ""; //$NON-NLS-1$
			}
		});
		columnDescriptorList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				PropertySetTableViewerColumnDescriptor columnDescriptor = columnDescriptorComposite.updateColumnDescriptor();
				if (columnDescriptor != null) {
					columnDescriptorList.refreshElement(columnDescriptor);
				}
				if (columnDescriptorList.getSelectedElement() != null) {
					columnDescriptorComposite.setColumnDescriptor(columnDescriptorList.getSelectedElement());
				}
			}
		});
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		
		addButton = new Button(buttonWrapper, SWT.PUSH);
		addButton.setLayoutData(gd);
		addButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.config.PropertySetTableViewerConfigurationComposite.addButton.text")); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PropertySetTableViewerColumnDescriptor columnDescriptor = columnDescriptorComposite.updateColumnDescriptor();
				if (columnDescriptor != null) {
					columnDescriptorList.refreshElement(columnDescriptor);
				}
				
				PropertySetTableViewerColumnDescriptor newColumnDescriptor = new PropertySetTableViewerColumnDescriptor(SecurityReflector
						.getUserDescriptor().getOrganisationID(), IDGenerator.nextID(PropertySetTableViewerColumnDescriptor.class));
				columnDescriptorList.addElement(newColumnDescriptor);
				columnDescriptorList.setSelection(newColumnDescriptor);
				
				if (columnDescriptorList.getSelectedElement() != null) {
					columnDescriptorComposite.setColumnDescriptor(columnDescriptorList.getSelectedElement());
				}
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		removeButton = new Button(buttonWrapper, SWT.PUSH);
		removeButton.setLayoutData(gd);
		removeButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.config.PropertySetTableViewerConfigurationComposite.removeButton.text")); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				columnDescriptorList.removeSelected();
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		upButton = new Button(buttonWrapper, SWT.PUSH);
		upButton.setLayoutData(gd);
		upButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.config.PropertySetTableViewerConfigurationComposite.upButton.text")); //$NON-NLS-1$
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = columnDescriptorList.getSelectionIndex();
				Collections.swap(columnDescriptorList.getElements(), selectionIndex, selectionIndex-1);
				columnDescriptorList.refresh();
				columnDescriptorList.setSelection(selectionIndex-1);
				updateButtonStates();
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		downButton = new Button(buttonWrapper, SWT.PUSH);
		downButton.setLayoutData(gd);
		downButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.config.PropertySetTableViewerConfigurationComposite.downButton.text")); //$NON-NLS-1$
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = columnDescriptorList.getSelectionIndex();
				Collections.swap(columnDescriptorList.getElements(), selectionIndex, selectionIndex+1);
				columnDescriptorList.refresh();
				columnDescriptorList.setSelection(selectionIndex+1);
				updateButtonStates();
			}
		});

		
		
		columnDescriptorComposite = new PropertySetTableViewerColumnDescriptorComposite(wrapper, StructLocalID.create(
				Organisation.DEV_ORGANISATION_ID, Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE));

		
		buttonWrapper.layout();
		wrapper.layout();
	}
	
	public void setViewerConfiguration(PropertySetTableViewerConfiguration config) {
		this.configuration = config;
		columnDescriptorList.setInput(new LinkedList<PropertySetTableViewerColumnDescriptor>(configuration.getColumnDescriptors()));
	}
	
	protected void updateButtonStates() {
		removeButton.setEnabled(columnDescriptorList.getSelectedElement() != null);
		upButton.setEnabled(columnDescriptorList.getSelectedElement() != null && columnDescriptorList.getSelectionIndex() != 0);
		downButton.setEnabled(columnDescriptorList.getSelectedElement() != null && columnDescriptorList.getSelectionIndex() != columnDescriptorList.getElements().size()-1);
	}

	public PropertySetTableViewerConfiguration getViewerConfiguration() {
		configuration.setColumnDescriptors(columnDescriptorList.getElements());
		return configuration;
	}

}
