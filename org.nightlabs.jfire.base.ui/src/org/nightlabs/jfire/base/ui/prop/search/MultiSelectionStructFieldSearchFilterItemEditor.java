package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.edit.MultiSelectionEditComposite;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.MultiSelectionStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;

public class MultiSelectionStructFieldSearchFilterItemEditor
extends AbstractStructFieldSearchFilterItemEditor<MultiSelectionStructField>
{
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public <T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields, MatchType matchType) {
			StructField<?> structField = structFields.iterator().next();
			if (!MultiSelectionStructField.class.isAssignableFrom(structField.getClass()))
				throw new IllegalArgumentException("The given structField is not of type SelectionStructField"); //$NON-NLS-1$
			
			return new MultiSelectionStructFieldSearchFilterItemEditor((MultiSelectionStructField) structField);
		}
	}
	
	private MultiSelectionEditComposite<MultiSelectionStructFieldValue> selectionEditComposite;
	
	public MultiSelectionStructFieldSearchFilterItemEditor(MultiSelectionStructField structField) {
		super(Collections.singleton(structField), MultiSelectionStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES.iterator().next());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createEditControl(Composite parent) {
		selectionEditComposite = new MultiSelectionEditComposite<MultiSelectionStructFieldValue>(parent, SWT.NONE, new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == null)
					return ""; //$NON-NLS-1$
				
				return ((MultiSelectionStructFieldValue) element).getValueName().getText();
			}
		}, false);
		
		selectionEditComposite.addModificationListener(new ModifyListener() {
			@Override
			public void modifyData() {
				notifySearchTriggerListeners();
			}
		});
		
		final List<MultiSelectionStructFieldValue> structFieldValues = new LinkedList<MultiSelectionStructFieldValue>(getFirstStructField().getStructFieldValues());
		selectionEditComposite.setInput(structFieldValues, Collections.EMPTY_SET);
		selectionEditComposite.setTitle(getFirstStructField().getName().getText());
		
		return selectionEditComposite;
	}
	
	
	protected Set<String> getSelectedStructFieldValueIDs() {
		Set<String> selectedStructFieldValueIDs = new HashSet<String>();
		Collection<MultiSelectionStructFieldValue> selectedValues = selectionEditComposite.getSelection();
		if (selectedValues != null) {
			for (MultiSelectionStructFieldValue value : selectedValues) {
				selectedStructFieldValueIDs.add(value.getStructFieldValueID());
			}
		}
		return selectedStructFieldValueIDs;
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		return new MultiSelectionStructFieldSearchFilterItem(getFirstStructField().getStructFieldIDObj(), getMatchType(), getSelectedStructFieldValueIDs());
	}
	
	@Override
	public boolean canHandleMultipleFields() {
		// StructFieldValues are unique to their StructField so it
		// does not make sense to search in multiple fields.
		return false;
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return MultiSelectionStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES;
	}

	@Override
	protected MatchType getDefaultMatchType() {
		return MatchType.EQUALS;
	}

	@Override
	public boolean hasSearchConstraint() {
		Set<String> selectedStructFieldValueIDs = getSelectedStructFieldValueIDs();
		return selectedStructFieldValueIDs != null && selectedStructFieldValueIDs.size() > 0;
	}

	@Override
	public String getInput() {
		StringBuilder sb = new StringBuilder();
		Set<String> selectedValueIDs = getSelectedStructFieldValueIDs();
		if (selectedValueIDs != null) {
			for (String selectedValueID : selectedValueIDs) {
				if (sb.length() > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(selectedValueID);
			}
		}
		return sb.toString();
	}

	@Override
	public void setInput(String input) {
		Set<String> selection = new HashSet<String>();
		if (input != null) {
			String[] inputValues = input.split(","); //$NON-NLS-1$
			for (String inputValue : inputValues) {
				if (inputValue != null) {
					selection.add(inputValue.trim());
				}
			}
		}
		selectionEditComposite.setSelection(selection);
	}
}