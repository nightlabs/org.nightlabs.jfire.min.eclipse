/**
 * 
 */
package org.nightlabs.jfire.base.ui.person.search;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.ui.prop.search.config.PropertySetTableViewerConfigurationComposite;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewer;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewerFactory;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonSearchFilter;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerConfiguration;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;
import org.nightlabs.jfire.security.SecurityReflector;

public class PersonTableViewerFactory implements IPropertySetViewerFactory<PropertySetID, Person, PropertySetTableViewerConfiguration> {
	
	private static final Collection<Class<? extends PropSearchFilter>> SUPPORTED_FILTER_CLASSES;
	private PropertySetTableViewerConfigurationComposite personTableViewerConfigurationComposite;
	
	static {
		SUPPORTED_FILTER_CLASSES = new LinkedList<Class<? extends PropSearchFilter>>();
		SUPPORTED_FILTER_CLASSES.add(PersonSearchFilter.class);
	}

	@Override
	public IPropertySetViewer<PropertySetID, Person, PropertySetTableViewerConfiguration> createViewer() {
		return new PersonTableViewer();
	}

	@Override
	public String getDescription() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonTableViewerFactory.description"); //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonTableViewerFactory.name"); //$NON-NLS-1$
	}
	
	@Override
	public Collection<Class<? extends PropSearchFilter>> getSupportedFilterClasses() {
		return SUPPORTED_FILTER_CLASSES;
	}

	@Override
	public String getViewerIdentifier() {
		return PersonTableViewer.class.getName();
	}

	@Override
	public Control createViewerConfigurationControl(Composite parent, PropertySetTableViewerConfiguration configuration) {
		personTableViewerConfigurationComposite = new PropertySetTableViewerConfigurationComposite(parent);
		personTableViewerConfigurationComposite.setViewerConfiguration(configuration);
		return personTableViewerConfigurationComposite;
	}

	@Override
	public PropertySetTableViewerConfiguration getViewerConfiguration() {
		return personTableViewerConfigurationComposite.getViewerConfiguration();
	}

	@Override
	public PropertySetViewerConfiguration createViewerConfiguration() {
		return new PropertySetTableViewerConfiguration(SecurityReflector.getUserDescriptor().getOrganisationID(), IDGenerator
				.nextID(PropertySetViewerConfiguration.class));
	}
}