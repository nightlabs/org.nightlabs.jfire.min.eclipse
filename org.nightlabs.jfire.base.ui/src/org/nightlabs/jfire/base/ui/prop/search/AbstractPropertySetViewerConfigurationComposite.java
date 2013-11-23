package org.nightlabs.jfire.base.ui.prop.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;

public abstract class AbstractPropertySetViewerConfigurationComposite extends XComposite {
	
	public AbstractPropertySetViewerConfigurationComposite(Composite parent, int style) {
		super(parent, style);
	}

	public abstract PropertySetViewerConfiguration getPropertySetViewerConfiguration();
}
