package org.nightlabs.jfire.base.ui.edit;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.prop.ModifyListener;

public abstract class AbstractEditComposite
extends XComposite implements IEntryEditor
{
	private org.eclipse.swt.events.ModifyListener swtModifyListener = new org.eclipse.swt.events.ModifyListener() {
		@Override
		public void modifyText(ModifyEvent arg0) {
			notifyModificationListeners();
		}
	};
	
	private SelectionListener swtSelectionListener = null;
	
	public SelectionListener getSwtSelectionListener() {
		if (swtSelectionListener == null) {
			swtSelectionListener =  new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					notifyModificationListeners();
				}
			};
		}
		return swtSelectionListener;
	}

	
	
	private ListenerList modificationListeners = new ListenerList();
	
	public AbstractEditComposite(Composite parent, int style) {
		super(parent, style);
	}

	public org.eclipse.swt.events.ModifyListener getSwtModifyListener() {
		return swtModifyListener;
	}
	
	public void addModificationListener(ModifyListener modifyListener) {
		modificationListeners.add(modifyListener);
	}
	
	public void removeModificationListener(ModifyListener modifyListener) {
		modificationListeners.remove(modifyListener);
	}
	
	protected void notifyModificationListeners() {
		for (Object listener : modificationListeners.getListeners()) {
			((ModifyListener) listener).modifyData();
		}
	}
	
	public void setEnabledState(boolean enabled, String tooltip) {
		for (Control child : getChildren()) {
			child.setEnabled(enabled);
		}
		
		if (!enabled)
			setToolTipText(tooltip);
		else
			setToolTipText(null);
	}
}