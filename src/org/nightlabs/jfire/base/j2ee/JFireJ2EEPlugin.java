package org.nightlabs.jfire.base.j2ee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.jfire.classloader.JFireRCDLDelegate;
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
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
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

	protected List<Pattern> getExcludeRemotePackagePatterns()
	{
		Set<String> res = new HashSet<String>();
		// TODO this should be populated by an extension point!!!
		res.add("org\\.nightlabs\\.editor2d");
		res.add("org\\.nightlabs\\.editor2d\\..*");
		res.add("org\\.nightlabs\\.ipanema\\.sun");
		res.add("org\\.nightlabs\\.ipanema\\.sun\\..*");
		res.add("org\\.nightlabs\\.ipanema\\.sand");
		res.add("org\\.nightlabs\\.ipanema\\.sand\\..*");

		List<Pattern> resP = new ArrayList<Pattern>(res.size());
		for (String regex : res) {
			resP.add(Pattern.compile(regex));
		}
		return resP;
	}

	/**
	 * This method rewrites (if necessary) the MANIFEST.MF of this plugin (i.e. <code>org.nightlabs.jfire.base.j2ee</code>).
	 *
	 * @return <code>true</code>, if the file had to be modified (and thus a reboot of the RCP is necessary).
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public boolean updateManifest() throws IOException, URISyntaxException
	{
		System.out.println("****************************************************************");

		// find the MANIFEST.MF
		File manifestFile;
		{
//			Bundle bundle = Platform.getBundle(PLUGIN_ID);
			Bundle bundle = getBundle();
			Path path = new Path("META-INF/MANIFEST.MF");
			URL fileURL = FileLocator.find(bundle, path, null);
			URL realURL = FileLocator.resolve(fileURL);
			manifestFile = new File(realURL.toURI());
			if (!manifestFile.exists())
				throw new IllegalStateException("The plugin's MANIFEST.MF does not exist: " + manifestFile.getAbsolutePath());
		}

		// read the MANIFEST.MF
		InputStream in = new FileInputStream(manifestFile);
		Manifest manifest;
		try {
			manifest = new Manifest(in);
		} finally {
			in.close();
		}

		String exportPackage = manifest.getMainAttributes().getValue("Export-Package");

		List<Pattern> excludeRemotePackagePatterns = getExcludeRemotePackagePatterns();
		StringBuffer newExportPackage = null;

		// parse the comma-separated packages
		StringTokenizer st = new StringTokenizer(exportPackage, ",");
		SortedSet<String> exportPackageSet = new TreeSet<String>();
		while (st.hasMoreTokens()) {
			String pkg = st.nextToken();
			boolean exclude = false;
			for (Pattern pattern : excludeRemotePackagePatterns) {
				if (pattern.matcher(pkg).matches()) {
					exclude = true;
					newExportPackage = new StringBuffer();
					break;
				}
			}

			if (!exclude)
				exportPackageSet.add(pkg);
		}

		if (newExportPackage != null) {
			boolean first = true;
			for (String pkg : exportPackageSet) {
				if (!first)
					newExportPackage.append(',');

				newExportPackage.append(pkg);
				first = false;
			}
		}

		// check, whether there are new remote packages that do not yet exist locally.
		Set<String> remotePackages = JFireRCDLDelegate.sharedInstance().getPublishedRemotePackages();
		iterateRemotePackages : for (String pkg : remotePackages) {
			for (Pattern pattern : excludeRemotePackagePatterns) {
				if (pattern.matcher(pkg).matches())
					continue iterateRemotePackages;
			}

			if (!exportPackageSet.contains(pkg)) {
				if (newExportPackage == null)
					newExportPackage = new StringBuffer(exportPackage);

				newExportPackage.append(',');
				newExportPackage.append(pkg);
			}
		}

		// if there are no new ones, we simply return false
		if (newExportPackage == null) {
			System.out.println("There are NO new remote packages. No need to rewrite the MANIFEST.MF!");
			return false;
		}

		// there are new remote packages => rewrite MANIFEST.MF
		System.out.println("There are new remote packages. We must rewrite the MANIFEST.MF!");
		manifest.getMainAttributes().put(new Attributes.Name("Export-Package"), newExportPackage.toString());
//		OutputStream out = new FileOutputStream(new File(manifestFile.getAbsolutePath()+".new"));
		OutputStream out = new FileOutputStream(manifestFile);
		try {
			manifest.write(out);
		} finally {
			out.close();
		}

		return true;
	}
}
