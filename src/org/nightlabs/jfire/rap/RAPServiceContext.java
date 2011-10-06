package org.nightlabs.jfire.rap;

import java.util.Map;

import org.eclipse.rwt.internal.lifecycle.FakeContextUtil;
import org.eclipse.rwt.internal.lifecycle.RWTLifeCycle;
import org.eclipse.rwt.internal.service.ContextProvider;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.context.UIContext;
import org.nightlabs.jfire.compatibility.SessionStoreRegistry;
import org.nightlabs.singleton.IServiceContext;

@SuppressWarnings("restriction")
public class RAPServiceContext implements IServiceContext {
	private Map<Thread, ISessionStore> threadMap;
	private ISessionStore store;
	private Display sessionDisplay;

	public RAPServiceContext(Map<Thread, ISessionStore> threadMap, ISessionStore store) {
		this.threadMap = threadMap;
		this.store = store;
		sessionDisplay = RWTLifeCycle.getSessionDisplay();
		if (sessionDisplay == null) {
			throw new IllegalStateException("RWTLifeCycle.getSessionDisplay() returned null.");
		}
		associateRunner(Thread.currentThread());
	}

	@Override
	public void associateThread() {
		associateThread(Thread.currentThread());		
	}

	@Override
	public void associateThread(Thread thread) {
		threadMap.put(thread, store);
		associateRunner(thread);
		SessionStoreRegistry.associateThread(thread, store);
		ContextProvider.setContext(FakeContextUtil.createFakeContext(store), thread);
	}

	private void associateRunner(Thread thread) {
		if (UIContext.sharedInstance().getRunner(thread) == null) {
			UIContext.sharedInstance().registerRunner(thread, new RAPUIContextRunner(sessionDisplay, store));
		}
	}

	@Override
	public void disposeThread() {
		disposeThread(Thread.currentThread());
	}

	@Override
	public void disposeThread(Thread thread) {
		threadMap.remove(thread);
		SessionStoreRegistry.disposeThread(thread);
		ContextProvider.disposeContext(thread);
	}
}
