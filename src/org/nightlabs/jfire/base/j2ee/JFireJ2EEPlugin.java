/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.jfire.base.j2ee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.FileLocator;
import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate;
import org.nightlabs.util.IOUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * @author Alexander Bieber
 * @author Marco Schulze
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireJ2EEPlugin
implements BundleActivator
{
	// The plug-in ID
	public static final String PLUGIN_ID = "org.nightlabs.jfire.base.j2ee";

	// The shared instance
	private static JFireJ2EEPlugin plugin;

	/**
	 * The constructor
	 */
	public JFireJ2EEPlugin() 
	{
		plugin = this;
	}

	private Bundle bundle;

	public Bundle getBundle() 
	{
		return bundle;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception 
	{
		this.bundle = context.getBundle();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception 
	{
		plugin = null;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////
// BEGIN: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.osgi.RemoteClassLoadingHook
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
//END: Must be exactly the same as in org.nightlabs.jfire.base.j2ee.osgi.RemoteClassLoadingHook
///////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JFireJ2EEPlugin getDefault() {
		return plugin;
	}

	protected Set<String> readPublishedRemotePackages(File f)
	throws IOException
	{
		HashSet<String> res = new HashSet<String>();
		Reader r = new BufferedReader(new FileReader(f));
		try {
			StreamTokenizer st = new StreamTokenizer(r);
			st.resetSyntax();
			st.wordChars(0, '\n' - 1); st.wordChars('\n' + 1, Integer.MAX_VALUE);
			while (st.ttype != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_WORD) {
					res.add(st.sval);
				}
				st.nextToken();
			}
		} finally {
			r.close();
		}
		return res;
	}

	protected void writePublishedRemotePackages(Set<String> packages, File f)
	throws IOException
	{
		Writer w = new BufferedWriter(new FileWriter(f));
		try {
			for (String pkg : packages) {
				w.write(pkg);
				w.write('\n');
			}
		} finally {
			w.close();
		}
	}

	/**
	 * Returns a file for the contents of the specified bundle.  Depending 
	 * on how the bundle is installed the returned file may be a directory or a jar file 
	 * containing the bundle content.  
	 * 
	 * @param bundle the bundle
	 * @return a file with the contents of the bundle
	 * @throws IOException if an error occurs during the resolution
	 * 
	 * @since org.eclipse.equinox.common 3.4
	 * 
	 * XXX: taken from Equinox 3.4 class FileLocator without any changes (Marc)
	 */
	private static File getBundleFile(Bundle bundle) throws IOException {
		URL rootEntry = bundle.getEntry("/"); //$NON-NLS-1$
		rootEntry = FileLocator.resolve(rootEntry);
		if ("file".equals(rootEntry.getProtocol())) //$NON-NLS-1$
			return new File(rootEntry.getPath());
		if ("jar".equals(rootEntry.getProtocol())) { //$NON-NLS-1$
			String path = rootEntry.getPath();
			if (path.startsWith("file:")) {
				// strip off the file: and the !/
				path = path.substring(5, path.length() - 2);
				return new File(path);
			}
		}
		throw new IOException("Unknown protocol"); //$NON-NLS-1$
	}
	
	
	/**
	 * Copy the bundle if it is deployed as directory or extract it if it is deployed as a jar. 
	 * @param bundle The bundle
	 * @param j2eePluginRuntimeDir The target directory
	 * @throws IOException In case of an error
	 */
	private void copyJ2eePluginToRuntimeDir(Bundle bundle, File j2eePluginRuntimeDir) throws IOException
	{
		File bundleFile = getBundleFile(bundle);
		if(bundleFile.isDirectory()) {
			// packaged as directory
			IOUtil.copyDirectory(bundleFile, j2eePluginRuntimeDir);
		} else if(bundleFile.isFile()) {
			// packaged as jar file
			IOUtil.unzipArchive(bundleFile, j2eePluginRuntimeDir);
		} else {
			throw new IOException("Invalid file type: "+bundleFile.getAbsolutePath());
		}
	}

	/**
	 * This method rewrites (if necessary) the MANIFEST.MF of this plugin (i.e. <code>org.nightlabs.jfire.base.j2ee</code>).
	 * <p>
	 * Since 2009-05-27, this method does not touch the original MANIFEST.MF anymore, but instead creates a runtime-version
	 * of the <code>org.nightlabs.jfire.base.j2ee</code> plug-in. If this runtime-version exists, it is installed immediately
	 * on OSGI-start by the class <code>org.nightlabs.jfire.base.j2ee.osgi.RemoteClassLoadingHook</code>.
	 * </p>
	 * <p>
	 * This new mechanism employing a runtime-copy of the j2ee plugin makes it possible to have a system-wide read-only
	 * JFire-installation.
	 * </p>
	 *
	 * @return <code>true</code>, if the file had to be modified (and thus a reboot of the RCP is necessary).
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public boolean updateManifest()
	throws IOException, URISyntaxException
	{
		System.out.println("****************************************************************");
		boolean changed = false;

		// find the MANIFEST.MF
		File manifestFile;
		{
			// Bundle bundle = Platform.getBundle(PLUGIN_ID);
			Bundle bundle = getBundle();
//			Path path = new Path("META-INF/MANIFEST.MF");
//			URL fileURL = FileLocator.find(bundle, path, null);
//			URL realURL = FileLocator.resolve(fileURL);
//
//			if (!realURL.getProtocol().equalsIgnoreCase("file"))
//				throw new IllegalStateException("The plugin org.nightlabs.jfire.j2ee is not deployed as directory-plugin. Its URL protocol is "+realURL.getProtocol());
//
//			manifestFile = new File(realURL.getPath());

			File j2eePluginRuntimeDir = getJ2eePluginRuntimeDir();
			if (!j2eePluginRuntimeDir.isDirectory()) {
				boolean successful = false;
				try {
					j2eePluginRuntimeDir.mkdirs();
					if (!j2eePluginRuntimeDir.isDirectory())
						throw new IllegalStateException("Creation of directory failed: " + j2eePluginRuntimeDir);

					copyJ2eePluginToRuntimeDir(bundle, j2eePluginRuntimeDir);

					successful = true;
				} finally {
					if (!successful) // clean up in case it was only partially created.
						IOUtil.deleteDirectoryRecursively(j2eePluginRuntimeDir);
				}
				changed = true;

				manifestFile = new File(j2eePluginRuntimeDir, "META-INF/MANIFEST.MF");
			}
			else {
				String bundleLocation = bundle.getLocation();
				if (!bundleLocation.startsWith("file:"))
					throw new IllegalStateException("The plugin org.nightlabs.jfire.j2ee is not loaded from its runtime-location, even though that location exists! Its loaded location is " + bundleLocation);

				URL realURL = new URL(bundleLocation);
				manifestFile = new File(realURL.getPath(), "META-INF/MANIFEST.MF");
			}

//			if (!realURL.getProtocol().equalsIgnoreCase("file"))
//				throw new IllegalStateException("The plugin org.nightlabs.jfire.j2ee is not deployed as directory-plugin. Its URL protocol is "+realURL.getProtocol());

			if (!manifestFile.exists())
				throw new IllegalStateException("The plugin's MANIFEST.MF does not exist: " + manifestFile.getAbsolutePath());
		}

		File metaInfDir = manifestFile.getParentFile();
		File origManifestFile = new File(metaInfDir, "MANIFEST.MF.orig");
		if (!origManifestFile.exists()) {
			// it seems, this is the first start - so create a backup of the original MANIFEST.MF

			InputStream in = new FileInputStream(manifestFile);
			try {
				OutputStream out = new FileOutputStream(origManifestFile);
				try {
					IOUtil.transferStreamData(in, out);
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
			origManifestFile.setLastModified(manifestFile.lastModified());
		}

		// read the last server-package-list
		File publishedRemotePackagesFile = new File(metaInfDir, "publishedRemotePackages.csv");
		Set<String> lastPublishedRemotePackages;
		if (publishedRemotePackagesFile.exists())
			lastPublishedRemotePackages = readPublishedRemotePackages(publishedRemotePackagesFile);
		else
			lastPublishedRemotePackages = new HashSet<String>();

		// obtain the current published remote packages
		Set<String> currentPublishedRemotePackages = JFireRCDLDelegate.sharedInstance().getPublishedRemotePackages();

		// diff the last and the new ones
		if (!changed) {
			for (String currPkg : currentPublishedRemotePackages) {
				if (!lastPublishedRemotePackages.contains(currPkg)) {
					changed = true; // there is a new one on the server which we don't have yet locally
					break;
				}
			}
		}

		if (!changed) {
			for (String lastPkg : lastPublishedRemotePackages) {
				if (!currentPublishedRemotePackages.contains(lastPkg)) {
					changed = true; // one of the packages that we still have locally doesn't exist anymore on the server
					break;
				}
			}
		}

		if (!changed)
			return false;

		// We need to read the MANIFEST.MF.orig, append all the current published packages and write it as MANIFEST.MF.

		// read the MANIFEST.MF.orig
		InputStream in = new FileInputStream(origManifestFile);
		Manifest manifest;
		try {
			manifest = new Manifest(in);
		} finally {
			in.close();
		}

		// get the Export-Package entry
		StringBuilder exportPackage = new StringBuilder(
				manifest.getMainAttributes().getValue("Export-Package"));

		// append all the packages from the server
		for (String pkg : currentPublishedRemotePackages) {
			exportPackage.append(',');
			exportPackage.append(pkg);
		}

		// write the MANIFEST.MF
		manifest.getMainAttributes().put(new Attributes.Name("Export-Package"), exportPackage.toString());
		OutputStream out = new FileOutputStream(manifestFile);
		try {
			manifest.write(out);
		} finally {
			out.close();
		}

		// write the server's packages
		writePublishedRemotePackages(currentPublishedRemotePackages, publishedRemotePackagesFile);

		return true;
	}
}
