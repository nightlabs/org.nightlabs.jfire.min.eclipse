package org.nightlabs.jfire.base.dashboard.ui.action;

import java.util.LinkedList;
import java.util.List;

import org.nightlabs.base.ui.action.IUpdateActionOrContributionItem;
import org.nightlabs.base.ui.action.SelectionAction;

/**
 * @author abieber
 *
 */
public class AbstractDashboardTableAction<T> extends SelectionAction implements IUpdateActionOrContributionItem {

	/**
	 * 
	 */
	public AbstractDashboardTableAction() {
	}

	@Override
	public boolean calculateEnabled() {
		return true;
	}
	
	@Override
	public boolean calculateVisible() {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public List<T> getSelectedTableItems() {
		List<?> selectedObjects = super.getSelectedObjects();
		if (selectedObjects == null)
			return null;
		List<T> tableItems = new LinkedList<T>();
		for (Object selection : selectedObjects) {
			try {
				T sel = (T) selection;
				sel.getClass();
			} catch (ClassCastException e) {
				continue;
			}
			tableItems.add((T) selection);
		}
		return tableItems;
	}
	
	public T getFirstSelectedTableItem() {
		List<T> tableItems = getSelectedTableItems();
		if (tableItems == null || tableItems.size() <= 0)
			return null;
		return tableItems.get(0);
	}
}
