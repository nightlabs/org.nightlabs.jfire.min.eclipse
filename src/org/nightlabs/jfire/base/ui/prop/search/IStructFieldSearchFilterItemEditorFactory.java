package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jfire.prop.StructField;

public interface IStructFieldSearchFilterItemEditorFactory {
	public IStructFieldSearchFilterItemEditor createEditorInstance(Collection<StructField<?>> structFields, MatchType matchType);
}
