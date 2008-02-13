/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.j2ee;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.j2ee.JMSQueueMessageList;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JMSQueueMessageListView extends LSDViewPart
{
	private JMSQueueMessageList listComposite;
	private boolean doAutoUpdate = false;

	private Action autoUpdateAction = new Action(
			Messages.getString("org.nightlabs.jfire.base.admin.ui.j2ee.JMSQueueMessageListView.autoUpdateAction.text_enable") //$NON-NLS-1$
		) {
		@Override
		public void run() {
			doAutoUpdate = !doAutoUpdate;
			listComposite.setAutoUpdate(doAutoUpdate);
			if (doAutoUpdate)
				setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.j2ee.JMSQueueMessageListView.autoUpdateAction.text_disable")); //$NON-NLS-1$
			else
				setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.j2ee.JMSQueueMessageListView.autoUpdateAction.text_enable")); //$NON-NLS-1$
			setChecked(doAutoUpdate);
		}
	};

	/**
	 * 
	 */
	public JMSQueueMessageListView() {
	}

	/** {@inheritDoc}
	 * @see org.nightlabs.base.ui.part.ControllablePart#createPartContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartContents(Composite parent) {
		listComposite = new JMSQueueMessageList(parent, SWT.NONE);
		autoUpdateAction.setChecked(true);
		getViewSite().getActionBars().getToolBarManager().add(autoUpdateAction);
		autoUpdateAction.setChecked(false);
	}

}
