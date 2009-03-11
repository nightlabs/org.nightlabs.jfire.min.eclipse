/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.jfire.base.expression.Composition;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.GenericDataFieldNotEmptyExpression;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public abstract class AbstractAddExpressionValidatorHandler 
implements IAddExpressionValidatorHandler 
{
	private ExpressionValidatorComposite expressionValidatorComposite;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddExpressionValidatorHandler#getExpressionValidatorComposite()
	 */
	@Override
	public ExpressionValidatorComposite getExpressionValidatorComposite() {
		return expressionValidatorComposite;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddExpressionValidatorHandler#setExpressionValidatorComposite(org.nightlabs.jfire.base.ui.prop.structedit.ExpressionValidatorComposite)
	 */
	@Override
	public void setExpressionValidatorComposite(ExpressionValidatorComposite composite) {
		this.expressionValidatorComposite = composite;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddExpressionValidatorHandler#addExpressionPressed()
	 */
	@Override
	public void addExpressionPressed() 
	{
		StructFieldID structFieldID = getStructFieldID();
		if (structFieldID != null) {
			IExpression newExpression = new GenericDataFieldNotEmptyExpression(structFieldID);
			IExpression selectedExpression = getExpressionValidatorComposite().getSelectedExpression();
			if (selectedExpression != null) {
				if (selectedExpression instanceof Composition) {
					Composition composition = (Composition) selectedExpression;
					composition.addExpression(newExpression);
					getExpressionValidatorComposite().refresh();
				}
			}
			else if (getExpressionValidatorComposite().getExpression() == null) {
				getExpressionValidatorComposite().setExpression(newExpression);
			}
		}
	}
	
	protected abstract StructFieldID getStructFieldID();
}
