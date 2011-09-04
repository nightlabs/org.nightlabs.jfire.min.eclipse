package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.EnumSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.edit.BooleanEditComposite;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.BooleanStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.BooleanStructField;
import org.nightlabs.util.CollectionUtil;

public class BooleanStructFieldSearchFilterItemEditor extends AbstractStructFieldSearchFilterItemEditor<BooleanStructField> {
	
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public <T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields, MatchType matchType) {
			Collection<BooleanStructField> textStructFields = CollectionUtil.castCollection(structFields);
			
			return new BooleanStructFieldSearchFilterItemEditor(textStructFields, matchType);
		}
	}
	
	public BooleanStructFieldSearchFilterItemEditor(Collection<BooleanStructField> structFields, MatchType matchType) {
		super(structFields, matchType);
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		return new BooleanStructFieldSearchFilterItem(getStructFieldIDs(), getMatchType(), booleanEditComposite.getValue());
	}

	@Override
	public boolean hasSearchConstraint() {
		return booleanEditComposite.getValue() != null;
	}

	@Override
	public boolean canHandleMultipleFields() {
		return true;
	}

	@Override
	public String getInput() {
		Boolean value = booleanEditComposite.getValue();
		return value != null ? value.toString() : "null";
	}

	@Override
	public void setInput(String input) {
		if ("null".equals(input))
			booleanEditComposite.setValue(null);
		else 
			booleanEditComposite.setValue(Boolean.valueOf(input));
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return BooleanStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES;
	}

	private BooleanEditComposite booleanEditComposite; 
	
	@Override
	protected Control createEditControl(Composite parent) {
		booleanEditComposite = new BooleanEditComposite(parent, SWT.NONE);
		booleanEditComposite.setTitle(getStructFieldNames());
		return booleanEditComposite;
	}

	@Override
	protected MatchType getDefaultMatchType() {
		return BooleanStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES.iterator().next();
	}
}
