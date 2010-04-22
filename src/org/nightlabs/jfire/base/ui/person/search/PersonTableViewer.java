package org.nightlabs.jfire.base.ui.person.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.IPropertySetTableConfig;
import org.nightlabs.jfire.base.ui.prop.PropertySetTable;
import org.nightlabs.jfire.base.ui.prop.search.PropertySetTableViewer;
import org.nightlabs.jfire.person.Person;

/**
 * Concrete {@link PropertySetTableViewer} for {@link Person}s (it uses {@link PersonResultTable}).
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PersonTableViewer extends PropertySetTableViewer<Person> {

	/**
	 * Create a new {@link PersonTableViewer}.
	 */
	public PersonTableViewer() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a {@link PersonResultTable}.
	 * </p>
	 */
	@Override
	protected PropertySetTable<Person, Person> createPropertySetTable(Composite parent) {
		if (getConfiguration() == null) {
			throw new IllegalStateException("The configuration for this PropertySetViewer was not set!");
		}
		PersonResultTable personResultTable = new PersonResultTable(parent, SWT.NONE) {
			@Override
			protected IPropertySetTableConfig getPropertySetTableConfig() {
				return PersonTableViewerConfigurationHelper.createPropertySetTableConfig(getConfiguration());
			}
		};
		return personResultTable;
	}

}
