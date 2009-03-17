/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IExpressionValidatorEditor 
{
	public void setExpression(IExpression expression);
	
	public IExpression getExpression(); 
	
	public IExpression getSelectedExpression();
	
	public void setMessage(I18nText message);

	public I18nText getMessage();
	
	public void setValidationResultType(ValidationResultType type);
	
	public ValidationResultType getValidationResultType();
	
	public void refresh(); 
}
