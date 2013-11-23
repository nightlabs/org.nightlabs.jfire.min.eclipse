package org.nightlabs.jfire.rap;

import org.apache.log4j.Logger;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.nightlabs.base.ui.NLBasePlugin;
import org.nightlabs.base.ui.app.AbstractApplication;
import org.nightlabs.base.ui.app.DefaultWorkbenchWindowAdvisor;
import org.nightlabs.base.ui.context.UIContext;
import org.nightlabs.eclipse.extension.RemoveExtensionRegistry;
import org.nightlabs.jfire.base.ui.app.JFireApplication;
import org.nightlabs.jfire.base.ui.app.JFireWorkbenchAdvisor;
import org.nightlabs.singleton.SingletonProviderFactory;

public class EntryPoint implements IEntryPoint {

	class RAPWorkbenchAdvisor extends JFireWorkbenchAdvisor {
		
		@Override
		public void initialize(IWorkbenchConfigurer configurer) {
			super.initialize(configurer);
			configurer.setSaveAndRestore(false);
		}
		
		@Override
		public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
			return new DefaultWorkbenchWindowAdvisor(configurer) {
				@Override
				public void preWindowOpen() {
					super.preWindowOpen();
					getWindowConfigurer().setShellStyle(SWT.TITLE);
				}
			
				@Override
				protected Point getScreenSize() {
					return null;
				}
				
				@Override
				public void postWindowCreate() {
					Shell shell = getWindowConfigurer().getWindow().getShell();
					shell.setMaximized( true );
				}
			};
			
		}
	}
	
	protected static final Logger logger = Logger.getLogger(EntryPoint.class);


	public EntryPoint() {
	}

	static {
		// FIXMERAP: Commented
		NLBasePlugin.setApplicationSingletonProvider(SingletonProviderFactory.createProviderForClass(JFireApplication.class));
	}
	
	public int createUI() {
 
		try {
			AbstractApplication application = NLBasePlugin.getDefault().getApplication();
			
			application.initExceptionHandling();

			application.initializeLogging();
			application.initConfig();

			try {
				// readded old RemoveExtensionRegistry because nearly all extensions are not adopted yet. Daniel 2010-08-30
				org.nightlabs.base.ui.extensionpoint.RemoveExtensionRegistry.sharedInstance().removeRegisteredExtensions();
				RemoveExtensionRegistry.sharedInstance().removeRegisteredExtensions();
			} catch (Throwable t) {
				logger.error("There occured an error while tyring to remove all registered extensions", t); //$NON-NLS-1$
			}

			application.preCreateWorkbench();

			Display display = PlatformUI.createDisplay();
			
			UIContext.sharedInstance().registerRunner(Thread.currentThread(), new RAPUIContextRunner(display, RWT.getSessionStore()));
			
			WorkbenchAdvisor advisor = new RAPWorkbenchAdvisor();
			int result = PlatformUI.createAndRunWorkbench(display, advisor);
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			return 0;
		}
	}
}
