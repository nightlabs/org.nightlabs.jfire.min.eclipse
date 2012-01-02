package org.nightlabs.jfire.base.dashboard.ui.internal.clientScripts.action;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.jface.action.Action;

public class RunClientScriptAction extends Action {

	private String clientScriptContent;
	
	public RunClientScriptAction(String clientScriptContent) {
		setId(RunClientScriptAction.class.getName());
		setText("Perform client script");
		this.clientScriptContent = clientScriptContent;
	}
	
	@Override
	public void run() {
		ScriptEngineManager manager = new ScriptEngineManager();
	    
		// Engines
		// =========================
		// EngineName: Mozilla Rhino
		// EngineVersion: 1.6 release 2
		// LanguageName: ECMAScript
		// LanguageVersion: 1.6
		// Extensions: [js]
		// MimeTypes: [application/javascript, application/ecmascript, text/javascript, text/ecmascript]
		// Names: [js, rhino, JavaScript, javascript, ECMAScript, ecmascript]
//		List<ScriptEngineFactory> factoryList = manager.getEngineFactories();
		
	    ScriptEngine engine = manager.getEngineByName("js");
	    
	    try {
//	    	String clientScriptContent = "var date = new Date();" + "date.getHours()";
//			Double hour = (Double) engine.eval(clientScriptContent);
//		    String msg;
//		      
//		    if (hour < 10)
//		    	msg = "Good morning";
//		    else if (hour < 16)
//		    	msg = "Good afternoon";
//		    else if (hour < 20)
//		    	msg = "Good evening";
//		    else
//		    	msg = "Good night";
//		      
//		    System.out.println(hour);
//		    System.out.println(msg);
		    
		    engine.eval(clientScriptContent);
		    
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}
}
