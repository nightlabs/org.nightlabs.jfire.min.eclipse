package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Collections;

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
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.util.NLLocale;

public class RoleGroupTableViewer extends TableViewer
{
	/**
	 * Content provider for role groups.
	 */
	private final class RoleGroupsContentProvider extends TableContentProvider
	{
		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.table.TableContentProvider#getElements(java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			Collection<RoleGroup> roleGroups = (Collection<RoleGroup>) inputElement;
			return roleGroups.toArray();
		}
	}

	/**
	 * Label provider for role groups.
	 */
	private class RoleGroupsLabelProvider extends EmulatedNativeCheckBoxTableLabelProvider
	{
		public RoleGroupsLabelProvider(TableViewer viewer) {
			super(viewer);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.table.TableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			switch (columnIndex) {
			case 1: return showCheckBoxes ? getCheckBoxImage(model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element)) : null;
			case 2:	return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), RoleGroupsLabelProvider.class);
			default: return null;
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof RoleGroup))
				throw new RuntimeException("Invalid object type, expected RoleGroup"); //$NON-NLS-1$
			RoleGroup g = (RoleGroup) element;
			switch (columnIndex) {
			case 0:
				if (model.isControlledByOtherUser() && model.getRoleGroupsAssignedToOtherUser().contains(element))
					return "O";
				if (model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element) && model.getRoleGroupsAssignedToUserGroups().contains(element))
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceDirectAndGroup"); //$NON-NLS-1$
				else if (model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element))
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceDirect"); //$NON-NLS-1$
				else if (model.getRoleGroupsAssignedToUserGroups().contains(element))
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceGroup"); //$NON-NLS-1$
				return "";
			case 2:	return g.getName().getText(NLLocale.getDefault().getLanguage());
			case 3:	return g.getDescription().getText(NLLocale.getDefault().getLanguage());
			}
			return null;
		}
	}

	private RoleGroupSecurityPreferencesModel model;
	private IDirtyStateManager dirtyStateManager;

	private boolean showCheckBoxes;

	public RoleGroupTableViewer(Table table, IDirtyStateManager dirtyStateManager, boolean showAssignmentSourceColum, boolean showCheckBoxes)
	{
		super(table);

		this.dirtyStateManager = dirtyStateManager;
		this.showCheckBoxes = showCheckBoxes;

		ViewerComparator roleGroupComparator = new ViewerComparator() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				RoleGroup r1 = (RoleGroup) e1;
				RoleGroup r2 = (RoleGroup) e2;
				return r1.getName().getText().compareTo(r2.getName().getText());
			}
		};

		setComparator(roleGroupComparator);

		// Layout stuff
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		getTable().setLayoutData(gd);

		// Table columns
		new TableColumn(getTable(), SWT.LEFT).setResizable(false); // total availability

		TableViewerColumn col2 = new TableViewerColumn(this, SWT.CENTER);
		col2.getColumn().setResizable(false);
		col2.getColumn().setText(""); //$NON-NLS-1$
		col2.setEditingSupport(checkboxEditingSupport);

		TableColumn col3 = new TableColumn(getTable(), SWT.NULL);
		col3.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroup")); //$NON-NLS-1$

		TableColumn col4 = new TableColumn(getTable(), SWT.NULL);
		col4.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.description")); //$NON-NLS-1$

		int column1Width = showAssignmentSourceColum ? 30 : 0;
		int column2Witdh = showCheckBoxes ? 22 : 0;

		TableLayout tlayout = new WeightedTableLayout(new int[] { -1, -1, 30, 70 }, new int[] { column1Width, column2Witdh, -1, -1 });
		getTable().setLayout(tlayout);
		getTable().setHeaderVisible(true);

//		new TableColumn(table, SWT.LEFT).setText("Name");
//		new TableColumn(table, SWT.LEFT).setText("Description");

		setContentProvider(new RoleGroupsContentProvider());
		setLabelProvider(new RoleGroupsLabelProvider(this));
	}

	private CheckboxEditingSupport<RoleGroup> checkboxEditingSupport = new CheckboxEditingSupport<RoleGroup>(this) {
		@Override
		protected boolean canEdit(Object element) {
			boolean result = model != null && model.isInAuthority();
			return result;
		}

		@Override
		protected boolean doGetValue(RoleGroup element) {
			boolean result = model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element);
			return result;
		}

		@Override
		protected void doSetValue(RoleGroup element, boolean value) {
			if (value)
				model.addRoleGroup(element);
			else
				model.removeRoleGroup(element);

			RoleGroupTableViewer.this.dirtyStateManager.markDirty();
		}
	};

	public void setModel(RoleGroupSecurityPreferencesModel model) {
		this.model = model;

		if (model == null)
			setInput(Collections.emptySet());
		else
			setInput(model.getAllRoleGroupsInAuthority());
	}
}
