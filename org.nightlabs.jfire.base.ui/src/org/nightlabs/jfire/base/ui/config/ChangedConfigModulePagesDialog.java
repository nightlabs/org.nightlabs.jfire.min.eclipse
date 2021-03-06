/**
 *
 */
package org.nightlabs.jfire.base.ui.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigModule;

/**
 * @author Marius Heinzmann <marius[AT]nightlabs[DOT]de>
 */
public class ChangedConfigModulePagesDialog
extends ResizableTitleAreaDialog
{
	private ChangedConfigModulePagesDialog() {
		super(RCPUtil.getActiveShell(), null);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setBlockOnOpen(false);
//		message = "The following configurations changed. Mark the Modules that shall be reloaded.";
	}

	protected static class ContentProvider implements ITreeContentProvider {
		Map<Config, Set<PageModulePair>> updatedModules = null;

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Map) {
				updatedModules = (Map<Config, Set<PageModulePair>>) inputElement;
				return updatedModules.keySet().toArray();
			}

			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Config) {
				if (updatedModules.get(element) != null)
					return true;
			}

			return false;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Config) {
				Config currentConfig = (Config) parentElement;
				return updatedModules.get(currentConfig).toArray();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	protected static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof Config)
				return getConfigLabel(((Config) element));

			return ((PageModulePair) element).getCorrespondingPage().getTitle();
		}

		/**
		 * Get a better human readable output from the ConfigKey of a Config.
		 *
		 * @param config The Config to label.
		 * @return The ObjectID - "ID" + first letter in upper-case of the object the Config is attached to appended with
		 * 	the id string. E.g. "jdo/org.nightlabs.jfire.security.id.UserID?organisationID=chezfrancois.jfire.org&userID=francois"
		 * 	-\> 'User "francois"'.
		 */
		protected String getConfigLabel(Config config)
		{
			StringBuilder labelBldr = new StringBuilder();
			final String configKey = config.getConfigKey();
			int beginningOfParams = configKey.indexOf('?');
			if (beginningOfParams == -1)
				return configKey;

			String paramsString = configKey.substring(beginningOfParams+1);
			String[] params = paramsString.split("&");
			for (String param : params)
			{
				if (!param.contains("ID") || param.contains("organisationID"))
					continue;

				String[] keyValue = param.split("=");
				String affectedObjectID = keyValue[0].replaceFirst("ID", "");
				String firstChar = String.valueOf(affectedObjectID.charAt(0));
				affectedObjectID = firstChar.toUpperCase() + affectedObjectID.substring(1);

				labelBldr.append(affectedObjectID).append(" \"").append(keyValue[1]).append("\"");
				break;
			}
			return labelBldr.toString();
		}
	}

	protected class PageModulePair {
		private final AbstractConfigModulePreferencePage page;
		private final ConfigModule updatedConfigModule;

		public PageModulePair(AbstractConfigModulePreferencePage page, ConfigModule updatedModule) {
			this.page = page;
			this.updatedConfigModule = updatedModule;
		}

		public AbstractConfigModulePreferencePage getCorrespondingPage() {
			return page;
		}

		public ConfigModule getUpdatedConfigModule() {
			return updatedConfigModule;
		}
	}

	private final Map<Config, Set<PageModulePair>> updatedConfigs = new HashMap<Config, Set<PageModulePair>>();

	private TreeViewer treeViewer;

	private static ChangedConfigModulePagesDialog sharedInstance = null;

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.base.ui.config.ChangedConfigModulePagesDialog.title")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.config.ChangedConfigModulePagesDialog.titlearea.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.base.ui.config.ChangedConfigModulePagesDialog.message")); //$NON-NLS-1$
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 300);
	}

	/**
	 * Adds an updated {@link ConfigModule} to the dialog , which asks the user to mark the modules that
	 * shall be updated from the server. If no dialog is open, a new one is created.
	 * <p>
	 * This method must be called on the GUI Thread!
	 * </p>
	 *
	 * @param page the {@link AbstractConfigModulePreferencePage}, which may be updated
	 * @param updatedModule the updated {@link ConfigModule}
	 * @param correspondingConfig the config corresponding to the changed module
	 */
	public static void addChangedConfigModule(AbstractConfigModulePreferencePage page, ConfigModule updatedModule)
	{
		// TODO we should NOT pass the correspondingConfig because it allows inconsistent data to be passed
		// to this dialog. Instead the correspondingConfig should be read from the config module.
		// It should, however, only be detached with the primary keys (FetchPlan.DEFAULT) and NOT any other fields!
		// TODO requires further thoughts because of modification notifications (the module would get dirty on the client side if the config is marked dirty)
		try {
			if (updatedModule.getConfig() == null)
				throw new RuntimeException("The updated ConfigModule does not belong to the given Config!"); //$NON-NLS-1$
		} catch (JDODetachedFieldAccessException e) {
			throw new RuntimeException("The ConfigModule passed to the ChangedConfigModuleDialog was not" + //$NON-NLS-1$
					" detached with its Config information! Please detach the Config as well; a flat copy is enough.", e); //$NON-NLS-1$
		}

		if (sharedInstance == null) {
			sharedInstance = new ChangedConfigModulePagesDialog();
			sharedInstance.open();
		}

		sharedInstance._addChangedConfigModule(page, updatedModule);
	}

	private void _addChangedConfigModule(AbstractConfigModulePreferencePage page, ConfigModule updatedModule)
	{
		Config correspConfig = updatedModule.getConfig();
		Set<PageModulePair> changedModulesOfConfig = updatedConfigs.get(correspConfig);
		if (changedModulesOfConfig == null) {
			changedModulesOfConfig = new HashSet<PageModulePair>();
			updatedConfigs.put(correspConfig, changedModulesOfConfig);
		}
		changedModulesOfConfig.add(new PageModulePair(page, updatedModule));

		treeViewer.refresh(updatedConfigs);
		treeViewer.expandToLevel(correspConfig, 2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		StructuredSelection selected = (StructuredSelection) treeViewer.getSelection();
		for (Iterator<?> it = selected.iterator(); it.hasNext();) {
			Object markedEntry = it.next();
			if (markedEntry instanceof Config) {
				Config markedConfig = (Config) markedEntry;
				for (PageModulePair item : updatedConfigs.get(markedConfig)) {
					// update each page with the updated ConfigModule
//					item.getCorrespondingPage().updateGuiWith(item.getUpdatedConfigModule());
					item.getCorrespondingPage().getConfigModuleController().updateGuiWith(item.getUpdatedConfigModule());
				}
			} // (markedEntry instanceof Config)
			else {
				PageModulePair item = (PageModulePair) markedEntry;
//				item.getCorrespondingPage().updateGuiWith(item.getUpdatedConfigModule());
				item.getCorrespondingPage().getConfigModuleController().updateGuiWith(item.getUpdatedConfigModule());
			}
		}
		super.okPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		treeViewer = new TreeViewer(area, SWT.CHECK);
		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		treeViewer.setInput(updatedConfigs);
		treeViewer.getTree().addSelectionListener( new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				setCheckedStatus(item);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// Following the javadoc, this should be called on a double click... well it isn't.
				TreeItem item = (TreeItem) e.item;
				setCheckedStatus(item);
			}

			private void setCheckedStatus(TreeItem item) {
				boolean checkedStatus = item.getChecked();
				if (item.getData() instanceof Config) {
					// clicked on a config -> mark / unmark all children
					for (TreeItem child : item.getItems()) {
						child.setChecked(checkedStatus);
					}
				} else {
					// clicked on a ConfigModule -> mark / unmark Config if all / none are checked
					TreeItem correspConfig = item.getParentItem();
					if (! checkedStatus) {
						// item was made unchecked -> if parent was checked --> mark unchecked
						if (correspConfig.getChecked())
							correspConfig.setChecked(false);
					} else {
						// item was marked checked -> if all ConfigModules are now checked --> check correspConfig
						boolean allChecked = true;
						for (int i = 0; i < correspConfig.getItemCount(); i++) {
							TreeItem configModule = correspConfig.getItem(i);
							if (! configModule.getChecked()) {
								allChecked = false;
								break;
							}
						}

						if (allChecked)
							correspConfig.setChecked(true);
					}
				} // item == PageConfigModulePair
			}
		});

//		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(SelectionChangedEvent event) {
//				StructuredSelection selection = (StructuredSelection) event.getSelection();
//				if (selection.isEmpty())
//					return;
//
//				for (Iterator it = selection.iterator(); it.hasNext();) {
//					Object markedEntry = it.next();
//					if (markedEntry instanceof Config) {
//						// How to get to the widget which represents this Config??
//						Config markedConfig = (Config) markedEntry;
//						treeViewer.setSelection( // sets the selection no the checked state.
//								new StructuredSelection(updatedConfigs.get(markedConfig).toArray()),
//								true
//								);
//					}
//				}
//			}
//		});
		return area;
	}

	@Override
	public boolean close() {
		boolean res = super.close();
		if (res)
			sharedInstance = null;

		return res;
	}

}
