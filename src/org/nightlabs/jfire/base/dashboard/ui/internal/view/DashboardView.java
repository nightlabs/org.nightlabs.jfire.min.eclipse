/**
 * 
 */
package org.nightlabs.jfire.base.dashboard.ui.internal.view;

import java.util.LinkedList;
import java.util.List;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.clientui.ui.layout.GridLayoutUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.dashboard.ui.DashboardGadgetRegistry;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.internal.DashboardGadgetContainer;
import org.nightlabs.jfire.base.dashboard.ui.internal.config.ConfigureDashboardGadgetWizard;
import org.nightlabs.jfire.base.jdo.GlobalJDOManagerProvider;
import org.nightlabs.jfire.base.login.ui.part.LSDViewPart;
import org.nightlabs.jfire.base.ui.config.ConfigUtil;
import org.nightlabs.jfire.config.dao.ConfigModuleDAO;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.notification.NotificationAdapterCallerThread;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author abieber
 *
 */
public class DashboardView extends LSDViewPart {

	private static final String[] CF_MOD_FETCH_GROUPS = new String[] {
							FetchPlan.DEFAULT,
							AbstractEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT, 
							AbstractEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES, 
							AbstractEditLayoutEntry.FETCH_GROUP_GRID_DATA};
	
	
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private List<DashboardGadgetContainer> gadgetContainers = new LinkedList<DashboardGadgetContainer>();
	
	private DashboardLayoutConfigModule<DashboardGadgetLayoutEntry<?>> configModule;
	
	private boolean viewStoredConfigModuleItself = false;
	
	private NotificationListener changeListener = new NotificationAdapterCallerThread() {
		@Override
		public void notify(NotificationEvent notificationEvent) {
			if (!viewStoredConfigModuleItself) {
				load();
			} else {
				viewStoredConfigModuleItself = false;
			}
		}
	};

	public DashboardView() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.part.ControllablePart#createPartContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartContents(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		
		GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().addNotificationListener(DashboardLayoutConfigModule.class, changeListener);
		form.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().removeNotificationListener(DashboardLayoutConfigModule.class, changeListener);
			}
		});

		load();
	}

	private void load() {
		Job loadConfigJob = new Job("Loading dashboard config...") {
			
			@SuppressWarnings("unchecked")
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				configModule = ConfigUtil.getUserCfMod(DashboardLayoutConfigModule.class, CF_MOD_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				
				createViewContents();
				
				return Status.OK_STATUS;
			}

		};
		loadConfigJob.schedule();
	}
	
	private void createViewContents() {
		form.getDisplay().syncExec(new Runnable() {
			public void run() {
				GridLayout layout = GridLayoutUtil.createGridLayout(configModule.getGridLayout());
				
				form.getBody().setLayout(layout);
				
				clearFormBody();
				gadgetContainers.clear();

				for (DashboardGadgetLayoutEntry<?> layoutEntry : configModule.getEditLayoutEntries()) {

					DashboardGadgetContainer gadgetContainer = createGadgetContainer(layoutEntry);
					gadgetContainers.add(gadgetContainer);
					
					gadgetContainer.createGadgetControl();
					gadgetContainer.setLayoutEntry(layoutEntry, true);
					
					if (gadgetContainer.getGadgetFactory() != null) {
						createGadgetActions(gadgetContainer);
					}

				}

				form.layout(true, true);
				form.getBody().layout(true, true);
				form.redraw();
				getSite().getShell().update();
				getSite().getShell().layout(true, true);
			}
		});		
	}
	
	private void clearFormBody() {
		Control[] children = form.getBody().getChildren();
		if (children.length > 0) {
			for (Control child : children) {
				child.dispose();
			}
		}
	}
	
	private DashboardGadgetContainer createGadgetContainer(DashboardGadgetLayoutEntry<?> layoutEntry) {
		ToolBarSectionPart gadgetSection = new ToolBarSectionPart(toolkit, form.getBody(), ExpandableComposite.TITLE_BAR, layoutEntry.getName());
		GridData gridData = GridLayoutUtil.createGridData(layoutEntry.getGridData());
		gadgetSection.getSection().setLayoutData(gridData);
		IDashboardGadgetFactory gadgetFactory = DashboardGadgetRegistry.sharedInstance().getFactory(layoutEntry.getEntryType());
		return new DashboardGadgetContainer(gadgetSection, gadgetFactory);
	}
	
	
	@SuppressWarnings("unchecked")
	private void removeGadget(DashboardGadgetContainer container) {
		configModule.removeEditLayoutEntry((DashboardGadgetLayoutEntry)container.getLayoutEntry());
		Job storeConfigJob = new Job("Storing dashboard config...") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				storeConfigModule(monitor);
				createViewContents();
				return Status.OK_STATUS;
			}
		};
		storeConfigJob.schedule();
	}
	
	@SuppressWarnings("unchecked")
	private void storeConfigModule(ProgressMonitor monitor) {
		viewStoredConfigModuleItself = true;
		configModule = (DashboardLayoutConfigModule<DashboardGadgetLayoutEntry<?>>) ConfigModuleDAO.sharedInstance().storeConfigModule(configModule, true, CF_MOD_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
	
	
	private void modifyGadget(final DashboardGadgetContainer changedContainer) {
		Job storeConfigJob = new Job("Storing dashboard config...") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				storeConfigModule(monitor);
				updateLayoutEntries(changedContainer);
				return Status.OK_STATUS;
			}
		};
		storeConfigJob.schedule();
	}
	
	private void updateLayoutEntries(final DashboardGadgetContainer changedContainer) {
		form.getDisplay().syncExec(new Runnable() {
			public void run() {		
				for (DashboardGadgetContainer container : gadgetContainers) {
					DashboardGadgetLayoutEntry<?> newLayoutEntry = getLayoutEntry(container.getLayoutEntry());
					container.setLayoutEntry(newLayoutEntry, container == changedContainer);
				}
			}
		});		
		
	}
	
	private DashboardGadgetLayoutEntry<?> getLayoutEntry(DashboardGadgetLayoutEntry<?> oldEntry) {
		List<DashboardGadgetLayoutEntry<DashboardGadgetLayoutEntry<?>>> editLayoutEntries = configModule.getEditLayoutEntries();
		for (DashboardGadgetLayoutEntry<DashboardGadgetLayoutEntry<?>> newEntry : editLayoutEntries) {
			if (oldEntry.equals(newEntry)) {
				return newEntry;
			}
		}
		return null;
	}
	
	private void createGadgetActions(final DashboardGadgetContainer container) {
		container.getToolBarManager().add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return AbstractUIPlugin.imageDescriptorFromPlugin(
						"org.nightlabs.jfire.base.dashboard.ui", 
						"icons/internal/view/DashboardView-ConfigureGadgetAction.16x16.png");
			}
			@Override
			public void run() {
				ConfigureDashboardGadgetWizard wiz = new ConfigureDashboardGadgetWizard(container.getGadgetFactory(), container.getLayoutEntry());
				WizardDialog dlg = new WizardDialog(getSite().getShell(), wiz);
				if (dlg.open() == Window.OK) {
					modifyGadget(container);
				}
			}
		});
		
		container.getToolBarManager().add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return AbstractUIPlugin.imageDescriptorFromPlugin(
						"org.nightlabs.jfire.base.dashboard.ui", 
						"icons/internal/view/DashboardView-DeleteGadgetAction.16x16.png");
			}
			@Override
			public void run() {
				if (MessageDialog.openQuestion(getSite().getShell(), "Remove gadget?", "Do you really want to remove this gadget?")) {
					removeGadget(container);
				}
			}
		});
		
		
		
		container.getToolBarManager().update(true);
	}
}
