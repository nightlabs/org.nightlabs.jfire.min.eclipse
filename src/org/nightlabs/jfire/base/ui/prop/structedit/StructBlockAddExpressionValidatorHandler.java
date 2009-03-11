/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructBlockAddExpressionValidatorHandler 
extends AbstractAddExpressionValidatorHandler
{
	private StructBlock structBlock;
	
	public StructBlockAddExpressionValidatorHandler(StructBlock structBlock) {
		this.structBlock = structBlock;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.AbstractAddExpressionValidatorHandler#getStructFieldID()
	 */
	@Override
	protected StructFieldID getStructFieldID() 
	{
		if (!structBlock.getStructFields().isEmpty()) {
			StructField<?> structField = structBlock.getStructFields().iterator().next();
			return (StructFieldID) JDOHelper.getObjectId(structField);
		}
		return null;
	}
	
}
