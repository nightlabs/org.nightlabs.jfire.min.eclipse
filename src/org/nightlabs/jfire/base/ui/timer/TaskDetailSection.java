/**
 * 
 */
package org.nightlabs.jfire.base.ui.timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.timepattern.TimePatternSetEditComposite;
import org.nightlabs.base.ui.timepattern.TimePatternSetModifyEvent;
import org.nightlabs.base.ui.timepattern.TimePatternSetModifyListener;
import org.nightlabs.jfire.timer.Task;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class TaskDetailSection
extends ToolBarSectionPart
{
	private Task task;
	private Button enableButton;
	private TimePatternSetEditComposite timePatternSetEditComposite;
	
	public TaskDetailSection(IFormPage page, Composite parent) {
		this(page, parent, "Task scheduling", "Enable task");
	}
	
	public TaskDetailSection(IFormPage page, Composite parent, String title, String enabledCaption) {
		super(page, parent, ExpandableComposite.TITLE_BAR, title);
		getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getSection().setLayout(new GridLayout());

		XComposite client = new XComposite(getSection(), SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		client.getGridLayout().numColumns = 1;

		getSection().setClient(client);
		
		enableButton = new Button(client, SWT.CHECK);
		enableButton.setText(enabledCaption);
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				markDirty();
			}
		});
		
		timePatternSetEditComposite = new TimePatternSetEditComposite(client, SWT.NONE, "Time pattern set");
		timePatternSetEditComposite.addTimePatternSetModifyListener(new TimePatternSetModifyListener() {
			@Override
			public void timePatternSetModified(TimePatternSetModifyEvent event) {
				markDirty();
			}
		});
	}
	
	@Override
	public boolean setFormInput(Object input) {
		task = getTask(input);
		return super.setFormInput(input);
	}
	
	protected Task getTask(Object input) {
		if (input instanceof Task) {
			return (Task) input;
		}
		return null;
	}
	
	@Override
	public void refresh() {
		super.refresh();
		if (task != null) {
			timePatternSetEditComposite.setTimePatternSet(task.getTimePatternSet());
			enableButton.setSelection(task.isEnabled());
		}
	}
	
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		task.setEnabled(enableButton.getSelection());
	}
	
}
