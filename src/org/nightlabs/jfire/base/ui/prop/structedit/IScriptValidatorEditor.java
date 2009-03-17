/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.nightlabs.jfire.prop.validation.IScriptValidator;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IScriptValidatorEditor 
{
	public IScriptValidator getScriptValidator();
	
	public String getCurrentKey();
	
	public String getScript();
	
	public void setScript(String script);
}
