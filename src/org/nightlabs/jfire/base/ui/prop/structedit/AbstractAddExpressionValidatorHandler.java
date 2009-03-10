/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

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

}
