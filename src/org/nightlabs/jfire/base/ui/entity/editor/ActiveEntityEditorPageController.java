/**
 * 
 */
package org.nightlabs.jfire.base.ui.entity.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.celleditor.ComboBoxCellEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.EntityEditorStaleHandler;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageStaleHandler;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

/**
 * EntityEditorPageController that manages one single object that it assumes to be a
 * JFire JDO object (PersistanceCapable). For this object the controller will register
 * a change and delete listener and will notify the user when the object was changed
 * on the server.
 * <p>
 * This should be used as base-class for EntityEditorPageControllers where ever possible
 * as it implements the best practice for such an editor with a listener for remote changes.
 * </p>
 * <p>
 * The controller delegates the loading of the object to {@link #retrieveEntity(ProgressMonitor)}
 * where a subclass would usually use the appropriate DAO object. After loading the object is
 * cloned, so that the Editor operates on a independent copy and the object is not modified in
 * the cache until it is not saved.
 * </p>
 * <p>
 * The saving is also delegated, to {@link #storeEntity(Object, ProgressMonitor)} that should also
 * use the appropriate DAO.
 * </p>
 * <p>
 * Loading and saving should use the same fetch-groups (saving for re-retrieving the object). These
 * fetch-groups are also used to put the retrieved object into the Cache and need therefore to be
 * returned in {@link #getEntityFetchGroups()} and it is advised to use this method in the retrieve and store methods.
 * </p>
 * <p>
 * This controller will register a change listener for the object.
 * The listener will first check if this controller is responsible to process the change notification. It will do
 * so by checking the ObjectID of the object and checking if the controller/Editor caused the change
 * itself (this is currently delegated to {@link #checkForSelfCausedChange(DirtyObjectID)}).
 * </p>
 * <p>
 * If the controller finds itself responsible it will first check if the Editor has local changes (isDirty()).
 * If so it will invoke (possibly lazy when the Editor gets activated/focus) a handler to react on that change.
 * The handler is invoked via the editors {@link EntityEditorStaleHandler}. The handler used for a change
 * notification can be overwritten by {@link #createEntityChangedHandler(DirtyObjectID)}.
 * </p>
 * <p>
 * The default change handler ({@link EntityChangedHandler}) will let the user choose from the following options when
 * a remote change was notified and the local copy was already modified:
 * <ul>
 *   <li>Keep the local changes, that will not reload the Editor. This might result in remote changes being overwritten when the local copy is saved.</li>
 *   <li>Load the remote object, that will simply reload the Editor discarding the local changes.</li>
 *   <li>Load the remote object in an other Editor instance. This will only be presented, when the controller returns something in {@link #createNewInstanceEditorInput()}.
 *   This option will then open another Editor instance so the user can compare the objects.</li>
 * </ul>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @param <EnityType> The type of entity this controller manages
s */
public abstract class ActiveEntityEditorPageController<EntityType> extends EntityEditorPageController {

	private static final boolean ENABLE_LISTENER = true;
	
	private static final Logger logger = Logger.getLogger(ActiveEntityEditorPageController.class);
	
	/**
	 * Enum of choices for the user when an object was changed.
	 */
	protected enum EntityStaleAction {
		keepLocalChanges(Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.keepLocalChanges.message"), Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.keepLocalChanges.description")), //$NON-NLS-1$ //$NON-NLS-2$
		loadRemoteChanges(Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.loadRemoteChanges.message"), Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.loadRemoteChanges.description")), //$NON-NLS-1$ //$NON-NLS-2$
		viewRemoteChanges(Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.viewRemoteChanges.message"), Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.viewRemoteChanges.description")), //$NON-NLS-1$ //$NON-NLS-2$
		closeEditor(Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.closeEditor.message"), Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.closeEditor.description")); //$NON-NLS-1$ //$NON-NLS-2$
		
		private String message;
		private String tooltip;
		
		EntityStaleAction(String message, String tooltip) {
			this.message = message;
			this.tooltip = tooltip;
		}
		
		public String getMessage() {
			return message;
		}
		
		public String getTooltip() {
			return tooltip;
		}
		
		public static EntityStaleAction getFromMessage(String msg) {
			for (EntityStaleAction action : EntityStaleAction.values()) {
				if (action.getMessage().equals(msg))
					return action;
			}
			return null;
		}
	}

	public static abstract class AbstractEntityStaleHandler implements IEntityEditorPageStaleHandler {

		private ComboBoxCellEditor cellEditor;
		private EntityStaleAction[] actions;
		private EntityStaleAction action;
		private IEntityEditorPageController controller;
		
		public AbstractEntityStaleHandler(
				EntityStaleAction[] actions,
				EntityStaleAction action,
				IEntityEditorPageController controller
		) {
			this.actions = actions;
			this.action = action;
			this.controller = controller;
		}
		
		private ComboBoxCellEditor getCreateCellEditor(Composite parent) {
			if (cellEditor == null) {
				String[] messages = new String[actions.length];
				for (int i = 0; i < actions.length; i++) {
					messages[i] = actions[i].getMessage();
				}
				cellEditor = new ComboBoxCellEditor(parent, messages) {
					@Override
					public void activate() {
						getComboBox().addSelectionListener(new SelectionListener() {
							public void widgetSelected(SelectionEvent e) {
								fireApplyEditorValue();
							}
							public void widgetDefaultSelected(SelectionEvent e) {}
						});
					}
				};
			}
			return cellEditor;
		}
		
		@Override
		public CellEditor getCellEditor(Composite parent, Object element) {
			return getCreateCellEditor(parent);
		}

		@Override
		public ITableLabelProvider getLabelProvider() {
			return new TableLabelProvider() {
				@Override
				public String getColumnText(Object element, int columnIndex) {
					if (element == AbstractEntityStaleHandler.this) {
						if (columnIndex == 0)
							return getPageController().getName();
						else if (columnIndex == 1)
							return action != null ? action.getMessage() : ""; //$NON-NLS-1$
					}
					return ""; //$NON-NLS-1$
				}
				
			};
		}

		@Override
		public IEntityEditorPageController getPageController() {
			return controller;
		}

		@Override
		public Object getValue(Object element) {
			if (element == this)
				return action != null ? action.getMessage() : null;
			return null;
		}

		@Override
		public void setValue(Object element, Object value) {
			this.action = EntityStaleAction.getFromMessage((String) value);
			
		}
		
		public EntityStaleAction getAction() {
			return action;
		}
	}
	
	/**
	 * Handler used by default when the entity is changed.
	 * It will display the {@link EntityChangeUserActionDialog}
	 * with choices of {@link EntityStaleAction} and
	 * react accordingly.
	 */
	protected class EntityChangedHandler extends AbstractEntityStaleHandler {
		
		public EntityChangedHandler(EntityStaleAction[] actions,
				EntityStaleAction action, IEntityEditorPageController controller) {
			super(actions, action, controller);
		}

		@Override
		public void run() {
			if (getAction() != null) {
				switch (getAction()) {
				case keepLocalChanges:
					// don't do anything, but also don't put the object into the cache, because its stale
					setStale(true);
					break;
				case loadRemoteChanges:
					// reload
					doReload(new org.nightlabs.progress.NullProgressMonitor());
					setStale(false);
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
	protected class EntityDeletedHandler  extends AbstractEntityStaleHandler {

		public EntityDeletedHandler(EntityStaleAction[] actions,
				EntityStaleAction action, IEntityEditorPageController controller) {
			super(actions, action, controller);
		}

		@Override
		public void run() {
			if (getAction() != null) {
				switch (getAction()) {
				case closeEditor:
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							getEntityEditor().close(false);
						}
					});
					break;
				default:
					break;
				}
			}
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
						setStale(true);
						if (dirtyObjectID.getLifecycleState() == JDOLifecycleState.DELETED) {
							// create the handler for the deletion of the object
							getEntityEditor().getStaleHandler().addEntityEdiorStaleHandler(createEntityDeletedHandler(dirtyObjectID));
						} else {
							if (checkForSelfCausedChange(dirtyObjectID)) {								
								// if this controller has caused the change then simply put the
								// object into the cache again.
								if (logger.isDebugEnabled()) {
									logger.debug("Found self-caused change, putting the entity in the cache:");
									logger.debug("Entity: " + controllerObject);
									logger.debug("EntityFetchGroups: " + getEntityFetchGroups());
								}
								Cache.sharedInstance().put(null, controllerObject, getEntityFetchGroups(), getEntityMaxFetchDepth());
								setStale(false);
							} else {
								// another controller/client has caused the change
								if (isDirty()) {
									// the controller is dirty / has local changes so the current version might differ from the remote one.
									// create the handler for the change of the object
									IEntityEditorPageStaleHandler entityChangedHandler = createEntityChangedHandler(dirtyObjectID);
									if (logger.isDebugEnabled())
										logger.debug("Found foreign change and a dirty editor, invoking changeHandler: " + entityChangedHandler);
									getEntityEditor().getStaleHandler().addEntityEdiorStaleHandler(entityChangedHandler);
								} else {
									if (logger.isDebugEnabled())
										logger.debug("Found foreign change but clean editor, will silently reload the entity");
									// no local changes, reload
									doReload(new SubProgressMonitor(new ProgressMonitorWrapper(getProgressMonitor()), 100));
									setStale(false);
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
	 * The EntityChangeListener of this controller.
	 * This value will be set when the change listener
	 * is registered {@link #doLoad(ProgressMonitor)}.
	 */
	private EntityChangeListener entityChangeListener = null;
	
	/**
	 * Create a new {@link ActiveEntityEditorPageController} that will not start background loading.
	 * @param editor The editor this controller is associated with.
	 */
	public ActiveEntityEditorPageController(EntityEditor editor) {
		this(editor, false);
	}
	
	/**
	 * Create a new {@link ActiveEntityEditorPageController}.
	 * @param editor The editor this controller is associated with.
	 * @param startBackgroundLoading Whether to start loading right away.
	 */
	public ActiveEntityEditorPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation calls {@link #retrieveEntity(ProgressMonitor)}
	 * and keeps a clone of the result as controller object (see {@link #getControllerObject()}).
	 * </p>
	 */
	@Override
	public void doLoad(ProgressMonitor monitor) {
		if (logger.isDebugEnabled())
			logger.debug("Started loading for " + this.getClass().getName());
		EntityType oldControllerObject = null;
		monitor.beginTask(getLoadJobName(), 100);
		synchronized (mutex) {
			oldControllerObject = getControllerObject();
			EntityType newObj = retrieveEntity(new SubProgressMonitor(monitor, 100));
			if (logger.isDebugEnabled())
				logger.debug("retrieveEntity returned: " + newObj);
			if (newObj == null)
				controllerObject = newObj;
			else {
				if (controllerObjectClass != null && !controllerObjectClass.equals(newObj.getClass())) {
					throw new IllegalStateException("The implementation of ActiveEntityEditorPageController '" + this.getClass().getSimpleName() + "' returned different types of objects on retrieveEntity (" + controllerObjectClass.getName() + " and " + newObj.getClass().getName() + ")."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				setControllerObject(Util.cloneSerializable(newObj));
				if (logger.isDebugEnabled()) 
					logger.debug("ControllerObject after cloning: " + getControllerObject());
				controllerObjectClass = controllerObject.getClass();
				if (entityChangeListener == null && ENABLE_LISTENER) {
					entityChangeListener = new EntityChangeListener(getProcessChangesJobName());
					JDOLifecycleManager.sharedInstance().addNotificationListener(controllerObjectClass, entityChangeListener);
					if (logger.isDebugEnabled())
						logger.debug("Registered changeListener for " + controllerObjectClass);
				}
			}
			setStale(false);
			markUndirty();
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
	public boolean doSave(ProgressMonitor monitor) {
		if (logger.isDebugEnabled())
			logger.debug("Started saving for " + this.getClass().getName());
		monitor.beginTask(getSaveJobName(), 100);
		EntityType oldControllerObject = null;
		synchronized (mutex) {
			oldControllerObject = controllerObject;
			if (logger.isDebugEnabled())
				logger.debug("Old controllerObject: " + oldControllerObject);
			controllerObject = storeEntity(controllerObject, new SubProgressMonitor(monitor, 100));
			if (logger.isDebugEnabled())
				logger.debug("storeEntity returned: " + controllerObject);
			// we don't put the result into the Cache, as the Cache will be notified
			// of the change and the change listener will put the object into the cache
			controllerObject = Util.cloneSerializable(controllerObject);
			if (logger.isDebugEnabled())
				logger.debug("Controller object after clone: " + getControllerObject());
			setStale(false);
			markUndirty();
		}
		monitor.done();
		fireModifyEvent(oldControllerObject, controllerObject);
		return true;
	}
	
	/**
	 * Reload the controller object. Currently only wraps {@link #reload(IProgressMonitor)}.
	 */
	protected void doReload(ProgressMonitor monitor) {
		// TODO: Think about doing this in a job and notifying the page before the reload (so it can show the progress view)
		setLoaded(false);
		doLoad(monitor);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!disposed) {
					getEntityEditor().editorDirtyStateChanged();
				}
			}
		});
	}

	private boolean disposed = false;
	/**
	 * Removes the entityChangeListener
	 */
	@Override
	public void dispose() {
		if (entityChangeListener != null) {
			JDOLifecycleManager.sharedInstance().removeNotificationListener(controllerObjectClass, entityChangeListener);
		}		
		super.dispose();
		disposed = true;
	}
	
	/**
	 * Returns the text that will be set as the load Jobs name.
	 * <p>
	 * Subclasses may override.
	 * </p>
	 * @return The text that will be set as the load Jobs name.
	 */
	protected String getLoadJobName() {
		return Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.job.load.name"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the text that will be set as the save Jobs name.
	 * <p>
	 * Subclasses may override.
	 * </p>
	 * @return The text that will be set as the save Jobs name.
	 */
	protected String getSaveJobName() {
		return Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.job.save.name"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the text that will be set as the name of the Job that processes entity changes.
	 * <p>
	 * Subclasses may override.
	 * </p>
	 * @return The text that will be set as the name of the Job that processes entity changes.
	 */
	protected String getProcessChangesJobName() {
		return Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController.job.process.name"); //$NON-NLS-1$
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
	 * This is the scope under which the entity should be put
	 * into the Cache.
	 * @return This implementation returns <code>null</code>.
	 */
	protected String getEntityCacheScope() {
		return null;
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
		// if JDO versioning is enabled for this object, we compare the version to the one we have locally
		if (dirtyObjectID.getObjectVersion() != null) {
			Object currentlyManagedObjectVersion = JDOHelper.getVersion(getControllerObject());
			Object notifiedVersion = dirtyObjectID.getObjectVersion();
			if (logger.isDebugEnabled())
				logger.debug("checkForSelfCausedChange comparing versions. currentlyManagedObjectVersion: " + currentlyManagedObjectVersion + ", notifiedVersion: " + notifiedVersion);
			return notifiedVersion.equals(currentlyManagedObjectVersion);
		}

//		// TODO: WORKAROUND: Notifications currently produce too many sourceSessionIDs,
//		// so we check if the current sessionID is in the sourceSessionIDs and not if
//		// it is the only one, see issue: https://www.jfire.org/modules/bugs/view.php?id=471
//		for (String sessionID : dirtyObjectID.getSourceSessionIDs()) {
//			if (sessionID.equals(Cache.sharedInstance().getSessionID()))
//				return true;
//		}
//		return false;
//		return true;
		if (logger.isDebugEnabled()) {
			logger.debug("checkForSelfCausedChange looking for sourceSessionIDs.");
			logger.debug("Cache sessionID is: " + Cache.sharedInstance().getSessionID());
		}
		for (String sessionID : dirtyObjectID.getSourceSessionIDs()) {
			if (!sessionID.equals(Cache.sharedInstance().getSessionID())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found non-matching sourceSessionID: " + sessionID + ", treated as froreign change");
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Create the {@link IEntityEditorPageStaleHandler} that will be used notify the user of
	 * a remote change of the controller object.
	 * <p>
	 * This will be set by the change listener to the editor {@link EntityEditorStaleHandler}
	 * which means that it is not necessarily executed instantly, when the editor currently doesn't have the focus,
	 * it will be executed lazily when the editor was activated again.
	 * </p>
	 * <p>
	 * This implementation returns {@link EntityChangedHandler}.
	 * </p>
	 * @param dirtyObjectID The {@link DirtyObjectID} that notified the change.
	 * @return The handler that will be executed to notify the user of
	 * 	a remote change of the controller object.
	 */
	protected IEntityEditorPageStaleHandler createEntityChangedHandler(DirtyObjectID dirtyObjectID) {
		List<EntityStaleAction> actions = new ArrayList<EntityStaleAction>(3);
		actions.add(EntityStaleAction.keepLocalChanges);
		actions.add(EntityStaleAction.loadRemoteChanges);
		return new EntityChangedHandler(
				actions.toArray(new EntityStaleAction[0]),
				EntityStaleAction.loadRemoteChanges,
				this
		);
	}
	
	/**
	 * Create the {@link IEntityEditorPageStaleHandler} that will be executed to notify the user that
	 * the controller object was deleted on the server.
	 * <p>
	 * This will be set by the change listener to the editor {@link EntityEditorStaleHandler}
	 * which means that it is not necessarily executed instantly, when the editor currently doesn't have the focus,
	 * it will be executed lazily when the editor was activated again.
	 * </p>
	 * <p>
	 * This implementation returns {@link EntityDeletedHandler}.
	 * </p>
	 * @param dirtyObjectID The {@link DirtyObjectID} that notified the change.
	 * @return The handler that will be executed when the object was deleted on the server.
	 */
	protected IEntityEditorPageStaleHandler createEntityDeletedHandler(DirtyObjectID dirtyObjectID) {
		return new EntityDeletedHandler(
				new EntityStaleAction[] {EntityStaleAction.closeEditor},
				EntityStaleAction.closeEditor,
				this
		);
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
	
	/**
	 * Sets current object managed by this controller.
	 * @param controllerObject The object to set.
	 */
	protected void setControllerObject(EntityType controllerObject) {
		this.controllerObject = controllerObject;
	}
}
