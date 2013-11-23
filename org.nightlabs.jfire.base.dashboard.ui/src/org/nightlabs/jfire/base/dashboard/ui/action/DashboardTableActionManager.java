package org.nightlabs.jfire.base.dashboard.ui.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.nightlabs.base.ui.action.ISelectionAction;
import org.nightlabs.base.ui.table.AbstractTableComposite;

/**
 * A manager that can be used to handle actions whose enabled state is dependent
 * on the selection in a table. The manager will install a
 * SelectionChangedListener and update the actions attributes. You'll need to
 * use {@link #addAction(Action)} so the manager knows the action.
 * <p>
 * The manager will also install an IOpenListener to the table that will execute
 * the first added action on double-click.
 * </p>
 * 
 * @author abieber
 */
public class DashboardTableActionManager<T> {
	
	private AbstractTableComposite<T> table;
	private MenuManager menuManager;
	private List<ISelectionAction> actions = new LinkedList<ISelectionAction>();
	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			updateActionStates();
		}
	};
	
	public DashboardTableActionManager(AbstractTableComposite<T> table) {
		this.table = table;
		table.getTableViewer().addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				if (actions.size() >= 1) {
					Action action = (Action) actions.get(0);
					if (action.isEnabled()) {
						action.run();
					}
				}
					
			}
		});
		menuManager = new MenuManager();
		createContextMenu(table);
		table.addSelectionChangedListener(selectionChangedListener);
	}

	private void createContextMenu(AbstractTableComposite<T> invoiceTable) {
		invoiceTable.getTableViewer().getTable().setMenu(menuManager.createContextMenu(invoiceTable.getTableViewer().getTable()));
	}
	
	public void addAction(Action action) {
		menuManager.add(action);
		if (action instanceof ISelectionAction) {
			actions.add((ISelectionAction) action);
		}
		updateActionStates();
	}
	
	private void updateActionStates() {
		for (ISelectionAction action : actions) {
			action.setSelection(table.getSelection());
			((Action)action).setEnabled(action.calculateEnabled());
		}
	}
}
