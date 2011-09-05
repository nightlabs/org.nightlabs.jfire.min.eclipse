package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.NumberStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.NumberStructField;

public class NumberStructFieldSearchFilterItemEditor
extends AbstractStructFieldSearchFilterItemEditor<NumberStructField>
{
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public <T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields, MatchType matchType) {
			StructField<?> structField = structFields.iterator().next();
			if (!NumberStructField.class.isAssignableFrom(structField.getClass()))
				throw new IllegalArgumentException("The given structField is not of type NumberStructField"); //$NON-NLS-1$
			
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
		return !numberEditComposite.getText().equals(""); //$NON-NLS-1$
	}

	@Override
	protected Control createEditControl(Composite parent) {
		numberEditComposite = new Text(parent, SWT.BORDER);
		numberEditComposite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				notifySearchTriggerListeners();
			}
		});
		
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
					MessageDialog.openError(
							numberEditComposite.getShell(), 
							Messages.getString("org.nightlabs.jfire.base.ui.prop.search.NumberStructFieldSearchFilterItemEditor.invalidNumberDialog.title"), //$NON-NLS-1$
							String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.NumberStructFieldSearchFilterItemEditor.invalidNumberDialog.message"), numberEditComposite.getText())); //$NON-NLS-1$
					return;
				}
				if (value < min) {
					MessageDialog.openError(numberEditComposite.getShell(), 
							Messages.getString("org.nightlabs.jfire.base.ui.prop.search.NumberStructFieldSearchFilterItemEditor.numberTooSmallDialog.title"), //$NON-NLS-1$ 
							String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.NumberStructFieldSearchFilterItemEditor.numberTooSmallDialog.message"), numberEditComposite.getText(), min)); //$NON-NLS-1$
				} else if (value > max) {
					MessageDialog.openError(numberEditComposite.getShell(), 
							Messages.getString("org.nightlabs.jfire.base.ui.prop.search.NumberStructFieldSearchFilterItemEditor.numberTooBigDialog.title"), //$NON-NLS-1$ 
							String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.search.NumberStructFieldSearchFilterItemEditor.numberTooBigDialog.message"), numberEditComposite.getText(), max)); //$NON-NLS-1$ 
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

	@Override
	public String getInput() {
		return numberEditComposite.getText();
	}

	@Override
	public void setInput(String input) {
		numberEditComposite.setText(input);
	}
}
