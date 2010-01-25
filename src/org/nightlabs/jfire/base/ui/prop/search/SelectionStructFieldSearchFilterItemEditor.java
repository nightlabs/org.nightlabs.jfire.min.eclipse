package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.edit.SelectionEditComposite;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.SelectionStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

public class SelectionStructFieldSearchFilterItemEditor
extends AbstractStructFieldSearchFilterItemEditor<SelectionStructField>
{
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public <T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields, MatchType matchType) {
			StructField<?> structField = structFields.iterator().next();
			if (!SelectionStructField.class.isAssignableFrom(structField.getClass()))
				throw new IllegalArgumentException("The given structField is not of type SelectionStructField");
			
			return new SelectionStructFieldSearchFilterItemEditor((SelectionStructField) structField);
		}
	}
	
	private SelectionEditComposite<StructFieldValue> selectionEditComposite;
	
	public SelectionStructFieldSearchFilterItemEditor(SelectionStructField structField) {
		super(Collections.singleton(structField), SelectionStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES.iterator().next());
	}

	@Override
	protected Control createEditControl(Composite parent) {
		selectionEditComposite = new SelectionEditComposite<StructFieldValue>(parent, SWT.NONE, new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == null)
					return "";
				
				return ((StructFieldValue) element).getValueName().getText();
			}
		}, false);
		
		selectionEditComposite.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				notifySearchTriggerListeners();
			}
		});
		
		final List<StructFieldValue> structFieldValues = new LinkedList<StructFieldValue>(getFirstStructField().getStructFieldValues());
		structFieldValues.add(0, null);
		selectionEditComposite.setInput(structFieldValues);
		selectionEditComposite.setTitle(getFirstStructField().getName().getText());
		
		return selectionEditComposite;
	}
	
	
	protected String getSelectedStructFieldValueID() {
		final StructFieldValue selectedElement = selectionEditComposite.getSelectedElement();
		
		if (selectedElement == null)
			return null;
		
		return selectedElement.getStructFieldValueID();
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		return new SelectionStructFieldSearchFilterItem(getFirstStructField().getStructFieldIDObj(), getSelectedStructFieldValueID());
	}
	
	@Override
	public boolean canHandleMultipleFields() {
		// StructFieldValues are unique to their StructField so it
		// does not make sense to search in multiple fields.
		return false;
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return SelectionStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES;
	}

	@Override
	protected MatchType getDefaultMatchType() {
		return MatchType.EQUALS;
	}

	@Override
	public boolean hasSearchConstraint() {
		return getSelectedStructFieldValueID() != null;
	}

	@Override
	public String getInput() {
		return getSelectedStructFieldValueID().toString();
	}

	@Override
	public void setInput(String input) {
		for (StructFieldValue value : selectionEditComposite.getElements()) {
			if (value != null && value.getStructFieldValueID().equals(input)) {
				selectionEditComposite.setSelectedElement(value);
				break;
			}
		}
	}
}