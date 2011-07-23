package org.nightlabs.jfire.base.login.rap.ui;

import javax.servlet.Servlet;

import org.eclipse.equinox.http.helper.BundleEntryHttpContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.eclipse.equinox.jsp.jasper.JspServlet;

public class HttpServiceTracker extends ServiceTracker {
	private static final String CONTEXT_NAME="/jfire";
	private static final String ALIAS_JSP=CONTEXT_NAME+"/*.jsp";
	private static final String ALIAS_LOGIN=CONTEXT_NAME+"/login";
	
	public HttpServiceTracker(BundleContext context) {
		super(context, HttpService.class.getName(), null);
	}

	public Object addingService(ServiceReference reference) {
		final HttpService httpService = (HttpService) context.getService(reference);
		try {
			HttpContext commonContext = new BundleEntryHttpContext(context.getBundle(), "/web"); 
			httpService.registerResources(CONTEXT_NAME, "/", commonContext); 

			Servlet jspServlet = new ContextPathServletAdaptor(new JspServlet(context.getBundle(), "/web"), CONTEXT_NAME);  
			httpService.registerServlet(ALIAS_JSP, jspServlet, null, commonContext);
			
			Servlet login = new LoginServlet();
			httpService.registerServlet(ALIAS_LOGIN, login, null, commonContext);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return httpService;
	}

	public void removedService(ServiceReference reference, Object service) {
		final HttpService httpService = (HttpService) service;
		
		httpService.unregister(CONTEXT_NAME); 
		httpService.unregister(ALIAS_JSP); 
		
		super.removedService(reference, service);
	}			
}
