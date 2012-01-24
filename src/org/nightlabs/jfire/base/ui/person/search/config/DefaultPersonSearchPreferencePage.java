package org.nightlabs.jfire.base.ui.person.search.config;

import org.nightlabs.jfire.base.ui.person.search.PersonSearchUseCaseConstants;
import org.nightlabs.jfire.base.ui.prop.search.config.AbstractPropertySetSearchPreferencePage;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;

/**
 * PropertySetSearchPreferencePage for the use-case {@link PersonSearchUseCaseConstants#USE_CASE_ID_DEFAULT}
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DefaultPersonSearchPreferencePage extends AbstractPropertySetSearchPreferencePage {

	@Override
	public String getConfigModuleID() {
		return AbstractEditLayoutConfigModule.getCfModID(AbstractEditLayoutConfigModule.CLIENT_TYPE_RCP,
				PersonSearchUseCaseConstants.USE_CASE_ID_DEFAULT);
	}
	
	@Override
	protected Class<? extends PropertySetEditLayoutConfigModule> getConfigModuleClass() {
		return PersonSearchConfigModule.class;
	}


	@Override
	public String getUseCaseDescription() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.config.DefaultPersonSearchPreferencePage.useCaseDescription"); //$NON-NLS-1$
	}
	
	@Override
	protected String getLayoutConfigTabText() {
		return Messages.getString("org.nightlabs.jfire.base.ui.person.search.config.DefaultPersonSearchPreferencePage.layoutConfigTabText"); //$NON-NLS-1$
	}
}