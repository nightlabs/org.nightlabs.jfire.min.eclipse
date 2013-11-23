/**
 * 
 */
package org.nightlabs.jfire.base.ui.app.rcp;

import org.nightlabs.singleton.IServiceContext;
import org.nightlabs.singleton.SimpleSingletonProvider;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [dOt] de -->
 *
 */
public class RCPSingletonProvider<C> extends SimpleSingletonProvider<C> {

	/**
	 * 
	 */
	public RCPSingletonProvider() {
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a {@link RCPServiceContext}.
	 * </p>
	 */
	@Override
	protected IServiceContext createServiceContext() {
		return new RCPServiceContext();
	}
}
