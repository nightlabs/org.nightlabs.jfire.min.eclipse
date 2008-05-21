package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
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
			Collection<Map.Entry<User, Boolean>> elements = (Collection<Map.Entry<User, Boolean>>)inputElement;
			return elements.toArray();
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
			Map.Entry<User, Boolean> me = (Map.Entry<User, Boolean>)element;
			switch(columnIndex) {
				case 0: return getCheckBoxImage(me.getValue());
				case 1:return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), UsersLabelProvider.class);
				default: return null;
			}
		}

		public String getColumnText(Object element, int columnIndex)
		{
			Map.Entry<User, Boolean> me = (Map.Entry<User, Boolean>)element;
			switch(columnIndex) {
				case 1: return me.getKey().getName();
				case 2: return me.getKey().getDescription();
			}
			return "";
		}
	}

	private IDirtyStateManager dirtyStateManager;

	public UserTableViewer(Composite parent, IDirtyStateManager dirtyStateManager) {
		super(parent, SWT.NONE);

		this.dirtyStateManager = dirtyStateManager;

		ViewerComparator userComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Map.Entry<User, Boolean> u1 = (Map.Entry<User, Boolean>) e1;
				Map.Entry<User, Boolean> u2 = (Map.Entry<User, Boolean>) e2;
				return u1.getKey().getName().compareTo((u2.getKey().getName()));
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
		col1.setEditingSupport(new CheckboxEditingSupport<Map.Entry<User, Boolean>>(this) {
			@Override
			protected boolean doGetValue(Map.Entry<User, Boolean> element) {
				return element.getValue().booleanValue();
			}

			@Override
			protected void doSetValue(Map.Entry<User, Boolean> element, boolean value) {
				element.setValue(value);
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
}
