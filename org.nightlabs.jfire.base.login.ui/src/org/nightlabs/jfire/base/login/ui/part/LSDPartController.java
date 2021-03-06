/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.login.ui.part;

import javax.security.auth.login.LoginException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.base.ui.part.ControllablePart;
import org.nightlabs.base.ui.part.PartController;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.base.login.ui.LoginStateChangeEvent;
import org.nightlabs.jfire.base.login.ui.LoginStateListener;
import org.nightlabs.singleton.ISingletonProvider;
import org.nightlabs.singleton.SingletonProviderFactory;
import org.nightlabs.singleton.ISingletonProvider.ISingletonFactory;

/**
 * PartController that will update the registered parts whenever the
 * LoginState changes. It will dispose the contents of all controlled parts
 * when the user logs out.
 * See {@link org.nightlabs.base.ui.part.PartController} and
 * {@link org.nightlabs.base.ui.part.ControllablePart} for detailed explanation on
 * how a PartController works, here is a exaple on how to use the LSDPartController.
 * 
 * In the constructor of your WorkbenchPart you want to make LoginStateDependent register
 * it to the sharedInstance of LSDPartController:
 * <pre>
 * 	public MyView() {
 * 		LSDPartController.sharedInstance().registerPart(this);
 * 	}
 * </pre>
 * 
 * Delegate the createPartControl() method of your WorkbenchPart to the sharedInstance:
 * <pre>
 *  public void createPartControl(Composite parent)
 *  {
 *  	LSDPartController.sharedInstance().createPartControl(this, parent);
 *  }
 * </pre>
 * And create the real WorkbenchPart contents in {@link org.nightlabs.base.ui.part.ControllablePart#createPartContents(Composite)}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class LSDPartController extends PartController implements LoginStateListener {

	@Override
	protected Composite createNewConditionUnsatisfiedComposite(Composite parent) {
		return new NeedLoginComposite(parent, SWT.BORDER);
	}

	/**
	 * @see org.nightlabs.base.ui.part.PartController#registerPart(org.nightlabs.base.ui.part.ControllablePart)
	 */
	@Override
	public void registerPart(ControllablePart part) {
		super.registerPart(part);
		Login.loginAsynchronously();
	}

	public void loginStateChanged(LoginStateChangeEvent event) {
		if (event.getNewLoginState() != LoginState.LOGGED_IN)
			disposePartContents();
		updateParts();
	}

	private static ISingletonProvider<LSDPartController> sharedInstance = SingletonProviderFactory.createProviderForFactory(new ISingletonFactory<LSDPartController>() {
		@Override
		public LSDPartController makeInstance() {
			LSDPartController controller = new LSDPartController();
			
			try {
				Login.getLogin(false).addLoginStateListener(controller);
			} catch (LoginException e) {
				throw new IllegalStateException("This should never happen as Login.getLogin(false) was called."); //$NON-NLS-1$
			}
			
			return controller;
		}
	});
	
	public static LSDPartController sharedInstance() {
		return sharedInstance.getInstance();
	}

	@Override
	public void registerPart(ControllablePart part, Layout layout)
	{
		super.registerPart(part, layout);
		Login.loginAsynchronously();
	}

}
