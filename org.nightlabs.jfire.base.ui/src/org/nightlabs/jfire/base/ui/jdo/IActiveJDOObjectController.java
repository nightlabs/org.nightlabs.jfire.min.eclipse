package org.nightlabs.jfire.base.ui.jdo;

import java.util.List;

import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedListener;

public interface IActiveJDOObjectController<JDOObjectID, JDOObject> {

	public abstract void addJDOObjectsChangedListener(
			JDOObjectsChangedListener<JDOObjectID, JDOObject> listener);

	public abstract void removeJDOObjectsChangedListener(
			JDOObjectsChangedListener<JDOObjectID, JDOObject> listener);

	/**
	 * You <b>must</b> call this method once you don't need this controller anymore.
	 * It performs some clean-ups, e.g. unregistering all listeners.
	 */
	public abstract void close();

	/**
	 * This method will immediately return. If there is no data available yet, this method will return <code>null</code>
	 * and a {@link Job} will be launched in order to fetch the data.
	 *
	 * @return <code>null</code>, if there is no data here yet. An instance of {@link List} containing
	 *		jdo objects. If a modification happened, this list will be recreated.
	 */
	public abstract List<JDOObject> getJDOObjects();

	/**
	 * This method clears the currently existing cache of JDOObjects.
	 * This is necessary in if I am controlling a UI showing sensitive data that is not allowed to be
	 * shown to every user. So the UI may check for User changes and clear my caches.
	 */
	public abstract void clearCache();

}