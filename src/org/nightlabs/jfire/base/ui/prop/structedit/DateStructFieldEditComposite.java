package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.AbstractListComposite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.l10n.IDateFormatter;

class DateStructFieldEditComposite extends XComposite {
	private DateStructField dateField;
	private final XComboComposite<String> dateFormatCombo;
	private final Label exampleLabel;
	private final XComposite comp;
	private final DateStructFieldEditor dateStructFieldEditor;

	private void updateExampleLabelText() {
		exampleLabel.setText(
				String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.DateStructFieldEditComposite.exampleLabel.text"), //$NON-NLS-1$
						new Object[] { dateField == null ? "" : DateFormatter.formatDate(new Date(), dateField.getDateTimeEditFlags()) })); //$NON-NLS-1$
		exampleLabel.pack();
		exampleLabel.getParent().layout();
	}

	public DateStructFieldEditComposite(final Composite parent, final int style, final DateStructFieldEditor _dateStructFieldEditor) {
		super(parent, style | SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		this.dateStructFieldEditor = _dateStructFieldEditor;

		dateFormatCombo = new XComboComposite<String>(this, AbstractListComposite.getDefaultWidgetStyle(this), (String) null);

		comp = new XComposite(this, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		exampleLabel = new Label(comp, SWT.NONE);
		updateExampleLabelText();

		dateFormatCombo.setInput( Arrays.asList(IDateFormatter.FLAG_NAMES) );

		dateFormatCombo.addSelectionListener(new SelectionListener() {
			private void selectionChanged(final SelectionEvent event) {
				final int selectionIndex = dateFormatCombo.getSelectionIndex();
				dateField.setDateTimeEditFlags(IDateFormatter.FLAGS[selectionIndex]);
				dateStructFieldEditor.setChanged();

				updateExampleLabelText();
			}

			public void widgetDefaultSelected(final SelectionEvent e) {
				selectionChanged(e);
			}

			public void widgetSelected(final SelectionEvent e) {
				selectionChanged(e);
			}
		});
	}

	/**
	 * Sets the currently display field.
	 * 
	 * @param field The {@link DateStructField} to be displayed. Can be null.
	 */
	public void setField(final DateStructField field) {
		//		if (field == null) // this is bad practice, imho. Either throw an exception or set it and support it. I'll support it now ;-) Marco.
		//			return;

		dateField = field;

		if (dateField == null)
			dateFormatCombo.setSelection(-1);
		else {
			int index = 0;
			while (index < IDateFormatter.FLAGS.length && IDateFormatter.FLAGS[index] != dateField.getDateTimeEditFlags()) {
				index++;
			}
			dateFormatCombo.setSelection(index);
		}

		updateExampleLabelText();
	}
}