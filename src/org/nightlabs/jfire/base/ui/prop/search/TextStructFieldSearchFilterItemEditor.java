package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.base.ui.edit.TextEditComposite;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.search.IStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.search.TextStructFieldSearchFilterItem;
import org.nightlabs.jfire.prop.structfield.TextStructField;
import org.nightlabs.util.CollectionUtil;

public class TextStructFieldSearchFilterItemEditor extends AbstractStructFieldSearchFilterItemEditor<TextStructField> {
	
	public static class Factory implements IStructFieldSearchFilterItemEditorFactory {
		@Override
		public IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<?>> structFields, MatchType matchType) {
			Collection<TextStructField> textStructFields = CollectionUtil.castCollection(structFields);
			
			return new TextStructFieldSearchFilterItemEditor(textStructFields, matchType);
		}
	}
	
	private TextEditComposite textEditComposite;

	public TextStructFieldSearchFilterItemEditor(Collection<TextStructField> structFields, MatchType matchType) {
		super(structFields, matchType);
	}
	
	public TextStructFieldSearchFilterItemEditor(TextStructField structField, MatchType matchType) {
		this(Collections.singleton(structField), matchType);
	}
	
	public TextStructFieldSearchFilterItemEditor(Collection<TextStructField> structFields) {
		this(structFields, null);
	}
	
	public TextStructFieldSearchFilterItemEditor(TextStructField structField) {
		this(Collections.singleton(structField));
	}

	@Override
	protected Control createEditControl(Composite parent) {
		textEditComposite = new TextEditComposite(parent, SWT.NONE, 1, false);
		return textEditComposite;
	}

	@Override
	public boolean canHandleMultipleFields() {
		return true;
	}

	@Override
	public IStructFieldSearchFilterItem getSearchFilterItem() {
		return new TextStructFieldSearchFilterItem(getStructFieldIDs(), getMatchType(), textEditComposite.getContent());
	}

	@Override
	public EnumSet<MatchType> getSupportedMatchTypes() {
		return TextStructFieldSearchFilterItem.SUPPORTED_MATCH_TYPES;
	}

	@Override
	protected MatchType getDefaultMatchType() {
		return MatchType.CONTAINS;
	}

	@Override
	public boolean hasSearchConstraint() {
		return !textEditComposite.getContent().equals("");
	}
}

