package org.nightlabs.jfire.base.admin.ui.editor.usergroup;

import java.util.Collection;

import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.EmulatedNativeCheckBoxTableLabelProvider;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.editor.user.CheckboxEditingSupport;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.User;

public class UserTableViewer extends TableViewer
{
	/**
	 * Content provider for users.
	 */
	private final class UsersContentProvider extends TableContentProvider
	{
		@Override
		public Object[] getElements(Object inputElement)
		{
			Collection<User> users = (Collection<User>)inputElement;
			return users.toArray();
		}
	}

	/**
	 * Label provider for users.
	 */
	private class UsersLabelProvider extends EmulatedNativeCheckBoxTableLabelProvider
	{
		public UsersLabelProvider(TableViewer viewer) {
			super(viewer);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			switch(columnIndex) {
//			case 0: return CheckboxCellEditorHelper.getCellEditorImage(model.getIncludedUsers().contains(element), false);
			case 0: return getCheckBoxImage(model.getIncludedUsers().contains(element));
			case 1:return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), UsersLabelProvider.class);
			default: return null;
			}
		}

		public String getColumnText(Object element, int columnIndex)
		{
			if(!(element instanceof User))
				throw new RuntimeException("Invalid object type, expected User"); //$NON-NLS-1$
			User u = (User)element;
			switch(columnIndex) {
			case 1: return u.getName();
			case 2: return u.getDescription();
			}
			return null;
		}
	}

	private GroupSecurityPreferencesModel model;
	private IDirtyStateManager dirtyStateManager;

	public UserTableViewer(Table table, IDirtyStateManager dirtyStateManager) {
		super(table);

		this.dirtyStateManager = dirtyStateManager;

		ViewerComparator userComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				User u1 = (User) e1;
				User u2 = (User) e2;
				return u1.getName().compareTo((u2.getName()));
			}
		};

		setComparator(userComparator);

		// Layout stuff
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		getTable().setLayoutData(gd);

		TableViewerColumn col1 = new TableViewerColumn(this, SWT.CENTER);
		col1.getColumn().setResizable(false);
		col1.getColumn().setText("");
		col1.setEditingSupport(new CheckboxEditingSupport<User>(this) {
			@Override
			protected boolean doGetValue(User element) {
				return model.getIncludedUsers().contains(element);
			}

			@Override
			protected void doSetValue(User element, boolean value) {
				if (value)
					model.addUser(element);
				else
					model.removeUser(element);

				UserTableViewer.this.dirtyStateManager.markDirty();
			}
		});

		TableColumn col2 = new TableColumn(getTable(), SWT.NULL);
		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usergroup.UsersSection.user")); //$NON-NLS-1$

		TableLayout tlayout = new WeightedTableLayout(new int[] { -1, 100 }, new int[] { 22, -1 });
		getTable().setLayout(tlayout);
		getTable().setHeaderVisible(true);

		setContentProvider(new UsersContentProvider());
		setLabelProvider(new UsersLabelProvider(this));
	}

	public void setModel(GroupSecurityPreferencesModel model) {
		this.model = model;
		setInput(model.getAvailableUsers());
	}
}
