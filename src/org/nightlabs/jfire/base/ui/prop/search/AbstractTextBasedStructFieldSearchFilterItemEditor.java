package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.EnumSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.edit.TextEditComposite;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.AbstractTextBasedStructFieldSearchFilterItem;

public abstract class AbstractTextBasedStructFieldSearchFilterItemEditor<T extends StructField> extends AbstractStructFieldSearchFilterItemEditor<T> {
	
	protected TextEditComposite textEditComposite;

	public AbstractTextBasedStructFieldSearchFilterItemEditor(Collection<T> structFields, MatchType matchType) {
		super(structFields, matchType);
	}

	@Override
	protected Control createEditControl(Composite parent) {
		textEditComposite = new TextEditComposite(parent, SWT.NONE, 1, false);
		textEditComposite.getFieldText().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				notifySearchTriggerListeners();
			}
		});
		return textEditComposite;
	}

	@Override
	public boolean canHandleMultipleFields() {
		return true;
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return AbstractTextBasedStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES;
	}

	@Override
	protected MatchType getDefaultMatchType() {
		return MatchType.CONTAINS;
	}

	@Override
	public boolean hasSearchConstraint() {
		return !textEditComposite.getContent().equals(""); //$NON-NLS-1$
	}

	@Override
	public String getInput() {
		return textEditComposite.getContent();
	}

	@Override
	public void setInput(String input) {
		textEditComposite.setContent(input);
	}
}