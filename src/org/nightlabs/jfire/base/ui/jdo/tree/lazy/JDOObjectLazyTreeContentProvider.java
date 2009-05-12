package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.nightlabs.jdo.ObjectID;

public class JDOObjectLazyTreeContentProvider
<JDOObjectID extends ObjectID,
JDOObject,
TreeNode extends JDOObjectLazyTreeNode<JDOObjectID, JDOObject, ? extends ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode>>>
implements ILazyTreeContentProvider
{
	private static final Logger logger = Logger.getLogger(JDOObjectLazyTreeContentProvider.class);
	private TreeViewer treeViewer;
	private ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode> controller = null;

	protected TreeViewer getTreeViewer() {
		if (treeViewer == null)
			throw new IllegalStateException("There is no TreeViewer assigned; inputChanged(...) was not yet called!");

		return treeViewer;
	}

	protected ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode> getController() {
		if (controller == null)
			throw new IllegalStateException("There is no ActiveJDOObjectLazyTreeController assigned; inputChanged(...) was not yet called!");

		return controller;
	}

	@Override
	public Object getParent(Object element) {
		if (logger.isTraceEnabled())
			logger.trace("getParent: entered for element=" + element);

		TreeNode child = null;
		if (element instanceof ActiveJDOObjectLazyTreeController) {
			// nothing
		}
		else if (element instanceof String) {
			// nothing
		}
		else {
			child = naiveCast(child, element);
		}
		if (child == null)
			return null;

		if (logger.isDebugEnabled())
			logger.debug("getParent: child.oid=" + child.getJdoObjectID());

		return child.getParent();
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		TreeNode parent = null;

		if (element instanceof ActiveJDOObjectLazyTreeController) {
			// nothing
		}
		else if (element instanceof String) {
			// nothing
		}
		else {
			parent = naiveCast(parent, element);
		}

		long realChildCount;
		long childCount = getController().getNodeCount(parent);
		if (childCount < 0) // loading
			realChildCount = 1; // the "Loading..." message
		else
			realChildCount = childCount;

		if (logger.isDebugEnabled())
			logger.debug("updateChildCount: parent.oid=" + (parent == null ? null : parent.getJdoObjectID()) + " childCount=" + childCount);

		if (realChildCount != currentChildCount)
			getTreeViewer().setChildCount(element, (int)realChildCount);
	}

	@Override
	public void updateElement(final Object parentElement, final int index) {
		TreeNode parent = null;

		if (parentElement instanceof ActiveJDOObjectLazyTreeController) {
			// nothing
		}
		else if (parentElement instanceof String) {
			// nothing
		}
		else {
			parent = naiveCast(parent, parentElement);
		}

		if (parent != null) {
			TreeNode n = parent;
			while (n != null) {
				if (collapsedNodes.contains(n)) // WORKAROUND for bug in TreeViewer: it calls updateElement for all children when collapsing a node. Strange but true.
					return;

				@SuppressWarnings("unchecked")
				TreeNode p = (TreeNode) n.getParent();
				n = p;
			}
		}

		TreeNode child = getController().getNode(parent, index);
		if (child == null) { // loading
			if (logger.isDebugEnabled())
				logger.debug("updateElement: parent.oid=" + (parent == null ? null : parent.getJdoObjectID()) + " :: Child is not yet loaded!");

			getTreeViewer().replace(parentElement, index, LOADING);
		}
		else {
			if (child.getJdoObject() == null)
				getTreeViewer().replace(parentElement, index, String.format(LOADING_OBJECT_ID, child.getJdoObjectID()));
			else
				getTreeViewer().replace(parentElement, index, child);

			long childChildNodeCount = getController().getNodeCount(child);

			if (logger.isDebugEnabled())
				logger.debug("updateElement: parent.oid=" + (parent == null ? null : parent.getJdoObjectID()) + " child.oid=" + child.getJdoObjectID() + " child.childCount=" + childChildNodeCount);

			if (childChildNodeCount < 0)
				childChildNodeCount = 1; // the "Loading..." message

			getTreeViewer().setChildCount(child, (int)childChildNodeCount);
		}
	}

	private static final String LOADING = "Loading...";
	private static final String LOADING_OBJECT_ID = "Loading %s ...";

	@Override
	public void dispose() {
		// nothing
	}

	/**
	 * This field is a WORKAROUND for a bug in the TreeViewer: It calls {@link #updateElement(Object, int)} for
	 * all children when collapsing a node. Totally unnecessary and highly inefficient :-( but fortunately possible
	 * to work-around it.
	 */
	private Set<TreeNode> collapsedNodes = new HashSet<TreeNode>();

	private ITreeViewerListener treeViewerListener = new ITreeViewerListener() {
		public void treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent event) {
			@SuppressWarnings("unchecked")
			TreeNode node = (TreeNode) event.getElement();

			if (logger.isDebugEnabled())
				logger.debug("treeViewerListener.treeCollapsed: node=" + node);

			collapsedNodes.add(node);
		}
		public void treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent event) {
			@SuppressWarnings("unchecked")
			TreeNode node = (TreeNode) event.getElement();

			if (logger.isDebugEnabled())
				logger.debug("treeViewerListener.treeExpanded: node=" + node);

			if (collapsedNodes.remove(node))
				getTreeViewer().refresh();
		}
	};

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.treeViewer != null) {
			this.treeViewer.removeTreeListener(treeViewerListener);
		}

		this.treeViewer = (TreeViewer) viewer;
		this.collapsedNodes.clear();
		if (this.treeViewer != null) {
			this.treeViewer.addTreeListener(treeViewerListener);
		}

		ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode> controller = null;
		if (newInput instanceof ActiveJDOObjectLazyTreeController) {
			controller = naiveCast(controller, newInput);
		}
		else if (newInput instanceof String) {
			// nothing
		}
		else {
			TreeNode parent = null;
			parent = naiveCast(parent, newInput);
			if (parent != null)
				controller = parent.getActiveJDOObjectLazyTreeController();
		}

		if (controller != null)
			this.controller = controller;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private <T> T naiveCast(T t, Object obj) {
		return (T) obj;
	}
}
