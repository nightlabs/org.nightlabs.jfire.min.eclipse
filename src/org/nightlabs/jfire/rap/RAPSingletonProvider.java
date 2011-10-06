package org.nightlabs.jfire.rap;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.ISessionStore;
import org.nightlabs.eclipse.compatibility.SessionStoreRegistry;
import org.nightlabs.singleton.AbstractSingletonProvider;
import org.nightlabs.singleton.IServiceContextAware;
import org.nightlabs.singleton.ISingletonProvider;

/**
 * {@link RAPSingletonProvider} will create an instance of C for each
 * RAP-SessionStore, i.e. fore each RAP-Session.
 * <p>
 * {@link RAPSingletonProvider} will resolve the SessionStore for the thread it
 * is asked for an instance on using two approaches.
 * <nl>
 * <li>It uses a Thread to SessionStore mapping that is held in instances of
 * RAPSingletonProvider and maintained by the {@link RAPServiceContext} the
 * provider uses.</li>
 * <li>It looks in the global {@link SessionStoreRegistry} where different
 * sources can link threads to SessionStores.</li>
 * </nl>
 * </p>
 * 
 * @author Alexey Aristov
 * @author Alexander Bieber <!-- alex [AT] nightlabs [dOt] de -->
 * 
 * @param <C>
 */
public class RAPSingletonProvider<C> extends AbstractSingletonProvider<C>
		implements ISingletonProvider<C> {

	/** Id-Prefix for the id used to associate a created instance with a SessionStore */
	private static final String ATTRIBUTE_ID_PREFIX = RAPSingletonProvider.class.getSimpleName() + "#";
	
	/** The id of this provider. It will be created uniquely for one java-vm */
	private final String id;
	/** Used to create the {@link #id} */
	private static long idCounter = 0;
	
	/**
	 * This map (its instance) is passed to the {@link RAPServiceContext}
	 * created for each instance. The {@link RAPServiceContext} will put a
	 * mapping from thread to SessionStore fore each thread it knows about.
	 */
	// TODO: Maybe use InheritableThreadLocal
	private static Map<Thread, ISessionStore> threadMap = new WeakHashMap<Thread, ISessionStore>();

	/**
	 * Default constructor.
	 */
	public RAPSingletonProvider() {
		synchronized (AbstractSingletonProvider.class) {
			// unlikely to overflow
			id = ATTRIBUTE_ID_PREFIX + Long.toHexString(++idCounter);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This provider will try to lookup the RAP-{@link ISessionStore} for the
	 * thread it is called on and then check whether an instance of C was
	 * already created for that Session. If not, a new instance is created and
	 * associated to the current {@link ISessionStore} using its
	 * {@link ISessionStore#setAttribute(String, Object)} method.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized C getInstance() {
		ISessionStore sessionStore = null;
		try {
			sessionStore = RWT.getSessionStore();
		} catch (IllegalStateException ex) {
			sessionStore = threadMap.get(Thread.currentThread());
		}

		if (sessionStore == null) {
			sessionStore = SessionStoreRegistry.getSessionStore();
		}

		if (sessionStore == null)
			throw new IllegalStateException(
					"Can't lookup RAP ISessionStore for thread "
							+ Thread.currentThread());

		C instance = (C) sessionStore.getAttribute(id);
		if (instance == null) {
			if (isFactorySet()) {
				instance = factory.makeInstance();
				if (instance instanceof IServiceContextAware) {
					((IServiceContextAware) instance)
							.setServiceContext(new RAPServiceContext(threadMap,
									sessionStore));
				}

				sessionStore.setAttribute(id, instance);
			} else
				throw new IllegalStateException("Instance not set");
		}

		return instance;
	}
}
