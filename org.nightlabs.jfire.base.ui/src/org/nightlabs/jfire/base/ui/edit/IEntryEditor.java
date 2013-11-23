package org.nightlabs.jfire.base.ui.edit;

public interface IEntryEditor {

	/**
	 * Sets the enabled state of the entry viewer and shows the given tooltip
	 * when disabled. The tooltip should be removed when the entry viewer is
	 * disabled.
	 * 
	 * @param enabled The enabled state of the entry viewer to set.
	 * @param tooltip The tooltip to be set when the entry viewer is disabled.
	 */
	public abstract void setEnabledState(boolean enabled, String tooltip);
	
	/**
	 * Sets the title of this entry viewer.
	 * 
	 * @param title The title to be set.
	 */
	public abstract void setTitle(String title);
}