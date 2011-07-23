package org.nightlabs.jfire.base.login.rap.ui;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.IOUtil;

public class LoginServlet extends HttpServlet {
	public static final String ATT_LOGIN = LoginServlet.class.getName() +".loginData";
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(LoginServlet.class);
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user = request.getParameter("user");
		if (user == null || user.trim().length() == 0) {
			redirectToIndex(response, "Login is not set");
			return;
		}

		String password = request.getParameter("password");
		if (password == null || password.trim().length() == 0) {
			redirectToIndex(response, "Password is not set");
			return;
		}

		String organization = request.getParameter("organization");
		if (organization == null || organization.trim().length() == 0) {
			redirectToIndex(response, "Organization is not set");
			return;
		}

		LoginData loginData = new LoginData();

		Base62Coder coder = Base62Coder.sharedInstance();
		loginData.setSessionID(coder.encode(System.currentTimeMillis(), 1) + '-' + coder.encode((long) (Math.random() * 14776335), 1)); 

		loginData.setUserID(user);
		loginData.setPassword(password);
		loginData.setInitialContextFactory("org.jboss.security.jndi.LoginInitialContextFactory");
		loginData.setOrganisationID(organization);
		loginData.setProviderURL("jnp://localhost:1099");
		loginData.setSecurityProtocol("jfire");

		// test once again. if not - where do we get AsyncLoginResult???
//		Login.AsyncLoginResult res = Login.testLogin(loginData);
		Login.AsyncLoginResult res = new JFireRAPLoginHandler().testLogin(loginData);
		if (res.isSuccess()) {
			checkOSGIProperties();
			request.getSession().setAttribute(ATT_LOGIN, loginData);
			String contextPath = request.getContextPath();
			String redirect = contextPath + "/rap?startup=jfire";
			response.sendRedirect(redirect);
			if (logger.isDebugEnabled()) {
				logger.debug("contextPath = "+contextPath);
			}
		} else
			redirectToIndex(response, "Login failed: " + res.getMessage());
	}

	@Override
	public void init() throws ServletException {
		super.init();
		checkOSGIProperties();
		JFireSecurityConfiguration.declareConfiguration();
	}

	private void redirectToIndex(HttpServletResponse response, String message) throws UnsupportedEncodingException, IOException {
		response.sendRedirect("index.jsp?message=" + URLEncoder.encode(message, "UTF-8"));
	}
	
	private void checkOSGIProperties() 
	{
		// Necessary to set the osgi instance area because otherwise the JFire Application will not start
		// because AbstractApplication.getRootDir() requires these system properties to be set
		String osgiInstanceArea = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
		if (osgiInstanceArea == null) {
			osgiInstanceArea = System.getProperty("osgi.instance.area.default"); //$NON-NLS-1$
			if (osgiInstanceArea == null) {
				try {
					File jfireDir = new File(IOUtil.getUserHome(), ".jfirerap");
					System.setProperty("osgi.instance.area", jfireDir.getCanonicalPath());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
