package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Locale;

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
import org.nightlabs.base.ui.table.CheckboxCellEditorHelper;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.RoleGroup;

public class RoleGroupTableViewer extends TableViewer
{
	/**
	 * Content provider for role groups.
	 */
	private final class RoleGroupsContentProvider extends TableContentProvider
	{
		@Override
		public Object[] getElements(Object inputElement) {
			Collection<RoleGroup> roleGroups = (Collection<RoleGroup>) inputElement;
			return roleGroups.toArray();
		}
	}

	/**
	 * Label provider for role groups.
	 */
	private class RoleGroupsLabelProvider extends TableLabelProvider
	{
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			switch (columnIndex) {
			case 1: return CheckboxCellEditorHelper.getCellEditorImage(model.getRoleGroups().contains(element), false);
			case 2:	return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), RoleGroupsLabelProvider.class);
			default: return null;
			}
		}

		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof RoleGroup))
				throw new RuntimeException("Invalid object type, expected RoleGroup"); //$NON-NLS-1$
			RoleGroup g = (RoleGroup) element;
			switch (columnIndex) {
			case 0:
				if (model.getRoleGroups().contains(element) && model.getRoleGroupsFromUserGroups().contains(element))
					return "DG";
				else if (model.getRoleGroups().contains(element))
					return "D";
				else if (model.getRoleGroupsFromUserGroups().contains(element))
					return "G";
				return "";
			case 2:	return g.getName().getText(Locale.getDefault().getLanguage());
			case 3:	return g.getDescription().getText(Locale.getDefault().getLanguage());
			}
			return null;
		}
	}

	private RoleGroupSecurityPreferencesModel model;
	private IDirtyStateManager dirtyStateManager;

	public RoleGroupTableViewer(Table table, IDirtyStateManager dirtyStateManager, boolean showTotalColum) {
		super(table);

		this.dirtyStateManager = dirtyStateManager;

		ViewerComparator roleGroupComparator = new ViewerComparator() {
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
		col2.getColumn().setText("");
		col2.setEditingSupport(new CheckboxEditingSupport<RoleGroup>(this) {
			@Override
			protected boolean doGetValue(RoleGroup element) {
				return model.getRoleGroups().contains(element);
			}

			@Override
			protected void doSetValue(RoleGroup element, boolean value) {
				if (value)
					model.addRoleGroup(element);
				else
					model.removeRoleGroup(element);

				RoleGroupTableViewer.this.dirtyStateManager.markDirty();
			}
		});

		TableColumn col3 = new TableColumn(getTable(), SWT.NULL);
		col3.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.roleGroup")); //$NON-NLS-1$

		TableColumn col4 = new TableColumn(getTable(), SWT.NULL);
		col4.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupsSection.description")); //$NON-NLS-1$
		
		int column1Width = showTotalColum ? 30 : 0;

		TableLayout tlayout = new WeightedTableLayout(new int[] { -1, -1, 30, 70 }, new int[] { column1Width, 20, -1, -1 });
		getTable().setLayout(tlayout);
		getTable().setHeaderVisible(true);

		new TableColumn(table, SWT.LEFT).setText("Name");
		new TableColumn(table, SWT.LEFT).setText("Description");

		setContentProvider(new RoleGroupsContentProvider());
		setLabelProvider(new RoleGroupsLabelProvider());
	}

	public void setModel(RoleGroupSecurityPreferencesModel model) {
		this.model = model;
		setInput(model.getAvailableRoleGroups());
	}
}
