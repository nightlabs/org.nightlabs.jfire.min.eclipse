/**
 *
 */
package org.nightlabs.jfire.base.admin.ui.editor;

import org.nightlabs.base.ui.entity.tree.EntityTreeView;
import org.nightlabs.base.ui.part.PartController;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.login.ui.part.LSDPartController;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class SysAdminEntityTreeView
extends EntityTreeView
{
	public static final String ID_VIEW = SysAdminEntityTreeView.class.getName();

	public SysAdminEntityTreeView() {
//		LSDPartController.sharedInstance().registerPart(this);
	}

	@Override
	protected PartController getPartController() {
		return LSDPartController.sharedInstance();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.part.ControllablePart#canDisplayPart()
	 */
	@Override
	public boolean canDisplayPart() {
		return Login.isLoggedIn();
	}

}
