package org.nightlabs.jfire.base.ui.editlock;

public enum ProcessLockAction {
	REFRESH_AND_CONTINUE("Continue editing"),
	RELEASE_AND_SAVE("Save changes"),
	RELEASE_AND_DISCARD("Discard changes");
	
	private String description;
	private ProcessLockAction(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static ProcessLockAction getByDescription(String description) {
		for (ProcessLockAction action : values()) {
			if (action.getDescription().equals(description))
				return action;
		}
		return null;
	}
}
