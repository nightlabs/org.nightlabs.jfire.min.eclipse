package org.nightlabs.jfire.base.ui.timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.timepattern.TimePatternSetEditComposite;
import org.nightlabs.base.ui.timepattern.TimePatternSetModifyEvent;
import org.nightlabs.base.ui.timepattern.TimePatternSetModifyListener;
import org.nightlabs.jfire.timer.Task;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class TaskDetailEditComposite extends XComposite {

	private Task task;
	private Button enableButton;
	private TimePatternSetEditComposite timePatternSetEditComposite;
	
	private String enabledCaption;
	private IDirtyStateManager dirtyStateManager;
	
	/**
	 * @param parent
	 * @param style
	 */
	public TaskDetailEditComposite(Composite parent, int style, String enabledCaption, IDirtyStateManager dirtyStateManager) {
		super(parent, style);
		this.enabledCaption = enabledCaption;
		this.dirtyStateManager = dirtyStateManager;
		init();
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutDataMode
	 */
	public TaskDetailEditComposite(Composite parent, int style, LayoutDataMode layoutDataMode, String enabledCaption, IDirtyStateManager dirtyStateManager) {
		super(parent, style, layoutDataMode);
		this.enabledCaption = enabledCaption;
		this.dirtyStateManager = dirtyStateManager;
		init();
	}

	protected void init() {
		enableButton = new Button(this, SWT.CHECK);
		enableButton.setText(enabledCaption);
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dirtyStateManager != null) {
					dirtyStateManager.markDirty();
				}
			}
		});
		timePatternSetEditComposite = new TimePatternSetEditComposite(this, SWT.NONE, "Time pattern set");
		timePatternSetEditComposite.addTimePatternSetModifyListener(new TimePatternSetModifyListener() {
			@Override
			public void timePatternSetModified(TimePatternSetModifyEvent event) {
				if (dirtyStateManager != null) {
					dirtyStateManager.markDirty();
				}
			}
		});
	}
	
	public void setTask(Task task) {
		this.task = task;
		if (task != null) {
			timePatternSetEditComposite.setTimePatternSet(task.getTimePatternSet());
			enableButton.setSelection(task.isEnabled());
		}
	}
	
	public Task getTask() {
		return task;
	}
	
	public void commitPropeties() {
		if (task != null) {
			task.setEnabled(enableButton.getSelection());
		}
	}
	
	
}
