/**
 * 
 */
package org.nightlabs.jfire.base.j2ee.osgi;

import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.nightlabs.classloader.osgi.DelegatingClassLoaderOSGI;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class RemoteClassLoadingHook implements ClassLoadingHook, HookConfigurator {

	/**
	 * 
	 */
	public RemoteClassLoadingHook() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#addClassPathEntry(java.util.ArrayList, java.lang.String, org.eclipse.osgi.baseadaptor.loader.ClasspathManager, org.eclipse.osgi.baseadaptor.BaseData, java.security.ProtectionDomain)
	 */
	public boolean addClassPathEntry(ArrayList cpEntries, String cp, ClasspathManager hostmanager, BaseData sourcedata, ProtectionDomain sourcedomain) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#createClassLoader(java.lang.ClassLoader, org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate, org.eclipse.osgi.framework.adaptor.BundleProtectionDomain, org.eclipse.osgi.baseadaptor.BaseData, java.lang.String[])
	 */
	public BaseClassLoader createClassLoader(ClassLoader parent, ClassLoaderDelegate delegate, BundleProtectionDomain domain, BaseData data, String[] bundleclasspath) {
		System.out.println("RemoteClassLoadingHook called createClassLoader for "+data.getBundle().getSymbolicName());
		System.out.println(delegate.getClass().toString());
		if (data.getBundle().getSymbolicName().equals("org.nightlabs.jfire.base.j2ee")) {
			System.out.println("RemoteClassLoadingHook called createClassLoader returning DelegatingClassLoaderOSGI");
			return DelegatingClassLoaderOSGI.createSharedInstance(parent, delegate, domain, data, bundleclasspath);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#findLibrary(org.eclipse.osgi.baseadaptor.BaseData, java.lang.String)
	 */
	public String findLibrary(BaseData data, String libName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#getBundleClassLoaderParent()
	 */
	public ClassLoader getBundleClassLoaderParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#initializedClassLoader(org.eclipse.osgi.baseadaptor.loader.BaseClassLoader, org.eclipse.osgi.baseadaptor.BaseData)
	 */
	public void initializedClassLoader(BaseClassLoader baseClassLoader, BaseData data) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#processClass(java.lang.String, byte[], org.eclipse.osgi.baseadaptor.loader.ClasspathEntry, org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry, org.eclipse.osgi.baseadaptor.loader.ClasspathManager)
	 */
	public byte[] processClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry, ClasspathManager manager) {
		return null;
	}

	public void addHooks(HookRegistry hookRegistry) {
		hookRegistry.addClassLoadingHook(new RemoteClassLoadingHook());
	}

}
