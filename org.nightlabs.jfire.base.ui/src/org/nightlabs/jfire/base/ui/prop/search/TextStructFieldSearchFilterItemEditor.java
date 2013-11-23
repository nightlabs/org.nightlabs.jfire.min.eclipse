package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.TextStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.TextStructField;
import org.nightlabs.util.CollectionUtil;

public class TextStructFieldSearchFilterItemEditor extends AbstractTextBasedStructFieldSearchFilterItemEditor<TextStructField> {
	
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public <T extends DataField> IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<T>> structFields, MatchType matchType) {
			Collection<TextStructField> textStructFields = CollectionUtil.castCollection(structFields);
			
			return new TextStructFieldSearchFilterItemEditor(textStructFields, matchType);
		}
	}
	
	public TextStructFieldSearchFilterItemEditor(Collection<TextStructField> structFields, MatchType matchType) {
		super(structFields, matchType);
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		return new TextStructFieldSearchFilterItem(getStructFieldIDs(), getMatchType(), textEditComposite.getContent());
	}
}
