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
import org.eclipse.swt.SWT;
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
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.clientui.ui.layout.GridLayoutUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.dashboard.ui.DashboardGadgetRegistry;
import org.nightlabs.jfire.base.dashboard.ui.IDashboardGadgetFactory;
import org.nightlabs.jfire.base.dashboard.ui.internal.DashboardGadgetContainer;
import org.nightlabs.jfire.base.dashboard.ui.internal.config.ConfigureDashboardGadgetWizard;
import org.nightlabs.jfire.base.dashboard.ui.resource.Messages;
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
	
	
	private FormToolkit toolkit;
	private ScrolledForm form;
	private XComposite wrapper;
	
	private DashboardLayoutConfigModule<DashboardGadgetLayoutEntry<?>> configModule;
	private List<DashboardGadgetContainer> gadgetContainers = new LinkedList<DashboardGadgetContainer>();
	
	
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
		Job loadConfigJob = new Job(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.view.DashboardView.loadConfigJob.name")) { //$NON-NLS-1$
			
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
				
				clearFormBody();
				
				configureBody(form.getBody());
				
				wrapper = new XComposite(form.getBody(), SWT.NONE, LayoutMode.TIGHT_WRAPPER);
				
				GridLayout layout = GridLayoutUtil.createGridLayout(configModule.getGridLayout());
				if (layout.verticalSpacing < 10)
					layout.verticalSpacing = 10;
				if (layout.horizontalSpacing < 10)
					layout.horizontalSpacing = 10;
				
				wrapper.setLayout(layout);
				// widthHint = 1 is a workaround for horizontally growing pages (preventing horizontal scrollbar)
				wrapper.getGridData().widthHint = 1;
				
				
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
				wrapper.layout(true, true);
				wrapper.update();
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
	
	protected void configureBody(Composite body) {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		body.setLayout(layout);
	}	
	
	private DashboardGadgetContainer createGadgetContainer(DashboardGadgetLayoutEntry<?> layoutEntry) {
		ToolBarSectionPart gadgetSection = new ToolBarSectionPart(toolkit, wrapper, ExpandableComposite.TITLE_BAR, layoutEntry.getName());
		GridData gridData = GridLayoutUtil.createGridData(layoutEntry.getGridData());
		gridData.verticalAlignment = SWT.BEGINNING;
		gadgetSection.getSection().setLayoutData(gridData);
		IDashboardGadgetFactory gadgetFactory = DashboardGadgetRegistry.sharedInstance().getFactory(layoutEntry.getEntryType());
		return new DashboardGadgetContainer(gadgetSection, gadgetFactory);
	}
	
	
	@SuppressWarnings("unchecked")
	private void removeGadget(DashboardGadgetContainer container) {
		configModule.removeEditLayoutEntry((DashboardGadgetLayoutEntry)container.getLayoutEntry());
		Job storeConfigJob = new Job(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.view.DashboardView.storeConfigJob.name")) { //$NON-NLS-1$
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
		Job storeConfigJob = new Job(Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.view.DashboardView.storeConfigJob.name")) { //$NON-NLS-1$
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
						"org.nightlabs.jfire.base.dashboard.ui",  //$NON-NLS-1$
						"icons/internal/view/DashboardView-ConfigureGadgetAction.16x16.png"); //$NON-NLS-1$
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
						"org.nightlabs.jfire.base.dashboard.ui",  //$NON-NLS-1$
						"icons/internal/view/DashboardView-DeleteGadgetAction.16x16.png"); //$NON-NLS-1$
			}
			@Override
			public void run() {
				if (MessageDialog.openQuestion(getSite().getShell(), Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.view.DashboardView.confirmDeleteDialog.title"), Messages.getString("org.nightlabs.jfire.base.dashboard.ui.internal.view.DashboardView.confirmDeleteDialog.message"))) { //$NON-NLS-1$ //$NON-NLS-2$
					removeGadget(container);
				}
			}
		});
		
		
		
		container.getToolBarManager().update(true);
	}
}
