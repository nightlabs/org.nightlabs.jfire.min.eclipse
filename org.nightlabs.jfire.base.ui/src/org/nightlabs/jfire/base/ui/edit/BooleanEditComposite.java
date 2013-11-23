package org.nightlabs.jfire.base.ui.edit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Small Composite to edit a Boolean. Note that this is tri-state capable. Value == null =&gt; checkbox grayed.
 * 
 * @author abieber
 */
public class BooleanEditComposite extends AbstractInlineEditComposite 
{
	private Button button;
	
	/**
	 * @param parent
	 * @param style
	 */
	public BooleanEditComposite(Composite parent, int style) {
		super(parent, style, false); // showTitle = false, we overwrite setTitle()
		button = new Button(this, SWT.CHECK);
		button.addListener (SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				if (button.getSelection()) {
					if (!button.getGrayed()) {
						button.setGrayed(true);
					}
				} else {
					if (button.getGrayed()) {
						button.setGrayed(false);
						button.setSelection(true);
					}
				}
			}
		});
		button.addSelectionListener(getSwtSelectionListener());
		
	}
	
	public void setValue(Boolean value) {
		button.setSelection(true);
		button.setGrayed(value == null);
		if (value != null) {
			button.setSelection(value);	
		}
	}
	
	public Boolean getValue() {
		if (button.getGrayed()) {
			return null;
		}
		return button.getSelection();
	}
	
	@Override
	public void setTitle(String title) {
		if (button != null) {
			button.setText(title);
		}
	}
}


