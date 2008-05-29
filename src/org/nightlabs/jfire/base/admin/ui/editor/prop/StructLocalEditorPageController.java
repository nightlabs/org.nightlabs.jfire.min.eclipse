package org.nightlabs.jfire.base.admin.ui.editor.prop;

import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class StructLocalEditorPageController
extends EntityEditorPageController
{
	public StructLocalEditorPageController(EntityEditor editor) {
		super(editor);
	}

	public StructLocalEditorPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	public void doLoad(ProgressMonitor monitor) {
		try {
			monitor.beginTask("Loading structure members", 1); //$NON-NLS-1$ // this doesn't take long and is therefore probably never displayed => no externalisation necessary. Marco.
			monitor.worked(1);
		} finally {
			monitor.done();
		}
	}

	public void doSave(ProgressMonitor monitor)
	{
		for (IFormPage page : getPages()) {
			if (page instanceof StructEditorPage) {
				StructEditorPage structEditorPage = (StructEditorPage) page;
				try {
					structEditorPage.getStructEditorSection().getStructEditor().storeStructure();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
