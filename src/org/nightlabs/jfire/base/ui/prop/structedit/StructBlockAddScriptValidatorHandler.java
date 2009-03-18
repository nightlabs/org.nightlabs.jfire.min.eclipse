package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructBlockAddScriptValidatorHandler 
extends AbstractAddScriptValidatorHandler 
{
	private StructBlock structBlock;
	
	public StructBlockAddScriptValidatorHandler(StructBlock structBlock) 
	{
		if (structBlock == null)
			throw new IllegalArgumentException("Param structBlock muts not be null!");
		
		this.structBlock = structBlock;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IAddScriptValidatorHandler#addTemplatePressed()
	 */
	@Override
	public String getTemplateText() 
	{
		final String LINE_BREAK = "\n"; 
		String key = getScriptValidatorEditor().getCurrentKey();
		StringBuilder sb = new StringBuilder();
		sb.append("importPackage(Packages.org.nightlabs.jfire.prop);");
		sb.append(LINE_BREAK);
		sb.append("importPackage(Packages.org.nightlabs.jfire.prop.id);");
		sb.append(LINE_BREAK);
		for (int i=0; i<structBlock.getStructFields().size(); i++) {
			StructField<?> sf = structBlock.getStructFields().get(i);			
			String structFieldName = getStructFieldVarName(sf);
			sb.append(structFieldName+"ID");
			sb.append(" = ");
			sb.append("new StructFieldID(");
			sb.append("\"");
			sb.append(sf.getStructFieldIDObj().toString());
			sb.append("\"");
			sb.append(");");
			sb.append(LINE_BREAK);
			sb.append(structFieldName+" = dataBlock.getDataField("+structFieldName+"ID);");
			sb.append(LINE_BREAK);
		}
		sb.append("if (");
		for (int i=0; i<structBlock.getStructFields().size(); i++) {			
			StructField<?> structField = structBlock.getStructFields().get(i);
			String structFieldName = getStructFieldVarName(structField);
			sb.append(structFieldName+".isEmpty()");
			if (i != structBlock.getStructFields().size() - 1) {
				sb.append(" || ");
			}
		}
		sb.append(")");
		sb.append("{");
		sb.append(LINE_BREAK);
		sb.append("\""+ key +"\";");
		sb.append(LINE_BREAK);
		sb.append("}");
		sb.append("else {");
		sb.append(LINE_BREAK);
		sb.append("undefined;");
		sb.append(LINE_BREAK);
		sb.append("}");
		return sb.toString();
	}

	private String getStructFieldVarName(StructField<?> sf) {
		String name = sf.getName().getText().trim();
		return name.replace(" ", "-");
	}
}
