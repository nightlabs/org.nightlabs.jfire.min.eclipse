package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.NumberStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.NumberStructField;

public class NumberStructFieldSearchFilterItemEditor
extends AbstractStructFieldSearchFilterItemEditor<NumberStructField>
{
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<?>> structFields, MatchType matchType) {
			StructField<?> structField = structFields.iterator().next();
			if (!NumberStructField.class.isAssignableFrom(structField.getClass()))
				throw new IllegalArgumentException("The given structField is not of type NumberStructField");
			
			return new NumberStructFieldSearchFilterItemEditor((NumberStructField) structField, matchType);
		}
	}
	
	private Text numberEditComposite;
	
	public NumberStructFieldSearchFilterItemEditor(NumberStructField numberStructField, MatchType matchType) {
		super(Collections.singleton(numberStructField), matchType);
	}
	
	public NumberStructFieldSearchFilterItemEditor(NumberStructField numberStructField) {
		this(numberStructField, null);
	}

	@Override
	public boolean canHandleMultipleFields() {
		return false;
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		int value;
		try {
			value = Integer.parseInt(numberEditComposite.getText());
		} catch (NumberFormatException nfe) {
			value = 0;
		}
		return new NumberStructFieldSearchFilterItem(getMatchType(), getFirstStructField().getStructFieldIDObj(), value);
	}
	
	@Override
	public boolean hasSearchConstraint() {
		return !numberEditComposite.getText().equals("");
	}

	@Override
	protected Control createEditControl(Composite parent) {
		numberEditComposite = new Text(parent, SWT.BORDER);
		
		final int min, max;
		if (getFirstStructField().isBounded()) {
			min = getFirstStructField().getMin();
			max = getFirstStructField().getMax();
		} else {
			min = 0;
			max = Integer.MAX_VALUE;
		}
		
		numberEditComposite.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				int value;
				try {
					value = Integer.parseInt(numberEditComposite.getText());
				} catch (NumberFormatException nfe) {
					MessageDialog.openError(numberEditComposite.getShell(), "Invalid number", "The value you have entered is no valid number.");
					return;
				}
				if (value < min) {
					MessageDialog.openError(numberEditComposite.getShell(), "Number too small", "The value you have entered may be at least " + min + ".");
				} else if (value > max) {
					MessageDialog.openError(numberEditComposite.getShell(), "Number too big", "The value you have entered may be at most " + min + ".");
				}
			}
		});
		
		return numberEditComposite;
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return NumberStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES;
	}

	@Override
	protected MatchType getDefaultMatchType() {
		return MatchType.EQUALS;
	}
}
