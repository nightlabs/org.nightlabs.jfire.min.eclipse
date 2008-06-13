/**
 * 
 */
package org.nightlabs.jfire.base.ui.exceptionhandler;

import java.rmi.ConnectException;

import javax.naming.CommunicationException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.nightlabs.base.ui.exceptionhandler.DefaultErrorDialog;
import org.nightlabs.base.ui.exceptionhandler.ErrorDialogFactory;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerParam;
import org.nightlabs.base.ui.exceptionhandler.IExceptionHandler;

/**
 * An Implementation of IExceptionHandler, which shows a message user friendly message
 * if a java.net.ConnectException occurs.
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ConnectExceptionHandler implements IExceptionHandler {

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.exceptionhandler.IExceptionHandler#handleException(org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerParam)
	 */
	@Override
	public boolean handleException(ExceptionHandlerParam handlerParam) 
	{
		Throwable triggerException = handlerParam.getTriggerException();
		Throwable thrownException = handlerParam.getThrownException();
		if (triggerException instanceof java.net.ConnectException) {
			String message = "The server "+getHost(thrownException)+" could not be reached. Please check your internet connection or consult your administrator. \n" +  
			"If your internet connection is ok, maybe the server is temporary not available. Then please try again later.";						
			ErrorDialogFactory.showError(DefaultErrorDialog.class,
				"Connection refused",
				message,
				handlerParam);
			return true;			
		}
		return false;		
	}

	private String getHost(Throwable thrownException) 
	{
		String host = "Unknown";
		Throwable t = null;
		int index = ExceptionUtils.indexOfThrowable(thrownException, ConnectException.class);
		if (index != -1) {
			t = (Throwable) ExceptionUtils.getThrowableList(thrownException).get(index);
			String message = t.getMessage();
			int messageIndex = message.lastIndexOf(":");
			if (messageIndex != -1) {
				host = message.substring(index + 1);
				return host;
			}			
		}
		
		int index2 = ExceptionUtils.indexOfThrowable(thrownException, CommunicationException.class);
		if (index2 != -1) {
			t = (Throwable) ExceptionUtils.getThrowableList(thrownException).get(index2);			
			String message = t.getMessage();
			String replace = message.replace("Could not obtain connection to any of these urls:", "");
			int lastIndex = replace.lastIndexOf("and discovery failed with error");
			if (lastIndex != -1) {
				host = replace.substring(0, lastIndex);
			}
		}		
		return host;		
	}
}
