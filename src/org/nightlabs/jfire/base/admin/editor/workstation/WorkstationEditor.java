package org.nightlabs.jfire.base.admin.editor.workstation;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.admin.editor.user.IConfigSetupEditor;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.id.WorkstationID;

public class WorkstationEditor
extends EntityEditor 
implements IConfigSetupEditor
{
	/**
	 * The editor id.
	 */
	public static final String EDITOR_ID = WorkstationEditor.class.getName();
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.entityeditor.EntityEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		super.firePropertyChange(PROP_TITLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getTitle()
	 */
	@Override
	public String getTitle()
	{
		if(getEditorInput() == null)
			return super.getTitle();
		return ((WorkstationID)((JDOObjectEditorInput)getEditorInput()).getJDOObjectID()).workstationID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getTitleImage()
	 */
	@Override
	public Image getTitleImage()
	{
		return super.getTitleImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#getTitleToolTip()
	 */
	@Override
	public String getTitleToolTip()
	{
		// TODO: Better tool-tip
		return ((WorkstationID)((JDOObjectEditorInput)getEditorInput()).getJDOObjectID()).workstationID;
	}

	public ConfigID getConfigID() {
		return ConfigID.create(getWorkstationID().organisationID, getWorkstationID(), Workstation.class);
	}
	
	public WorkstationID getWorkstationID() {
		return ((WorkstationEditorInput)getEditorInput()).getJDOObjectID();
	}
	
}
