/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import java.rmi.RemoteException;
import java.util.Collection;

import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;

/**
 * @author alex
 *
 */
public class StructEditorUtil {

	public static PropertyManager getPropertyManager() {
		try {
			return JFireEjbUtil.getBean(PropertyManager.class, Login.getLogin().getInitialContextProperties());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static Collection<StructID> getAvailableStructIDs() {
		try {
			return getPropertyManager().getAvailableStructIDs();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static Collection<StructLocalID> getAvailableStructLocalIDs() {
		try {
			return getPropertyManager().getAvailableStructLocalIDs();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static long getDataFieldInstanceCount(StructFieldID structFieldID) {
		try {
			return getPropertyManager().getDataFieldInstanceCount(structFieldID);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
