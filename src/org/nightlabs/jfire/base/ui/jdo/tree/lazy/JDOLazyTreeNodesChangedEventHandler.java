/**
 * 
 */
package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.HashSet;

import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.jdo.ObjectID;

/**
 * Event handler that applies the changes found in an {@link JDOLazyTreeNodesChangedEvent} to a {@link TreeViewer}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JDOLazyTreeNodesChangedEventHandler {

	public JDOLazyTreeNodesChangedEventHandler() {
	}
	
	public static void handle(TreeViewer treeViewer, JDOLazyTreeNodesChangedEvent<? extends ObjectID, ? extends JDOObjectLazyTreeNode> changedEvent) {
		if (treeViewer.getTree().isDisposed())
			return;
		HashSet<JDOObjectLazyTreeNode> refreshed = new HashSet<JDOObjectLazyTreeNode>();
		boolean refreshAll = false;
		if (changedEvent.getParentsToRefresh() != null) {
			for (JDOObjectLazyTreeNode node : changedEvent.getParentsToRefresh()) {
				if (node == null) {
					refreshAll = true;
					break;
				}

				if (refreshed.add(node))
					treeViewer.refresh(node);
			}
		}

		if (refreshAll)
			treeViewer.refresh();
		else {
			for (JDOObjectLazyTreeNode node : changedEvent.getLoadedTreeNodes()) {
				if (refreshed.add(node))
					treeViewer.refresh(node);
			}
		}
	}

}
