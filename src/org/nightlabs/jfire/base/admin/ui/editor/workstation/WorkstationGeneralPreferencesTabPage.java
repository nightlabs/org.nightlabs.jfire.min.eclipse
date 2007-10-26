package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;

public class WorkstationGeneralPreferencesTabPage extends EntityEditorPageWithProgress {
	
	private WorkstationDataSection workstationDataSection;
	
	public static final String ID_PAGE = WorkstationGeneralPreferencesTabPage.class.getName();
	
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new WorkstationGeneralPreferencesTabPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new WorkstationPreferencesController(editor);
		}
	}

	public WorkstationGeneralPreferencesTabPage(FormEditor editor) {
		super(editor, ID_PAGE, "General");
	}
	
	@Override
	protected void addSections(Composite parent) {
		workstationDataSection = new WorkstationDataSection(this, parent, "Workstation data");
		getManagedForm().addPart(workstationDataSection);
	}
	
	@Override
	protected void asyncCallback() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				workstationDataSection.setWorkstation(((WorkstationPreferencesController)getPageController()).getWorkstation());
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return "General";
	}
}
