package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

/**
 * Listener that can be used to track changes of the display name of a PropertySet
 * while it is edited by the user.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface DisplayNameChangedListener {
	/**
	 * Called to notify the listener that the display name has changed.
	 * 
	 * @param displayNameChangedEvent The event object references the old and the new value of the display name.
	 */
	void displayNameChanged(DisplayNameChangedEvent displayNameChangedEvent);
}
 