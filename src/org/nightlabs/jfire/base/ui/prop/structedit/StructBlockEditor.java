package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.jfire.prop.StructBlock;

public class StructBlockEditor extends AbstractStructPartEditor<StructBlock> {

	private StructBlockEditorComposite structBlockEditorComposite;
	
	public Composite createComposite(Composite parent, int style, StructEditor structEditor, LanguageChooser languageChooser) {
		structBlockEditorComposite = new StructBlockEditorComposite(parent, style, languageChooser);
		structBlockEditorComposite.getBlockNameEditor().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				notifyModifyListeners();
			}
		});
		structBlockEditorComposite.getUniqueButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				notifyModifyListeners();
			}
		});
		return structBlockEditorComposite;
	}

	public Composite getComposite() {
		return structBlockEditorComposite;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.StructPartEditor#setData(java.lang.Object)
	 */
	public void setData(StructBlock data) {
		if (structBlockEditorComposite == null)
			throw new IllegalStateException("You have to call createComposite(...) prior to calling setData(...)"); //$NON-NLS-1$
		
		structBlockEditorComposite.setStructBlock(data);
	}

	public void setEnabled(boolean enabled) {
		if (structBlockEditorComposite != null)
			structBlockEditorComposite.setEnabled(enabled);
	}

	public I18nTextEditor getPartNameEditor() {
		return structBlockEditorComposite.getBlockNameEditor();
	}
	
	@Override
	public void setFocus() {
		if (!getPartNameEditor().isDisposed()) {
			getPartNameEditor().setFocus();
		}
	}
}
