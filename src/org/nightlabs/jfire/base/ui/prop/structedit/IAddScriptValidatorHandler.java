/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IAddScriptValidatorHandler 
{
	/**
	 * Returns the IScriptValidatorEditor.
	 * @return the {@link IScriptValidatorEditor}
	 */
	public IScriptValidatorEditor getScriptValidatorEditor();
	
	/**
	 * Sets the IScriptValidatorEditor.
	 * @param editor the IScriptValidatorEditor to set.
	 */
	public void setScriptValidatorEditor(IScriptValidatorEditor editor);
	
	/**
	 * This method get called when the users presses on the add template button,
	 * in the IScriptValidatorEditor.
	 */
	public void addTemplatePressed();
}
