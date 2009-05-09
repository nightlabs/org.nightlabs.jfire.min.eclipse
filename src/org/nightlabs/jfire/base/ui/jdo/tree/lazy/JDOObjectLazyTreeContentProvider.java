package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.List;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.nightlabs.jdo.ObjectID;

public abstract class JDOObjectLazyTreeContentProvider
<JDOObjectID extends ObjectID,
JDOObject,
TreeNode extends JDOObjectLazyTreeNode<JDOObjectID, JDOObject, ? extends ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode>>>
implements ILazyTreeContentProvider
{
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
		return null;
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

		int realChildCount;
		List<TreeNode> children = getController().getNodes(parent);
		if (children == null) // loading
			realChildCount = 1; // the "Loading..." message
		else
			realChildCount = children.size();

		if (realChildCount != currentChildCount)
			getTreeViewer().setChildCount(element, realChildCount);
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

		List<TreeNode> children = getController().getNodes(parent);
		if (children == null) // loading
			getTreeViewer().replace(parentElement, index, LOADING);
		else {
			final TreeNode child = children.get(index);
			treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
				public void run() {
					getTreeViewer().replace(parentElement, index, child);
					getTreeViewer().setChildCount(child, 1);
				}
			});
		}
	}

	private static final String LOADING = "Loading...";

	@Override
	public void dispose() {
		// nothing
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.treeViewer = (TreeViewer) viewer;

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
