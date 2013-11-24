package org.nightlabs.jfire.base.ui.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.context.UIContext;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.jfire.base.jdo.GlobalJDOManagerProvider;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedEvent;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.ui.jdo.IActiveJDOObjectController;
import org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.notification.SubjectCarrier;
import org.nightlabs.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class ActiveJDOObjectSearchController<JDOObjectID, JDOObject> implements IActiveJDOObjectController<JDOObjectID, JDOObject>
{
	private static final Logger logger = LoggerFactory.getLogger(ActiveJDOObjectSearchController.class);

	/**
	 * This method is called on a worker thread and must retrieve JDO objects for
	 * the given object-ids from the server.
	 *
	 * @param objectIDs The jdo object ids representing the desired objects.
	 * @param monitor The monitor.
	 * @return Returns the jdo objects that correspond to the requested <code>objectIDs</code>.
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, ProgressMonitor monitor);

	/**
	 * This method is called on a worker thread and must retrieve all JDO
	 * objects this controller shall manage. In many cases, this is simply
	 * the complete extent of a class (i.e. all instances that exist in the datastore).
	 * If this is not the complete extent of the class specified by
	 * {@link #getJDOObjectClass()}, you must override {@link #createJDOLifecycleListenerFilter()}
	 * in order to filter newly created objects already on the server side.
	 *
	 * @param monitor The monitor.
	 * @return Returns all those jdo objects that this
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(ProgressMonitor monitor);

	/**
	 * This method is called when the controller receives a notification of a
	 * changed or new object that is not yet part of the table. The method
	 * decides whether or not to integrate that object into the table.
	 * <p>
	 * The default implementation returns <code>true</code> for eery object. Subclasses may overwrite.
	 * </p>
	 * 
	 * @param jdoObjectID The Id of the object to check.
	 * @return <code>true</code> if the object with the given Id should be integrated.
	 */
	protected boolean isIntegrateNewObject(JDOObjectID jdoObjectID) {
		return true;
	}

	private ListenerList jdoObjectsChangedListeners = new ListenerList();

	/**
	 * This method is always called on the UI thread. You can chose whether you override it in order to react on changes
	 * or add a listener via {@link #addJDOObjectsChangedListener(JDOObjectsChangedListener)}.
	 *
	 * @param event The event containing details about which JDOObjects have been loaded from the server or have been deleted.
	 */
	protected void onJDOObjectsChanged(JDOObjectsChangedEvent<JDOObjectID, JDOObject> event)
	{
	}

	private void fireJDOObjectsChangedEvent(Collection<JDOObject> loadedJDOObjects, Map<JDOObjectID, JDOObject> ignoredJDOObjects, Map<JDOObjectID, JDOObject> deletedJDOObjects)
	{
		if (closed) {
			logger.warn("fireJDOObjectsChangedEvent: already closed: " + this); //$NON-NLS-1$
			return;
		}

		JDOObjectsChangedEvent<JDOObjectID, JDOObject> event = new JDOObjectsChangedEvent<JDOObjectID, JDOObject>(
				this, loadedJDOObjects, ignoredJDOObjects, deletedJDOObjects);

		onJDOObjectsChanged(event);
		if (!jdoObjectsChangedListeners.isEmpty()) {
			Object[] listeners = jdoObjectsChangedListeners.getListeners();
			for (Object listener : listeners) {
				@SuppressWarnings("unchecked")
				JDOObjectsChangedListener<JDOObjectID, JDOObject> l = (JDOObjectsChangedListener<JDOObjectID, JDOObject>) listener;
				l.onJDOObjectsChanged(event);
			}
		}
	}

	public void addJDOObjectsChangedListener(JDOObjectsChangedListener<JDOObjectID, JDOObject> listener)
	{
		jdoObjectsChangedListeners.add(listener);
	}

	public void removeJDOObjectsChangedListener(JDOObjectsChangedListener<JDOObjectID, JDOObject> listener)
	{
		jdoObjectsChangedListeners.remove(listener);
	}

	protected abstract void sortJDOObjects(List<JDOObject> objects);

	/**
	 * Unfortunately, it is not possible to determine the class of a generic at runtime. Therefore,
	 * we cannot know with which types the generic ActiveTreeContentProvider has been created.
	 * I hope that Java will - in the future - improve the generics! Marco.
	 */
	protected abstract Class<? extends JDOObject> getJDOObjectClass();

	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new SimpleLifecycleListenerFilter(
				getJDOObjectClass(), true,
				new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	private Map<JDOObjectID, JDOObject> jdoObjectID2jdoObject = null;
	private Object jdoObjectID2jdoObjectMutex = new Object();
	private List<JDOObject> jdoObjects = null;
	private JDOLifecycleListener lifecycleListener = new JDOLifecycleAdapterJob(Messages.getString("org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectController.loadingNewObjectsJob")) //$NON-NLS-1$
	{
		/**
		 * The filter send to the server.
		 * Changed the initialisation of this field to be done lazily, otherwise subclasses won't be
		 * able to override the #createJDOLifecycleListenerFilter() properly (The constructor of the
		 * subclass has been executed before #createJDOLifecycleListenerFilter() is called).
		 */
		private IJDOLifecycleListenerFilter lifecycleListenerFilter;

		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
		{
			if (lifecycleListenerFilter == null)
			{
				lifecycleListenerFilter = createJDOLifecycleListenerFilter();
			}
			return lifecycleListenerFilter;
		}

		public void notify(final JDOLifecycleEvent event)
		{
			
			Set<JDOObjectID> changedIDs = new HashSet<JDOObjectID>();
			// The lifecycleListener is registered only for JDOLifecylceState.NEW by default, but this might be overwritten
			SortedSet<DirtyObjectID> dirtyObjectIDs = event.getDirtyObjectIDs();
			for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
				if (dirtyObjectID.getLifecycleState() == JDOLifecycleState.DIRTY) {
					changedIDs.add((JDOObjectID) dirtyObjectID.getObjectID());
				}
			}
			handleChangeNotification(changedIDs, getProgressMonitor());	
		}

	};

	private NotificationListener notificationListener = new NotificationAdapterJob(Messages.getString("org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectController.loadingChangedObjectsJob")) //$NON-NLS-1$
	{
		public void notify(final NotificationEvent notificationEvent)
		{
			Set<JDOObjectID> changedIDs = new HashSet<JDOObjectID>();
			List<SubjectCarrier> subjectCarriers = notificationEvent.getSubjectCarriers();
			for (SubjectCarrier subjectCarrier : subjectCarriers) {
				DirtyObjectID dirtyObjectID = (DirtyObjectID) subjectCarrier.getSubject();
				if (dirtyObjectID.getLifecycleState() == JDOLifecycleState.DIRTY) {
					changedIDs.add((JDOObjectID) dirtyObjectID.getObjectID());
				}
			}
			handleChangeNotification(changedIDs, getProgressMonitor());
		}
	};

	private void handleChangeNotification(Set<JDOObjectID> changedObjectIDs, ProgressMonitor monitor) {
		Collection<JDOObjectID> newSearchResult = searchJDOObjectIDs();
		HashSet<JDOObjectID> jdoObjectIDsToLoad = new HashSet<JDOObjectID>();
		HashSet<JDOObjectID> deletedObjectIDs = new HashSet<JDOObjectID>(jdoObjectID2jdoObject.keySet());
		final Map<JDOObjectID, JDOObject> deletedObjects = new HashMap<JDOObjectID, JDOObject>();
		
		for (JDOObjectID jdoObjectID : newSearchResult) {
			if (changedObjectIDs.contains(jdoObjectID) || !jdoObjectID2jdoObject.containsKey(jdoObjectID)) {
				jdoObjectIDsToLoad.add(jdoObjectID);
			}
			deletedObjectIDs.remove(jdoObjectID);
		}
		Collection<JDOObject> deletedObjectsList = retrieveJDOObjects(deletedObjectIDs, monitor);
		for (JDOObject jdoObject : deletedObjectsList) {
			JDOObjectID deletedObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
			jdoObjectID2jdoObject.remove(deletedObjectID);
			deletedObjects.put(deletedObjectID, jdoObject);
		}
		
		final Map<JDOObjectID, JDOObject> ignoredJDOObjects = new HashMap<JDOObjectID, JDOObject>();
		if (!jdoObjectIDsToLoad.isEmpty()) {
			// Note: retrieveJDOObjects might not return all objects to the given Collection.
			Collection<JDOObject> jdoObjects = retrieveJDOObjects(jdoObjectIDsToLoad, monitor);
			integrateLoadedObjectAndComputeIgnoredObjects(jdoObjectIDsToLoad, jdoObjects, ignoredJDOObjects);
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run()
			{
				fireJDOObjectsChangedEvent(jdoObjectID2jdoObject.values(), ignoredJDOObjects, deletedObjects);
			}
		});
	}

	private Map<JDOObjectID, JDOObject> integrateLoadedObjectAndComputeIgnoredObjects(
			HashSet<JDOObjectID> jdoObjectIDsToLoad,
			Collection<JDOObject> jdoObjects, Map<JDOObjectID, JDOObject> ignoredJDOObjects) {
		final Set<JDOObjectID> ignoredJDOObjectIDs;
		ignoredJDOObjectIDs = new HashSet<JDOObjectID>(jdoObjectIDsToLoad);
		if (jdoObjects != null) {
			for (JDOObject jdoObject : jdoObjects) {
				@SuppressWarnings("unchecked")
				JDOObjectID jdoObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
				ignoredJDOObjectIDs.remove(jdoObjectID);
				jdoObjectID2jdoObject.put(jdoObjectID, jdoObject);
			}
		}
		if (ignoredJDOObjectIDs.isEmpty())
			ignoredJDOObjects = null;
		else {
			ignoredJDOObjects = new HashMap<JDOObjectID, JDOObject>(ignoredJDOObjectIDs.size());
			for (JDOObjectID jdoObjectID : ignoredJDOObjectIDs) {
				JDOObject jdoObject = jdoObjectID2jdoObject.remove(jdoObjectID);
				ignoredJDOObjects.put(jdoObjectID, jdoObject);
			}
		}
		return ignoredJDOObjects;
	}

	protected abstract Collection<JDOObjectID> searchJDOObjectIDs();
	
	private boolean listenersExist = false;
	private boolean closed = false;

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("This instance of ActiveJDOObjectController is already closed: " + this); //$NON-NLS-1$
	}

	/**
	 * You <b>must</b> call this method once you don't need this controller anymore.
	 * It performs some clean-ups, e.g. unregistering all listeners.
	 */
	public void close()
	{
		assertOpen();
		if (listenersExist) {
			if (logger.isDebugEnabled())
				logger.debug("close: unregistering listeners (" + getJDOObjectClass() + ')'); //$NON-NLS-1$

			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().removeLifecycleListener(lifecycleListener);
			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().removeNotificationListener(getJDOObjectClass(), notificationListener);
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("close: there are no listeners - will not unregister (" + getJDOObjectClass() + ')'); //$NON-NLS-1$
		}
		closed = true;
	}

	protected void createJDOObjectList()
	{
		jdoObjects = new ArrayList<JDOObject>(jdoObjectID2jdoObject.values());
		sortJDOObjects(jdoObjects);
	}

	/**
	 * This method will immediately return. If there is no data available yet, this method will return <code>null</code>
	 * and a {@link Job} will be launched in order to fetch the data.
	 *
	 * @return <code>null</code>, if there is no data here yet. An instance of {@link List} containing
	 *		jdo objects. If a modification happened, this list will be recreated.
	 */
	public List<JDOObject> getJDOObjects()
	{
		assertOpen();
		if (!listenersExist) {
			if (logger.isDebugEnabled())
				logger.debug("getElements: registering listeners (" + getJDOObjectClass() + ')'); //$NON-NLS-1$

			listenersExist = true;
			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().addLifecycleListener(lifecycleListener);
			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().addNotificationListener(getJDOObjectClass(), notificationListener);
		}

		if (jdoObjects != null)
			return jdoObjects;

		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectController.loadingDataJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor)
			{
				final Collection<JDOObject> jdoObjects = retrieveJDOObjects(monitor);

				synchronized (jdoObjectID2jdoObjectMutex) {
					if (jdoObjectID2jdoObject == null)
						jdoObjectID2jdoObject = new HashMap<JDOObjectID, JDOObject>();

					jdoObjectID2jdoObject.clear();
					for (JDOObject jdoObject : jdoObjects) {
						@SuppressWarnings("unchecked")
						JDOObjectID jdoObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
						jdoObjectID2jdoObject.put(jdoObjectID, jdoObject);
					}

					createJDOObjectList();
				} // synchronized (jdoObjectID2jdoObjectMutex) {

				UIContext.getDisplay().asyncExec(new Runnable()
				{
					public void run()
					{
						if (closed) {
							// Even though fireJDOObjectsChangedEvent(...) checks the closed state,
							// we'll get an exception due to the call to getJDOObjects(). Hence we check hre again.
							logger.warn("getJDOObjects.job.run: already closed: " + ActiveJDOObjectSearchController.this); //$NON-NLS-1$
							return;
						}

						fireJDOObjectsChangedEvent(getJDOObjects(), null, null);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		job.schedule();

		return null;
	}

	/**
	 * This method clears the currently existing cache of JDOObjects.
	 * This is necessary in if I am controlling a UI showing sensitive data that is not allowed to be
	 * shown to every user. So the UI may check for User changes and clear my caches.
	 */
	public void clearCache()
	{
		assertOpen();

		synchronized (jdoObjectID2jdoObjectMutex)
		{
			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().removeLifecycleListener(lifecycleListener);
			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().removeNotificationListener(getJDOObjectClass(), notificationListener);
			listenersExist = false;

			if (jdoObjectID2jdoObject != null)
				jdoObjectID2jdoObject.clear();

			jdoObjects = null;
		}
	}
}
