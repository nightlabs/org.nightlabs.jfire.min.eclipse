package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;

public class AuthorizedObjectSection
extends ToolBarSectionPart
implements ISelectionProvider
{
	private AuthorizedObjectTableViewer authorizedObjectTable;

	private CheckSelectedAction checkSelectedAction;
	private UncheckSelectedAction uncheckSelectedAction;
	private CheckAllAction checkAllAction;
	private UncheckAllAction uncheckAllAction;
	
	public AuthorizedObjectSection(IFormPage page, Composite parent) {
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorizedObjectSection.title.authorizedObjects")); //$NON-NLS-1$

		authorizedObjectTable = new AuthorizedObjectTableViewer(getContainer(), this,
				AbstractTableComposite.DEFAULT_STYLE_SINGLE | XComposite.getBorderStyle(getContainer()));
		authorizedObjectTable.setInput(authorizedObjects);
		authorizedObjectTable.getTable().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				setAuthorityPageControllerHelper(null);
			}
		});
		
		authorizedObjectTable.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == ' ') { 
					final List<AuthorizedObject> selectedObjects = getSelectedAuthorizedObjects();
					Display.getCurrent().asyncExec(new Runnable() {
						@Override
						public void run() {
							for (AuthorizedObject selectedObject : selectedObjects) {
								for (Map.Entry<AuthorizedObject, Boolean> me : authorizedObjects) {
									if (me.getKey().equals(selectedObject)) {
										me.setValue(!me.getValue());
									}
								}
							}
							authorizedObjectTable.refresh();
							markDirty();
						}
					});
				}
			}
		});

		authorizedObjectTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedAuthorizedObjects = null;
				selection = null;
				fireSelectionChangedEvent();
			}
		});

		checkSelectedAction = new CheckSelectedAction();
		uncheckSelectedAction = new UncheckSelectedAction();
		checkAllAction = new CheckAllAction();
		uncheckAllAction = new UncheckAllAction();

		getToolBarManager().add(checkSelectedAction);
		getToolBarManager().add(uncheckSelectedAction);
		getToolBarManager().add(checkAllAction);
		getToolBarManager().add(uncheckAllAction);
		
		updateToolBarManager();
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(checkSelectedAction);
		menuManager.add(uncheckSelectedAction);
		menuManager.add(checkAllAction);
		menuManager.add(uncheckAllAction);
		
		Menu menu = menuManager.createContextMenu(authorizedObjectTable.getTable());
		authorizedObjectTable.getTable().setMenu(menu);	
	}

	private List<Map.Entry<AuthorizedObject, Boolean>> authorizedObjects = new ArrayList<Map.Entry<AuthorizedObject,Boolean>>();
	private AuthorityPageControllerHelper authorityPageControllerHelper;

	protected AuthorityPageControllerHelper getAuthorityPageControllerHelper() {
		return authorityPageControllerHelper;
	}

	/**
	 * Set the {@link AuthorityPageControllerHelper} that is used for the current editor page. It is possible to
	 * pass <code>null</code> in order to indicate that there is nothing to be managed right now (and thus to clear
	 * the UI).
	 *
	 * @param authorityPageControllerHelper an instance of <code>AuthorityPageControllerHelper</code> or <code>null</code>.
	 */
	public synchronized void setAuthorityPageControllerHelper(AuthorityPageControllerHelper authorityPageControllerHelper) {
		if (this.authorityPageControllerHelper != null) {
			this.authorityPageControllerHelper.removePropertyChangeListener(
					AuthorityPageControllerHelper.PROPERTY_NAME_AUTHORITY_LOADED,
					propertyChangeListenerAuthorityLoaded
			);
		}

		this.authorityPageControllerHelper = authorityPageControllerHelper;

		getSection().getDisplay().asyncExec(new Runnable() {
			public void run() {
				authorityChanged();
			}
		});

		if (this.authorityPageControllerHelper != null) {
			this.authorityPageControllerHelper.addPropertyChangeListener(
					AuthorityPageControllerHelper.PROPERTY_NAME_AUTHORITY_LOADED,
					propertyChangeListenerAuthorityLoaded
			);
		}
	}

	private PropertyChangeListener propertyChangeListenerAuthorityLoaded = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			getSection().getDisplay().asyncExec(new Runnable() {
				public void run() {
					authorityChanged();
				}
			});
		}
	};

	private void authorityChanged()
	{
		authorizedObjects.clear();
		if (authorityPageControllerHelper != null && authorityPageControllerHelper.getAuthority() != null)
			authorizedObjects.addAll(authorityPageControllerHelper.createModifiableAuthorizedObjectList());

		if (!authorizedObjectTable.getTable().isDisposed())
			authorizedObjectTable.refresh();
	}

	private ListenerList selectionChangedListeners = new ListenerList();

	protected void fireSelectionChangedEvent()
	{
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());

		for (Object listener : selectionChangedListeners.getListeners())
			((ISelectionChangedListener)listener).selectionChanged(event);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	private List<AuthorizedObject> selectedAuthorizedObjects = null;
	private IStructuredSelection selection = null;

	/**
	 * Get the selected authorizedObjects. This method provides a more specific API than the general (and not typed)
	 * {@link #getSelection()}, but the returned instances of {@link AuthorizedObject} are the same.
	 *
	 * @return the selected authorizedObjects.
	 */
	public List<AuthorizedObject> getSelectedAuthorizedObjects() {
		getSelection(); // ensure the existence of our data and that we are on the correct thread
		return selectedAuthorizedObjects;
	}

	/**
	 * Get an {@link IStructuredSelection} containing {@link AuthorizedObject} instances. The instances are the same as
	 * returned by {@link #getSelectedAuthorizedObjects()}.
	 *
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	public ISelection getSelection() {
		if (Display.getCurrent() == null)
			throw new IllegalStateException("Wrong thread! This method must be called on the SWT UI thread!"); //$NON-NLS-1$

		if (selectedAuthorizedObjects == null || selection == null) {
			selectedAuthorizedObjects = new ArrayList<AuthorizedObject>();
			selection = null;
			IStructuredSelection sel = (IStructuredSelection) authorizedObjectTable.getSelection();

			for (Object object : sel.toArray()) {
				Map.Entry<AuthorizedObject, Boolean> me = (Entry<AuthorizedObject, Boolean>) object;
				selectedAuthorizedObjects.add(me.getKey());
			}

			selection = new StructuredSelection(selectedAuthorizedObjects);
		}

		return selection;
	}

	/**
	 * Set an {@link IStructuredSelection} containing {@link AuthorizedObject} or {@link AuthorizedObjectID} instances.
	 */
	@Override
	public void setSelection(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			throw new IllegalArgumentException("selection must be an instance of IStructuredSelection!"); //$NON-NLS-1$

		if (Display.getCurrent() == null)
			throw new IllegalStateException("Wrong thread! This method must be called on the SWT UI thread!"); //$NON-NLS-1$

		IStructuredSelection sel = (IStructuredSelection) selection;
		Set<AuthorizedObjectID> selectedAuthorizedObjectIDs = new HashSet<AuthorizedObjectID>(sel.size());
		for (Object object : sel.toArray()) {
			if (object instanceof AuthorizedObjectID)
				selectedAuthorizedObjectIDs.add((AuthorizedObjectID) object);
			else if (object instanceof AuthorizedObject) {
				AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) JDOHelper.getObjectId(object);
				if (authorizedObjectID == null)
					throw new IllegalArgumentException("The selection contains a AuthorizedObject that has no AuthorizedObjectID assigned!"); // should never happen, since all the authorizedObjects we manage are already persisted and detached. //$NON-NLS-1$

				selectedAuthorizedObjectIDs.add(authorizedObjectID);
			}
			else
				throw new IllegalArgumentException("The selection contains an object that's neither an instance of AuthorizedObjectID nor an instance of AuthorizedObject! The object is: " + object); //$NON-NLS-1$
		}

		// now that we have all AuthorizedObjectIDs that should be selected in our set, we iterate the authorizedObjects that are in our authorizedObjectTable and collect the elements that should be selected
		List<Map.Entry<AuthorizedObject, Boolean>> elementsToBeSelected = new ArrayList<Entry<AuthorizedObject,Boolean>>(selectedAuthorizedObjectIDs.size());
		for (Map.Entry<AuthorizedObject, Boolean> me : authorizedObjects) {
			if (selectedAuthorizedObjectIDs.contains(JDOHelper.getObjectId(me.getKey())))
				elementsToBeSelected.add(me);
		}

		authorizedObjectTable.setSelection(new StructuredSelection(elementsToBeSelected));
	}
	
	public class CheckSelectedAction extends Action {
		public CheckSelectedAction() {
			super();
			setId(CheckSelectedAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					AuthorizedObjectSection.class,
					"CheckSelected")); //$NON-NLS-1$
			setToolTipText("Check Selected");
			setText("Check Selected");
		}

		@Override
		public void run() {
			final List<AuthorizedObject> selectedObjects = getSelectedAuthorizedObjects();
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (AuthorizedObject selectedObject : selectedObjects) {
						for (Map.Entry<AuthorizedObject, Boolean> me : authorizedObjects) {
							if (me.getKey().equals(selectedObject)) {
								me.setValue(true);
							}
						}
					}
					authorizedObjectTable.refresh();
					markDirty();
				}
			});
		}
	}
	
	public class UncheckSelectedAction extends Action {
		public UncheckSelectedAction() {
			super();
			setId(UncheckSelectedAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					AuthorizedObjectSection.class,
					"UncheckSelected")); //$NON-NLS-1$
			setToolTipText("Uncheck Selected");
			setText("Uncheck Selected");
		}

		@Override
		public void run() {
			final List<AuthorizedObject> selectedObjects = getSelectedAuthorizedObjects();
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (AuthorizedObject selectedObject : selectedObjects) {
						for (Map.Entry<AuthorizedObject, Boolean> me : authorizedObjects) {
							if (me.getKey().equals(selectedObject)) {
								me.setValue(false);
							}
						}
					}
					authorizedObjectTable.refresh();
					markDirty();
				}
			});
		}
	}
	
	public class CheckAllAction extends Action {
		public CheckAllAction() {
			super();
			setId(CheckAllAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					AuthorizedObjectSection.class,
					"CheckAll")); //$NON-NLS-1$
			setToolTipText("Check All");
			setText("Check All");
		}

		@Override
		public void run() {
			for (Map.Entry<AuthorizedObject, Boolean> me : authorizedObjects) {
				me.setValue(true);
			}
			authorizedObjectTable.refresh();
			markDirty();
		}
	}
	
	public class UncheckAllAction extends Action {
		public UncheckAllAction() {
			super();
			setId(UncheckAllAction.class.getName());
			setImageDescriptor(SharedImages.getSharedImageDescriptor(
					BaseAdminPlugin.getDefault(),
					AuthorizedObjectSection.class,
					"UncheckAll")); //$NON-NLS-1$
			setToolTipText("Uncheck All");
			setText("Uncheck All");
		}

		@Override
		public void run() {
			for (Map.Entry<AuthorizedObject, Boolean> me : authorizedObjects) {
				me.setValue(false);
			}
			authorizedObjectTable.refresh();
			markDirty();
		}
	}
	
	private void createContextMenu() {
//		// Relegate the menu-items to the menu-manager.
//		menuMgr = authorizedObjectTable.getgetMenuManager();
//		menuMgr.setRemoveAllWhenShown(true);
//		menuMgr.addMenuListener(new IMenuListener() {
//			public void menuAboutToShow(IMenuManager m) {
//				menuMgr.add(editIssueAction);
//				menuMgr.add(deleteIssueAction);
//				menuMgr.add(resolveAllAction);
//				menuMgr.add(closeAllAction);
//				menuMgr.add(assignAllAction);
//				menuMgr.add(setPropertyAction);
//			}
//		});
//
//		// Create the menu and attach it to the IssueTable.
//		Menu menu = menuMgr.createContextMenu(issueResultTable);
//		issueResultTable.setMenu(menu); // <-- Hmm... this seems enough? Do we need to do this: 'getSite().registerContextMenu(menuMgr, issueTable);'
//
//		// Define the behaviour of the menu items wrt the selections, or lack thereof, in the IssueTable.
//		issueResultTable.addSelectionChangedListener(new ISelectionChangedListener(){
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) { handleContextMenuItems(); }
//		});
	}
}
