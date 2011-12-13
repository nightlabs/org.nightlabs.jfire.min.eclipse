/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.clientui.ui.layout.GridLayoutUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.login.ui.part.LSDViewPart;
import org.nightlabs.jfire.base.ui.config.ConfigUtil;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author abieber
 *
 */
public class DashboardView extends LSDViewPart {

	private ScrolledForm form;
	private FormToolkit toolkit;

	/**
	 * 
	 */
	public DashboardView() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.part.ControllablePart#createPartContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartContents(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
//		XComposite wrapper = new XComposite(form.getBody(), SWT.NONE);
		load();
	}

	private void load() {
		Job loadConfigJob = new Job("Loading dashboard config...") {
			
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				String[] fetchGroups = new String[] {
						FetchPlan.DEFAULT,
						AbstractEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT, 
						AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES, 
						AbstractEditLayoutEntry.FETCH_GROUP_GRID_DATA};
				final DashboardLayoutConfigModule<DashboardGadgetLayoutEntry<?>> configModule = ConfigUtil.getUserCfMod(DashboardLayoutConfigModule.class, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				
				form.getDisplay().syncExec(new Runnable() {
					public void run() {
						GridLayout layout = GridLayoutUtil.createGridLayout(configModule.getGridLayout());
						form.getBody().setLayout(layout);
						
						for (DashboardGadgetLayoutEntry<?> layoutEntry : configModule.getEditLayoutEntries()) {
							
							Section gadgetSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
							gadgetSection.setText(layoutEntry.getName());
							
							GridData gridData = GridLayoutUtil.createGridData(layoutEntry.getGridData());
							gadgetSection.setLayoutData(gridData);
						}
						
						form.layout(true, true);
						form.getBody().layout(true, true);
					}
				});
				
				return Status.OK_STATUS;
			}
		};
		loadConfigJob.schedule();
	}

}
