package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.eclipse.core.runtime.ListenerList;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;

public class BaseModel
{
	private ListenerList changeListeners = new ListenerList();
	
	public void addModelChangeListener(ModelChangeListener listener) {
		changeListeners.add(listener);
	}
	
	public void removeModelChangeListener(ModelChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	protected void modelChanged() {
		for (Object listener : changeListeners.getListeners())
			((ModelChangeListener)listener).modelChanged(new ModelChangeEvent());
	}
}
