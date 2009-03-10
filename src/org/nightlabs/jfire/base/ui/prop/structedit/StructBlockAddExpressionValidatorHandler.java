/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructBlockAddExpressionValidatorHandler 
extends AbstractAddExpressionValidatorHandler
{
	private StructBlockID structBlockID;
	
	public StructBlockAddExpressionValidatorHandler(StructBlockID structBlockID) {
		this.structBlockID = structBlockID;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddExpressionValidatorHandler#addExpressionPressed()
	 */
	@Override
	public void addExpressionPressed() {
		
	}

}
