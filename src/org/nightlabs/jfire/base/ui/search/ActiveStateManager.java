package org.nightlabs.jfire.base.ui.search;

/**
 * A simple interface for a state manager that manages a state of the assigned object and can be
 * either active or inactive.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface ActiveStateManager
{
	/**
	 * Sets the active state of the corresponding object to <code>active</code>.
	 *
	 * @param active The new active state of the managed object.
	 */
	void setActive(final boolean active);

	/**
	 * Returns the active state of the corresponding object.
	 * @return the active state of the corresponding object.
	 */
	boolean isActive();

	/**
	 * Sets the selection state of the corresponding object according to the value of the given parameter.
	 * @param active The selection state to be set for the managed object.
	 */
	void setSelection(final boolean active);
}
