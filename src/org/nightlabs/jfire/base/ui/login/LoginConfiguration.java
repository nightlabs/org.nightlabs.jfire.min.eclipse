package org.nightlabs.jfire.base.ui.login;

import java.io.Serializable;

import org.nightlabs.j2ee.LoginData;

/**
 * This class holds a single login configuration without the password. It is intended to be used in {@link LoginConfigModule} to
 * be able to store multiple login identities to let the user select the one to be used for the next login.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marius Heinzmann -- Marius[at]NightLabs[dot]de
 */
public class LoginConfiguration 
	implements Serializable, Cloneable 
{
	private static final long serialVersionUID = 5L;

	private LoginData loginData;
	private boolean automaticUpdate = false;
	
	private String name = null;

	public LoginConfiguration() {
		this.loginData = new LoginData();
	}

	/**
	 * TODO amend for use with automatic update
	 *
	 * @param _loginData 
	 */
	public LoginConfiguration(LoginData _loginData)
	{
		this.loginData = new LoginData(_loginData);
	}
	
	public boolean isAutomaticUpdate() {
		return automaticUpdate;
	}

	public void setAutomaticUpdate(boolean automaticUpdate) {
		this.automaticUpdate = automaticUpdate;
	}

	/**
	 * @return the loginData of this configuration.
	 */
	public LoginData getLoginData() {
		return loginData;
	}
	
	/**
	 * @param loginData the loginData to use.
	 */
	public void setLoginData(LoginData loginData) {
		this.loginData = loginData;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String configurationName) {
		this.name = configurationName;
	}
	
	@Override
	public String toString() {
		if (name == null || "".equals(name)) //$NON-NLS-1$
			return loginData.getUserID() + "@" + loginData.getOrganisationID() + " (" + loginData.getWorkstationID() + ") (" + loginData.getProviderURL() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		else
			return name;
	}
	
	public String toShortString() {
		if (name == null || "".equals(name)) { //$NON-NLS-1$
			return shorten(loginData.getUserID(), 8) +	"@" + shorten(loginData.getOrganisationID(), 8) + " (" + shorten(loginData.getWorkstationID(), 8) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return name;
	}
	
	public String shorten(String target, int count) {
		if (target.length() <= count)
			return target;
		
		int tailCount = count/2;
		int headCount = count - tailCount;
		String front = target.substring(0, headCount);
		String tail = target.substring(target.length()-tailCount);
		return front + ".." + tail; //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		if (name != null)
			return name.hashCode();
		else
			return loginData.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		final LoginConfiguration other = (LoginConfiguration) obj;
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		// both names == null
		return loginData.equals(other.loginData);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		LoginConfiguration clone = (LoginConfiguration) super.clone();
		clone.loginData = new LoginData(loginData);
		return clone;
	}
}