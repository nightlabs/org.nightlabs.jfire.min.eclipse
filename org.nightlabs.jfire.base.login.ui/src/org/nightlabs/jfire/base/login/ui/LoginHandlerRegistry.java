package org.nightlabs.jfire.base.login.ui;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.eclipse.extension.AbstractEPProcessor;
import org.osgi.framework.BundleContext;

public class LoginHandlerRegistry extends AbstractEPProcessor
{
	private static LoginHandlerRegistry sharedInstance;

	public static synchronized LoginHandlerRegistry sharedInstance() {
		if (sharedInstance == null) {
			LoginHandlerRegistry r = new LoginHandlerRegistry();
			r.process();

			// commented because registration now also works via service registration, which is processed afterwards. Daniel
//			if (r.getLoginHandler() == null)
//				throw new IllegalStateException("There is no extension to the extension-point '" + r.getExtensionPointID() + "' registered! Exactly one ILoginHandler is required!");

			sharedInstance = r;
		}
		return sharedInstance;
	}

	private IExtension firstExtension;
	private ILoginHandler loginHandler;

	@Override
	public String getExtensionPointID() {
		return "org.nightlabs.jfire.base.login.ui.loginHandler";
	}

	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception
	{
		if (firstExtension != null)
			throw new IllegalStateException("There are multiple extensions to the extension-point '" + getExtensionPointID() + "'! Only exactly one extension is allowed! First extension was found in plugin '" + firstExtension.getNamespaceIdentifier() + "', second extension was found in plugin '" + extension.getNamespaceIdentifier() + "'.");

		BundleContext context = JFireLoginPlugin.getDefault().getBundle().getBundleContext();
		Object extensionImpl = element.createExecutableExtension("class");
		if (!(extensionImpl instanceof ILoginHandler))
			throw new IllegalStateException("Extension to the extension-point '" + getExtensionPointID() + "' in plugin '" + extension.getNamespaceIdentifier() + "' does not implement ILoginHandler!");

		this.loginHandler = (ILoginHandler) extensionImpl;
		context.registerService(ILoginHandler.class.getName(), extensionImpl, null);
		this.firstExtension = extension;
	}

	public ILoginHandler getLoginHandler()
	{
		return loginHandler;
	}
}
