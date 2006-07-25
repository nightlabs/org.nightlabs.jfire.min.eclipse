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

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.classloader.JFireRCDLDelegateFilter;

/**
 * This extension-point registry manages extensions to the point
 * <code>org.nightlabs.jfire.base.j2ee.remoteResourceFilter</code>.
 * It implements {@link JFireRCDLDelegateFilter} and thus allows to
 * exclude resources (including classes) from the remote-loading,
 * even if the server publishes them.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RemoteResourceFilterRegistry
extends AbstractEPProcessor
implements JFireRCDLDelegateFilter
{
	private static RemoteResourceFilterRegistry _sharedInstance = null;

	public static RemoteResourceFilterRegistry sharedInstance()
	throws EPProcessorException
	{
		if (_sharedInstance == null) {
			synchronized (RemoteResourceFilterRegistry.class) {
				if (_sharedInstance == null) {
					RemoteResourceFilterRegistry reg = new RemoteResourceFilterRegistry();
					reg.process();
					_sharedInstance = reg;
				}
			}
		}
		return _sharedInstance;
	}

	@Override
	public String getExtensionPointID()
	{
		return "org.nightlabs.jfire.base.j2ee.remoteResourceFilter";
	}

	private LinkedList<Pattern> exclusionPatterns = new LinkedList<Pattern>();

	@Override
	public void processElement(IExtension extension, IConfigurationElement element)
			throws EPProcessorException
	{
		try {
			String pattern = element.getAttribute("pattern");
			exclusionPatterns.add(Pattern.compile(pattern));
		} catch (Throwable t) {
			throw new EPProcessorException("Extension to "+getExtensionPointID()+" by extension "+extension.getContributor().getName()+" has errors!", t);
		}
	}

	public boolean includeResource(String name)
	{
		for (Pattern pattern : exclusionPatterns) {
			if (pattern.matcher(name).matches()) {
				return false;
			}
		}
		return true;
	}

}
