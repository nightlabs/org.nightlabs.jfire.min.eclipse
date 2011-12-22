package org.nightlabs.jfire.base.dashboard.ui;

import org.nightlabs.base.ui.wizard.IWizardHopPage;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;

/**
 * Implementations of this interface are served by
 * {@link IDashboardGadgetFactory#createConfigurationWizardPage()}. They are
 * used to build a wizard where the user can configure a gadget of his
 * dashboard.
 * <p>
 * For each wizard a new page will be created. The page will be created and
 * {@link #initialize(DashboardGadgetLayoutEntry)} will be called before the
 * page contents are created. When the user finishes the wizard
 * {@link #configure(DashboardGadgetLayoutEntry)} will be called (Note that this
 * can happen even without the page-contents having been created).
 * </p>
 * <p>
 * Use {@link AbstractDashbardGadgetConfigPage} as base for your implementation.
 * </p>
 * 
 * @author abieber
 */
public interface IDashboardGadgetConfigPage<T> extends IWizardHopPage {
	
	/**
	 * Initialize this page with the given {@link DashboardGadgetLayoutEntry layoutEntry}. 
	 */
	void initialize(DashboardGadgetLayoutEntry<?> layoutEntry);

	/**
	 * Configure the given {@link DashboardGadgetLayoutEntry layoutEntry} with
	 * the values from this page. This should be either values changed by the
	 * user or the values this page was initialized with.
	 */
	void configure(DashboardGadgetLayoutEntry<?> layoutEntry);
		
}
