package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.TreeLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

/**
 * A controller to be used as datasource for JDO tree datastructures.
 * <p>
 * The controller is <em>active</em> as it tracks changes to the structure (new/deleted objects, changed objects)
 * keeps the data up-to-date and uses a callback to notify the user of the changes (see {@link #onJDOObjectsChanged(JDOLazyTreeNodesChangedEvent)}).
 * </p>
 * <p>
 * More details about how to use this class can be found in our wiki:
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/ActiveJDOObjectLazyTreeController">https://www.jfire.org/modules/phpwiki/index.php/ActiveJDOObjectLazyTreeController</a>
 * </p>
 *
 * @author Marco Schulze
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @param <JDOObjectID> The type of the {@link ObjectID} the tree sturcture uses
 * @param <JDOObject> The type of the JDO object used
 * @param <TreeNode> The type of {@link JDOObjectLazyTreeNode} used to hold the data
 */
public abstract class ActiveJDOObjectLazyTreeController<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectLazyTreeNode>
{
	private static final Logger logger = Logger.getLogger(ActiveJDOObjectLazyTreeController.class);

	protected abstract Collection<JDOObjectID> retrieveChildObjectIDs(JDOObjectID parentID, IProgressMonitor monitor);

	protected abstract Map<JDOObjectID, Long> retrieveChildCount(Set<JDOObjectID> parentIDs, IProgressMonitor monitor);

	/**
	 * This method is called on a worker thread and must retrieve JDO objects for
	 * the given object-ids from the server. It is called when changes to the structure were tracked.
	 *
	 * @param objectIDs The jdo object ids representing the desired objects.
	 * @param monitor The monitor.
	 * @return Returns the jdo objects that correspond to the requested <code>objectIDs</code>.
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, IProgressMonitor monitor);

	/**
	 * Creates a subclass of {@link JDOObjectLazyTreeNode} which represents the node object of the active tree.
	 * @return the subclass of {@link JDOObjectLazyTreeNode} for this ActiveJDOObjectLazyTreeController.
	 */
	protected abstract TreeNode createNode();

	/**
	 * This pseudo-node is used to hold the real root elements. Its creation is synchronized via {@link #objectID2TreeNode} - ensuring
	 * it is not created twice.
	 */
	private TreeNode hiddenRootNode = null;

	private Map<JDOObjectID, TreeNode> objectID2TreeNode = new HashMap<JDOObjectID, TreeNode>();

	/**
	 * This method is called by the default implementation of {@link #createJDOLifecycleListenerFilter()}.
	 * It is responsible for creating a {@link TreeNodeParentResolver} for the actual
	 * type of JDOObject.
	 */
	protected abstract TreeNodeParentResolver createTreeNodeParentResolver();

	private TreeNodeParentResolver treeNodeParentResolver = null;

	/**
	 * Get the {@link TreeNodeParentResolver} for this controller.
	 * It will be created lazily by a call to {@link #createTreeNodeParentResolver()}.
	 *
	 * @return The {@link TreeNodeParentResolver} for this controller.
	 */
	public TreeNodeParentResolver getTreeNodeParentResolver()
	{
		if (treeNodeParentResolver == null)
			treeNodeParentResolver = createTreeNodeParentResolver();

		return treeNodeParentResolver;
	}

	/**
	 * Get the {@link Class} (type) of the JDO object this controller is for.
	 * Should be the same this controller was typed with.
	 *
	 * @return The {@link Class} (type) of the JDO object this controller is for.
	 */
	protected abstract Class<? extends JDOObject> getJDOObjectClass();

	/**
	 * Creates an {@link IJDOLifecycleListenerFilter} that will be used to
	 * track new objects that are children of one of the objects referenced by
	 * the given parentObjectIDs.
	 * By default this will create a {@link TreeLifecycleListenerFilter}
	 * for {@link JDOLifecycleState#NEW}.
	 *
	 * @param parentObjectIDs The {@link ObjectID}s of the parent objects new children should be tracked for.
	 * @return A new {@link IJDOLifecycleListenerFilter}
	 */
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter(Set<? extends ObjectID> parentObjectIDs)
	{
		return new TreeLifecycleListenerFilter(
				getJDOObjectClass(), true,
				parentObjectIDs, getTreeNodeParentResolver(),
				new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	/**
	 * Creates a {@link JDOLifecycleListener} with the {@link IJDOLifecycleListenerFilter} obtained
	 * by {@link #createJDOLifecycleListenerFilter(Set)}.
	 *
	 * @param parentObjectIDs The {@link ObjectID}s of the parent objects new children should be tracked for.
	 * @return  A new {@link JDOLifecycleListener}
	 */
	protected JDOLifecycleListener createJDOLifecycleListener(Set<? extends ObjectID> parentObjectIDs)
	{
		IJDOLifecycleListenerFilter filter = createJDOLifecycleListenerFilter(parentObjectIDs);
		return new LifecycleListener(filter);
	}

	/**
	 * This will be called when a change in the tree structure was tracked and after the changes
	 * were retrieved. The {@link JDOLazyTreeNodesChangedEvent} contains references to the
	 * {@link TreeNode}s that need update or were removed.
	 * <p>
	 * This method is called on the UI thread.
	 * </p>
	 * <p>
	 * You can choose whether you want to override this method or register listeners via {@link #addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)}.
	 * In most use cases, simply overriding this method is easier and less code.
	 * </p>
	 *
	 * @param changedEvent The {@link JDOLazyTreeNodesChangedEvent} containing references to changed/new and deleted {@link TreeNode}s
	 */
	protected void onJDOObjectsChanged(JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent)
	{
	}

	private NotificationListener changeListener;

//	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void handleChangeNotification(NotificationEvent notificationEvent, IProgressMonitor monitor) {
		synchronized (objectID2TreeNode) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();

			Collection<DirtyObjectID> dirtyObjectIDs = notificationEvent.getSubjects();
			final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
			final Map<JDOObjectID, TreeNode> dirtyNodes = new HashMap<JDOObjectID, TreeNode>();
			final Map<JDOObjectID, TreeNode> deletedNodes = new HashMap<JDOObjectID, TreeNode>();
			for (DirtyObjectID objectID : dirtyObjectIDs) {
				TreeNode dirtyNode = objectID2TreeNode.get(objectID.getObjectID());
				if (dirtyNode == null)
					continue;
				JDOObjectID jdoObjectID = (JDOObjectID) objectID.getObjectID();
				switch (objectID.getLifecycleState()) {
					case DIRTY: dirtyNodes.put(jdoObjectID, dirtyNode); break;
					case DELETED: deletedNodes.put(jdoObjectID, dirtyNode); break;
					case NEW: break; // do nothing for new objects
				}
			}
			final Map<JDOObjectID, TreeNode> ignoredNodes = new HashMap<JDOObjectID, TreeNode>();
			ignoredNodes.putAll(dirtyNodes);
			Collection<JDOObject> retrievedObjects = retrieveJDOObjects(dirtyNodes.keySet(), monitor);
			for (JDOObject retrievedObject : retrievedObjects) {
				JDOObjectID retrievedID = (JDOObjectID) JDOHelper.getObjectId(retrievedObject);
				ignoredNodes.remove(retrievedID);
				TreeNode node = dirtyNodes.get(retrievedID);
				node.setJdoObject(retrievedObject);
			}
			for (Entry<JDOObjectID, TreeNode> deletedEntry : deletedNodes.entrySet()) {
				// TODO getJdoObject() might return null in a lazy tree!
				JDOObjectID parentID = (JDOObjectID) treeNodeParentResolver.getParentObjectID(deletedEntry.getValue().getJdoObject());
				TreeNode parentNode = objectID2TreeNode.get(parentID);
				objectID2TreeNode.remove(deletedEntry.getKey());
				if (parentNode != null) {
					parentNode.removeChildNode(deletedEntry.getValue());
					parentsToRefresh.add(parentNode == hiddenRootNode ? null : parentNode);
				}
			}

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(
							ActiveJDOObjectLazyTreeController.this,
							parentsToRefresh,
							new ArrayList<TreeNode>(dirtyNodes.values()),
							ignoredNodes,
							deletedNodes
					)
					);
				}
			});
		} // synchronized (objectID2TreeNode) {
	}

	protected class ChangeListener extends NotificationAdapterJob {

		public ChangeListener(String name) {
			super(name);
		}

		public void notify(NotificationEvent notificationEvent) {
			handleChangeNotification(notificationEvent, getProgressMonitor());
		}
	};

	protected void registerJDOLifecycleListener()
	{
		if (lifecycleListener != null) {
			if (logger.isDebugEnabled())
				logger.debug("registerJDOLifecycleListeners: removing old listener"); //$NON-NLS-1$

			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}

		Set<JDOObjectID> activeParentObjectIDs = getActiveParentObjectIDs();

		if (logger.isDebugEnabled()) {
			logger.debug("registerJDOLifecycleListeners: creating and registering JDOLifecycleListener for " + activeParentObjectIDs.size() + " activeParentObjectIDs"); //$NON-NLS-1$ //$NON-NLS-2$
			if (logger.isTraceEnabled()) {
				for (JDOObjectID jdoObjectID : activeParentObjectIDs)
					logger.trace("  - " + jdoObjectID); //$NON-NLS-1$
			}
		}

		lifecycleListener = createJDOLifecycleListener(activeParentObjectIDs);
		JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
	}

	protected void registerChangeListener() {
		if (changeListener == null) {
			changeListener = new ChangeListener("Loading changes");
			JDOLifecycleManager.sharedInstance().addNotificationListener(getJDOObjectClass(), changeListener);
		}
	}

	protected void unregisterChangeListener() {
		if (changeListener != null) {
			JDOLifecycleManager.sharedInstance().removeNotificationListener(getJDOObjectClass(), changeListener);
			changeListener = null;
		}
	}

	public void close()
	{
		if (lifecycleListener != null) {
			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}
		unregisterChangeListener();
	}

	private JDOLifecycleListener lifecycleListener = null;
	protected class LifecycleListener extends JDOLifecycleAdapterJob
	{
		private IJDOLifecycleListenerFilter filter;

		public LifecycleListener(IJDOLifecycleListenerFilter filter)
		{
			this.filter = filter;
		}

		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
		{
			return filter;
		}

		public void notify(JDOLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("LifecycleListener#notify: enter"); //$NON-NLS-1$

			synchronized (objectID2TreeNode) {
				if (hiddenRootNode == null)
					hiddenRootNode = createNode();

				Set<JDOObjectID> objectIDs = new HashSet<JDOObjectID>(event.getDirtyObjectIDs().size());
				final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
				final List<TreeNode> loadedTreeNodes = new ArrayList<TreeNode>();

				if (logger.isDebugEnabled())
					logger.debug("LifecycleListener#notify: got notification with " + event.getDirtyObjectIDs().size() + " DirtyObjectIDs"); //$NON-NLS-1$ //$NON-NLS-2$

				for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
					objectIDs.add((JDOObjectID) dirtyObjectID.getObjectID());

					if (logger.isDebugEnabled())
						logger.debug("LifecycleListener#notify:   - " + dirtyObjectID); //$NON-NLS-1$
				}

				Collection<JDOObject> objects = retrieveJDOObjects(objectIDs, getProgressMonitor());
				for (JDOObject object : objects) {
					TreeNode parentNode;
					boolean ignoreNodeBecauseParentUnknown = false;
					ObjectID parentID = getTreeNodeParentResolver().getParentObjectID(object);
					if (parentID == null) {
//						parentNode = null;
						parentNode = hiddenRootNode;
					}
					else {
						parentNode = objectID2TreeNode.get(parentID);
						if (parentNode == null)
							ignoreNodeBecauseParentUnknown = true;
					}

					if (ignoreNodeBecauseParentUnknown) {
						logger.warn("LifecycleListener#notify: ignoring new object, because its parent is unknown! objectID=\"" + JDOHelper.getObjectId(object) + "\" parentID=\"" + parentID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						continue;
					}

//					parentsToRefresh.add(null); // TODO what's this??? I think, this causes the whole tree to be refreshed. => Do we really need this???

					JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(object);
					TreeNode tn;
					tn = objectID2TreeNode.get(objectID);

					if (logger.isDebugEnabled())
						logger.debug("LifecycleListener#notify: treeNodeAlreadyExists=\"" + (tn != null) + "\" objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					if (tn != null && parentNode != tn.getParent()) { // parent changed, completely replace!
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: treeNode's parent changed! newParent=\"" + parentNode + "\" oldParent=\"" + tn.getParent() + "\" objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

						TreeNode p = (TreeNode) tn.getParent();
						parentsToRefresh.add(p == hiddenRootNode ? null : p);
						if (p == null) {
							throw new IllegalStateException("How the hell can TreeNode.getParent() return null?! If it is a root-node, it should have hiddenRootNode as its parent-node!"); //$NON-NLS-1$
//							if (rootElements != null) {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: removing TreeNode from rootElements (for replacement)! objectID=\"" + objectID + "\"");
//
//								if (!rootElements.remove(p))
//									logger.warn("LifecycleListener#notify: removing TreeNode from rootElements (for replacement) failed - the TreeNode was not found in the rootElements! objectID=\"" + objectID + "\"");
//							}
//							else {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: rootElements is null! Cannot remove old TreeNode! objectID=\"" + objectID + "\"");
//							}
						}
						else
							p.removeChildNode(tn);

						objectID2TreeNode.remove(objectID);
						tn = null;
					}

					if (tn == null) {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: creating TreeNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

						tn = createNode();
						tn.setActiveJDOObjectLazyTreeController(ActiveJDOObjectLazyTreeController.this);
					}
					else {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: reusing existing TreeNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}

					tn.setJdoObject(object);
					if (tn.getParent() != parentNode) {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: tn.getParent() != parentNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

						tn.setParent(parentNode);
//						if (parentNode != null) // should never be null now - we have introduced hiddenRootNode!
						parentNode.addChildNode(tn);
					}
					else {
						if (logger.isDebugEnabled())
							logger.debug("LifecycleListener#notify: tn.getParent() == parentNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}

					parentsToRefresh.add(parentNode == hiddenRootNode ? null : parentNode);
					objectID2TreeNode.put(objectID, tn);
					loadedTreeNodes.add(tn);
				}

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, loadedTreeNodes));
					}
				});
			}
		} // synchronized (objectID2TreeNode) {
	}

	/**
	 * These objects will be watched for new children to pop up. May contain <code>null</code> for root-elements
	 * (which is very likely).
	 */
	private Set<JDOObjectID> _activeParentObjectIDs = new HashSet<JDOObjectID>();
	private Set<JDOObjectID> _activeParentObjectIDs_ro = null;

	protected Set<JDOObjectID> getActiveParentObjectIDs() {
		synchronized (_activeParentObjectIDs) {
			if (_activeParentObjectIDs_ro == null)
				_activeParentObjectIDs_ro = Collections.unmodifiableSet(new HashSet<JDOObjectID>(_activeParentObjectIDs));

			return _activeParentObjectIDs_ro;
		}
	}

	/**
	 * @param jdoObjectID The OID of the parent-object that should be surveilled for newly created children.
	 * @param autoReregister If <code>true</code>, the method {@link #registerJDOLifecycleListener()} will automatically be called
	 *		if necessary. If <code>false</code>, this method triggered, even if a truly new <code>jdoObjectID</code> has been added.
	 *
	 * @return <code>false</code>, if the given <code>jdoObjectID</code> was already previously surveilled; <code>true</code> if it
	 *		has been added.
	 */
	protected boolean addActiveParentObjectID(JDOObjectID jdoObjectID, boolean autoReregister) {
		synchronized (_activeParentObjectIDs) {
			if (_activeParentObjectIDs.contains(jdoObjectID))
				return false;

			_activeParentObjectIDs.add(jdoObjectID);
			_activeParentObjectIDs_ro = null;
		}

		if (autoReregister)
			registerJDOLifecycleListener();

		return true;
	}

	/**
	 * @deprecated I think that's not necessary. Will be removed.
	 */
	@Deprecated
	public TreeNode getHiddenRootNode()
	{
		synchronized (objectID2TreeNode) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();

			return hiddenRootNode;
		}
	}

	/**
	 * Get the number of either root-nodes, if <code>parent == null</code>, or child-nodes
	 * of the specified parent. Alternatively, this method can return <code>-1</code>,
	 * if the data is not yet available. In this case, a new {@link Job} will be spawned to load the data.
	 *
	 * @param _parent the parent node or <code>null</code>.
	 */
	public long getNodeCount(TreeNode _parent)
	{
		if (_parent != null && _parent == hiddenRootNode)
			throw new IllegalArgumentException("Why the hell is the hiddenRootNode passed to this method?! If this ever happens - maybe we should map it to null here?"); //$NON-NLS-1$

		if (logger.isDebugEnabled())
			logger.debug("getNodeCount: entered for parentTreeNode.jdoObjectID=\"" + (_parent == null ? null : _parent.getJdoObjectID()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$

		synchronized (objectID2TreeNode) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();
		}

		long nodeCount = -1;

		registerChangeListener();

		if (_parent == null) {
			_parent = hiddenRootNode;
		}

		addActiveParentObjectID((JDOObjectID)_parent.getJdoObjectID(), true);
		nodeCount = _parent.getChildNodeCount();

		if (nodeCount >= 0) {
			if (logger.isDebugEnabled())
				logger.debug("getNodeCount: returning previously loaded count."); //$NON-NLS-1$

			return nodeCount;
		}

		if (logger.isDebugEnabled())
			logger.debug("getNodeCount: returning -1 and spawning Job."); //$NON-NLS-1$

		Job job = new Job("Loading child count") {
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				if (logger.isDebugEnabled())
					logger.debug("getNodeCount.Job#run: entered"); //$NON-NLS-1$

				// Give it some time to collect objects in the treeNodesWaitingForChildCountRetrieval
				// before we start processing them.
				try { Thread.sleep(500); } catch (InterruptedException x) { } // ignore InterruptedException

				Set<TreeNode> parentTreeNodes;
				synchronized(treeNodesWaitingForChildCountRetrieval) {
					jobChildCountRetrieval = null;
					parentTreeNodes = new HashSet<TreeNode>(treeNodesWaitingForChildCountRetrieval);
					treeNodesWaitingForChildCountRetrieval.clear();
				}
				Set<JDOObjectID> parentObjectIDs = new HashSet<JDOObjectID>(parentTreeNodes.size());
				boolean retrieveRootCount = false;
				final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();

				for (TreeNode treeNode : parentTreeNodes) {
					// Check, if it is still necessary - the number of children might have already been fetched.
					// Thus, we should prevent calling the retrieve methods twice.
					if (treeNode.getChildNodeCount() >= 0) {
						parentsToRefresh.add(treeNode == hiddenRootNode ? null : treeNode); // still force refresh - even though we prevent loading.
						continue;
					}

					if (treeNode == hiddenRootNode) {
						retrieveRootCount = true;
					}
					else {
						JDOObjectID parentJDOID = (JDOObjectID) treeNode.getJdoObjectID();
						parentObjectIDs.add(parentJDOID);
					}
				}

				if (retrieveRootCount) {
					Set<JDOObjectID> s = new HashSet<JDOObjectID>(1);
					s.add(null);
					Map<JDOObjectID, Long> parentOID2childCount = retrieveChildCount(
							s, new SubProgressMonitor(monitor, 50) // TODO correct % numbers!
					);
					Long count = parentOID2childCount.get(null);
					if (count == null)
						throw new IllegalStateException("retrieveChildCount(...) returned a null value (count) in its result map! Check your implementation in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName() + "!!!");

					hiddenRootNode.setChildNodeCount(count);
					parentsToRefresh.add(null);
				}

				if (!parentObjectIDs.isEmpty()) {
					Map<JDOObjectID, Long> parentOID2childCount = retrieveChildCount(parentObjectIDs, new SubProgressMonitor(monitor, 50)); // TODO correct % numbers!

					synchronized (objectID2TreeNode) {
						for (Map.Entry<JDOObjectID, Long> me : parentOID2childCount.entrySet()) {
							JDOObjectID parentJDOID = me.getKey();
							if (parentJDOID == null)
								throw new IllegalStateException("retrieveChildCount(...) returned a null key (parent-OID) in its result map even though no null element (parent-OID) was passed to it! Check your implementation in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName() + "!!!");

							Long childCount = me.getValue();
							if (childCount == null)
								throw new IllegalStateException("retrieveChildCount(...) returned a null value (count) in its result map! Check your implementation in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName() + "!!!");

							TreeNode parentTreeNode = objectID2TreeNode.get(parentJDOID);
							if (parentTreeNode == null)
								throw new IllegalStateException("Cannot find TreeNode for parentJDOID: " + parentJDOID);

							parentTreeNode.setChildNodeCount(childCount.longValue());
							parentsToRefresh.add(parentTreeNode);
						}

					} // synchronized (objectID2TreeNode) {
				}

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh));
					}
				});

				return Status.OK_STATUS;
			}
		};

		synchronized(treeNodesWaitingForChildCountRetrieval) {
			// enqueue in the todo-list
			treeNodesWaitingForChildCountRetrieval.add(_parent);

			// and launch a new job, if there is none active (don't do this always in order to prevent millions of jobs to be queued).
			if (jobChildCountRetrieval == null) {
				jobChildCountRetrieval = job;
				job.setRule(schedulingRule_jobChildCountRetrieval);
				job.schedule();
			}
		}
		return -1;
	}

	private Job jobChildCountRetrieval = null;
	private Set<TreeNode> treeNodesWaitingForChildCountRetrieval = new HashSet<TreeNode>();

	private ISchedulingRule schedulingRule_jobChildCountRetrieval = new SelfConflictingSchedulingRule();
	private ISchedulingRule schedulingRule_jobChildObjectIDRetrieval = new SelfConflictingSchedulingRule();
	private ISchedulingRule schedulingRule_jobObjectRetrieval = new SelfConflictingSchedulingRule();

	private static class SelfConflictingSchedulingRule implements ISchedulingRule
	{
		@Override
		public boolean contains(ISchedulingRule rule) {
			return this == rule;
		}
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return this == rule;
		}
	};

	private Job jobObjectRetrieval = null;
	private Set<TreeNode> treeNodesWaitingForObjectRetrieval = new HashSet<TreeNode>();

	/**
	 * This method returns either a root-node, if <code>parent == null</code> or a child of the given
	 * <code>parent</code> (if non-<code>null</code>). Alternatively, this method can return <code>null</code>,
	 * if the data is not yet available. In this case, a new {@link Job} will be spawned to load the data.
	 * <p>
	 * If a {@link TreeNode} is returned, it might be partially loaded (only the ID)!!! In this case, another
	 * Job is spawned and the real object loaded as well.
	 * </p>
	 *
	 * @param _parent the parent node or <code>null</code>.
	 * @return a {@link TreeNode} or <code>null</code>, if data is not yet ready.
	 */
	public TreeNode getNode(TreeNode _parent, int index)
	{
		if (_parent != null && _parent == hiddenRootNode)
			throw new IllegalArgumentException("Why the hell is the hiddenRootNode passed to this method?! If this ever happens - maybe we should map it to null here?"); //$NON-NLS-1$

		if (logger.isDebugEnabled())
			logger.debug("getNode: entered for parentTreeNode.jdoObjectID=\"" + (_parent == null ? null : _parent.getJdoObjectID()) + "\" index=" + index); //$NON-NLS-1$ //$NON-NLS-2$

		synchronized (objectID2TreeNode) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();
		}

		TreeNode node = null;

		registerChangeListener();

		if (_parent == null) {
			_parent = hiddenRootNode;
		}

		addActiveParentObjectID(
				(JDOObjectID)_parent.getJdoObjectID(), // this is null in case of hiddenRootNode
				true
		);
		List<TreeNode> childNodes = _parent.getChildNodes();

		if (childNodes != null) {
			if (index >= childNodes.size()) {
				logger.warn("getNode: index >= childNodes.size() :: " + index + " >= " + childNodes.size(), new Exception("StackTrace")); //$NON-NLS-1$
			}
			else
				node = childNodes.get(index);
		}

		if (node != null && node.getJdoObject() != null) {
			if (logger.isDebugEnabled())
				logger.debug("getNode: returning previously loaded complete child-node."); //$NON-NLS-1$

			return node;
		}

		if (node == null) {
			if (logger.isDebugEnabled())
				logger.debug("getNode: returning null and spawning Job."); //$NON-NLS-1$

			final TreeNode parent = _parent;

			Job job1 = new Job("Loading children") {
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					if (logger.isDebugEnabled())
						logger.debug("getNode.job1#run: entered for parentTreeNode.jdoObjectID=\"" + parent.getJdoObjectID() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

					final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
					parentsToRefresh.add(parent == hiddenRootNode ? null : parent);
					List<TreeNode> loadedNodes = null;

					// In the mean-time, the data for this parentTreeNode might already be retrieved - check it again (prevent multiple
					// calls to the retrieveChildObjectIDs(...) method for the same parent).
					if (parent.getChildNodes() != null) {
						if (logger.isDebugEnabled())
							logger.debug("getNode.job1#run: children already loaded for parentTreeNode.jdoObjectID=\"" + parent.getJdoObjectID() + "\". Skipping!"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else {
						JDOObjectID parentJDOID = (JDOObjectID) parent.getJdoObjectID();

						if (logger.isDebugEnabled())
							logger.debug("getNode.job1#run: retrieving children for parentTreeNode.jdoObjectID=\"" + parent.getJdoObjectID() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

						Collection<JDOObjectID> jdoObjectIDs = retrieveChildObjectIDs(parentJDOID, monitor);

						if (jdoObjectIDs == null)
							throw new IllegalStateException("Your implementation of retrieveChildObjectIDs(...) returned null! The error is probably in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName()); //$NON-NLS-1$


						loadedNodes = new ArrayList<TreeNode>(jdoObjectIDs.size());

						synchronized (objectID2TreeNode) {
							for (JDOObjectID jdoObjectID : jdoObjectIDs) {
								TreeNode tn = objectID2TreeNode.get(jdoObjectID);
								if (tn != null && parent != tn.getParent()) { // parent changed, completely replace!
									if (logger.isDebugEnabled())
										logger.debug("getNode.job1#run: treeNode's parent changed! objectID=\"" + jdoObjectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

									TreeNode p = (TreeNode) tn.getParent();
									parentsToRefresh.add(p == hiddenRootNode ? null : p);
									if (p != null)
										p.removeChildNode(tn);

									objectID2TreeNode.remove(jdoObjectID);
									tn = null;
								}

								if (tn == null) {
									if (logger.isTraceEnabled())
										logger.trace("getNode.job1#run: creating node for objectID=\"" + jdoObjectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$

									tn = createNode();
									tn.setActiveJDOObjectLazyTreeController(ActiveJDOObjectLazyTreeController.this);
								}
								else {
									if (logger.isTraceEnabled())
										logger.trace("getNode.job1#run: reusing existing node for objectID=\"" + jdoObjectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
								}

								tn.setJdoObjectID(jdoObjectID);
								tn.setParent(parent);
								objectID2TreeNode.put(jdoObjectID, tn);
								loadedNodes.add(tn);
							}

							parent.setChildNodes(loadedNodes);
						} // synchronized (objectID2TreeNode) {

					}

					final List<TreeNode> loadedNodes_final = loadedNodes;

					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, loadedNodes_final));
						}
					});

					return Status.OK_STATUS;
				}
			};
			job1.setRule(schedulingRule_jobChildObjectIDRetrieval);
			job1.schedule();
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("getNode: returning previously loaded INcomplete child-node and spawning Job."); //$NON-NLS-1$

			Job job2 = new Job("Loading tree nodes' objects") {
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					if (logger.isDebugEnabled())
						logger.debug("getNode.job2#run: entered"); //$NON-NLS-1$

					// Give it some time to collect objects in the treeNodesWaitingForObjectRetrieval
					// before we start processing them.
					try { Thread.sleep(500); } catch (InterruptedException x) { } // ignore InterruptedException

					Set<TreeNode> nodesWaitingForObjectRetrieval;
					synchronized (treeNodesWaitingForObjectRetrieval) {
						jobObjectRetrieval = null;
						nodesWaitingForObjectRetrieval = new HashSet<TreeNode>(treeNodesWaitingForObjectRetrieval);
						treeNodesWaitingForObjectRetrieval.clear();
					}

					Set<JDOObjectID> jdoObjectIDs = new HashSet<JDOObjectID>(nodesWaitingForObjectRetrieval.size());
					for (TreeNode treeNode : nodesWaitingForObjectRetrieval) {
						jdoObjectIDs.add((JDOObjectID) treeNode.getJdoObjectID());
					}

					Collection<JDOObject> jdoObjects = retrieveJDOObjects(jdoObjectIDs, monitor);
					Set<JDOObjectID> ignoredJDOObjectIDs = new HashSet<JDOObjectID>(jdoObjectIDs);

					final List<TreeNode> loadedTreeNodes = new ArrayList<TreeNode>(jdoObjects.size());
					final Map<JDOObjectID, TreeNode> ignoredJDOObjects = new HashMap<JDOObjectID, TreeNode>();
					final Map<JDOObjectID, TreeNode> deletedJDOObjects = null;

					synchronized (objectID2TreeNode) {

						for (JDOObject jdoObject : jdoObjects) {
							JDOObjectID jdoObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
							ignoredJDOObjectIDs.remove(jdoObjectID);
							TreeNode treeNode = objectID2TreeNode.get(jdoObjectID);
							if (treeNode == null)
								logger.warn("getNode.job2#run: There is no TreeNode existing for objectID=\"" + jdoObjectID + "\"!", new IllegalStateException("StackTrace"));
							else {
								treeNode.setJdoObject(jdoObject);
								loadedTreeNodes.add(treeNode);
							}
						}
						for (JDOObjectID jdoObjectID : ignoredJDOObjectIDs) {
							TreeNode treeNode = objectID2TreeNode.get(jdoObjectID);
							if (treeNode == null)
								logger.warn("getNode.job2#run: There is no TreeNode existing for objectID=\"" + jdoObjectID + "\"!", new IllegalStateException("StackTrace"));
							else
								ignoredJDOObjects.put(jdoObjectID, treeNode);
						}
					} // synchronized (objectID2TreeNode) {

					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, loadedTreeNodes, ignoredJDOObjects, deletedJDOObjects));
						}
					});

					return Status.OK_STATUS;
				}
			};

			synchronized (treeNodesWaitingForObjectRetrieval) {
				treeNodesWaitingForObjectRetrieval.add(node);
				if (jobObjectRetrieval == null) {
					jobObjectRetrieval = job2;
					job2.setRule(schedulingRule_jobObjectRetrieval);
					job2.schedule();
				}
			}
		}

		return node;
	}

//	/**
//	 * This method returns either root-nodes, if <code>parent == null</code> or children of the given
//	 * <code>parent</code> (if non-<code>null</code>). Alternatively, this method can return <code>null</code>,
//	 * if the data is not yet available. In this case, a new {@link Job} will be spawned to load the data.
//	 *
//	 * @param _parent the parent node or <code>null</code>.
//	 * @return a list of {@link TreeNode}s or <code>null</code>, if data is not yet ready.
//	 * @deprecated Will soon be removed!
//	 */
//	@Deprecated
//	private List<TreeNode> getNodes(TreeNode _parent)
//	{
//		if (_parent != null && _parent == hiddenRootNode)
//			throw new IllegalArgumentException("Why the hell is the hiddenRootNode passed to this method?! If this ever happens - maybe we should map it to null here?"); //$NON-NLS-1$
//
//		if (logger.isDebugEnabled())
//			logger.debug("getNodes: entered for parentTreeNode.jdoObjectID=\"" + (_parent == null ? null : JDOHelper.getObjectId(_parent.getJdoObject())) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//		synchronized (objectID2TreeNode) {
//			if (hiddenRootNode == null)
//				hiddenRootNode = createNode();
//		}
//
//		List<TreeNode> nodes = null;
//
//		registerChangeListener();
//
//		if (_parent == null) {
//			_parent = hiddenRootNode;
//		}
//
//		addActiveParentObjectID(
//				(JDOObjectID)JDOHelper.getObjectId(_parent.getJdoObject()), // this is null in case of hiddenRootNode
//				true
//		);
//		nodes = _parent.getChildNodes();
//
//		if (nodes != null) {
//			if (logger.isDebugEnabled())
//				logger.debug("getNodes: returning previously loaded child-nodes."); //$NON-NLS-1$
//
//			return nodes;
//		}
//
//		final TreeNode parent = _parent;
//
//		if (logger.isDebugEnabled())
//			logger.debug("getNodes: returning null and spawning Job."); //$NON-NLS-1$
//
//		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.jdo.tree.ActiveJDOObjectLazyTreeController.loadingChildNodesJob")) { //$NON-NLS-1$
//			@Override
//			protected IStatus run(IProgressMonitor monitor)
//			{
//				if (logger.isDebugEnabled())
//					logger.debug("getNodes.Job#run: entered for parentTreeNode.jdoObjectID=\"" + (parent == null ? null : JDOHelper.getObjectId(parent.getJdoObject())) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//				synchronized (objectID2TreeNode) {
//					JDOObject parentJDO = parent == null ? null : (JDOObject) parent.getJdoObject();
//					JDOObjectID parentJDOID = (JDOObjectID) JDOHelper.getObjectId(parentJDO);
//
//					if (logger.isDebugEnabled())
//						logger.debug("getNodes.Job#run: retrieving children for parentTreeNode.jdoObjectID=\"" + (parent == null ? null : JDOHelper.getObjectId(parent.getJdoObject())) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//					// TODO needs different implementation!
//					Collection<JDOObject> jdoObjects = null;// retrieveChildren(parentJDOID, parentJDO, monitor);
//
//					if (jdoObjects == null)
//						throw new IllegalStateException("Your implementation of retrieveChildren(...) returned null! The error is probably in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName()); //$NON-NLS-1$
//
//					List<JDOObject> jdoObjectList;
//					if (jdoObjects instanceof List)
//						jdoObjectList = (List<JDOObject>) jdoObjects;
//					else
//						jdoObjectList = new ArrayList<JDOObject>(jdoObjects);
//
//					sortJDOObjects(jdoObjectList);
//
//					final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
//					parentsToRefresh.add(parent == hiddenRootNode ? null : parent);
//
//					final List<TreeNode> loadedNodes = new ArrayList<TreeNode>(jdoObjectList.size());
//					for (JDOObject jdoObject : jdoObjectList) {
//						JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
//						TreeNode tn = objectID2TreeNode.get(objectID);
//						if (tn != null && parent != tn.getParent()) { // parent changed, completely replace!
//							if (logger.isDebugEnabled())
//								logger.debug("getNodes.Job#run: treeNode's parent changed! objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//							TreeNode p = (TreeNode) tn.getParent();
//							parentsToRefresh.add(p == hiddenRootNode ? null : p);
//							if (p != null)
//								p.removeChildNode(tn);
//
//							objectID2TreeNode.remove(objectID);
//							tn = null;
//						}
//
//						if (tn == null) {
//							if (logger.isDebugEnabled())
//								logger.debug("getNodes.Job#run: creating node for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//							tn = createNode();
//							tn.setActiveJDOObjectLazyTreeController(ActiveJDOObjectLazyTreeController.this);
//						}
//						else {
//							if (logger.isDebugEnabled())
//								logger.debug("getNodes.Job#run: reusing existing node for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//						}
//
//						tn.setJdoObject(jdoObject);
//						tn.setParent(parent);
//						objectID2TreeNode.put(objectID, tn);
//						loadedNodes.add(tn);
//					}
//
////					if (parent == null)
////						rootElements = loadedNodes;
////					else
////						parent.setChildNodes(loadedNodes);
//
//					parent.setChildNodes(loadedNodes);
//
//					Display.getDefault().asyncExec(new Runnable()
//					{
//						public void run()
//						{
//							fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, loadedNodes));
//						}
//					});
//				} // synchronized (objectID2TreeNode) {
//
//				return Status.OK_STATUS;
//			}
//		};
//		job.setRule(schedulingRule);
//		job.schedule();
//		return null;
//	}

	private ListenerList treeNodesChangedListeners = new ListenerList();

	/**
	 * This method can be used to add {@link JDOLazyTreeNodesChangedListener}s which will be called on the UI thread whenever
	 * the tree's data has been changed.
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @param listener The listener to be added.
	 * @see #removeJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)
	 */
	public void addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> listener) {
		treeNodesChangedListeners.add(listener);
	}

	/**
	 * This method can be used to remove listeners which have been previously added by {@link #addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)}.
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @param listener The listener to be removed.
	 * @see #addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)
	 */
	public void removeJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> listener) {
		treeNodesChangedListeners.remove(listener);
	}

	private void fireJDOObjectsChangedEvent(JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent)
	{
		if (logger.isDebugEnabled()) {
			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.parentsToRefresh.size()=" + //$NON-NLS-1$
					(changedEvent.getParentsToRefresh() == null ? null : changedEvent.getParentsToRefresh().size()));
			if (logger.isTraceEnabled() && changedEvent.getParentsToRefresh() != null) {
				for (TreeNode treeNode : changedEvent.getParentsToRefresh()) {
					if (treeNode == null)
						logger.trace("    parentTreeNode=null"); //$NON-NLS-1$
					else
						logger.trace("    parentTreeNode.jdoObjectID=" + treeNode.getJdoObjectID()); //$NON-NLS-1$
				}
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.ignoredJDOObjects.size()=" + //$NON-NLS-1$
					(changedEvent.getIgnoredJDOObjects() == null ? null : changedEvent.getIgnoredJDOObjects().size()));
			if (logger.isTraceEnabled() && changedEvent.getIgnoredJDOObjects() != null) {
				for (Map.Entry<JDOObjectID, TreeNode> me : changedEvent.getIgnoredJDOObjects().entrySet())
					logger.trace("    " + me.getKey()); //$NON-NLS-1$
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.loadedTreeNodes.size()=" + //$NON-NLS-1$
					(changedEvent.getLoadedTreeNodes() == null ? null : changedEvent.getLoadedTreeNodes().size()));
			if (logger.isTraceEnabled() && changedEvent.getLoadedTreeNodes() != null) {
				for (TreeNode treeNode : changedEvent.getLoadedTreeNodes())
					logger.trace("    " + treeNode.getJdoObjectID()); //$NON-NLS-1$
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.deletedJDOObjects.size()=" + //$NON-NLS-1$
					(changedEvent.getDeletedJDOObjects() == null ? null : changedEvent.getDeletedJDOObjects().size()));
			if (logger.isTraceEnabled() && changedEvent.getDeletedJDOObjects() != null) {
				for (Map.Entry<JDOObjectID, TreeNode> me : changedEvent.getDeletedJDOObjects().entrySet())
					logger.trace("    " + me.getKey()); //$NON-NLS-1$
			}
		}

		onJDOObjectsChanged(changedEvent);

		if (!treeNodesChangedListeners.isEmpty()) {
			Object[] listeners = treeNodesChangedListeners.getListeners();
			for (Object listener : listeners) {
				JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> l = (JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode>) listener;
				l.onJDOObjectsChanged(changedEvent);
			}
		}
	}

	protected TreeNode getTreeNode(JDOObjectID jdoObjectID) {
		return objectID2TreeNode.get(jdoObjectID);
	}

}
