/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.jfire.prop.StructField;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructFieldAddScriptValidatorHandler 
extends AbstractAddScriptValidatorHandler 
{
	private StructField<?> structField;
	
	public StructFieldAddScriptValidatorHandler(StructField<?> structField) {
		this.structField = structField;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddScriptValidatorHandler#getTemplateText()
	 */
	@Override
	public String getTemplateText() 
	{
		final String LINE_BREAK = "\n"; 
		String key = getScriptValidatorEditor().getCurrentKey();
		StringBuilder sb = new StringBuilder();
		sb.append("importPackage(Packages.org.nightlabs.jfire.prop);" + LINE_BREAK);
		sb.append("if (dataField.isEmpty()) {" + LINE_BREAK);
		sb.append("\""+ key +"\";" + LINE_BREAK);
		sb.append("}" + LINE_BREAK);
		sb.append("else {" + LINE_BREAK);
		sb.append("	undefined" + ";" + LINE_BREAK);
		sb.append("}");
		return sb.toString();
	}

}
