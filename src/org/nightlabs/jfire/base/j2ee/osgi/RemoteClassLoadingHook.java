/**
 *
 */
package org.nightlabs.jfire.base.j2ee.osgi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
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
import org.osgi.framework.BundleContext;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class RemoteClassLoadingHook implements ClassLoadingHook, HookConfigurator {

	public RemoteClassLoadingHook() {
		System.out.println("RemoteClassLoadingHook instantiated");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#addClassPathEntry(java.util.ArrayList, java.lang.String, org.eclipse.osgi.baseadaptor.loader.ClasspathManager, org.eclipse.osgi.baseadaptor.BaseData, java.security.ProtectionDomain)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean addClassPathEntry(ArrayList cpEntries, String cp, ClasspathManager hostmanager, BaseData sourcedata, ProtectionDomain sourcedomain) {
		return false;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////
//BEGIN: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin
	private String minusHexEncode(String plain) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Writer w = new OutputStreamWriter(bout, Charset.forName("UTF-8"));
		try {
			w.write(plain);
			w.close();
		} catch (IOException e) {
			// writing into an in-memory-stream should never fail
			throw new RuntimeException(e);
		}

		StringBuffer sb = new StringBuffer();
		for (byte bb : bout.toByteArray()) {
			int i = 0xff & bb;
			if (minusHexLiteralAllowed(i))
				sb.append((char)i); // this *MUST* be casted to char, because the sb.append(char) method must be called - not sb.append(int) - the result is quite different!
			else {
				sb.append('-');
				String s = Integer.toHexString(i);
				if (s.length() == 1) {
					sb.append('0');
					sb.append(s);
				}
				else if (s.length() == 2) {
					sb.append(s);
				}
			}
		}
		return sb.toString();
	}

	private boolean minusHexLiteralAllowed(int c)
	{
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
	}

	private File getJ2eePluginRuntimeDir()
	{
		File tempDir = new File(System.getProperty("java.io.tmpdir"));

		String userName = System.getProperty("user.name");
		String encodedUserName = minusHexEncode(userName);

		File j2eePluginRuntimeDir = new File(new File(tempDir, "jfire." + encodedUserName), "org.nightlabs.jfire.base.j2ee");
		j2eePluginRuntimeDir = j2eePluginRuntimeDir.getAbsoluteFile();
		return j2eePluginRuntimeDir;
	}
//END: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin
///////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * If the plugin org.nightlabs.jfire.base.j2ee exists in its runtime-location (i.e. temp-directory,
	 * where it can be modified), this method installs it into OSGI from there. Otherwise, it will be
	 * found by the Eclipse RCP plugin-finder and during first login, it will create itself this
	 * runtime-directory.
	 * <p>
	 * See the code in org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin#updateManifest() for further details.
	 * </p>
	 */
	private void installJ2eePlugin(BundleContext bundleContext)
	{
		System.out.println("Installing J2EE plugin...");
		try {
			File j2eePluginRuntimeDir = getJ2eePluginRuntimeDir();
			if (j2eePluginRuntimeDir.exists()) {
				String j2eePluginRuntimeURL = j2eePluginRuntimeDir.toURI().toURL().toExternalForm();
				bundleContext.installBundle(j2eePluginRuntimeURL);
				System.out.println("J2EE plugin installed.");
			}
			else
				System.out.println("J2EE plugin runtime directory does not (yet) exist: " + j2eePluginRuntimeDir);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean j2eePluginIsInstalled = false;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#createClassLoader(java.lang.ClassLoader, org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate, org.eclipse.osgi.framework.adaptor.BundleProtectionDomain, org.eclipse.osgi.baseadaptor.BaseData, java.lang.String[])
	 */
	@Override
	public BaseClassLoader createClassLoader(ClassLoader parent, ClassLoaderDelegate delegate, BundleProtectionDomain domain, BaseData data, String[] bundleclasspath) {
		System.out.println("RemoteClassLoadingHook called createClassLoader for "+data.getBundle().getSymbolicName());
		System.out.println(delegate.getClass().toString());

		if (!j2eePluginIsInstalled) {
			installJ2eePlugin(data.getBundle().getBundleContext());
			j2eePluginIsInstalled = true;
		}

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
	@Override
	public String findLibrary(BaseData data, String libName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#getBundleClassLoaderParent()
	 */
	@Override
	public ClassLoader getBundleClassLoaderParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#initializedClassLoader(org.eclipse.osgi.baseadaptor.loader.BaseClassLoader, org.eclipse.osgi.baseadaptor.BaseData)
	 */
	@Override
	public void initializedClassLoader(BaseClassLoader baseClassLoader, BaseData data) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook#processClass(java.lang.String, byte[], org.eclipse.osgi.baseadaptor.loader.ClasspathEntry, org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry, org.eclipse.osgi.baseadaptor.loader.ClasspathManager)
	 */
	@Override
	public byte[] processClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry, ClasspathManager manager) {
		return null;
	}

	@Override
	public void addHooks(HookRegistry hookRegistry) {
		hookRegistry.addClassLoadingHook(new RemoteClassLoadingHook());
	}

}
