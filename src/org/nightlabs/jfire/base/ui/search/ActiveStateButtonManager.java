package org.nightlabs.jfire.base.ui.search;

import org.eclipse.swt.widgets.Button;

/**
 * A simple active state manager that controls the selection flag of a corresponding button.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class ActiveStateButtonManager
	extends DefaultActiveStateManager
{
	/**
	 * The corresponding button whose selection state will be changed according to my active state.
	 */
	private Button activeStateButton;

	/**
	 * @param activeStateButton the button whose selection will be tied to my active state.
	 */
	public ActiveStateButtonManager(final Button activeSectionButton)
	{
		assert activeSectionButton != null;
		this.activeStateButton = activeSectionButton;
	}

	@Override
	public void setActive(final boolean active)
	{
		final boolean previousActiveState = isActive();
		super.setActive(active);

		if (previousActiveState != isActive())
			activeStateButton.setSelection(isActive());
	}


	@Override
	public void setSelection(final boolean active)
	{
		activeStateButton.setSelection(active);
	}

}
