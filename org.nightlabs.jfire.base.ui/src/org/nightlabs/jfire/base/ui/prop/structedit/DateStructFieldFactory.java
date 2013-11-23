package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.l10n.IDateFormatter;

public class DateStructFieldFactory extends AbstractStructFieldFactory {

	public DateStructField createStructField(final StructBlock block, final WizardPage wizardPage) {
		final DateStructField field = new DateStructField(block);
		field.setDateTimeEditFlags(IDateFormatter.FLAGS_DATE_LONG);
		return field;
	}
}
