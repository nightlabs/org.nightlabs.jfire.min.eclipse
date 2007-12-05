package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.jface.wizard.WizardPage;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.structfield.TextStructField;

public class TextStructFieldFactory extends AbstractStructFieldFactory {
	public StructField createStructField(StructBlock block, WizardPage wizardPage) {
		return new TextStructField(block);
	}
}