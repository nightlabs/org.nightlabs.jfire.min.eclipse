package org.nightlabs.jfire.base.ui.prop.search;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.ui.search.SearchFilterProvider;
import org.nightlabs.jdo.query.ui.search.SearchResultFetcher;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.ui.person.search.PersonTableViewerConfigurationHelper;
import org.nightlabs.jfire.base.ui.prop.IPropertySetTableConfig;
import org.nightlabs.jfire.base.ui.prop.PropertySetTable;
import org.nightlabs.jfire.base.ui.prop.view.AbstractPropertySetTableViewer;
import org.nightlabs.jfire.base.ui.prop.view.IPropertySetViewer;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.dao.TrimmedPropertySetDAO;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerConfiguration;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.ObjectCarrier;
import org.nightlabs.util.Util;

/**
 * {@link IPropertySetViewer} for displaying a Table of {@link PropertySet}s that uses {@link PropertySetTable}.
 * Additionally {@link PropertySetTableViewer} implements the {@link SearchResultFetcher} interface so
 * that it will be asked to do the fetching of the search-results it should display itself, that way
 * it has the best control of how the Persons are queried from the server. This implementation uses
 * the {@link TrimmedPropertySetDAO} to get the search-results.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 */
public class PropertySetTableViewer<SelectionType extends PropertySet>  
extends AbstractPropertySetTableViewer<PropertySetID, SelectionType, SelectionType, PropertySetTableViewerConfiguration>
implements SearchResultFetcher {

	private Logger logger = Logger.getLogger(PropertySetTableViewer.class);

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a {@link PropertySetTable} whose columns are configured using the this viewers
	 * configuration {@link #getConfiguration()}. If the configuration was not set before this
	 * method is called, an IllegalStateException will be thrown.
	 * </p>
	 */
	@Override
	protected PropertySetTable<SelectionType, SelectionType> createPropertySetTable(Composite parent) {
		if (getConfiguration() == null) {
			throw new IllegalStateException("The configuration for this PropertySetViewer was not set!");
		}
		PropertySetTable<SelectionType, SelectionType> personResultTable = new PropertySetTable<SelectionType, SelectionType>(parent, SWT.NONE) {
			@Override
			protected IPropertySetTableConfig getPropertySetTableConfig() {
				return PersonTableViewerConfigurationHelper.createPropertySetTableConfig(getConfiguration());
			}
			@Override
			protected SelectionType convertInputElement(SelectionType inputElement) {
				return inputElement;
			}
		};
		return personResultTable;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation takes the input-{@link PropertySetID}s and fetches the appropriate
	 * Persons trimmed, i.e. it uses the {@link TrimmedPropertySetDAO} in order to fetch only the
	 * data used to display the configured columns.
	 * </p>
	 */
	@Override
	public void setInput(Collection<PropertySetID> propertySetIDs, ProgressMonitor monitor) {
		if (getConfiguration() == null) {
			throw new IllegalStateException("The configuration for this PropertySetViewer was not set!");
		}
		monitor.beginTask("Loading PropertySets", 2);
		monitor.worked(1);
		final Collection<? extends PropertySet> trimmedPropertySets = TrimmedPropertySetDAO.sharedInstance().getTrimmedPropertySets(
				new HashSet<PropertySetID>(propertySetIDs), getConfiguration().getAllStructFieldIDs(), null,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 1));
		monitor.done();
		getPropertySetTable().getDisplay().asyncExec(new Runnable() {
			public void run() {
				getPropertySetTable().setInput(trimmedPropertySets);
			}
		});
		notifiyContentChangedListeners(propertySetIDs);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Uses the {@link PropertyManagerRemote#searchPropertySetIDs(PropSearchFilter)} method to fetch
	 * the {@link PropertySetID}s matching the current criteria in the given filterProvider and
	 * calls {@link #setInput(Collection, ProgressMonitor)} with the results.
	 * </p>
	 */
	@Override
	public void searchTriggered(final SearchFilterProvider filterProvider) {
		logger.debug("Search triggered, getting PersonManager"); //$NON-NLS-1$
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				setMessage("Searching...");
			}
		});

		Job loadJob = new Job("Loading person search result...") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				monitor.beginTask(getName(), 10);
				try {
					PropertyManagerRemote propertyManager;
					try {
						propertyManager = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, Login.getLogin().getInitialContextProperties());
					} catch (LoginException e1) {
						throw new RuntimeException(e1);
					}

					logger.debug("Have PersonManager searching"); //$NON-NLS-1$
					final ObjectCarrier<PropSearchFilter> oc = new ObjectCarrier<PropSearchFilter>();
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							oc.setObject((PropSearchFilter) filterProvider.getSearchFilter());
						}
					});
					PropSearchFilter searchFilter = oc.getObject();

					try {
						long start = System.currentTimeMillis();
						Set<PropertySetID> propIDs = new HashSet<PropertySetID>(propertyManager.searchPropertySetIDs(searchFilter));
						logger.debug("ID search for " + propIDs.size() + " entries took " + Util.getTimeDiffString(start)); //$NON-NLS-1$ //$NON-NLS-2$
						monitor.worked(1);

						start = System.currentTimeMillis();
						setInput(propIDs, new SubProgressMonitor(monitor, 9));

						start = System.currentTimeMillis();
					} catch (Exception e) {
						logger.error("Error searching persons.", e); //$NON-NLS-1$
						throw new RuntimeException(e);
					}
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		loadJob.schedule();
	}
}
