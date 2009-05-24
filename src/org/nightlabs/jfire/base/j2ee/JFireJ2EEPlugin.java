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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegate;
import org.nightlabs.util.IOUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JFireJ2EEPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.nightlabs.jfire.base.j2ee";

	// The shared instance
	private static JFireJ2EEPlugin plugin;

	/**
	 * The constructor
	 */
	public JFireJ2EEPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

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
	 * This method rewrites (if necessary) the MANIFEST.MF of this plugin (i.e. <code>org.nightlabs.jfire.base.j2ee</code>).
	 *
	 * @return <code>true</code>, if the file had to be modified (and thus a reboot of the RCP is necessary).
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public boolean updateManifest()
	throws IOException, URISyntaxException
	{
		System.out.println("****************************************************************");

		// find the MANIFEST.MF
		File manifestFile;
		{
			// Bundle bundle = Platform.getBundle(PLUGIN_ID);
			Bundle bundle = getBundle();
			Path path = new Path("META-INF/MANIFEST.MF");
			URL fileURL = FileLocator.find(bundle, path, null);
			URL realURL = FileLocator.resolve(fileURL);
			if (!realURL.getProtocol().equalsIgnoreCase("file"))
				throw new IllegalStateException("The plugin org.nightlabs.jfire.j2ee is not deployed as directory-plugin. Its URL protocol is "+realURL.getProtocol());

			manifestFile = new File(realURL.getPath());
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
		boolean changed = false;
		for (String currPkg : currentPublishedRemotePackages) {
			if (!lastPublishedRemotePackages.contains(currPkg)) {
				changed = true; // there is a new one on the server which we don't have yet locally
				break;
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
