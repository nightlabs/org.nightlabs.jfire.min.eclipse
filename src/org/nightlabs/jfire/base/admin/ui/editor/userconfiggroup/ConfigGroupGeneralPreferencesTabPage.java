package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;

public class ConfigGroupGeneralPreferencesTabPage extends EntityEditorPageWithProgress {
	
	private ConfigGroupDataSection configGroupDataSection;
	
	public static final String ID_PAGE = ConfigGroupGeneralPreferencesTabPage.class.getName();
	
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new ConfigGroupGeneralPreferencesTabPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new ConfigGroupPreferencesController(editor);
		}
	}

	public ConfigGroupGeneralPreferencesTabPage(FormEditor editor) {
		super(editor, ID_PAGE, "General");
	}
	
	@Override
	protected void addSections(Composite parent) {
		configGroupDataSection = new ConfigGroupDataSection(this, parent, "Config group data");
		getManagedForm().addPart(configGroupDataSection);
	}
	
	@Override
	protected void asyncCallback() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				configGroupDataSection.setConfigGroup(((ConfigGroupPreferencesController)getPageController()).getConfigGroup());
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return "General";
	}
}
