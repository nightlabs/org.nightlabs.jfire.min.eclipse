package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.RegexStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.RegexStructField;
import org.nightlabs.util.CollectionUtil;

public class RegexStructFieldSearchFilterItemEditor extends AbstractTextBasedStructFieldSearchFilterItemEditor<RegexStructField> {
	
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public <T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields, MatchType matchType) {
			Collection<RegexStructField> textStructFields = CollectionUtil.castCollection(structFields);
			
			return new RegexStructFieldSearchFilterItemEditor(textStructFields, matchType);
		}
	}
	
	public RegexStructFieldSearchFilterItemEditor(Collection<RegexStructField> structFields, MatchType matchType) {
		super(structFields, matchType);
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		return new RegexStructFieldSearchFilterItem(getStructFieldIDs(), getMatchType(), textEditComposite.getContent());
	}
}
