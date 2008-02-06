/**
 * 
 */
package org.nightlabs.jfire.base.ui.entity.editor;

import java.util.ArrayList;
import java.util.Collections;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.dialog.CenteredDialog;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

/**
 * Best practice for one-object controllers
 * cloning of object (not working on copy in cache)
 * listener for remote changes and notification for the user
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class ActiveEntityEditorPageController<EntityType> extends EntityEditorPageController {

	/**
	 * Enum of choices for the user when an object was changed.
	 */
	protected enum EntityChangeAction {
		keepLocalChanges("Keep the local changes (Save might overwrite remote changes)"),
		loadRemoteChanges("Load the changed object (Looses local changes)"),
		viewRemoteChanges("View the remote object in another editor");
		
		private String message;
		
		EntityChangeAction(String message) {
			this.message = message;
		}
		
		public String getMessage() {
			return message;
		}
	}
	
	/**
	 * Dialog used to present the user his choices when the entity was changed on the server.
	 */
	protected class EntityChangeUserActionDialog extends CenteredDialog 
	{
		private EntityChangeAction selectedChoice;
		private List choiceList;
		private java.util.List<EntityChangeAction> choices;

		public EntityChangeUserActionDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA);
			Label label = new Label(wrapper, SWT.BOLD | SWT.WRAP);
			label.setText(
					String.format("The object currently edited in '%s' was changed on the server.\nWhat do you want to do?",
							getEntityEditor().getPartName()
					)
			);
			GridData gd = new GridData();
			label.setLayoutData(gd);
			
			choiceList = new List(wrapper, SWT.BORDER | SWT.SINGLE);

			choices = new ArrayList<EntityChangeAction>();
			choices.add(EntityChangeAction.keepLocalChanges);
			choices.add(EntityChangeAction.loadRemoteChanges);
			if (createNewInstanceEditorInput() != null) {
				choices.add(EntityChangeAction.viewRemoteChanges);
			}			
			for (EntityChangeAction choice : choices) {
				choiceList.add(choice.getMessage());
			}
			choiceList.setSelection(1);
			choiceList.setLayoutData(new GridData(GridData.FILL_BOTH));
			choiceList.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int idx = choiceList.getSelectionIndex(); 
					if (idx >= 0 && idx < choices.size()) {
						selectedChoice = choices.get(idx);
					} else {
						selectedChoice = null;						 
					}
					// TODO set OK button enable-state
				}
			});

			return wrapper;
		}


		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Entity changed");
			newShell.setSize(400, 300);
		}

		public EntityChangeAction getSelectedChoice() {
			return selectedChoice;
		}
	}	
	
	/**
	 * Dialog used to tell the user that the entity was deleted on the server.
	 */
	protected class EntityDeleteUserActionDialog extends CenteredDialog 
	{
		public EntityDeleteUserActionDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA);
			Label label = new Label(wrapper, SWT.BOLD | SWT.WRAP);
			label.setText(
					String.format("The object currently edited in '%s' was deleted on the server.\nDo you want to close the editor?",
							getEntityEditor().getPartName()
					)
			);
			GridData gd = new GridData();
			label.setLayoutData(gd);
			return wrapper;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Entity deleted");
			newShell.setSize(400, 200);
		}
	}
	/**
	 * Handler used by default when the entity is changed.
	 * It will display the {@link EntityChangeUserActionDialog}
	 * with choices of {@link EntityChangeAction} and
	 * react accordingly.
	 */
	protected class HandleChangeRunnable implements Runnable {
		
		public HandleChangeRunnable() {
		}
		
		@Override
		public void run() {
			final EntityChangeAction[] choice = new EntityChangeAction[1];
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					EntityChangeUserActionDialog dlg = new EntityChangeUserActionDialog(Display.getDefault().getActiveShell());
					if (dlg.open() == Window.OK) {
						choice[0] = dlg.getSelectedChoice();
					}
				}
			});
			if (choice[0] != null) {
				switch (choice[0]) {
				case keepLocalChanges:
					// don't do anything, but also don't put the object into the cache, because its stale
					setStale(true);
					break;
				case loadRemoteChanges:
					// reload					
					reload(new NullProgressMonitor());
					break;
				case viewRemoteChanges:
					// open the editor with the fresh object
					try {
						RCPUtil.openEditor(createNewInstanceEditorInput(), getEntityEditor().getEditorID());
					} catch (PartInitException e) {
						throw new RuntimeException(e);
					}
					// but also keep this one, mark it stale
					setStale(true);
					break;
				default:
					break;
				}
			}
		}
	}
	/**
	 * Handler used by default when the entity is deleted on the server.
	 * It will display the {@link EntityDeleteUserActionDialog}
	 * and will close the editor when wished.
	 */
	protected class HandleDeleteRunnable implements Runnable {
		
		public HandleDeleteRunnable() {
		}
		
		@Override
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					EntityDeleteUserActionDialog dlg = new EntityDeleteUserActionDialog(Display.getDefault().getActiveShell());
					if (dlg.open() == Window.OK) {
						// close the editor without saving when the user clicks OK
						getEntityEditor().close(false);
					}
				}
			});
		}
	}	
	
	/**
	 * The change listener that will determine whether a
	 * reaction on the remote change is necessary (change not caused by itself and local changes present).
	 * It will {@link #setHandleEntityChangeRunnable(Runnable)} either with {@link #createEntityChangedHandler(DirtyObjectID)}
	 * or {@link #createEntityDeletedHandler(DirtyObjectID)}.
	 */
	protected class EntityChangeListener extends NotificationAdapterJob {
		
		public EntityChangeListener(String jobName) {
			super(jobName);
		}

		public void notify(NotificationEvent notificationEvent) {
			synchronized (mutex) {
				// if no object managed, return
				if (controllerObject == null)
					return;

				Object controllerObjectID = JDOHelper.getObjectId(controllerObject);
				ArrayList<DirtyObjectID> reverseSubjects = new ArrayList<DirtyObjectID>(notificationEvent.getSubjects());
				Collections.reverse(reverseSubjects);
				for (DirtyObjectID dirtyObjectID : reverseSubjects) {
					if (controllerObjectID.equals(dirtyObjectID.getObjectID())) {
						if (dirtyObjectID.getLifecycleState() == JDOLifecycleState.DELETED) {
							// create the handler for the deletion of the object
							setHandleEntityChangeRunnable(createEntityDeletedHandler(dirtyObjectID));
						} else {
							if (checkForSelfCausedChange(dirtyObjectID)) {
								// if this controller has caused the change then simply put the 
								// object into the cache again.
								Cache.sharedInstance().put(null, controllerObject, getEntityFetchGroups(), getEntityMaxFetchDepth());
							} else {
								// another controller/client has caused the change
								if (isDirty()) {
									// the controller is dirty / has local changes so the current version might differ from the remote one.
									// create the handler for the change of the object
									setHandleEntityChangeRunnable(createEntityChangedHandler(dirtyObjectID));
								} else {
									// no local changes, reload
									doReload(new org.eclipse.core.runtime.SubProgressMonitor(getProgressMonitor(), 100));
								}
							}
						}

						// only the last event is taken into account
						break;
					}						
				}
			}
		}
	}	
	
	/**
	 * The object currently managed by this controller.
	 */
	private EntityType controllerObject = null;
	
	/**
	 * This is determined on load and the {@link #entityChangeListener}
	 * will be registered on this class.
	 */
	private Class<?> controllerObjectClass = null;
	
	/**
	 * Determines whether the object held by the current controller
	 * is not in sync with the server any more, that means a change
	 * was notified but the user has neglected it.
	 */
	private boolean stale = false;  
	
	/**
	 * This is used to synchronize.
	 */
	private Object mutex = new Object();
	
	/**
	 * This is set to react on changes, but only when the the editor gets activated.
	 * This value is checked and executed by {@link #handleEditorActivated()} in its default implementation.
	 */
	private Runnable handleEntityChangeRunnable = null;
	
	/**
	 * The EntityChangeListener of this controller.
	 * This value will be set when the change listener
	 * is registered {@link #doLoad(IProgressMonitor)}.
	 */
	private EntityChangeListener entityChangeListener = null;
	
	/**
	 * Create a new {@link ActiveEntityEditorPageController}. It
	 * will register a 
	 * @param editor
	 */
	public ActiveEntityEditorPageController(EntityEditor editor) {
		super(editor);		
		if (getEntityEditor() != null) {
			// add a part listener that will react on the activation of the
			// associated editor and notify the user of a change if necessary.
			RCPUtil.getActiveWorkbenchPage().addPartListener(new IPartListener() {
				@Override
				public void partActivated(final IWorkbenchPart part) {
					if (part.equals(getEntityEditor())) {
						// part was activated, handle notify the user if necessary
						handleEditorActivated();
					}
				}
				@Override
				public void partBroughtToTop(final IWorkbenchPart part) {
				}
				@Override
				public void partClosed(final IWorkbenchPart part) {
					if (part.equals(getEntityEditor())) {
						// part was closed, remove the change listener.
						if (entityChangeListener != null && controllerObjectClass != null) {
							JDOLifecycleManager.sharedInstance().removeNotificationListener(controllerObjectClass, entityChangeListener);
						}
					}
				}
				@Override
				public void partDeactivated(final IWorkbenchPart part) {
				}
				@Override
				public void partOpened(final IWorkbenchPart part) {
				}
			});
		}		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation calls {@link #retrieveEntity(ProgressMonitor)} 
	 * and keeps a clone of the result as controller object (see {@link #getControllerObject()}).
	 * </p>
	 */
	@Override
	public void doLoad(IProgressMonitor monitor) {
		EntityType oldControllerObject = null;
		monitor.beginTask(getLoadJobName(), 100);
		synchronized (mutex) {
			oldControllerObject = controllerObject;
			ProgressMonitorWrapper pMonitor = new ProgressMonitorWrapper(monitor);
			controllerObject = retrieveEntity(new SubProgressMonitor(pMonitor, 100));
			if (controllerObject != null) {
				controllerObject = Util.cloneSerializable(controllerObject);
				if (controllerObjectClass != null && !controllerObjectClass.equals(controllerObject.getClass())) {
					throw new IllegalStateException("The implementation of ActiveEntityEditorPageController '" + this.getClass().getSimpleName() + "' returned different types of objects on retrieveEntity (" + controllerObjectClass.getName() + " and " + controllerObject.getClass().getName() + ").");
				}
				controllerObjectClass = controllerObject.getClass();
				if (entityChangeListener == null) {
					entityChangeListener = new EntityChangeListener(getProcessChangesJobName());
					JDOLifecycleManager.sharedInstance().addNotificationListener(controllerObjectClass, entityChangeListener);
				}
			}
			setStale(false);
		}
		monitor.done();
		fireModifyEvent(oldControllerObject, controllerObject);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation calls {@link #storeEntity(ProgressMonitor)} 
	 * and keeps a clone of the result as controller object (see {@link #getControllerObject()}).
	 * </p>
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask(getSaveJobName(), 100);
		EntityType oldControllerObject = null;
		synchronized (mutex) {
			oldControllerObject = controllerObject;
			ProgressMonitorWrapper pMonitor = new ProgressMonitorWrapper(monitor);
			controllerObject = storeEntity(controllerObject, new SubProgressMonitor(pMonitor, 100));
			// we don't put the result into the Cache, as the Cache will be notified
			// of the change and the change listener will put the object into the cache
			controllerObject = Util.cloneSerializable(controllerObject);
			setStale(false);
		}
		monitor.done();
		fireModifyEvent(oldControllerObject, controllerObject);
	}	

	
	/**
	 * This method is called when the associated Editor
	 * is activated and will run the {@link #handleEntityChangeRunnable} if set. 
	 */
	protected void handleEditorActivated() {
		if (handleEntityChangeRunnable != null) {
			Runnable runnable = handleEntityChangeRunnable;
			handleEntityChangeRunnable = null;
			runnable.run();
		}
	}
	
	/**
	 * Reload the controller object. Currently only wraps {@link #reload(IProgressMonitor)}. 
	 */
	protected void doReload(IProgressMonitor monitor) {
		// TODO: Think about doing this in a job and notifying the page before the reload (so it can show the progress view)
		reload(monitor);
	}

	
	protected String getLoadJobName() {
		return "Loading entity...";
	}
	
	protected String getSaveJobName() {
		return "Saving entity...";
	}
	
	protected String getProcessChangesJobName() {
		return "Processing entity changes...";
	}
	
	/**
	 * Subclasses need to implement the retrieval of the controllers
	 * object here. Usually this will be a call to the DAO object.
	 *  
	 * @param monitor The monitor to use.
	 * @return The controllers object.
	 */
	protected abstract EntityType retrieveEntity(ProgressMonitor monitor);
	
	/**
	 * Subclasses need to implement the storing of the given controller object here.
	 * Usually this will be a call to the DAO object. The saved object (newly retrieved from the server)
	 * should be returned.
	 * @param controllerObject The controllerObject to store. 
	 * @param monitor The monitor to use.
	 * @return The controllers object.
	 */
	protected abstract EntityType storeEntity(EntityType controllerObject, ProgressMonitor monitor);
	
	/**
	 * These fetch-groups are used for putting the controller object
	 * into the {@link Cache}. However retrieving the object should be done
	 * with the same fetch-group and so this method should be used by
	 * {@link #retrieveEntity(ProgressMonitor)} and {@link #storeEntity(Object, ProgressMonitor)} also.
	 * 
	 * @return The fetch-groups for retrieving the controller object.
	 */
	protected abstract String[] getEntityFetchGroups();
	
	/**
	 * This fetch-depth is used for putting the controller object
	 * into the {@link Cache}. However retrieving the object should be done
	 * with the same fetch-depth and so this method should be used by
	 * {@link #retrieveEntity(ProgressMonitor)} and {@link #storeEntity(Object, ProgressMonitor)} also.
	 * The default implementation returns {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}.
	 * 
	 * @return The fetch-depth for retrieving the controller object.
	 */
	protected int getEntityMaxFetchDepth() {
		return NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT;
	}
	
	/**
	 * If this does not return <code>null</code> the returned {@link IEditorInput}
	 * will be used to open a new editor instance for the controller object
	 * that will load the remote changes. This way the user can compare the local
	 * and the remote version.
	 * <p>
	 * The default implementation returns <code>null</code>.
	 * </p> 
	 * @return {@link IEditorInput} that allows opening of a new instance of the associated editor for the object with the same id but different version. 
	 */
	protected IEditorInput createNewInstanceEditorInput() {
		return null;
	}

	/**
	 * This method checks if the change notified by the given {@link DirtyObjectID} was caused
	 * by this controller (actually this is not correct, as it only checks if the change was caused by this client).
	 * 
	 * @param dirtyObjectID The {@link DirtyObjectID} to check.
	 * @return Whether the changed notified by the given {@link DirtyObjectID} was caused by this controller.
	 */
	protected boolean checkForSelfCausedChange(DirtyObjectID dirtyObjectID) {
		for (String sessionID : dirtyObjectID.getSourceSessionIDs()) {
			if (!sessionID.equals(Cache.sharedInstance().getSessionID()))
				return false;
		}
		return true;
	}
	
	/**
	 * Create the runnable that will be executed to notify the user of 
	 * a remote change of the controller object.
	 * <p>
	 * This runnable will be set by the change listener, but when the editor currently doesn't have the focus, 
	 * it will be executed lazily when the editor was activated again.
	 * </p>
	 * <p>
	 * This implementation returns {@link HandleChangeRunnable}.
	 * </p>
	 * @param dirtyObjectID The {@link DirtyObjectID} that notified the change.
	 * @return The runnable that will be executed to notify the user of 
	 * 	a remote change of the controller object.
	 */
	protected Runnable createEntityChangedHandler(DirtyObjectID dirtyObjectID) {
		return new HandleChangeRunnable();
	}
	
	/**
	 * Create the runnable that will be executed to notify the user that
	 * the controller object was deleted on the server.
	 * <p>
	 * This runnable will be set by the change listener, but when the editor currently doesn't have the focus, 
	 * it will be executed lazily when the editor was activated again.
	 * </p>
	 * <p>
	 * This implementation returns {@link HandleDeleteRunnable}.
	 * </p>
	 * @param dirtyObjectID The {@link DirtyObjectID} that notified the change.
	 * @return The runnable that will be executed to notify the user that
	 * 		the controller object was deleted on the server.
	 */
	protected Runnable createEntityDeletedHandler(DirtyObjectID dirtyObjectID) {
		return new HandleDeleteRunnable();
	}
	
	/**
	 * Set the runnable that will be executed on editor activation.
	 * <p>
	 * If the associated editor is the active editor in the active WorkbenchPage
	 * the runnable will be run instantly otherwise {@link #handleEntityChangeRunnable}
	 * will be set (that will be run by {@link #handleEditorActivated()}).
	 * </p>
	 * @param runnable The runnable to set.
	 */
	protected void setHandleEntityChangeRunnable(Runnable runnable) {
		if (this.handleEntityChangeRunnable == null) {
			final IEditorPart activeEditor = RCPUtil.getActiveWorkbenchPage().getActiveEditor();
			if (activeEditor != null && activeEditor == getEntityEditor()) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
			} else {
				this.handleEntityChangeRunnable = runnable;
			}
		}
	}

	/**
	 * Determines whether the object held by the current controller
	 * is not in sync with the server any more, that means a change
	 * was notified but the user has neglected it.
	 * 
	 * @return The stale state.
	 */
	public boolean isStale() {
		return stale;
	}
	/**
	 * Set the stale state.
	 * @param stale The stale state.
	 */
	protected void setStale(boolean stale) {
		this.stale = stale;
	}
	
	/**
	 * Returns the current object managed by this controller.
	 * @return The current object managed by this controller.
	 */
	public EntityType getControllerObject() {
		return controllerObject;
	}
	
}
