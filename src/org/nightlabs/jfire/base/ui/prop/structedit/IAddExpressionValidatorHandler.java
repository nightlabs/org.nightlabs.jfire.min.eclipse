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
	public void setExpressionValidatorComposite(ExpressionValidatorComposite composite);
	public ExpressionValidatorComposite getExpressionValidatorComposite();
	public void addExpressionPressed();
}
