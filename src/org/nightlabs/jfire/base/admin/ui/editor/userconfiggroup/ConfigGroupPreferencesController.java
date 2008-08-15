package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.dao.ConfigDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.progress.ProgressMonitor;

public class ConfigGroupPreferencesController extends EntityEditorPageController {
	private ConfigID configGroupID;
	private ConfigGroup configGroup;

	public ConfigGroupPreferencesController(EntityEditor editor) {
		super(editor);
		this.configGroupID = ((JDOObjectEditorInput<ConfigID>)editor.getEditorInput()).getJDOObjectID();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageController#doLoad(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public void doLoad(ProgressMonitor monitor) {
		configGroup = (ConfigGroup) ConfigDAO.sharedInstance().getConfig(configGroupID, new String[] { FetchPlan.DEFAULT }, -1, monitor);
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.entity.editor.IEntityEditorPageController#doSave(org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	public boolean doSave(ProgressMonitor monitor) {
		ConfigDAO.sharedInstance().storeConfig(configGroup, false, new String[] { User.FETCH_GROUP_NAME }, -1, monitor);
		return true;
	}
	
	public ConfigGroup getConfigGroup() {
		return configGroup;
	}
}
