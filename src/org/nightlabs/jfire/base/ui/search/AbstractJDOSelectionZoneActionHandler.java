/**
 * 
 */
package org.nightlabs.jfire.base.ui.search;

import java.util.ArrayList;
import java.util.Collection;

import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.base.ui.search.AbstractSelectionZoneActionHandler;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.notification.NotificationEvent;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public abstract class AbstractJDOSelectionZoneActionHandler
extends AbstractSelectionZoneActionHandler
{
	@Override
	public void run() {
		Collection selectedObjects = getSearchResultProvider().getSelectedObjects();
		if (selectedObjects != null) {
			// TODO: before use NLJDOHelper.getObjectIDSet check if maybe already ObjectIDs are contained  
			Collection selectedObjectIDs = NLJDOHelper.getObjectIDSet(selectedObjects);
			Collection<Class> subjectClassesToClear = new ArrayList<Class>();
			subjectClassesToClear.add(getSearchResultProvider().getFactory().getResultTypeClass());
			if (selectedObjects != null) {
				SelectionManager.sharedInstance().notify(new NotificationEvent(
						AbstractJDOSelectionZoneActionHandler.this,
						getSelectionZone(),
						selectedObjectIDs,
						subjectClassesToClear));
			}
		}
	}

}
