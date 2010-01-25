package org.nightlabs.jfire.base.ui.prop.search;

import java.util.Collection;

import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.id.StructFieldID;

public interface IPropertySetSearchResultViewer {
	public void setStructFieldIDs(Collection<StructFieldID> structFieldIDs);
	
	public Control createControl();
}
