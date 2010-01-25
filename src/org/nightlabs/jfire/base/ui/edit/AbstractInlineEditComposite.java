package org.nightlabs.jfire.base.ui.edit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public abstract class AbstractInlineEditComposite
extends AbstractEditComposite
{
	protected Label title;
	
	public AbstractInlineEditComposite(Composite parent, int style, boolean showTitle) {
		super(parent, style);
		
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$
		
		setLayout(getDefaultLayout());
		if (showTitle) {
			// FIXME: titles used within TextDataFieldComposite always indent some pixels. Why? Marc
			title = new Label(this, SWT.NONE);
			title.setLayoutData(createTitleLayoutData());
		}
	}

	public AbstractInlineEditComposite(Composite parent, int style) {
		this (parent, style, true);
	}

	protected Object createTitleLayoutData() {
		GridData nameData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		//		nameData.grabExcessHorizontalSpace = true;
		return nameData;
	}

	/**
	 * Sets the title of this edit composite.
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		if (this.title != null)
			this.title.setText(title);
	}

	/**
	 * Creates a standard {@link GridLayout} for DataFieldEditComposites.
	 * @return a standard {@link GridLayout} to be used in DataFieldEditors
	 */
	protected GridLayout getDefaultLayout() {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		// TODO: this is a quickfix for the Formtoolkit Boarderpainter, which paints to the outside of the elements -> there needs to be space in the enclosing composite for the borders
		layout.verticalSpacing = 4;
		layout.marginHeight = 0;
		// removed the marginWidth... be tight! Marc
		//layout.marginWidth = 2;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		return layout;
	}
	
	@Override
	public void setEnabledState(boolean enabled, String tooltip) {
		for (Control child : getChildren()) {
			if (title != child)
				child.setEnabled(enabled);
		}
		
		if (!enabled)
			setToolTipText(tooltip);
		else
			setToolTipText(null);
	}
}