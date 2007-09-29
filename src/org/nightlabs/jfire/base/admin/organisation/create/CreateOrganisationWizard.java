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

package org.nightlabs.jfire.base.admin.organisation.create;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.jfire.base.admin.resource.Messages;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.organisation.OrganisationManager;
import org.nightlabs.jfire.organisation.OrganisationManagerUtil;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class CreateOrganisationWizard extends Wizard
{
	private static final Logger logger = Logger.getLogger(CreateOrganisationWizard.class);

	private CreateOrganisationPage coPage;

	public CreateOrganisationWizard() 
	throws LoginException, NamingException, RemoteException, CreateException
	{
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.getString("org.nightlabs.jfire.base.admin.organisation.create.CreateOrganisationWizard.windowTitle")); //$NON-NLS-1$
	}

	public void addPages() 
	{
		coPage = new CreateOrganisationPage();
		addPage(coPage);
	}

	public boolean performFinish()
	{
		final String organisationID = coPage.getOrganisationID(); 
		final String organisationDisplayName = coPage.getOrganisationDisplayName();
		final String userID = coPage.getUserID();
		final String password = coPage.getPassword1();
		final boolean isServerAdmin = coPage.isServerAdmin();

		String createOrganisationJobName = String.format(
				Messages.getString("org.nightlabs.jfire.base.admin.organisation.create.CreateOrganisationWizard.createOrganisationJob.name"), //$NON-NLS-1$
				new Object[] { organisationID, organisationDisplayName });
		Job job = new Job(createOrganisationJobName) {
			@Implement
			protected IStatus run(IProgressMonitor monitor)
			{
				try {
					int monitorTotalWorked = 0;
					monitor.beginTask(
							String.format(Messages.getString("org.nightlabs.jfire.base.admin.organisation.create.CreateOrganisationWizard.createOrganisationJob.monitor.taskName_initiatingCreation"), //$NON-NLS-1$
									new Object[] { organisationID, organisationDisplayName } ),
							100);

					OrganisationManager organisationManager = OrganisationManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
					CreateOrganisationProgressID createOrganisationProgressID = organisationManager.createOrganisationAsync(
							organisationID, 
							organisationDisplayName,
							userID,
							password,
							isServerAdmin);

					monitor.worked(3);
					monitorTotalWorked += 3;

					// close the dialog if the creation could be started (if the parameters are checked already, it should be ok to close now).
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							((WizardDialog)getContainer()).close(); // not clean, but should work - how can I achieve this behaviour in a cleaner way? Marco.
						}
					});

					String taskName = null;
					CreateOrganisationProgress createOrganisationProgress;
					do {
						createOrganisationProgress = organisationManager.getCreateOrganisationProgress(createOrganisationProgressID);
						if (createOrganisationProgress == null)
							throw new IllegalStateException("Server doesn't know our id: " + createOrganisationProgressID); //$NON-NLS-1$

						List<CreateOrganisationStatus> statusList = createOrganisationProgress.getCreateOrganisationStatusList();
						int percentage = 100 * statusList.size() / Math.max(1, createOrganisationProgress.getStepsTotal());
						if (logger.isDebugEnabled())
							logger.debug("createOrganisationProgress.stepsTotal=" + createOrganisationProgress.getStepsTotal() + " stepsDone=" + statusList.size() + " percentage=" + percentage); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						if (createOrganisationProgress.getLastCreateOrganisationStatus() != null) {
							String messageArgs = (createOrganisationProgress.getLastCreateOrganisationStatus().getMessageArgs() == null ?
									"" : //$NON-NLS-1$
									CollectionUtil.toString(createOrganisationProgress.getLastCreateOrganisationStatus().getMessageArgs()));

							String newTaskName = String.format(
									Messages.getString("org.nightlabs.jfire.base.admin.organisation.create.CreateOrganisationWizard.createOrganisationJob.monitor.taskName_step"), //$NON-NLS-1$
									new Object[] {
										organisationID,
										organisationDisplayName,
										createOrganisationProgress.getLastCreateOrganisationStatus().getCreateOrganisationStep().toString(),
										new Integer(percentage),
										messageArgs
									}
							);

							if (!Util.equals(newTaskName, taskName)) {
								taskName = newTaskName;
								monitor.setTaskName(taskName);
								logger.info("taskName: " + taskName); //$NON-NLS-1$
							}

							if (percentage > monitorTotalWorked) {
								int diff = percentage - monitorTotalWorked;
								monitor.worked(diff);
								monitorTotalWorked += diff;
							}
						}

						try {
							Thread.sleep(3000);
						} catch (InterruptedException x) {
							// ignore
						}
					} while (!createOrganisationProgress.isDone());

				} catch (Throwable e) {
					ExceptionHandlerRegistry.asyncHandleException(e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();

		return false;

//		IRunnableWithProgress op = new IRunnableWithProgress() 
//		{
//			public void run(IProgressMonitor monitor) 
//			throws InvocationTargetException 
//			{
//				try
//				{
//					monitor.beginTask("Creating organisation", 100);
//					organisationManager.createOrganisation(coPage.getOrganisationID(), 
//							coPage.getOrganisationDisplayName(),
//							coPage.getUserID(),
//							coPage.getPassword1(),
//							coPage.isServerAdmin());
//					monitor.worked(100);
//				}
//				catch (RemoteException e)
//				{
//					throw new InvocationTargetException(e);
//				}
//				catch (ModuleException e)
//				{
//					throw new InvocationTargetException(e);
//				}
//				monitor.done();
//			}
//		};
//		try 
//		{
//			getContainer().run(false, false, op);
//		} 
//		catch (Exception e) 
//		{
//			ExceptionHandlerRegistry.syncHandleException(e);
//			return false;
//		}
//		return true;
	}
}
