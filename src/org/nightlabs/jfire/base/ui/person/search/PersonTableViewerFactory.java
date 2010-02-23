/**
 * 
 */
package org.nightlabs.jfire.base.ui.person.search;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewer;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewerFactory;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.view.PersonTableViewerConfiguration;

public class PersonTableViewerFactory implements IPropertySetViewerFactory<PersonTableViewerConfiguration> {
	
	private static final Collection<Class<? extends PropertySet>> SUPPORTED_CLASSES;
	private PersonTableViewerConfigurationComposite personTableViewerConfigurationComposite;
	
	static {
		SUPPORTED_CLASSES = new LinkedList<Class<? extends PropertySet>>();
		SUPPORTED_CLASSES.add(Person.class);
	}

	@Override
	public IPropertySetViewer<PersonTableViewerConfiguration> createViewer() {
		return new PersonTableViewer();
	}

	@Override
	public String getDescription() {
		return "A table for persons.";
	}

	@Override
	public String getName() {
		return "Person table";
	}
	
	@Override
	public Collection<Class<? extends PropertySet>> getSupportedCandidateClasses() {
		return SUPPORTED_CLASSES;
	}

	@Override
	public String getViewerIdentifier() {
		return PersonTableViewer.class.getName();
	}

	@Override
	public Control createViewerConfigurationControl(Composite parent, PersonTableViewerConfiguration configuration) {
		personTableViewerConfigurationComposite = new PersonTableViewerConfigurationComposite(parent);
		personTableViewerConfigurationComposite.setViewerConfiguration(configuration);
		return personTableViewerConfigurationComposite;
	}

	@Override
	public PersonTableViewerConfiguration getViewerConfiguration() {
		return personTableViewerConfigurationComposite.getViewerConfiguration();
	}
}