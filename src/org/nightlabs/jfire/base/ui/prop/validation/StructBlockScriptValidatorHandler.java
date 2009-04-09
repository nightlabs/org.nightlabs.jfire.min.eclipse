package org.nightlabs.jfire.base.ui.prop.validation;

import org.nightlabs.jfire.base.idgenerator.IDGeneratorClient;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.validation.IScriptValidator;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class StructBlockScriptValidatorHandler 
extends AbstractScriptValidatorHandler 
{
	private StructBlock structBlock;
	
	public StructBlockScriptValidatorHandler(StructBlock structBlock) 
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
	
	@Override
	public String validateScript(String script) 
	{
		IScriptValidator validator = getScriptValidatorEditor().getScriptValidator();
		String oldScript = validator.getScript();
		IStruct struct = structBlock.getStruct();
		try {
			validator.setScript(script);
			if (struct instanceof StructLocal) 
			{
				StructLocal structLocal = (StructLocal) struct; 
				PropertySet propertySet = new PropertySet(IDGeneratorClient.getOrganisationID(), 
						IDGeneratorClient.nextID(PropertySet.class), structLocal);
				propertySet.inflate(struct);
				DataBlockGroup dataBlockGroup;
				try {
					dataBlockGroup = propertySet.getDataBlockGroup(structBlock.getStructBlockIDObj());
					if (dataBlockGroup != null && !dataBlockGroup.isEmpty()) {
						DataBlock dataBlock = dataBlockGroup.getDataBlocks().iterator().next();
						ValidationResult result = validator.validate(dataBlock, structBlock);
						if (result == null) {
							return "result is null";
						}
						return null;
					}
					else {
						return "DataBlockGroup is empty";
					}
				} catch (DataBlockGroupNotFoundException e) {
					return e.getMessage();
				}
			}
			else {
				return "Struct is no StructLocal";	
			}			
		} finally {
			validator.setScript(oldScript);
		}
	}
}
