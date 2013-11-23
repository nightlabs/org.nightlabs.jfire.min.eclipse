package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.prop.structfield.BooleanStructField;

public class BooleanStructFieldEditor extends AbstractStructFieldEditor<BooleanStructField> {
	public static class BooleanStructFieldFactory extends AbstractStructFieldEditorFactory {
		@Override
		public StructFieldEditor<?> createStructFieldEditor() {
			return new BooleanStructFieldEditor();
		}
	}

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		return null;
	}

	@Override
	protected void setSpecialData(BooleanStructField field) {
	}
}
