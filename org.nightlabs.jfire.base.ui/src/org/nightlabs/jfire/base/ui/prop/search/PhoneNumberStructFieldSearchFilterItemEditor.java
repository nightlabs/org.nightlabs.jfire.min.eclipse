package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.PhoneNumberStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.PhoneNumberStructField;
import org.nightlabs.util.CollectionUtil;

public class PhoneNumberStructFieldSearchFilterItemEditor extends AbstractTextBasedStructFieldSearchFilterItemEditor<PhoneNumberStructField> {
	
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public <T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields, MatchType matchType) {
			Collection<PhoneNumberStructField> textStructFields = CollectionUtil.castCollection(structFields);
			
			return new PhoneNumberStructFieldSearchFilterItemEditor(textStructFields, matchType);
		}
	}
	
	public PhoneNumberStructFieldSearchFilterItemEditor(Collection<PhoneNumberStructField> structFields, MatchType matchType) {
		super(structFields, matchType);
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		return new PhoneNumberStructFieldSearchFilterItem(getStructFieldIDs(), getMatchType(), textEditComposite.getContent());
	}
}
