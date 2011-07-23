package org.nightlabs.jfire.base.ui.app.rcp;

import org.nightlabs.jfire.base.ui.app.JFireApplication;

/**
 * <p>
 * JFire's RCP application.
 * </p><p>
 * This class only exists to make sure this bundle is activated before org.nightlabs.jfire.base.ui.
 * The activation order is necessary, because {@link JFireRCPAppPlugin} initialises the RCP environment
 * in its bundle-start-method.
 * </p>
 * @author marco
 */
public class JFireRCPApplication extends JFireApplication {

}
