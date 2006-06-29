/* *****************************************************************************
 * DelegatingClassLoader - NightLabs extendable classloader                    *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.classloader.osgi;

import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.nightlabs.classloader.ClassDataLoaderDelegate;
import org.nightlabs.classloader.ClassLoaderDelegate;
import org.nightlabs.classloader.ClassLoadingDelegator;
import org.nightlabs.classloader.IClassLoaderDelegate;
import org.nightlabs.classloader.IClassLoadingDelegator;
import org.nightlabs.classloader.LogUtil;

/**
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class DelegatingClassLoaderOSGI 
	extends DefaultClassLoader 
	implements IClassLoadingDelegator, IClassLoaderDelegate
{

	private ClassLoadingDelegator classLoadingDelegator;
	
	public DelegatingClassLoaderOSGI(ClassLoader parent, org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate delegate, ProtectionDomain domain, BaseData bundledata, String[] classpath)
	{
		super(parent, delegate, domain, bundledata, classpath);
		classLoadingDelegator = new ClassLoadingDelegator(this);
		LogUtil.log_info(this.getClass(), "init", "DelegatingClassLoader instantiated.");
	}

	/**
	 * DelegatingClassLoader overrides findLocal* methods to check its
	 * delegates when classes or resources could not be found by the
	 * parent implementation.
	 */
	@Override
	public Class findLocalClass(String classname) throws ClassNotFoundException {
		LogUtil.log_debug(this.getClass(), "findLocalClass", "Asked for "+classname);
		Class result = null;
		try {
			result = super.findLocalClass(classname);
		} catch (ClassNotFoundException e) {
			// ignore
		}
		if (result == null) {
			LogUtil.log_debug(this.getClass(), "findLocalClass", "Not found locally ask delegates.");
			result = findDelegateClass(classname);
		}
		LogUtil.log_debug(this.getClass(), "findLocalClass", "Returning "+result);
		return result;		
	}
	
	/**
	 * DelegatingClassLoader overrides findLocal* methods to check its
	 * delegates when classes or resources could not be found by the
	 * parent implementation.
	 */
	@Override
	public URL findLocalResource(String resource) {
		LogUtil.log_debug(this.getClass(), "findLocalResource", "Asked for "+resource);
		URL result = null;
		result = super.findLocalResource(resource);
		if (result == null) {
			LogUtil.log_debug(this.getClass(), "findLocalResource", "Not found locally ask delegates.");
			List<URL> res = null;
			try {
				res = findDelegateResources(resource, true);
			} catch (IOException e) {
				result = null;
			}
			if (res != null && res.iterator().hasNext())				
				result = res.iterator().next();
		}
		LogUtil.log_debug(this.getClass(), "findLocalResource", "Returning "+result);
		return result;		
	}
	
	/**
	 * DelegatingClassLoader overrides findLocal* methods to check its
	 * delegates when classes or resources could not be found by the
	 * parent implementation.
	 */
	@Override
	public Enumeration findLocalResources(String resource) {
		LogUtil.log_debug(this.getClass(), "findLocalResources", "Asked for "+resource);
		Enumeration result = null;
		result = super.findLocalResources(resource);
		if (result == null) {
			LogUtil.log_debug(this.getClass(), "findLocalResources", "Not found locally ask delegates.");
			List<URL> res = null;
			try {
				res = findDelegateResources(resource, false);
			} catch (IOException e) {
				result = null;
			}
			if (res != null)				
				result = new ClassLoadingDelegator.ResourceEnumeration<URL>(res.iterator());
		}
		LogUtil.log_debug(this.getClass(), 	"findLocalResources", "Returning "+result);
		return result;		
	}

	public void addDelegate(ClassDataLoaderDelegate delegate) {
		classLoadingDelegator.addDelegate(delegate);
	}

	public void addDelegate(ClassLoaderDelegate delegate) {
		classLoadingDelegator.addDelegate(delegate);
	}

	public Class findDelegateClass(String name) throws ClassNotFoundException {
		return classLoadingDelegator.findDelegateClass(name);
	}

	public List<URL> findDelegateResources(String name, boolean returnAfterFoundFirst) throws IOException {
		return classLoadingDelegator.findDelegateResources(name, returnAfterFoundFirst);
	}

	public void removeDelegate(Object delegate) {
		classLoadingDelegator.removeDelegate(delegate);
	}

	public Class<?> delegateDefineClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) {
		return defineClass(name, b, off, len, protectionDomain);
	}
	
	private static DelegatingClassLoaderOSGI sharedInstance;
	
	public static DelegatingClassLoaderOSGI createSharedInstance(ClassLoader parent, org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate delegate, ProtectionDomain domain, BaseData bundledata, String[] classpath) {
		if (sharedInstance != null)
			throw new IllegalStateException("Multiple calls to createSharedInstance()!");
		sharedInstance = new DelegatingClassLoaderOSGI(parent, delegate, domain, bundledata, classpath);
		return sharedInstance;
	}
	
	public static DelegatingClassLoaderOSGI getSharedInstance() {
		if (sharedInstance == null)
			throw new IllegalStateException("SharedInstance is null. Call createSharedInstance() before accessing it.");		
		return sharedInstance;
	}
}
