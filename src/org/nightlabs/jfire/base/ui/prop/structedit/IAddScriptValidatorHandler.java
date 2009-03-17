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
	public IScriptValidatorEditor getScriptValidatorEditor();
	public void setScriptValidatorEditor(IScriptValidatorEditor dialog);
//	public String getTemplateText();
	public void addTemplatePressed();
}
