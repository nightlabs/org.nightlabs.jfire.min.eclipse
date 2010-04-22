package org.nightlabs.jfire.base.ui.prop.view;

/**
 * Listener that is notified when the input of an {@link IPropertySetViewer} changes.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IPropertySetViewerInputChangedListener {
	/**
	 * Called when the content/input of the given {@link IPropertySetViewer} has changed.
	 * 
	 * @param propertySetViewer The viewer whose content has changed.
	 */
	void inputChanged(IPropertySetViewer<?, ?, ?> propertySetViewer);
}
