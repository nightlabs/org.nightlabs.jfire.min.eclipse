package org.nightlabs.jfire.base.ui.person.search;


import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewer;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.view.PersonTableViewerConfiguration;

public class PersonTableViewer implements IPropertySetViewer<PersonTableViewerConfiguration> {
	
	private PersonResultTable personResultTable;

	@Override
	public Control createControl(Composite parent) {
		personResultTable = new PersonResultTable(parent, SWT.NONE);
		return personResultTable;
	}

	@Override
	public void setPropertySets(Collection<PropertySet> propertySets) {
		personResultTable.setInput(propertySets);
	}
	
	@Override
	public void setConfiguration(PersonTableViewerConfiguration config) {
		// TODO Set the configuration of the person result table
	}

	@Override
	public void loadDefaultConfiguration() {
		// TODO load a default configuration
	}
}
