/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

/**
 * Event used to notify {@link DisplayNameChangedListener}s of a change of the display name.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DisplayNameChangedEvent {

	private String oldDisplayName;
	private String newDisplayName;
	
	/**
	 * Create a new {@link DisplayNameChangedEvent}. 
	 */
	public DisplayNameChangedEvent(String oldDisplayName, String newDisplayName) {
		this.oldDisplayName = oldDisplayName;
		this.newDisplayName = newDisplayName;
	}

	/**
	 * @return The old display name of the edited PropertySet.
	 */
	public String getOldDisplayName() {
		return oldDisplayName;
	}
	/**
	 * @return The new display name of the edited PropertySet.
	 */
	public String getNewDisplayName() {
		return newDisplayName;
	}

}
