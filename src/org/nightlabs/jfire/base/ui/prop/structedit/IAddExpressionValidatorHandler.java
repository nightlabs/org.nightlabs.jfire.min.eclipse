/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IAddExpressionValidatorHandler 
{
	public void setExpressionValidatorEditor(IExpressionValidatorEditor editor);
	public IExpressionValidatorEditor getExpressionValidatorEditor();
	public void addExpressionPressed();
}
