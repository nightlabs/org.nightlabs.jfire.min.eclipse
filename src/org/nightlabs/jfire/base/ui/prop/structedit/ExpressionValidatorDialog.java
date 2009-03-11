/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.ui.prop.structedit.ExpressionValidatorComposite.Mode;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class ExpressionValidatorDialog extends ResizableTitleAreaDialog 
{
	private ExpressionValidatorComposite expressionValidatorComposite;
	private IExpression expression;
	private I18nText message;
	private ValidationResultType validationResultType;
	private IStruct struct;
	private IAddExpressionValidatorHandler handler;
	private Mode mode;
	
	/**
	 * @param shell
	 * @param resourceBundle
	 */
	public ExpressionValidatorDialog(Shell shell, ResourceBundle resourceBundle, IExpression expression,
			IStruct struct, IAddExpressionValidatorHandler handler, Mode mode) 
	{
		super(shell, resourceBundle);
		this.expression = expression;
		this.struct = struct;
		this.handler = handler;
		this.mode = mode;
	}

	@Override
	protected Control createDialogArea(Composite parent) 
	{
		setTitle("Expression Validator");
		getShell().setText("Expression Validator");
		setMessage("Add/Edit an expression based validator");
		
		expressionValidatorComposite = new ExpressionValidatorComposite(parent, SWT.NONE, expression, struct, handler, mode);
		if (message != null) {
			expressionValidatorComposite.setMessage(message);
		}
		if (validationResultType != null) {
			expressionValidatorComposite.setValidationResultType(validationResultType);
		}
		return expressionValidatorComposite;
	}
		
	public ExpressionValidatorComposite getExpressionValidatorComposite() {
		return expressionValidatorComposite;
	}
	
	/**
	 * Sets the message.
	 * @param message the message to set
	 */
	public void setMessage(I18nText message) {
		// remember because usually called before #createDialogArea and can then be set there
		this.message = message;
		if (expressionValidatorComposite != null) {
			expressionValidatorComposite.setMessage(message);
		}
	}

	/**
	 * Sets the validationResultType.
	 * @param validationResultType the validationResultType to set
	 */
	public void setValidationResultType(ValidationResultType validationResultType) {
		// remember because usually called before #createDialogArea and can then be set there
		this.validationResultType = validationResultType;
		if (expressionValidatorComposite != null) {
			expressionValidatorComposite.setValidationResultType(validationResultType);
		}
	}

}
