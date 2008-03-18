package org.nightlabs.jfire.base.ui.overview.search;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Button;

/**
 * A simple active state manager that controlles the selection flag of a corresponding button.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class ActiveStateButtonManager
	implements ActiveStateManager
{
	/**
	 * The count of parts that can be active and if more than one element is active -> 
	 * this manager is active as well. 
	 */
	private int activePartsCounter;
	
	/**
	 * The corresponding button whose selection state will be changed according to my active state.
	 */
	private Button activeStateButton;
	
	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(ActiveStateButtonManager.class);
	
	/**
	 * @param activeStateButton the button whose selection will be tied to my active state.
	 */
	public ActiveStateButtonManager(Button activeSectionButton)
	{
		assert activeSectionButton != null;
		this.activeStateButton = activeSectionButton;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.search.ActiveStateManager#isActive()
	 */
	@Override
	public boolean isActive()
	{
		return activePartsCounter > 0;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.search.ActiveStateManager#setActive(boolean)
	 */
	@Override
	public void setActive(boolean active)
	{
		final boolean previousActiveState = isActive();
		
		if (active)
		{
			activePartsCounter++;
		}
		else
		{
			activePartsCounter--;
		}
		
		if (activePartsCounter < 0)
		{
			logger.warn("There seems to be an incorrect usage of this ActiveStateManager, since " +
					"setActive(false) is called at least one time too ofter (counter is getting negative)!",
					new Exception());
			
			activePartsCounter = 0;
		}
		
		if (previousActiveState != active)
		{
			activeStateButton.setSelection(isActive());			
		}
	}

}
