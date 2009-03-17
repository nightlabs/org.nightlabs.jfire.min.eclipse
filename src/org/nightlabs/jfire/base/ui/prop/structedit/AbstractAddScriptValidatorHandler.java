/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public abstract class AbstractAddScriptValidatorHandler 
implements IAddScriptValidatorHandler 
{
	private IScriptValidatorEditor editor;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddScriptValidatorHandler#getDialog()
	 */
	@Override
	public IScriptValidatorEditor getScriptValidatorEditor() {
		return editor;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddScriptValidatorHandler#setScriptValidatorDialog(org.nightlabs.jfire.base.ui.prop.structedit.ScriptValidatorDialog)
	 */
	@Override
	public void setScriptValidatorEditor(IScriptValidatorEditor editor) {
		this.editor = editor;
	}

	public abstract String getTemplateText();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddScriptValidatorHandler#addTemplatePressed()
	 */
	@Override
	public void addTemplatePressed() {
		getScriptValidatorEditor().setScript(getTemplateText());
	}
	
}
