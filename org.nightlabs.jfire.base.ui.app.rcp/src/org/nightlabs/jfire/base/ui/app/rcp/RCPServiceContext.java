/**
 * 
 */
package org.nightlabs.jfire.base.ui.app.rcp;

import org.nightlabs.base.ui.context.DefaultUIContextRunner;
import org.nightlabs.base.ui.context.UIContext;
import org.nightlabs.singleton.IServiceContext;

/**
 * An {@link RCPServiceContext} associates a {@link DefaultUIContextRunner} with
 * the threads the context is associated to.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [dOt] de -->
 */
public class RCPServiceContext implements IServiceContext {

	/**
	 * Default constructor.  
	 */
	public RCPServiceContext() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void associateThread() {
		associateRunner(Thread.currentThread());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void associateThread(Thread thread) {
		associateRunner(thread);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disposeThread() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disposeThread(Thread arg0) {
	}
	
	private void associateRunner(Thread thread) {
		if (UIContext.sharedInstance().getRunner(thread) == null) {
			UIContext.sharedInstance().registerRunner(thread, new DefaultUIContextRunner());
		}
	}
	

}
