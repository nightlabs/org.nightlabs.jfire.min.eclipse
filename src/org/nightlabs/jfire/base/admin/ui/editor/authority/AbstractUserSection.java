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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

public abstract class AbstractUserSection
extends ToolBarSectionPart
implements ISelectionProvider
{
	private UserTableViewer userTable;

	public AbstractUserSection(IFormPage page, Composite parent) {
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED, "Users && user groups in authority");

		userTable = new UserTableViewer(getContainer(), this);
		userTable.setInput(users);
		userTable.getTable().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				setAuthorityPageControllerHelper(null);
			}
		});

		userTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedUsers = null;
				selection = null;
				fireSelectionChangedEvent();
			}
		});
	}

	private List<Map.Entry<User, Boolean>> users = new ArrayList<Map.Entry<User,Boolean>>();
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
	protected void setAuthorityPageControllerHelper(AuthorityPageControllerHelper authorityPageControllerHelper) {
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
		users.clear();
		if (authorityPageControllerHelper != null && authorityPageControllerHelper.getAuthority() != null)
			users.addAll(authorityPageControllerHelper.createModifiableUserList());

		if (!userTable.getTable().isDisposed())
			userTable.refresh();
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

	private List<User> selectedUsers = null;
	private IStructuredSelection selection = null;

	/**
	 * Get the selected users. This method provides a more specific API than the general (and not typed)
	 * {@link #getSelection()}, but the returned instances of {@link User} are the same.
	 *
	 * @return the selected users.
	 */
	public List<User> getSelectedUsers() {
		getSelection(); // ensure the existence of our data and that we are on the correct thread
		return selectedUsers;
	}

	/**
	 * Get an {@link IStructuredSelection} containing {@link User} instances. The instances are the same as
	 * returned by {@link #getSelectedUsers()}.
	 *
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ISelection getSelection() {
		if (Display.getCurrent() == null)
			throw new IllegalStateException("Wrong thread! This method must be called on the SWT UI thread!");

		if (selectedUsers == null || selection == null) {
			selectedUsers = new ArrayList<User>();
			selection = null;
			IStructuredSelection sel = (IStructuredSelection) userTable.getSelection();
			
			for (Object object : sel.toArray()) {
				Map.Entry<User, Boolean> me = (Entry<User, Boolean>) object;
				selectedUsers.add(me.getKey());
			}

			selection = new StructuredSelection(selectedUsers);
		}

		return selection;
	}

	/**
	 * Set an {@link IStructuredSelection} containing {@link User} or {@link UserID} instances.
	 */
	@Override
	public void setSelection(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			throw new IllegalArgumentException("selection must be an instance of IStructuredSelection!");

		if (Display.getCurrent() == null)
			throw new IllegalStateException("Wrong thread! This method must be called on the SWT UI thread!");

		IStructuredSelection sel = (IStructuredSelection) selection;
		Set<UserID> selectedUserIDs = new HashSet<UserID>(sel.size());
		for (Object object : sel.toArray()) {
			if (object instanceof UserID)
				selectedUserIDs.add((UserID) object);
			else if (object instanceof User) {
				UserID userID = (UserID) JDOHelper.getObjectId(object);
				if (userID == null)
					throw new IllegalArgumentException("The selection contains a User that has no UserID assigned!"); // should never happen, since all the users we manage are already persisted and detached.

				selectedUserIDs.add(userID);
			}
			else
				throw new IllegalArgumentException("The selection contains an object that's neither an instance of UserID nor an instance of User! The object is: " + object);
		}

		// now that we have all UserIDs that should be selected in our set, we iterate the users that are in our userTable and collect the elements that should be selected
		List<Map.Entry<User, Boolean>> elementsToBeSelected = new ArrayList<Entry<User,Boolean>>(selectedUserIDs.size());
		for (Map.Entry<User, Boolean> me : users) {
			if (selectedUserIDs.contains(JDOHelper.getObjectId(me.getKey())))
				elementsToBeSelected.add(me);
		}

		userTable.setSelection(new StructuredSelection(elementsToBeSelected));
	}
}
